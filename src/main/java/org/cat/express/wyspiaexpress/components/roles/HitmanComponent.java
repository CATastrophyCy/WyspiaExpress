package org.cat.express.wyspiaexpress.components.roles;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.cat.express.wyspiaexpress.components.PlayerShopComponent;
import org.cat.express.wyspiaexpress.config.ShopConfig;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Objects;
import java.util.UUID;

public final class HitmanComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<HitmanComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "hitman"), HitmanComponent.class);
    private final PlayerEntity player;
    private UUID target;
    private String targetName = "";
    private boolean stage2;
    private PlayerMoodComponent.Task targetTask;

    public HitmanComponent(PlayerEntity player) { this.player = player; }
    public boolean isStage2() { return stage2; }
    public PlayerMoodComponent.Task getTargetTask() { return targetTask; }
    public boolean isTarget(PlayerEntity other) { return other.getUuid().equals(target); }
    public UUID target() { return target; }
    public Text targetName() { return target == null ? Text.translatable("hud.wyspiaexpress.hitman.no_target") : Text.literal(targetName); }

    public void assign() {
        stage2 = false;
        selectTarget();
        KEY.sync(player);
    }

    public void reset() {
        if (target == null && !stage2) return;
        target = null;
        targetName = "";
        targetTask = null;
        stage2 = false;
        KEY.sync(player);
    }

    private void selectTarget() {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        var candidates = player.getWorld().getPlayers().stream()
                .filter(other -> other != player && GameFunctions.isPlayerAliveAndSurvival(other)
                        && game.getRole(other) != null).toList();
        var civilians = candidates.stream().filter(game::isInnocent).toList();
        var pool = civilians.isEmpty() ? candidates : civilians;
        PlayerEntity chosen = pool.isEmpty() ? null : pool.get(player.getRandom().nextInt(pool.size()));
        target = chosen == null ? null : chosen.getUuid();
        targetName = chosen == null ? "" : chosen.getGameProfile().getName();
        targetTask = getTask(chosen);
    }

    private static PlayerMoodComponent.Task getTask(PlayerEntity player) {
        return player == null ? null : PlayerMoodComponent.KEY.get(player).tasks.keySet().stream().findFirst().orElse(null);
    }

    private void onDeath(PlayerEntity victim, PlayerEntity killer, Identifier deathReason) {
        if (killer == player) {
            if (isTarget(victim)) {
                boolean advancing = !stage2;
                stage2 = true;
                ShopUtil.addCoin(player, WyspiaExpress.ROLES_CONFIG.roleConfig.hitmanConfig.targetReward());
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
        if (isTarget(victim)) {
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
        } else {
            var task = getTask(current);
            if (task != targetTask) {
                targetTask = task;
                KEY.sync(player);
            }
        }
    }

    @Override public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        if (target != null) tag.putUuid("target", target);
        tag.putString("targetName", targetName);
        tag.putBoolean("stage2", stage2);
        if (targetTask != null) tag.putString("targetTask", targetTask.name());
    }

    @Override public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        target = tag.containsUuid("target") ? tag.getUuid("target") : null;
        targetName = tag.getString("targetName");
        stage2 = tag.getBoolean("stage2");
        targetTask = tag.contains("targetTask") ? PlayerMoodComponent.Task.valueOf(tag.getString("targetTask")) : null;
    }
}
