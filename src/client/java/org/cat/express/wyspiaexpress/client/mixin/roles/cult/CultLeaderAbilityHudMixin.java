package org.cat.express.wyspiaexpress.client.mixin.roles.cult;

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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class CultLeaderAbilityHudMixin {
    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void wyspiaexpress$cultLeaderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        var player =  MinecraftClient.getInstance().player;
        if(player == null) return;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        AbilityCooldownComponent abilityPlayerComponent = AbilityCooldownComponent.KEY.get(player);
        if (gameWorldComponent.isRole(player, WyspiaExpressRoles.CULT_LEADER) &&
                GameFunctions.isPlayerAliveAndSurvival(player)) {
            int drawY = context.getScaledWindowHeight();
            Text line = getText(abilityPlayerComponent);
            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line, context.getScaledWindowWidth() - getTextRenderer().getWidth(line), drawY, WyspiaExpressRoles.CULT_LEADER.color());
        }
    }
    @Unique
    private static @NotNull Text getText(AbilityCooldownComponent abilityPlayerComponent) {
        Text line = Text.translatable("tip.wyspiaexpress.ability.cult_leader.revive");
        if (abilityPlayerComponent.cooldown > 0) {
            line = Text.translatable("tip.wyspiaexpress.ability.cooldown", abilityPlayerComponent.cooldown/20);
        }
        return line;
    }
}