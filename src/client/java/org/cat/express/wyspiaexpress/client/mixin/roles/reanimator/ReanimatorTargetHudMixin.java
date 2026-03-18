package org.cat.express.wyspiaexpress.client.mixin.roles.reanimator;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.WyspiaexpressClient;
import org.cat.express.wyspiaexpress.client.roles.ReanimatorUtil;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.roles.ReanimatorReviveComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class ReanimatorTargetHudMixin {

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void getTargetHud(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ReanimatorUtil.BODY == null || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        AbilityCooldownComponent ability = AbilityCooldownComponent.KEY.get(player);
        ReanimatorReviveComponent component = ReanimatorReviveComponent.KEY.get(player.getWorld());
        if (gameWorld.isRole(player, WyspiaExpressRoles.REANIMATOR) && WatheClient.isPlayerAliveAndInSurvival()) {
            if (ability.cooldown > 0
                || component.getAvailableRevives() <= 0
                || gameWorld.getRole(ReanimatorUtil.BODY.getPlayerUuid()) == WyspiaExpressRoles.REANIMATOR_GHOUL)
                return;
            context.getMatrices().push();
            context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F, (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
            context.getMatrices().scale(0.6F, 0.6F, 1.0F);
            Text targetInfo = Text.translatable("hud.wyspiaexpress.reanimator.revive", WyspiaexpressClient.abilityBind.getBoundKeyLocalizedText()).withColor(WyspiaExpressRoles.REANIMATOR.color());
            context.drawTextWithShadow(renderer, targetInfo, -renderer.getWidth(targetInfo) / 2, 32, WyspiaExpressRoles.REANIMATOR.color());
            context.getMatrices().pop();
        }
    }

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/game/GameFunctions;isPlayerSpectatingOrCreative(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private static void getTarget(TextRenderer renderer, @NotNull ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        if (!gameWorld.isRole(player, WyspiaExpressRoles.REANIMATOR)) {ReanimatorUtil.BODY = null;return;}
        HitResult line = ProjectileUtil.getCollision(player, (entity) -> entity instanceof PlayerBodyEntity, WyspiaExpress.ROLES_CONFIG.roleConfig.reanimatorConfig.range());
        ReanimatorUtil.BODY = null;
        if (line instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof PlayerBodyEntity playerBodyEntity) {
                ReanimatorUtil.BODY = playerBodyEntity;
            }
        }
    }
}

