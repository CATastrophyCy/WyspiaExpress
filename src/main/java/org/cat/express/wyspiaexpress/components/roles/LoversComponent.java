package org.cat.express.wyspiaexpress.components.roles;

import de.maxhenkel.voicechat.api.Group;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.agmas.harpymodloader.Harpymodloader;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.cat.express.wyspiaexpress.util.RoleUtil;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class LoversComponent implements AutoSyncedComponent {
    public static final ComponentKey<LoversComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "lovers"), LoversComponent.class);
    public static final Identifier BROKEN_HEART = Identifier.of(WyspiaExpress.MOD_ID, "broken_heart");
    private final World world;
    private final List<Pair> pairs = new ArrayList<>();
    private final Set<UUID> pendingConversions = new HashSet<>();

    private static final class Pair {
        final UUID first;
        final UUID second;
        final UUID voiceGroup;
        int lives;

        Pair(UUID first, UUID second, UUID voiceGroup, int lives) {
            this.first = first;
            this.second = second;
            this.voiceGroup = voiceGroup;
            this.lives = lives;
        }

        boolean contains(UUID player) { return first.equals(player) || second.equals(player); }
        UUID partner(UUID player) { return first.equals(player) ? second : first; }
    }

    public LoversComponent(World world) { this.world = world; }

    public static boolean isLover(Role role) {
        return role == WyspiaExpressRoles.LOVER_1 || role == WyspiaExpressRoles.LOVER_2;
    }

    @Nullable
    private Pair getPair(UUID player) {
        for (Pair pair : pairs) {
            if (pair.contains(player)) return pair;
        }
        return null;
    }

    @Nullable
    public UUID getPartner(UUID player) {
        Pair pair = getPair(player);
        return pair == null ? null : pair.partner(player);
    }

    public int getLives(UUID player) {
        Pair pair = getPair(player);
        return pair == null ? 0 : pair.lives;
    }

    public boolean arePartners(UUID first, UUID second) { return second.equals(getPartner(first)); }

    public void pair(UUID first, UUID second) {
        if (first.equals(second) || getPair(first) != null || getPair(second) != null) {
            throw new IllegalArgumentException("Lovers must be two distinct, unpaired players");
        }
        pairs.add(new Pair(first, second, UUID.randomUUID(), 2));
        sync();
    }
    public int assignPairs(int requestedPairs, List<ServerPlayerEntity> primaryCandidates) {
        if (!(world instanceof ServerWorld serverWorld)) return 0;
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        List<ServerPlayerEntity> candidates = new ArrayList<>(primaryCandidates);
        candidates.removeIf(player -> !isUnassignedCivilian(game, player));
        List<ServerPlayerEntity> forced = new ArrayList<>(candidates.stream().filter(player ->
                Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(player.getUuid()) == WyspiaExpressRoles.LOVER_1).toList());
        candidates.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid()));
        List<ServerPlayerEntity> companions = new ArrayList<>(serverWorld.getPlayers().stream()
                .filter(player -> isUnassignedCivilian(game, player)
                        && !Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())).toList());
        int count = 0;
        int limit = RoleUtil.getRemainingRoleSpawnLimit(WyspiaExpressRoles.LOVER_1, game);
        while (count < limit && (count < requestedPairs || !forced.isEmpty())) {
            if (forced.isEmpty() && candidates.isEmpty()) break;
            ServerPlayerEntity first = !forced.isEmpty() ? forced.removeFirst()
                    : RoleUtil.selectPlayersByRoleHistory(candidates, List.of(WyspiaExpressRoles.LOVER_1),
                            1, game.areWeightsEnabled(), world.getRandom()).getFirst();
            List<ServerPlayerEntity> partners = companions.stream().filter(player -> player != first).toList();
            if (partners.isEmpty()) break;
            partners = RoleUtil.preferRoleCandidates(partners, 1,
                    player -> !PlayerConfigComponent.KEY.get(player).avoidNeutral());
            // Prefer civilians outside this draft's already-selected neutral recipients.
            List<ServerPlayerEntity> civilians = partners.stream().filter(player -> !primaryCandidates.contains(player)).toList();
            if (!civilians.isEmpty()) partners = new ArrayList<>(civilians);
            ServerPlayerEntity second = partners.get(world.getRandom().nextInt(partners.size()));
            pair(first.getUuid(), second.getUuid());
            game.addRole(first, WyspiaExpressRoles.LOVER_1);
            game.addRole(second, WyspiaExpressRoles.LOVER_2);
            RoleUtil.recordRoleAssignment(first, WyspiaExpressRoles.LOVER_1);
            RoleUtil.recordRoleAssignment(second, WyspiaExpressRoles.LOVER_2);
            candidates.remove(first);
            candidates.remove(second);
            companions.remove(first);
            companions.remove(second);
            count++;
        }
        return count;
    }

    private boolean isUnassignedCivilian(GameWorldComponent game, ServerPlayerEntity player) {
        return Harpymodloader.OVERWRITE_ROLES.contains(game.getRole(player))
                && !game.canUseKillerFeatures(player) && getPair(player.getUuid()) == null;
    }

    /** Spend the extra life after other protections. The last life ends only on an actual death. */
    public boolean trySurviveLethalHit(PlayerEntity victim) {
        Pair pair = getPair(victim.getUuid());
        if (pair == null || pair.lives <= 1) return false;
        pair.lives--;
        sync();
        for (UUID member : List.of(pair.first, pair.second)) {
            PlayerEntity player = world.getPlayerByUuid(member);
            if (player != null) player.sendMessage(Text.translatable("message.wyspiaexpress.lovers.life_saved", pair.lives)
                    .withColor(WyspiaExpressRoles.LOVER_1.color()), true);
        }
        return true;
    }

    public void onDeath(PlayerEntity victim) {
        Pair pair = getPair(victim.getUuid());
        if (pair == null || pair.lives == 0) return;
        pair.lives = 0;
        sync();

        UUID partnerId = pair.partner(victim.getUuid());
        PlayerEntity partner = world.getPlayerByUuid(partnerId);
        if (partner == null) {
            WorldComponent.KEY.get(world).addPlayerDead(partnerId);
        } else if (partner instanceof ServerPlayerEntity serverPartner && GameFunctions.isPlayerAliveAndSurvival(partner)) {
            GameUtil.forceDeath(serverPartner, true, BROKEN_HEART);
        }
    }

    @Nullable
    public UUID endBond(UUID player) {
        Pair pair = getPair(player);
        if (pair == null) return null;
        pairs.remove(pair);
        closePhone(pair);
        sync();
        return pair.partner(player);
    }

    public void queueConversion(UUID player) {
        pendingConversions.add(player);
        GameWorldComponent.KEY.get(world).addRole(player, WyspiaExpressRoles.CULTIST);
        sync();
    }

    public void onJoin(ServerPlayerEntity player) {
        if (!GameWorldComponent.KEY.get(world).isRunning()) return;
        if (pendingConversions.remove(player.getUuid())) {
            GameUtil.convertToCultist(null, player);
            sync();
        }
    }

    public boolean teleport(ServerPlayerEntity player) {
        Pair pair = getPair(player.getUuid());
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (pair == null || pair.lives <= 0 || !game.isRunning()
                || !GameFunctions.isPlayerAliveAndSurvival(player) || !isLover(game.getRole(player))) return false;
        ServerPlayerEntity partner = player.getServer().getPlayerManager().getPlayer(pair.partner(player.getUuid()));
        if (!GameFunctions.isPlayerAliveAndSurvival(partner)) {
            player.sendMessage(Text.translatable("message.wyspiaexpress.lovers.partner_unavailable"), true);
            return false;
        }
        player.teleport(partner.getServerWorld(), partner.getX(), partner.getY(), partner.getZ(),
                Set.of(), player.getYaw(), player.getPitch());
        AbilityCooldownComponent.KEY.get(player).setAbilityCooldown(WyspiaExpress.ROLES_CONFIG.roleConfig.loversConfig.cooldown());
        return true;
    }

    public boolean togglePhone(ServerPlayerEntity player) {
        Pair pair = getPair(player.getUuid());
        if (pair == null || pair.lives <= 0 || !GameFunctions.isPlayerAliveAndSurvival(player)
                || !isLover(GameWorldComponent.KEY.get(world).getRole(player))) return false;
        var api = TrainVoicePlugin.SERVER_API;
        var connection = api == null ? null : api.getConnectionOf(player.getUuid());
        if (connection == null) {
            player.sendMessage(Text.translatable("message.wyspiaexpress.lovers.voice_unavailable"), true);
            return false;
        }
        boolean leaving = connection.getGroup() != null && pair.voiceGroup.equals(connection.getGroup().getId());
        if (leaving) connection.setGroup(null);
        else {
            Group group = api.getGroups().stream().filter(existing -> pair.voiceGroup.equals(existing.getId())).findFirst().orElse(null);
            if (group == null) group = api.groupBuilder().setId(pair.voiceGroup).setName("Lovers")
                    .setHidden(true).setPersistent(true).setType(Group.Type.ISOLATED).build();
            connection.setGroup(group);
        }
        player.sendMessage(Text.translatable(leaving ? "message.wyspiaexpress.lovers.phone_off" : "message.wyspiaexpress.lovers.phone_on"), true);
        return true;
    }

    private void leavePhone(UUID player, Pair pair) {
        var api = TrainVoicePlugin.SERVER_API;
        var connection = api == null ? null : api.getConnectionOf(player);
        if (connection != null && connection.getGroup() != null && pair.voiceGroup.equals(connection.getGroup().getId())) {
            connection.setGroup(null);
        }
    }

    private void closePhone(Pair pair) {
        leavePhone(pair.first, pair);
        leavePhone(pair.second, pair);
        if (TrainVoicePlugin.SERVER_API != null) TrainVoicePlugin.SERVER_API.removeGroup(pair.voiceGroup);
    }

    /** A pair is a team of its own; two different pairs never share a victory. */
    public List<UUID> winningPair(List<ServerPlayerEntity> alive) {
        if (alive.isEmpty()) return List.of();
        Pair pair = getPair(alive.getFirst().getUuid());
        if (pair == null || pair.lives <= 0 || alive.stream().anyMatch(player -> !pair.contains(player.getUuid()))) return List.of();
        return List.of(pair.first, pair.second);
    }

    public void reset() {
        for (Pair pair : pairs) closePhone(pair);
        pairs.clear();
        pendingConversions.clear();
        sync();
    }

    private void sync() { if (!world.isClient) KEY.sync(world); }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        NbtList pairTags = new NbtList();
        for (Pair pair : pairs) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("first", pair.first);
            entry.putUuid("second", pair.second);
            entry.putUuid("voiceGroup", pair.voiceGroup);
            entry.putInt("lives", pair.lives);
            pairTags.add(entry);
        }
        tag.put("pairs", pairTags);
        NbtList conversions = new NbtList();
        for (UUID player : pendingConversions) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("player", player);
            conversions.add(entry);
        }
        tag.put("conversions", conversions);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        pairs.clear();
        pendingConversions.clear();
        for (NbtElement element : tag.getList("pairs", NbtElement.COMPOUND_TYPE)) {
            NbtCompound entry = (NbtCompound) element;
            Pair pair = new Pair(entry.getUuid("first"), entry.getUuid("second"), entry.getUuid("voiceGroup"),
                    Math.clamp(entry.getInt("lives"), 0, 2));
            pairs.add(pair);
        }
        for (NbtElement element : tag.getList("conversions", NbtElement.COMPOUND_TYPE)) {
            pendingConversions.add(((NbtCompound) element).getUuid("player"));
        }
    }
}
