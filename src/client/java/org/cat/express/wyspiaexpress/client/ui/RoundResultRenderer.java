package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.api.Role;

import org.cat.express.wyspiaexpress.client.util.PlayerProfileUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;
import org.agmas.harpymodloader.Harpymodloader;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Wyspia results use captured winners and roles while retaining Wathe's end timing and audio. */
public final class RoundResultRenderer {
    private static final int CELL_WIDTH = 88;
    private static final int ROW_HEIGHT = 56;
    private static final int CATEGORY_GAP = 20;
    private static final float ROLE_SCALE = 0.75f;

    private enum Category {
        KILLER(0xFF8080), KILLER_NEUTRAL(0xEAB185), TRUE_NEUTRAL(0xC7A3EE), CIVILIAN(0xACDA99);

        final int color;
        Category(int color) { this.color = color; }
        Text title() { return Text.translatable("gui.wyspiaexpress.results.category." + name().toLowerCase(Locale.ROOT)); }

        static Category of(Role role) {
            if (WyspiaExpressRoles.TRUE_NEUTRALS.contains(role)) return TRUE_NEUTRAL;
            if (WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) return KILLER_NEUTRAL;
            return role != null && role.canUseKiller() ? KILLER
                    : role != null && role.isInnocent() ? CIVILIAN : TRUE_NEUTRAL;
        }
    }

    private record Group(Text title, int color, List<RoundResultComponent.Player> players) {}

    private record CategoryRow(List<Group> groups, int playerColumns, int height) {}

    private record Layout(int width, int playerColumns, float titleScale, int headingHeight,
                          List<OrderedText> description, List<CategoryRow> rows, int height, float scale) {}

    private RoundResultRenderer() {}

    public static void render(DrawContext context, TextRenderer font, RoundResultComponent result) {
        context.fillGradient(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                0xA60D1118, 0xD00D1118);
        List<RoundResultComponent.Player> participants = result.players();
        List<RoundResultComponent.Player> winners = participants.stream()
                .filter(entry -> result.didWin(entry.player().getId())).toList();
        List<Group> categories = new ArrayList<>();
        for (Category category : Category.values()) {
            var players = participants.stream().filter(entry -> !result.didWin(entry.player().getId())
                    && Category.of(result.role(entry.player().getId())) == category).toList();
            if (!players.isEmpty()) categories.add(new Group(category.title(), category.color, players));
        }
        Layout layout = planLayout(font, result, winners, categories, participants.size(),
                context.getScaledWindowWidth(), context.getScaledWindowHeight());
        int availableHeight = Math.max(1, context.getScaledWindowHeight() - 92);

        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2f,
                28 + (availableHeight - layout.height() * layout.scale()) / 2f, 0);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1);
        context.getMatrices().push();
        context.getMatrices().scale(layout.titleScale(), layout.titleScale(), 1);
        context.drawCenteredTextWithShadow(font, result.title(), 0, 0, readableColor(result.color()));
        context.getMatrices().pop();
        int y = layout.headingHeight();
        for (var line : layout.description()) {
            context.drawTextWithShadow(font, line, -font.getWidth(line) / 2, y, 0xE4E4E4);
            y += font.fontHeight + 2;
        }
        y += 14;
        renderGroup(context, font, result, new Group(Text.translatable(winners.isEmpty()
                ? "gui.wyspiaexpress.results.no_winners" : "gui.wyspiaexpress.results.winners"), 0xA8DB92, winners),
                layout.playerColumns(), 0, y);
        y += groupHeight(winners, layout.playerColumns());
        for (CategoryRow row : layout.rows()) {
            int categoryWidth = (layout.width() - CATEGORY_GAP * (row.groups().size() - 1)) / row.groups().size();
            for (int i = 0; i < row.groups().size(); i++) {
                int x = (2 * i - row.groups().size() + 1) * (categoryWidth + CATEGORY_GAP) / 2;
                renderGroup(context, font, result, row.groups().get(i), row.playerColumns(), x, y);
            }
            y += row.height();
        }
        context.getMatrices().pop();
    }

    /** Choose the arrangement that gives heads, names and roles the largest shared scale. */
    private static Layout planLayout(TextRenderer font, RoundResultComponent result,
                                     List<RoundResultComponent.Player> winners, List<Group> categories,
                                     int participants, int screenWidth, int screenHeight) {
        Layout best = null;
        // Up to 24 players: test four to twelve columns instead of committing to a narrow canvas.
        for (int columns = 4; columns <= Math.clamp(participants, 4, 12); columns++) {
            int width = columns * CELL_WIDTH + CATEGORY_GAP;
            List<CategoryRow> rows = arrangeCategories(categories, columns, width);
            var description = font.wrapLines(result.description(), width);
            float titleScale = Math.min(2.2f, width / (float) Math.max(1, font.getWidth(result.title())));
            int headingHeight = MathHelper.ceil(font.fontHeight * titleScale) + 12;
            int height = headingHeight + description.size() * (font.fontHeight + 2) + 14
                    + groupHeight(winners, columns) + rows.stream().mapToInt(CategoryRow::height).sum();
            // Leave room for Wathe's lobby counter/autostart above and credits/hotbar below.
            float scale = Math.min(1.5f, Math.min(Math.max(1, screenWidth - 24) / (float) width,
                    Math.max(1, screenHeight - 92) / (float) height));
            if (best == null || scale > best.scale() + 0.001f
                    || Math.abs(scale - best.scale()) <= 0.001f && titleScale > best.titleScale()) {
                best = new Layout(width, columns, titleScale, headingHeight, description, rows, height, scale);
            }
        }
        return best;
    }

    /** Preserve category order; pair neighbors only when that uses less height than full-width rows. */
    private static List<CategoryRow> arrangeCategories(List<Group> groups, int columns, int width) {
        int halfColumns = Math.max(1, (width - CATEGORY_GAP) / 2 / CELL_WIDTH);
        int[] heights = new int[groups.size() + 2];
        boolean[] paired = new boolean[groups.size()];
        for (int i = groups.size() - 1; i >= 0; i--) {
            heights[i] = groupHeight(groups.get(i).players(), columns) + heights[i + 1];
            if (i + 1 < groups.size()) {
                int pairHeight = Math.max(groupHeight(groups.get(i).players(), halfColumns),
                        groupHeight(groups.get(i + 1).players(), halfColumns)) + heights[i + 2];
                if (pairHeight <= heights[i]) {
                    heights[i] = pairHeight;
                    paired[i] = true;
                }
            }
        }
        List<CategoryRow> rows = new ArrayList<>();
        for (int i = 0; i < groups.size();) {
            int count = paired[i] ? 2 : 1;
            rows.add(new CategoryRow(groups.subList(i, i + count), paired[i] ? halfColumns : columns,
                    heights[i] - heights[i + count]));
            i += count;
        }
        return rows;
    }

    private static int groupHeight(List<?> entries, int columns) {
        return entries.isEmpty() ? 22 : 22 + ((entries.size() + columns - 1) / columns) * ROW_HEIGHT;
    }

    private static void renderGroup(DrawContext context, TextRenderer font, RoundResultComponent result,
                                   Group group, int columns, int centerX, int y) {
        context.drawCenteredTextWithShadow(font, group.title(), centerX, y, group.color());
        int headY = y + 14;
        for (int i = 0; i < group.players().size(); i++) {
            var entry = group.players().get(i);
            int row = i / columns;
            int rowSize = Math.min(columns, group.players().size() - row * columns);
            int x = centerX + (i % columns) * CELL_WIDTH - rowSize * CELL_WIDTH / 2 + CELL_WIDTH / 2;
            int top = headY + row * ROW_HEIGHT;
            float tint = entry.wasDead() ? 0.5f : 1f;
            context.setShaderColor(tint, tint, tint, 1);
            PlayerSkinDrawer.draw(context, PlayerProfileUtil.skin(entry.player()).texture(), x - 10, top, 20);
            context.setShaderColor(1, 1, 1, 1);
            if (entry.wasDead()) context.drawTextWithShadow(font, "×", x + 4, top + 12, 0xFF7777);
            String name = font.trimToWidth(entry.player().getName(), CELL_WIDTH - 4);
            context.drawCenteredTextWithShadow(font, name, x, top + 23, entry.wasDead() ? 0x999999 : 0xE4E4E4);
            Role role = result.role(entry.player().getId());
            Text roleName = role == null ? Text.translatable("gui.wyspiaexpress.results.unknown_role") : Harpymodloader.getRoleName(role);
            var roleLines = font.wrapLines(roleName, (int) ((CELL_WIDTH - 6) / ROLE_SCALE));
            context.getMatrices().push();
            context.getMatrices().translate(x, top + 35, 0);
            context.getMatrices().scale(ROLE_SCALE, ROLE_SCALE, 1);
            for (int line = 0; line < Math.min(2, roleLines.size()); line++) {
                var text = roleLines.get(line);
                context.drawTextWithShadow(font, text, -font.getWidth(text) / 2, line * (font.fontHeight + 1),
                        role == null ? 0xB0B0B0 : readableColor(role.color()));
            }
            context.getMatrices().pop();
        }
    }

    private static int readableColor(int color) {
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        if (Math.max(red, Math.max(green, blue)) < 150) {
            red = (red + 255) / 2;
            green = (green + 255) / 2;
            blue = (blue + 255) / 2;
        }
        return red << 16 | green << 8 | blue;
    }
}
