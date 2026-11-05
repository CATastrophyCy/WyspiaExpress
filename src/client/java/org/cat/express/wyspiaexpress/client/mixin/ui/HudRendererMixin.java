package org.cat.express.wyspiaexpress.client.mixin.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.cat.express.wyspiaexpress.client.ui.DurationHud;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.roles.HitmanComponent;
import org.cat.express.wyspiaexpress.client.ui.SurvivalHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class HudRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    public void renderExtraBars(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        if (!gameWorld.isRunning()) return;

        DurationHud.render(context, tickCounter);

        SurvivalHud.render(context);
        if (gameWorld.isRole(client.player, WyspiaExpressRoles.HITMAN) && GameFunctions.isPlayerAliveAndSurvival(client.player)) {
            HitmanComponent hitman = HitmanComponent.KEY.get(client.player);
            Text text = Text.translatable("hud.wyspiaexpress.hitman.target", hitman.targetName());
            context.drawTextWithShadow(client.textRenderer, text,
                    (context.getScaledWindowWidth() - client.textRenderer.getWidth(text)) / 2,
                    context.getScaledWindowHeight() - 64, WyspiaExpressRoles.HITMAN.color());
            if (hitman.target() != null) {
                String task = hitman.getTargetTask() == null ? "none" : hitman.getTargetTask().name().toLowerCase(java.util.Locale.ROOT);
                Text taskText = Text.translatable("hud.wyspiaexpress.hitman.task." + task,
                        hitman.targetName().copy().withColor(WyspiaExpressRoles.HITMAN.color()));
                context.drawTextWithShadow(client.textRenderer, taskText,
                        context.getScaledWindowWidth() - client.textRenderer.getWidth(taskText) - 6,
                        context.getScaledWindowHeight() - 38, 0xFFFFFF);
            }
        }
    }
}
