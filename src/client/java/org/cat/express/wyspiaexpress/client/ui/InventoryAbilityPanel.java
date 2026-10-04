package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.BsXinQin.kinswathe.client.roles.judge.JudgePlayerWidget;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class InventoryAbilityPanel extends ClickableWidget {
    public static final class Group {
        public final String id;
        public final Text title;
        public List<ClickableWidget> widgets;
        public final boolean heads;
        public ClickableWidget rowBreakBefore;
        public Group(String id, Text title, List<ClickableWidget> widgets, boolean heads) {
            this.id = id; this.title = title; this.widgets = widgets; this.heads = heads;
        }
    }
    public record Placement(ClickableWidget widget, int page, int x, int y, int width, int height) {}
    private final LimitedInventoryScreen screen;
    private final List<Group> groups;
    private List<Placement> placements = List.of();
    private int selected, page, pages = 1;
    private ClickableWidget focusedChild;
    private boolean tabbed;
    private List<GroupHeading> headings = List.of();
    private record GroupHeading(Text title, int x, int y) {}
    private record Layout(List<Placement> placements, List<GroupHeading> headings, int pages, int usedHeight) {}

    public InventoryAbilityPanel(LimitedInventoryScreen screen, List<Group> groups, String selectedId) {
        super(Math.max(4, (screen.width - Math.min(560, screen.width - 12)) / 2), panelTop(screen.height),
                Math.clamp(screen.width - 12, 1, 560), Math.max(1, screen.height - panelTop(screen.height) - 6), Text.empty());
        this.screen = screen; this.groups = groups;
        for (int i = 0; i < groups.size(); i++) if (groups.get(i).id.equals(selectedId)) selected = i;
        // Dependency callbacks rebuild the screen when their editor opens.
        for (int i = 0; i < groups.size(); i++) if (hasInput(groups.get(i))) { selected = i; break; }
        reflow();
    }
    private static int panelTop(int screenHeight) {
        int hotbarTop = (screenHeight - 32) / 2;
        return hotbarTop + 41;
    }
    public String selectedId() { return groups.get(selected).id; }
    public Group group() { return groups.get(selected); }
    public List<Placement> placements() { return placements; }
    public int page() { return page; }
    public int pages() { return pages; }
    public boolean tabbed() { return tabbed; }
    public void refresh() { page = 0; reflow(); }
    public void select(String id) {
        for (int i = 0; i < groups.size(); i++) if (groups.get(i).id.equals(id)) {
            focus(null); selected = i; page = 0; reflow(); return;
        }
    }
    private static boolean hasInput(Group group) {
        return group.widgets.stream().anyMatch(TextFieldWidget.class::isInstance);
    }
    private Layout layout(List<Group> shown, int availableHeight, boolean showTitles, int layoutWidth, int originX) {
        var result = new ArrayList<Placement>();
        var titles = new ArrayList<GroupHeading>();
        int availableWidth = Math.max(1, layoutWidth - 12);
        int x = 0, y = 0, rowHeight = 0, currentPage = 0;
        for (Group group : shown) {
            if (group.widgets.isEmpty()) continue;
            if (x > 0) { x = 0; y += rowHeight + 4; rowHeight = 0; }
            if (showTitles) { titles.add(new GroupHeading(group.title, originX + layoutWidth / 2, getY() + 2 + y)); y += 12; }
            for (var widget : group.widgets) {
                boolean head = group.heads && !(widget instanceof TextFieldWidget) && widget.getWidth() == 16;
                boolean judge = widget instanceof JudgePlayerWidget;
                int headWidth = judge ? Math.max(36, MinecraftClient.getInstance().textRenderer.getWidth(judgePrice()) + 14) : 36;
                int cellWidth = Math.min(availableWidth, head ? headWidth : widget.getWidth() + 4);
                int cellHeight = Math.min(availableHeight, head ? (judge ? 44 : 32) : Math.max(24, widget.getHeight() + 4));
                if (x > 0 && (widget == group.rowBreakBefore || x + cellWidth > availableWidth)) { x = 0; y += rowHeight + 2; rowHeight = 0; }
                if (y > 0 && y + cellHeight > availableHeight) { currentPage++; x = 0; y = 0; rowHeight = 0; }
                int offset = head ? Math.clamp(cellHeight - 23, 0, 10) : 2;
                int widgetWidth = head ? 16 : Math.clamp(cellWidth - 4, 1, widget.getWidth());
                int widgetHeight = head ? 16 : Math.clamp(cellHeight - 4, 1, widget.getHeight());
                result.add(new Placement(widget, currentPage, originX + 6 + x + (head ? 7 : 2),
                        getY() + 2 + y + offset, widgetWidth, widgetHeight));
                x += cellWidth + 4; rowHeight = Math.max(rowHeight, cellHeight);
            }
        }
        // Center inputs independently of suggestion count; keep suggestion grid columns fixed.
        var rows = new LinkedHashMap<String, List<Integer>>();
        for (int i = 0; i < result.size(); i++) {
            Placement p = result.get(i);
            rows.computeIfAbsent(p.page + ":" + p.y, key -> new ArrayList<>()).add(i);
        }
        boolean editing = shown.stream().anyMatch(InventoryAbilityPanel::hasInput);
        for (var row : rows.values()) {
            Placement first = result.get(row.getFirst()), last = result.get(row.getLast());
            Placement input = row.stream().map(result::get).filter(p -> p.widget instanceof TextFieldWidget).findFirst().orElse(null);
            int offset;
            if (input != null) {
                int centered = originX + (layoutWidth - input.width) / 2 - input.x;
                // Leave room for Back without letting it shift the input as results change.
                offset = Math.clamp(centered, originX + 6 - first.x, originX + layoutWidth - 6 - last.x - last.width);
            } else if (editing) {
                int cellWidth = first.width + 8;
                int columns = Math.max(1, (availableWidth + 4) / cellWidth);
                int gridWidth = (columns - 1) * cellWidth + first.width;
                offset = originX + (layoutWidth - gridWidth) / 2 - first.x;
            } else {
                offset = originX + (layoutWidth - (last.x + last.width - first.x)) / 2 - first.x;
            }
            for (int index : row) {
                Placement p = result.get(index);
                result.set(index, new Placement(p.widget, p.page, p.x + offset, p.y, p.width, p.height));
            }
        }
        return new Layout(result, titles, currentPage + 1, y + rowHeight);
    }
    private void reflow() {
        // Prefer stacked abilities. Use columns only when stacking cannot fit.
        Layout combined = layout(groups, Integer.MAX_VALUE, groups.size() > 1, width, getX());
        if (combined.usedHeight > Math.max(1, height - 4) && groups.size() > 1 && width / groups.size() >= 100
                && groups.stream().flatMap(g -> g.widgets.stream()).allMatch(w -> w.getWidth() == 16 && w.getHeight() == 16)) {
            var columnWidgets = new ArrayList<Placement>();
            var columnTitles = new ArrayList<GroupHeading>();
            int columnWidth = width / groups.size(), usedHeight = 0;
            for (int i = 0; i < groups.size(); i++) {
                Layout column = layout(List.of(groups.get(i)), Integer.MAX_VALUE, true, columnWidth, getX() + i * columnWidth);
                columnWidgets.addAll(column.placements); columnTitles.addAll(column.headings);
                usedHeight = Math.max(usedHeight, column.usedHeight);
            }
            if (usedHeight < combined.usedHeight) combined = new Layout(columnWidgets, columnTitles, 1, usedHeight);
        }
        boolean editing = groups.stream().anyMatch(InventoryAbilityPanel::hasInput);
        tabbed = groups.size() > 1 && (editing || combined.usedHeight > Math.max(1, height - 4));
        Layout chosen = (tabbed || editing) ? layout(List.of(group()), Math.max(1, height - (tabbed ? 23 : 4)), false, width, getX())
                : combined.usedHeight <= Math.max(1, height - 4) ? combined
                : layout(groups, Math.max(1, height - 4), false, width, getX());
        var result = chosen.placements;
        headings = chosen.headings;
        placements = result; pages = chosen.pages; page = Math.min(page, pages - 1);
        positionPage();
    }
    private void positionPage() {
        for (Group group : groups) for (var widget : group.widgets) widget.visible = false;
        for (Placement p : placements) {
            p.widget.setX(p.x); p.widget.setY(p.y); p.widget.setWidth(p.width); p.widget.setHeight(p.height);
            p.widget.visible = p.page == page;
        }
        if (focusedChild != null && !focusedChild.visible) focus(null);
    }
    public void focusWidget(ClickableWidget widget) { focus(widget); }
    public void blurForClick(double mouseX, double mouseY) {
        if (focusedChild instanceof TextFieldWidget && !focusedChild.isMouseOver(mouseX, mouseY)) focus(null);
    }
    public void captureInitialFocus() {
        for (Placement p : placements) if (p.page == page && p.widget.isFocused()) { focus(p.widget); return; }
    }
    private void focus(ClickableWidget widget) {
        if (widget != null && screen.getFocused() != this) screen.setFocused(this);
        if (focusedChild != null) focusedChild.setFocused(false);
        focusedChild = widget;
        if (widget != null) widget.setFocused(true);
    }
    @Override public void setFocused(boolean focused) {
        super.setFocused(focused);
    }
    private Text judgePrice() {
        return Text.literal(ConfigWorldComponent.KEY.get(screen.player.getWorld()).JudgeAbilityPrice + "");
    }
    @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        var client = MinecraftClient.getInstance();
        int tabY = tabbed ? getY() + height - 17 : getY() + height;
        int tabWidth = Math.max(1, width / groups.size());
        for (int i = 0; tabbed && i < groups.size(); i++) {
            int x = getX() + i * tabWidth;
            if (i == selected) context.fill(x + 4, tabY + 15, x + tabWidth - 6, tabY + 16, 0xFFC39251);
            Text title = groups.get(i).title;
            String label = client.textRenderer.trimToWidth(title.getString(), Math.max(1, tabWidth - 8));
            context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(label), x + tabWidth / 2, tabY + 4,
                    i == selected ? 0xFFEAB3 : 0xCCCCCC);
        }
        // Only visible-page widgets render. Their hover names/tooltips must be allowed
        // outside the panel bounds, just like their original inventory rendering.
        context.draw();
        for (GroupHeading heading : headings) context.drawCenteredTextWithShadow(client.textRenderer, heading.title, heading.x, heading.y, 0xDDDDDD);
        for (Placement p : List.copyOf(placements)) if (p.page == page) {
            p.widget.render(context, mouseX, mouseY, delta);
            if (p.widget instanceof JudgePlayerWidget) {
                int price = ConfigWorldComponent.KEY.get(screen.player.getWorld()).JudgeAbilityPrice;
                int color = PlayerShopComponent.KEY.get(screen.player).balance >= price ? 0xFFBF49 : 0xFF7777;
                context.drawCenteredTextWithShadow(client.textRenderer, judgePrice(), p.x + p.width / 2, p.y + 24, color);
            }
        }
        context.draw();
        if (pages > 1) {
            Text count = Text.literal((page + 1) + "/" + pages);
            context.drawTextWithShadow(client.textRenderer, count, getX() + width - client.textRenderer.getWidth(count) - 2,
                    getY() - 9, 0xDDDDDD);
        }
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY) || button != 0) return false;
        if (tabbed && mouseY >= getY() + height - 17) {
            int index = Math.min(groups.size() - 1, (int) (mouseX - getX()) / Math.max(1, width / groups.size()));
            select(groups.get(index).id); playDownSound(MinecraftClient.getInstance().getSoundManager()); return true;
        }
        focus(null);
        for (Placement p : List.copyOf(placements)) if (p.page == page && p.widget.mouseClicked(mouseX, mouseY, button)) {
            if (screen.children().contains(this) && groups.stream().anyMatch(g -> g.widgets.contains(p.widget)) && p.widget.visible) focus(p.widget);
            return true;
        }
        return true;
    }
    @Override public boolean mouseReleased(double x, double y, int button) { return focusedChild != null && focusedChild.mouseReleased(x, y, button); }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) { return focusedChild != null && focusedChild.mouseDragged(x, y, button, dx, dy); }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (!isMouseOver(x, y) || pages <= 1) return false;
        page = Math.clamp(page + (vertical < 0 ? 1 : -1), 0, pages - 1); positionPage(); return true;
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (focusedChild != null && focusedChild.visible) {
            if (focusedChild.keyPressed(key, scan, modifiers)) return true;
            // Typing into an editor must not close inventory or activate hotbar shortcuts.
            if (focusedChild instanceof TextFieldWidget && key != GLFW.GLFW_KEY_ESCAPE) return true;
        }
        if (pages > 1 && (key == GLFW.GLFW_KEY_PAGE_UP || key == GLFW.GLFW_KEY_PAGE_DOWN)) {
            page = Math.clamp(page + (key == GLFW.GLFW_KEY_PAGE_DOWN ? 1 : -1), 0, pages - 1);
            positionPage(); return true;
        }
        return false;
    }
    @Override public boolean charTyped(char chr, int modifiers) { return focusedChild != null && focusedChild.visible && focusedChild.charTyped(chr, modifiers); }
    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) { builder.put(NarrationPart.TITLE, group().title); }
}
