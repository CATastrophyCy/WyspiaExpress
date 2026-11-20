package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.explosion.Explosion;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.client.items.ThrowablePreview;
import org.cat.express.wyspiaexpress.util.GameUtil;

public final class SleepingHitbox {
    private SleepingHitbox() {}

    public static void register() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(SleepingHitbox::render);
    }

    private static void render(WorldRenderContext context) {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null || player.isSpectator() || client.options.hudHidden || client.currentScreen != null
                || !client.options.getPerspective().isFirstPerson()) return;
        double range = getWeaponRange(player);
        if (range <= 0 && !ThrowablePreview.hasGrenadePreview()) return;
        if (GameUtil.isUnderSmokeEffect(player)
                || WorldBlackoutComponent.KEY.get(player.getWorld()).isBlackoutActive()) return;

        var matrices = context.matrixStack();
        var consumers = context.consumers();
        if (matrices == null || consumers == null) return;
        var camera = context.camera().getPos();
        var cameraOffset = camera.negate();
        for (PlayerEntity target : player.getWorld().getPlayers()) {
            if (target == player || !GameUtil.isSleepingOnBed(target) || !GameFunctions.isPlayerAliveAndSurvival(target)
                    || target.isInvisible() || GameUtil.isUnderSmokeEffect(target)) continue;
            if (!ThrowablePreview.isVisibleGrenadeTarget(target, camera)
                    && (range <= 0 || GameUtil.distanceToSleepingHitbox(player, target) > range
                    || Explosion.getExposure(camera, target) <= 0)) continue;
            WorldRenderer.drawBox(matrices, consumers.getBuffer(RenderLayer.getLines()),
                    target.getBoundingBox().offset(cameraOffset), 1.0F, 0.84F, 0.56F, 0.5F);
        }
    }

    private static double getWeaponRange(PlayerEntity player) {
        var held = player.getMainHandStack();
        if (held.isOf(WatheItems.BAT) || held.isOf(KinsWatheItems.POISON_INJECTOR)) {
            return player.getEntityInteractionRange();
        }
        if (held.isOf(WatheItems.DERRINGER)) return 7;
        if (held.isOf(WatheItems.REVOLVER) || held.isOf(WyspiaExpressItems.OUTLAW_REVOLVER)
                || held.isOf(KinsWatheItems.BLOWGUN)) return 15;
        if (held.isOf(WatheItems.KNIFE) || held.isOf(KinsWatheItems.HUNTING_KNIFE)
                || held.isOf(WyspiaExpressItems.RITUAL_DAGGER) || held.isOf(KinsWatheItems.PAN)) return 3;
        return 0;
    }
}
