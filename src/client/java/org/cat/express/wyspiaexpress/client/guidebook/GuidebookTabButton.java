package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.SoundManager;

/** Horizontal section tab attached above the book frame. */
final class GuidebookTabButton extends ButtonWidget {
    GuidebookTabButton(int x, int y, int width, GuidebookTab tab, Runnable action) {
        super(x, y, width, 18, GuidebookStyle.font(tab.title()),
                button -> action.run(), DEFAULT_NARRATION_SUPPLIER);
    }

    @Override public void playDownSound(SoundManager soundManager) {
        GuidebookSounds.playClick(soundManager);
    }

    @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX() + width, getY() + height, isHovered() ? 0xFF514936 : 0xFF39352C);
        context.drawBorder(getX(), getY(), width, height, 0xFF806E50);
        context.fill(getX() + 1, getY() + height - 2, getX() + width - 1, getY() + height, 0xFF000000 | GuidebookStyle.GOLD);
        var renderer = MinecraftClient.getInstance().textRenderer;
        context.getMatrices().push();
        context.getMatrices().translate(getX() + width / 2f, getY() + height / 2f, 0);
        context.drawText(renderer, getMessage(), -renderer.getWidth(getMessage()) / 2, -4, GuidebookStyle.GOLD, false);
        context.getMatrices().pop();
    }
}
