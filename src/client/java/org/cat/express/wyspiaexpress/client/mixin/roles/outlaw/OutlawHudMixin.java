package org.cat.express.wyspiaexpress.client.mixin.roles.outlaw;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.WyspiaexpressClient;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class OutlawHudMixin {

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void getAbilityHud(@NotNull DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        AbilityCooldownComponent ability = AbilityCooldownComponent.KEY.get(MinecraftClient.getInstance().player);
        PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(MinecraftClient.getInstance().player);
        if (gameWorld.isRole(MinecraftClient.getInstance().player, WyspiaExpressRoles.OUTLAW) && WatheClient.isPlayerAliveAndInSurvival()) {
            int drawY = context.getScaledWindowHeight();
            Text line;
            if (playerShop.balance < WyspiaExpress.ROLES_CONFIG.roleConfig.outlawConfig.cost()) {
                line = Text.translatable("tip.wyspiaexpress.ability.not_enough_money", WyspiaExpress.ROLES_CONFIG.roleConfig.outlawConfig.cost());
            } else if (ability.cooldown > 0) {
                line = Text.translatable("tip.wyspiaexpress.ability.cooldown", ability.cooldown / 20);
            } else {
                line = Text.translatable("tip.wyspiaexpress.ability.can_use", WyspiaexpressClient.abilityBind.getBoundKeyLocalizedText());
            }
            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line, context.getScaledWindowWidth() - getTextRenderer().getWidth(line), drawY, WyspiaExpressRoles.OUTLAW.color());
        }
    }

}
