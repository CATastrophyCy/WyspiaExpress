package org.cat.express.wyspiaexpress.components;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.nbt.NbtOps;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.util.RoleUtil;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedGameMode;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Participating profiles captured at round start, then final roles/winners retained through lobby rendering. */
public final class RoundResultComponent implements AutoSyncedComponent {
    public static final ComponentKey<RoundResultComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "round_result"), RoundResultComponent.class);

    public enum Outcome {
        CIVILIANS("civilian", "passengers"), KILLERS("killer", "killers"), TIMEOUT("civilian", "time"),
        CULT("kinswathe.cult"), LICENSED_VILLAIN("kinswathe.villain_licensed"),
        EDDIE_WAFFLES("kinswathe.eddie_waffles"), THIEF("kinswathe.thief"),
        ARSONIST("stupid_express.arsonist"), LOVERS("stupid_express.lovers"), NEUTRAL("wyspiaexpress.neutral");

        private final String announcement;
        private final String explanation;

        Outcome(String key) { this(key, key); }
        Outcome(String announcement, String explanation) {
            this.announcement = announcement;
            this.explanation = explanation;
        }
    }

    private final World world;
    private Outcome outcome;
    private int color;
    private Set<UUID> winners = Set.of();
    private Map<UUID, Role> roles = Map.of();
    private Map<UUID, GameProfile> profiles = Map.of();
    private Set<UUID> eliminated = Set.of();

    public record Player(GameProfile player, Role role, boolean wasDead) {}

    public RoundResultComponent(World world) { this.world = world; }

    public boolean hasResult() { return outcome != null; }
    public boolean ownsResult() {
        return hasResult() && GameWorldComponent.KEY.get(world).getGameMode() instanceof WyspiaExpressModdedGameMode;
    }
    public boolean didWin(UUID player) { return winners.contains(player); }
    public boolean participated(UUID player) { return profiles.containsKey(player); }
    public GameProfile profile(UUID player) { return profiles.get(player); }
    public Map<UUID, GameProfile> profiles() { return profiles; }
    public List<Player> players() {
        return profiles.values().stream().filter(profile -> roles.containsKey(profile.getId()))
                .map(profile -> new Player(profile, roles.get(profile.getId()), eliminated.contains(profile.getId()))).toList();
    }
    public Role role(UUID player) { return roles.get(player); }
    public Outcome outcome() { return outcome; }
    public int color() { return color; }
    public Set<UUID> winners() { return winners; }
    public MutableText title() {
        return Text.translatable("announcement.win." + outcome.announcement);
    }
    public MutableText description() {
        return Text.translatable("game.win." + outcome.explanation);
    }

    /** Capture only the actual round participants, never the world's spectator/late-join list. */
    public void startRound(List<ServerPlayerEntity> participants) {
        clearResult();
        Map<UUID, GameProfile> captured = new LinkedHashMap<>();
        for (ServerPlayerEntity player : participants) {
            GameProfile profile = new GameProfile(player.getUuid(), player.getGameProfile().getName());
            profile.getProperties().putAll(player.getGameProfile().getProperties());
            captured.put(profile.getId(), profile);
        }
        profiles = Collections.unmodifiableMap(captured);
        KEY.sync(world);
    }

    private void clearResult() {
        outcome = null;
        color = 0;
        winners = Set.of();
        roles = Map.of();
        eliminated = Set.of();
    }

    public void reset() {
        clearResult();
        profiles = Map.of();
        KEY.sync(world);
    }

    /** First result wins. Native and external snapshots cannot replace a committed custom claim. */
    public void setVictory(Outcome outcome, int color, Collection<UUID> winners) {
        if (hasResult()) return;
        this.outcome = outcome;
        this.color = color;
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        Map<UUID, Role> capturedRoles = new HashMap<>();
        Set<UUID> capturedEliminated = new HashSet<>();
        for (UUID player : profiles.keySet()) {
            Role role = game.getRole(player);
            if (role == null) continue;
            capturedRoles.put(player, role);
            if (GameFunctions.isPlayerEliminated(world.getPlayerByUuid(player))) capturedEliminated.add(player);
        }
        this.roles = Map.copyOf(capturedRoles);
        this.eliminated = Set.copyOf(capturedEliminated);
        this.winners = Set.copyOf(roles.entrySet().stream()
                .filter(entry -> winners.contains(entry.getKey()) || (outcome == Outcome.CULT
                        && (entry.getValue() == WyspiaExpressRoles.CULT_LEADER
                        || entry.getValue() == WyspiaExpressRoles.CULTIST)))
                .map(Map.Entry::getKey).toList());
        KEY.sync(world);
    }

    /** Called before Wathe snapshots/syncs its player profiles, while final roles still exist. */
    public void capture(GameFunctions.WinStatus status) {
        if (hasResult()) return;
        var kin = org.BsXinQin.kinswathe.component.CustomWinnerComponent.KEY.get(world);
        var stupid = pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent.KEY.get(world);
        // Items such as the upstream Arsonist lighter can commit a result outside the mode loop.
        if (kin.hasCustomWinner()) {
            setVictory(externalOutcome(kin.getWinningTextId()), kin.getColor(),
                    kin.getWinners().stream().filter(java.util.Objects::nonNull).map(ServerPlayerEntity::getUuid).toList());
        } else if (stupid.hasCustomWinner()) {
            setVictory(externalOutcome(stupid.getWinningTextId()), stupid.getColor(),
                    stupid.getWinners().stream().filter(java.util.Objects::nonNull).map(player -> player.getUuid()).toList());
        } else {
            boolean killerVictory = status == GameFunctions.WinStatus.KILLERS;
            if (!killerVictory && status != GameFunctions.WinStatus.PASSENGERS && status != GameFunctions.WinStatus.TIME) return;
            GameWorldComponent game = GameWorldComponent.KEY.get(world);
            Outcome outcome = killerVictory ? Outcome.KILLERS
                    : status == GameFunctions.WinStatus.TIME ? Outcome.TIMEOUT : Outcome.CIVILIANS;
            setVictory(outcome, killerVictory ? 0xD23C3C : 0x4EDD35,
                    game.getRoles().entrySet().stream()
                            .filter(entry -> killerVictory
                                    ? entry.getValue().canUseKiller() || WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(entry.getValue())
                                    : entry.getValue().isInnocent())
                            .map(Map.Entry::getKey).toList());
        }
    }

    private static Outcome externalOutcome(String textId) {
        String path = textId.substring(textId.indexOf(':') + 1);
        return switch (path) {
            case "cult" -> Outcome.CULT;
            case "licensed_villain", "villain_licensed" -> Outcome.LICENSED_VILLAIN;
            case "eddie_waffles" -> Outcome.EDDIE_WAFFLES;
            case "thief" -> Outcome.THIEF;
            case "arsonist" -> Outcome.ARSONIST;
            case "lovers" -> Outcome.LOVERS;
            default -> Outcome.NEUTRAL;
        };
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        if (outcome != null) tag.putString("Outcome", outcome.name());
        tag.putInt("Color", color);
        tag.put("Winners", toNbt(winners));
        tag.put("Eliminated", toNbt(eliminated));
        NbtList profileTags = new NbtList();
        for (GameProfile profile : profiles.values()) {
            profileTags.add(ProfileComponent.CODEC.encodeStart(NbtOps.INSTANCE, new ProfileComponent(profile)).getOrThrow());
        }
        tag.put("Profiles", profileTags);
        NbtCompound roleTags = new NbtCompound();
        roles.forEach((player, role) -> roleTags.putString(player.toString(), RoleUtil.getRoleId(role)));
        tag.put("Roles", roleTags);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        outcome = tag.contains("Outcome", NbtElement.STRING_TYPE) ? Outcome.valueOf(tag.getString("Outcome")) : null;
        color = tag.getInt("Color");
        Map<UUID, GameProfile> capturedProfiles = new LinkedHashMap<>();
        for (NbtElement entry : tag.getList("Profiles", NbtElement.COMPOUND_TYPE)) {
            GameProfile profile = ProfileComponent.CODEC.parse(NbtOps.INSTANCE, entry).getOrThrow().gameProfile();
            capturedProfiles.put(profile.getId(), profile);
        }
        profiles = Collections.unmodifiableMap(capturedProfiles);
        eliminated = fromNbt(tag.getList("Eliminated", NbtElement.INT_ARRAY_TYPE));
        winners = fromNbt(tag.getList("Winners", NbtElement.INT_ARRAY_TYPE));
        NbtCompound roleTags = tag.getCompound("Roles");
        Map<UUID, Role> capturedRoles = new HashMap<>();
        for (String player : roleTags.getKeys()) {
            String roleId = roleTags.getString(player);
            WatheRoles.ROLES.stream().filter(role -> RoleUtil.getRoleId(role).equals(roleId)).findFirst()
                    .ifPresent(role -> capturedRoles.put(UUID.fromString(player), role));
        }
        roles = Map.copyOf(capturedRoles);
    }

    private static NbtList toNbt(Set<UUID> players) {
        NbtList list = new NbtList();
        for (UUID player : players) list.add(NbtHelper.fromUuid(player));
        return list;
    }

    private static Set<UUID> fromNbt(NbtList list) {
        Set<UUID> players = new HashSet<>();
        for (NbtElement element : list) players.add(NbtHelper.toUuid(element));
        return Set.copyOf(players);
    }
}
