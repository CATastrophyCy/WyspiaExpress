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
            renderDepressionBar(context, depressedTick, depressedTimerTick);
        }

        if (freezeTick > 0 && WyspiaExpress.SERVER_CONFIG.freeze()) {
            int freezeTimer = WyspiaExpress.SERVER_CONFIG.freezeTimer() * 20 * 2 ;
            renderFreezeBar(context, freezeTick, freezeTimer);
        }
    }

    @Unique
    private void renderDepressionBar(DrawContext context, float currentValue, float maxValue) {
        float percent = Math.clamp(currentValue / maxValue, 0, 1);

        if (percent <= 0) return;

        float visualPercent = 1.0f - percent;


        // Bar dimensions and positioning (below MoodRenderer which is around y=20)
        int x = 26;
        int y = 32;
        int maxWidth = 135;
        int height = 4;

        // Border and Background colors
        int borderColor = 0xFF1A002A; // Solid deep dark purple
        int bgColor = 0x990D0015;     // Semi-transparent dark purple track

        context.fill(x - 1, y - 1, x + maxWidth + 1, y + height + 1, borderColor);

        context.fill(x, y, x + maxWidth, y + height, bgColor);

        int fillWidth = (int) (maxWidth * visualPercent);
        if (fillWidth <= 0) return;


        int red = (int) (255 * (1f - percent) + 75 * percent);
        int green = (int) (255 * (1f - percent) + 0 * percent);
        int blue = (int) (255 * (1f - percent) + 130 * percent);
        int barColor = 0xFF000000 | (red << 16) | (green << 8) | blue;

        context.fill(x, y, x + fillWidth, y + height, barColor);                  // Left to Right Fill
    }

    @Unique
    private void renderFreezeBar(DrawContext context, float currentValue, float maxValue) {
        float percent = Math.clamp(currentValue / maxValue, 0, 1);
        if (percent <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int screenHeight = client.getWindow().getScaledHeight();
        float visualPercent = 1.0f - percent;

        // Bar dimensions and positioning (Mid-Left)
        int x = 10;
        int maxHeight = 135;
        int width = 4;

        // Vertically centered on the left
        int yBottom = screenHeight / 2 + (maxHeight / 2);
        int yTopBound = yBottom - maxHeight;

        // Border and Background colors
        int borderColor = 0xFF000B1A; // Solid deep navy blue
        int bgColor = 0x9900050D;     // Semi-transparent dark navy track

        context.fill(x - 1, yTopBound - 1, x + width + 1, yBottom + 1, borderColor);

        context.fill(x, yTopBound, x + width, yBottom, bgColor);

        int fillHeight = (int) (maxHeight * visualPercent);
        if (fillHeight <= 0) return;

        // Bottom-to-up rendering calculation
        int fillTopY = yBottom - fillHeight;

        int red = (int) (255 * (1f - percent) + 0 * percent);
        int green = (int) (180 * (1f - percent) + 200 * percent);
        int blue = (int) (50 * (1f - percent) + 255 * percent);
        int barColor = 0xFF000000 | (red << 16) | (green << 8) | blue;

        context.fill(x, fillTopY, x + width, yBottom, barColor);                    // Bottom to Top Fill
    }
}