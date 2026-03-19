package org.cat.express.wyspiaexpress.client.mixin.roles.lich;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class LichHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void lichHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        var player =  MinecraftClient.getInstance().player;
        if(player == null) return;
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        AbilityCooldownComponent abilityPlayerComponent = (AbilityCooldownComponent) AbilityCooldownComponent.KEY.get(player);
        if (gameWorldComponent.isRole(player, WyspiaExpressRoles.LICH) &&
                GameFunctions.isPlayerAliveAndSurvival(player)) {
            LichReviveComponent component = LichReviveComponent.KEY.get(player.getWorld());
            int drawY = context.getScaledWindowHeight();
            Text line = getText(component, abilityPlayerComponent);
            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line, context.getScaledWindowWidth() - getTextRenderer().getWidth(line), drawY, WyspiaExpressRoles.LICH.color());
        }
    }
    @Unique
    private static @NotNull Text getText(LichReviveComponent component, AbilityCooldownComponent abilityPlayerComponent) {
        Text line = Text.translatable("tip.wyspiaexpress.ability.lich.revive", component.getAvailableRevives());
        ;
        if(component.getAvailableRevives() <= 0){
            line = Text.translatable("tip.wyspiaexpress.ability.lich.no_revive");
        }
        if (abilityPlayerComponent.cooldown > 0) {
            line = Text.translatable("tip.wyspiaexpress.ability.cooldown", abilityPlayerComponent.cooldown/20);
        }
        return line;
    }
}
