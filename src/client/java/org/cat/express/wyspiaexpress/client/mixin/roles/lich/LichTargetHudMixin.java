package org.cat.express.wyspiaexpress.client.mixin.roles.lich;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.WyspiaexpressClient;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class LichTargetHudMixin {

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void wyspiaexpress$getTargetHud(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (WyspiaexpressClient.TARGET_BODY == null || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        AbilityCooldownComponent ability = AbilityCooldownComponent.KEY.get(player);
        LichReviveComponent component = LichReviveComponent.KEY.get(player.getWorld());
        if (gameWorld.isRole(player, WyspiaExpressRoles.LICH) && WatheClient.isPlayerAliveAndInSurvival()) {
            if (ability.cooldown > 0
                || component.getAvailableRevives() <= 0
                || gameWorld.getRole(WyspiaexpressClient.TARGET_BODY.getPlayerUuid()) == WyspiaExpressRoles.LICH_GHOUL)
                return;
            context.getMatrices().push();
            context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F, (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
            context.getMatrices().scale(0.6F, 0.6F, 1.0F);
            Text targetInfo = Text.translatable("hud.wyspiaexpress.lich.revive", WyspiaexpressClient.abilityBind.getBoundKeyLocalizedText()).withColor(WyspiaExpressRoles.LICH.color());
            context.drawTextWithShadow(renderer, targetInfo, -renderer.getWidth(targetInfo) / 2, 32, WyspiaExpressRoles.LICH.color());
            context.getMatrices().pop();
        }
    }
}

