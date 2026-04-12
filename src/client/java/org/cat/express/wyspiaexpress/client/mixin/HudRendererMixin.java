package org.cat.express.wyspiaexpress.client.mixin;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.components.PlayerDepressedComponent;
import org.cat.express.wyspiaexpress.components.PlayerFreezeComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class HudRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    public void renderExtraBars(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || !WatheClient.isPlayerAliveAndInSurvival()) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        if (!gameWorld.isRunning()) return;

        int depressedTick = PlayerDepressedComponent.KEY.get(client.player).getDepressionTick();
        int freezeTick = PlayerFreezeComponent.KEY.get(client.player).getFreezeTick();

        if (depressedTick > 0 && WyspiaExpress.SERVER_CONFIG.depressionKilling()) {
            int depressedTimerTick = WyspiaExpress.SERVER_CONFIG.depressedTimer() * 20 * 2;
            renderHeatBar(context, depressedTick, depressedTimerTick);
        }

        if (freezeTick > 0 && WyspiaExpress.SERVER_CONFIG.freeze()) {
            int freezeTimer = WyspiaExpress.SERVER_CONFIG.freezeTimer() * 20 * 2 ;
            renderFreezeBar(context, freezeTick, freezeTimer);
        }
    }

    @Unique
    private void renderHeatBar(DrawContext context, float currentValue, float maxValue) {
        float percent = Math.clamp(currentValue / maxValue, 0, 1);
        if (percent <= 0) return;

        // Bar dimensions and positioning (below MoodRenderer which is around y=20)
        int x = 26;
        int y = 35;
        int maxWidth = 100;
        int height = 3;

        int fillWidth = (int) (maxWidth * percent);
        if (fillWidth <= 0) return;

        int red = 255;
        int green = (int) (200 * (1f - percent));
        int blue = (int) (200 * (1f - percent));
        int barColor = 0xFF000000 | (red << 16) | (green << 8) | blue;

        context.fill(x - 1, y - 1, x + maxWidth + 1, y + height + 1, 0x60000000); // Background outline
        context.fill(x, y, x + fillWidth, y + height, barColor);                  // Left to Right Fill
    }

    @Unique
    private void renderFreezeBar(DrawContext context, float currentValue, float maxValue) {
        float percent = Math.clamp(currentValue / maxValue, 0, 1);
        if (percent <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int screenHeight = client.getWindow().getScaledHeight();

        // Bar dimensions and positioning (Mid-Left)
        int x = 10;
        int maxHeight = 80;
        int width = 4;

        // Vertically centered on the left
        int yBottom = screenHeight / 2 + (maxHeight / 2);
        int yTopBound = yBottom - maxHeight;

        int fillHeight = (int) (maxHeight * percent);
        if (fillHeight <= 0) return;

        // Bottom-to-up rendering calculation
        int fillTopY = yBottom - fillHeight;

        // Gradient: Less blue (pale/cyan) to more and more freezing blue
        int red = (int) (180 * (1f - percent));
        int green = (int) (240 * (1f - percent) + 100 * percent); // Drops towards a deeper tint
        int blue = 255;
        int barColor = 0xFF000000 | (red << 16) | (green << 8) | blue;

        context.fill(x - 1, yTopBound - 1, x + width + 1, yBottom + 1, 0x60000000); // Background outline
        context.fill(x, fillTopY, x + width, yBottom, barColor);                    // Bottom to Top Fill
    }
}