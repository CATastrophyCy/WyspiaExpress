package org.cat.express.wyspiaexpress.components.roles;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.cat.express.wyspiaexpress.components.PlayerShopComponent;
import org.cat.express.wyspiaexpress.config.ShopConfig;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

public final class HitmanComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<HitmanComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "hitman"), HitmanComponent.class);
    private static final TagKey<Item> WEAPONS = TagKey.of(RegistryKeys.ITEM, Identifier.of(WyspiaExpress.MOD_ID, "hitman_weapons"));

    public enum TargetStatus { ARMED, PROTECTED, IN_LOVE, POISONED, DELUDED }

    private final PlayerEntity player;
    private UUID target;
    private String targetName = "";
    private boolean firstTarget;
    private boolean stage2;
    private PlayerMoodComponent.Task targetTask;
    private EnumSet<TargetStatus> targetStatuses = EnumSet.noneOf(TargetStatus.class);

    public HitmanComponent(PlayerEntity player) { this.player = player; }
    public boolean isStage2() { return stage2; }
    public PlayerMoodComponent.Task getTargetTask() { return targetTask; }
    public boolean hasTargetStatus(TargetStatus status) { return targetStatuses.contains(status); }
    public boolean isTarget(PlayerEntity other) { return other.getUuid().equals(target); }
    public UUID target() { return target; }
    public Text targetName() { return target == null ? Text.translatable("hud.wyspiaexpress.hitman.no_target") : Text.literal(targetName); }

    public void onAssign() {
        stage2 = false;
        firstTarget = true;
        selectTarget();
        KEY.sync(player);
    }

    public void reset() {
        if (target == null && !stage2 && !firstTarget) return;
        clearTarget();
        firstTarget = false;
        stage2 = false;
        KEY.sync(player);
    }

    private void clearTarget() {
        target = null;
        targetName = "";
        targetTask = null;
        targetStatuses.clear();
    }

    private void selectTarget() {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        var candidates = player.getWorld().getPlayers().stream()
                .filter(other -> other != player && GameFunctions.isPlayerAliveAndSurvival(other)
                        && game.getRole(other) != null).toList();
        if (firstTarget) {
            var preferred = candidates.stream().filter(other -> {
                var role = game.getRole(other);
                return role != WatheRoles.VIGILANTE && role != Noellesroles.BETTER_VIGILANTE;
            }).toList();
            if (!preferred.isEmpty()) candidates = preferred;
        }
        var civilians = candidates.stream().filter(game::isInnocent).toList();
        var pool = civilians.isEmpty() ? candidates : civilians;
        if (pool.isEmpty()) {
            clearTarget();
            return;
        }
        PlayerEntity chosen = pool.get(player.getRandom().nextInt(pool.size()));
        firstTarget = false;
        target = chosen.getUuid();
        targetName = chosen.getGameProfile().getName();
        targetTask = getTask(chosen);
        targetStatuses = getStatuses(chosen, game);
    }

    private static PlayerMoodComponent.Task getTask(PlayerEntity player) {
        return PlayerMoodComponent.KEY.get(player).tasks.keySet().stream().findFirst().orElse(null);
    }

    private static EnumSet<TargetStatus> getStatuses(PlayerEntity target, GameWorldComponent game) {
        var statuses = EnumSet.noneOf(TargetStatus.class);
        if (target.getMainHandStack().isIn(WEAPONS) || target.getOffHandStack().isIn(WEAPONS)) {
            statuses.add(TargetStatus.ARMED);
        }
        if (isProtected(target)) statuses.add(TargetStatus.PROTECTED);
        if (LoversComponent.isLover(game.getRole(target))) statuses.add(TargetStatus.IN_LOVE);
        var poison = PlayerPoisonComponent.KEY.get(target);
        if (poison.poisonTicks > 0) {
            statuses.add(GameUtil.isDeluded(poison) ? TargetStatus.DELUDED : TargetStatus.POISONED);
        }
        return statuses;
    }

    private static boolean isProtected(PlayerEntity target) {
        return BartenderPlayerComponent.KEY.get(target).armor > 0
                || PhysicianComponent.KEY.get(target).physicianArmor > 0
                || DreamerComponent.KEY.get(target).dreamArmor > 0
                || LoversComponent.KEY.get(target.getWorld()).getLives(target.getUuid()) > 1;
    }

    private void onDeath(PlayerEntity victim, PlayerEntity killer, Identifier deathReason) {
        boolean targetDied = isTarget(victim);
        if (killer == player) {
            if (targetDied) {
                boolean advancing = !stage2;
                stage2 = true;
                ShopUtil.addCoin(player, WyspiaExpress.ROLES_CONFIG.roleConfig.hitmanConfig.targetReward());
                player.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.8f, 1.1f);
                if (advancing) {
                    var config = WyspiaExpress.ROLES_CONFIG.roleConfig.hitmanConfig;
                    PlayerShopComponent.KEY.get(player).setShop(config.basic.enableShop()
                            ? ShopConfig.fromStrings(config.stage2ShopEntries()) : java.util.List.of());
                    player.sendMessage(Text.translatable("message.wyspiaexpress.hitman.stage2"), false);
                }
            } else if (!stage2) {
                player.sendMessage(Text.translatable("message.wyspiaexpress.hitman.wrong_kill"), false);
                GameFunctions.killPlayer(player, true, null, deathReason);
                return;
            }
        }
        if (targetDied) {
            selectTarget();
            KEY.sync(player);
        }
    }

    public static void onPlayerDeath(PlayerEntity victim, PlayerEntity killer, Identifier deathReason) {
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        if (!game.isRunning()) return;
        for (PlayerEntity hitman : victim.getWorld().getPlayers()) {
            if (game.isRole(hitman, WyspiaExpressRoles.HITMAN) && GameFunctions.isPlayerAliveAndSurvival(hitman))
                KEY.get(hitman).onDeath(victim, killer, deathReason);
        }
    }

    @Override public void serverTick() {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        if (player.getWorld().getTime() % 20 != 0 || !game.isRunning()
                || !game.isRole(player, WyspiaExpressRoles.HITMAN) || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        // Also catches targets that disconnected while this Hitman was offline.
        PlayerEntity current = target == null ? null : player.getWorld().getPlayerByUuid(target);
        if (GameFunctions.isPlayerEliminated(current)) {
            UUID previous = target;
            selectTarget();
            if (!Objects.equals(previous, target)) KEY.sync(player);
            return;
        }

        var task = getTask(current);
        var statuses = getStatuses(current, game);
        boolean taskChanged = task != targetTask;
        if (!taskChanged && statuses.equals(targetStatuses)) return;
        targetTask = task;
        targetStatuses = statuses;
        if (taskChanged) KEY.sync(player);
        else KEY.sync(player, this, recipient -> recipient == player);
    }

    @Override public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        NbtCompound tag = new NbtCompound();
        writeToNbt(tag, buf.getRegistryManager());
        if (recipient != player) tag.remove("targetStatuses");
        buf.writeNbt(tag);
    }

    @Override public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        if (target != null) tag.putUuid("target", target);
        tag.putString("targetName", targetName);
        if (firstTarget) tag.putBoolean("awaitingFirstTarget", true);
        tag.putBoolean("stage2", stage2);
        if (targetTask != null) tag.putString("targetTask", targetTask.name());
        if (!targetStatuses.isEmpty()) {
            NbtCompound statuses = new NbtCompound();
            for (TargetStatus status : targetStatuses) statuses.putBoolean(status.name(), true);
            tag.put("targetStatuses", statuses);
        }
    }

    @Override public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        target = tag.containsUuid("target") ? tag.getUuid("target") : null;
        targetName = tag.getString("targetName");
        firstTarget = tag.getBoolean("awaitingFirstTarget");
        stage2 = tag.getBoolean("stage2");
        targetTask = tag.contains("targetTask") ? PlayerMoodComponent.Task.valueOf(tag.getString("targetTask")) : null;
        targetStatuses.clear();
        var statuses = tag.getCompound("targetStatuses");
        for (TargetStatus status : TargetStatus.values()) {
            if (statuses.getBoolean(status.name())) targetStatuses.add(status);
        }
    }
}
