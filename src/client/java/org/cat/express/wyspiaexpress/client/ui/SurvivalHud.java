package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.components.PlayerDepressedComponent;
import org.cat.express.wyspiaexpress.components.PlayerFreezeComponent;

/** Minimal remaining-safety meters for cold and insanity. */
public final class SurvivalHud {
    private static final int BORDER = 0x9952647A;
    private static final int TRACK = 0x6630394A;
    private static final int WHITE = 0xFFF4F7FC;
    private static final int WARM = 0xFFFFBA6A;
    private static final int COLD = 0xFF68C9F2;
    private static final int STABLE = 0xFF9FE0C0;
    private static final int INSANE = 0xFFB75DE5;
    private static final String[] SNOWFLAKE = {
            "....#....", ".#..#..#.", "..#.#.#..", "...###...", "#########",
            "...###...", "..#.#.#..", ".#..#..#.", "....#...."
    };
    private static final String[] SAD_FACE = {
            ".#######.", "#.......#", "#.#...#.#", "#.......#", "#...#...#",
            "#..#.#..#", "#.......#", ".#######.", "........."
    };

    private SurvivalHud() {}

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null || client.options.hudHidden || !WatheClient.isPlayerAliveAndInSurvival()
                || !GameWorldComponent.KEY.get(player.getWorld()).isRunning()) return;

        int freezeTicks = PlayerFreezeComponent.KEY.get(player).getFreezeTick();
        if (WyspiaExpress.SERVER_CONFIG.freeze() && freezeTicks > 0) {
            renderFreezing(context, client, meter(freezeTicks, WyspiaExpress.SERVER_CONFIG.freezeTimer()));
        }
        int depressionTicks = PlayerDepressedComponent.KEY.get(player).getDepressionTick();
        if (WyspiaExpress.SERVER_CONFIG.depressionKilling() && depressionTicks > 0) {
            renderDepression(context, client, meter(depressionTicks, WyspiaExpress.SERVER_CONFIG.depressedTimer()));
        }
    }

    private static void renderFreezing(DrawContext context, MinecraftClient client, Meter meter) {
        // Preserve the old 4 x 135 meter, including its original top and bottom edges.
        int x = 10;
        int height = 135;
        int bottom = context.getScaledWindowHeight() / 2 + height / 2;
        int top = bottom - height;
        int fillTop = bottom - MathHelper.ceil(height * meter.remaining());
        context.fill(x - 1, top - 1, x + 5, bottom + 1, BORDER);
        context.fill(x, top, x + 4, bottom, TRACK);
        for (int y = fillTop; y < bottom; y++) {
            // Warm at the top, cold at the bottom; only the remaining lower portion is filled.
            int color = blend(COLD, WARM, 1 - (y - top) / (float) (height - 1));
            context.fill(x, y, x + 4, y + 1, color);
        }
        int temperatureColor = blend(COLD, WARM, meter.remaining());
        // A small bulb turns the familiar thin vertical meter into a thermometer.
        context.fill(x - 2, bottom + 1, x + 6, bottom + 7, BORDER);
        context.fill(x - 1, bottom + 2, x + 5, bottom + 6, temperatureColor);

        drawIcon(context, x - 2, top - 13, SNOWFLAKE, COLD);
        Text amount = amount(meter);
        int numberX = Math.max(2, x + 2 - client.textRenderer.getWidth(amount) / 2);
        context.drawTextWithShadow(client.textRenderer, amount, numberX, bottom + 10, WHITE);
    }

    private static void renderDepression(DrawContext context, MinecraftClient client, Meter meter) {
        int x = 24;
        // Sit just below the mood bar, following it when the task list grows.
        int y = 27 + MathHelper.ceil(Math.max(0, MoodRenderer.moodOffset) * 10);
        int width = 137;
        int color = blend(INSANE, STABLE, meter.remaining());
        drawIcon(context, 12, y - 2, SAD_FACE, color);
        context.fill(x - 1, y - 1, x + width + 1, y + 5, BORDER);
        context.fill(x, y, x + width, y + 4, TRACK);
        int fillWidth = MathHelper.ceil(width * meter.remaining());
        if (fillWidth > 0) {
            context.fill(x, y, x + fillWidth, y + 4, color);
            context.fill(x, y, x + fillWidth, y + 1, 0x44FFFFFF);
        }
        int numberY = y + 2 - client.textRenderer.fontHeight / 2;
        context.drawTextWithShadow(client.textRenderer, amount(meter), x + width + 6, numberY, WHITE);
    }

    private static Meter meter(int dangerTicks, int durationSeconds) {
        // Danger grows by 2 per server tick. Express remaining safety, not percent afflicted.
        double limit = Math.max(1.0, durationSeconds * 40.0);
        double safety = Math.clamp(limit - dangerTicks, 0, limit);
        return new Meter((float) (safety / limit), (int) Math.ceil(safety * 100 / limit));
    }

    private static Text amount(Meter meter) {
        return Text.translatable("hud.wyspiaexpress.survival.remaining", meter.percent())
                .styled(style -> style.withColor(WHITE));
    }

    private static int blend(int from, int to, float progress) {
        progress = MathHelper.clamp(progress, 0, 1);
        int red = Math.round(MathHelper.lerp(progress, (from >> 16) & 255, (to >> 16) & 255));
        int green = Math.round(MathHelper.lerp(progress, (from >> 8) & 255, (to >> 8) & 255));
        int blue = Math.round(MathHelper.lerp(progress, from & 255, to & 255));
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static void drawIcon(DrawContext context, int x, int y, String[] icon, int color) {
        for (int row = 0; row < icon.length; row++) {
            for (int column = 0; column < icon[row].length(); column++) {
                if (icon[row].charAt(column) == '#') context.fill(x + column, y + row, x + column + 1, y + row + 1, color);
            }
        }
    }

    private record Meter(float remaining, int percent) {}
}
