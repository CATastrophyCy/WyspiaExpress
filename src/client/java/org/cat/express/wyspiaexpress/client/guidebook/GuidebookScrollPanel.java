package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/** Independent clipping, scrolling and scrollbar hit testing shared by both guide panels. */
final class GuidebookScrollPanel {
    int x, y, width, height, contentHeight;
    double offset;
    private boolean dragging;
    private double grab;

    void bounds(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = Math.max(1, height);
        clamp();
    }

    void contentHeight(int height) { contentHeight = height; clamp(); }
    void clamp() { offset = MathHelper.clamp(offset, 0, Math.max(0, contentHeight - height)); }
    boolean contains(double mx, double my) { return mx >= x && mx < x + width && my >= y && my < y + height; }
    int contentWidth() { return width - 9; }
    int drawY(int localY) { return y + localY - (int) offset; }
    void begin(DrawContext context) { context.enableScissor(x, y, x + width - 7, y + height); }
    void end(DrawContext context) { context.disableScissor(); }

    boolean scroll(double mx, double my, double amount) {
        if (!contains(mx, my)) return false;
        offset -= amount * 24; clamp(); return true;
    }

    private int thumbHeight() { return Math.min(height, Math.max(14, height * height / Math.max(1, contentHeight))); }
    private int thumbY() {
        return y + (int) (offset * (height - thumbHeight()) / Math.max(1, contentHeight - height));
    }

    void scrollbar(DrawContext context) {
        if (contentHeight <= height) return;
        context.fill(x + width - 4, y, x + width - 3, y + height, 0xFF25353F);
        context.fill(x + width - 6, thumbY(), x + width - 1, thumbY() + thumbHeight(), 0xFF71909F);
    }

    boolean click(double mx, double my) {
        if (contentHeight <= height || !contains(mx, my) || mx < x + width - 7) return false;
        dragging = true;
        grab = my >= thumbY() && my < thumbY() + thumbHeight() ? my - thumbY() : thumbHeight() / 2.0;
        drag(mx, my); return true;
    }

    boolean drag(double mx, double my) {
        if (!dragging) return false;
        offset = (my - y - grab) * (contentHeight - height) / Math.max(1, height - thumbHeight());
        clamp(); return true;
    }

    void release() { dragging = false; }
    void reveal(int top, int bottom) {
        if (top < offset) offset = top;
        else if (bottom > offset + height) offset = bottom - height;
        clamp();
    }
}
