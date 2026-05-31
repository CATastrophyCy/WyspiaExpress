package org.cat.express.wyspiaexpress.client.mixin;


import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.cat.express.wyspiaexpress.client.WyspiaexpressClient;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class GetTargetMixin {


    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/game/GameFunctions;isPlayerSpectatingOrCreative(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private static void wyspiaexpress$getTarget(TextRenderer renderer, @NotNull ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HitResult line = ProjectileUtil.getCollision(player, (entity) -> entity instanceof PlayerBodyEntity, 3.0);
        WyspiaexpressClient.TARGET_BODY = null;
        if (line instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof PlayerBodyEntity playerBodyEntity) {
                WyspiaexpressClient.TARGET_BODY  = playerBodyEntity;
            }
        }
        HitResult hitResult = ProjectileUtil.getCollision(player, entity -> entity instanceof @NotNull PlayerEntity target
                && GameFunctions.isPlayerAliveAndSurvival(target), 2.0);
        WyspiaexpressClient.TARGET_PLAYER = null;
        if (hitResult instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof PlayerEntity playerEntity) {
                WyspiaexpressClient.TARGET_PLAYER  = playerEntity;
            }
        }
    }
}

