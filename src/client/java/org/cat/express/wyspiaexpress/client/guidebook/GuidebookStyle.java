package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

final class GuidebookStyle {
    static final Identifier FONT = Identifier.ofVanilla("default");
    static final int TEXT = 0xEEEAE1, MUTED = 0xACAEA9, GOLD = 0xDBC18B;

    private GuidebookStyle() {}

    static Text font(Text text) {
        var result = Text.empty();
        text.visit((style, value) -> {
            result.append(Text.literal(value).setStyle(fontStyle(style)));
            return java.util.Optional.empty();
        }, net.minecraft.text.Style.EMPTY);
        return result;
    }

    static Text colored(Text text, int color) {
        var result = Text.empty();
        text.visit((style, value) -> {
            result.append(Text.literal(value).setStyle(fontStyle(style).withColor(color)));
            return java.util.Optional.empty();
        }, net.minecraft.text.Style.EMPTY);
        return result;
    }

    private static net.minecraft.text.Style fontStyle(net.minecraft.text.Style style) {
        return style.withFont(FONT);
    }

    /** Keep the role's hue, lifting dark colors so names contrast with the page. */
    static int readable(int rgb) {
        int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
        double light = .2126 * r + .7152 * g + .0722 * b;
        double lift = light < 150 ? (150 - light) / (255 - light) : 0;
        return (int) (r + (255 - r) * lift) << 16 | (int) (g + (255 - g) * lift) << 8 | (int) (b + (255 - b) * lift);
    }

    static int category(GuidebookEntry.Category category) {
        return GOLD;
    }
}
