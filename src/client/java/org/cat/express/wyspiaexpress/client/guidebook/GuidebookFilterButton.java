package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;

/** Solid selected state and distinct filter colors, without relying on a text glint. */
final class GuidebookFilterButton extends ButtonWidget {
    private final int accent;
    private boolean selected;

    GuidebookFilterButton(int x, int y, int width, int height, Text message, int accent, Runnable action) {
        super(x, y, width, height, GuidebookStyle.font(message), button -> action.run(), DEFAULT_NARRATION_SUPPLIER);
        this.accent = accent;
    }

    void selected(boolean selected) { this.selected = selected; }

    @Override public void playDownSound(SoundManager soundManager) {
        GuidebookSounds.playClick(soundManager);
    }

    @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int background = selected ? 0xFF000000 | accent : isHovered() || isFocused() ? 0xFF373A3B : 0xFF252A2E;
        context.fill(getX(), getY(), getX() + width, getY() + height, background);
        context.drawBorder(getX(), getY(), width, height, selected ? 0xFFF7EAD0 : 0xFF000000 | accent);
        var renderer = MinecraftClient.getInstance().textRenderer;
        Text message = GuidebookStyle.colored(getMessage(), selected ? 0x171A1D : accent);
        context.drawText(renderer, message, getX() + (width - renderer.getWidth(message)) / 2,
                getY() + (height - 9) / 2, 0xFFFFFFFF, false);
    }
}
