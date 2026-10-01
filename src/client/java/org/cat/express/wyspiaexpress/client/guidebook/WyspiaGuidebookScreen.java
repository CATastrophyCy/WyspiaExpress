package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static org.cat.express.wyspiaexpress.client.guidebook.GuidebookStyle.*;

public final class WyspiaGuidebookScreen extends Screen {
    private final GuidebookScrollPanel listPanel = new GuidebookScrollPanel();
    private final GuidebookScrollPanel pagePanel = new GuidebookScrollPanel();
    private final List<ListRow> rows = new ArrayList<>();
    private final List<PagePart> parts = new ArrayList<>();
    private final EnumSet<GuidebookEntry.Category> collapsed = EnumSet.noneOf(GuidebookEntry.Category.class);
    private List<GuidebookEntry> catalogue = List.of(), filtered = List.of();
    private GuidebookEntry selected;
    private GuidebookCatalog.Availability availability = GuidebookCatalog.Availability.ENABLED;
    private boolean mine, copycat, relatedView, defaultsApplied, transparentBackground, selectionRestored;
    private GuidebookTab activeTab = GuidebookTab.ROLES;
    private GuidebookCatalog.Availability savedAvailability;
    private String query = "", savedQuery = "";
    private TextFieldWidget search;
    private GuidebookFilterButton allButton, enabledButton, disabledButton, mineButton, copycatButton, backgroundButton;
    private int generation = -1, left, top, right, bottom, detailX, filterX, filterWidth, entryInset;
    private String language = "", pageSignature = "", listSignature = "";
    private List<Text> hoveredTooltip;

    public WyspiaGuidebookScreen() {
        super(label("title"));
        var preferences = GuidebookPreferences.get();
        transparentBackground = preferences.transparent;
        collapsed.addAll(preferences.collapsed);
        if (preferences.navigationSaved) {
            defaultsApplied = true; availability = preferences.availability;
            mine = preferences.mine; copycat = preferences.copycat; query = preferences.query;
            savedAvailability = preferences.savedAvailability; savedQuery = preferences.savedQuery;
        }
    }
    private static Text label(String key, Object... arguments) {
        return font(Text.translatable("gui.wyspiaexpress.guidebook." + key, arguments));
    }

    @Override protected void init() {
        int panelWidth = Math.min(670, Math.min(width - 8, Math.max(326, Math.round(width * .92f))));
        int panelHeight = Math.min(400, Math.min(height - 26, Math.max(220, Math.round(height * .92f))));
        left = (width - panelWidth) / 2; top = Math.max(22, (height - panelHeight) / 2);
        right = left + panelWidth; bottom = top + panelHeight;
        entryInset = panelWidth < 400 ? 8 : 12;
        filterX = left + 6; filterWidth = panelWidth < 400 ? 26 : 30;
        int listX = filterX + filterWidth + 6;
        int listWidth = Math.min(146, Math.max(84, panelWidth / 4));
        detailX = listX + listWidth + 8;
        search = new TextFieldWidget(textRenderer, listX, top + 24, listWidth - 4, 17, label("search"));
        search.setMaxLength(128); search.setPlaceholder(label("search"));
        search.setRenderTextProvider((value, first) -> font(Text.literal(value)).asOrderedText());
        search.setEditableColor(TEXT);
        search.setText(query);
        search.setChangedListener(value -> { query = value; relatedView = false; listPanel.offset = 0; refreshList(); });
        addDrawableChild(search);
        listPanel.bounds(listX, top + 45, listWidth, bottom - top - 61);
        pagePanel.bounds(detailX + 10, top + 25, Math.max(50, right - detailX - 20), bottom - top - 34);
        int step = Math.max(19, Math.min(25, (bottom - top - 28) / 5));
        enabledButton = filterButton(top + 25, "enabled", 0xA6DCA0, () -> setAvailability(GuidebookCatalog.Availability.ENABLED));
        mineButton = filterButton(top + 25 + step, "mine", 0x9DBDED, () -> toggleQuickFilter(false));
        copycatButton = filterButton(top + 25 + step * 2, "copycat", 0xC5A6ED, () -> toggleQuickFilter(true));
        disabledButton = filterButton(top + 25 + step * 3, "disabled", 0xCF9388, () -> setAvailability(GuidebookCatalog.Availability.DISABLED));
        allButton = filterButton(top + 25 + step * 4, "all", 0xB9BAB3, () -> setAvailability(GuidebookCatalog.Availability.ALL));
        addDrawableChild(new GuidebookTabButton(left + 3, top - 18, 57, GuidebookTab.ROLES,
                () -> activeTab = GuidebookTab.ROLES));
        backgroundButton = addDrawableChild(new GuidebookFilterButton(right - 54, top + 5, 27, 15,
                label("background.short"), GOLD, () -> { transparentBackground = !transparentBackground; updateFilterButtons(); }));
        backgroundButton.setTooltip(Tooltip.of(label("background")));
        addDrawableChild(new GuidebookFilterButton(right - 22, top + 5, 16, 15, Text.literal("×"), GOLD, this::close));
        if (!defaultsApplied) {
            defaultsApplied = true;
            if (GuidebookCatalog.isCopycat()) {
                savedAvailability = availability; savedQuery = query;
                copycat = true; availability = GuidebookCatalog.Availability.ALL;
            }
        }
        if (copycat && !GuidebookCatalog.isCopycat()) leaveQuickFilter();
        rows.clear();
        reloadCatalogue();
        if (!selectionRestored) {
            selectionRestored = true;
            String lastSelected = GuidebookPreferences.get().selected;
            filtered.stream().filter(entry -> entry.key().equals(lastSelected)).findFirst().ifPresent(this::select);
        }
        pageSignature = ""; refreshPage();
    }

    @Override public void removed() {
        var preferences = GuidebookPreferences.get();
        preferences.transparent = transparentBackground; preferences.navigationSaved = defaultsApplied;
        preferences.collapsed.clear(); preferences.collapsed.addAll(collapsed);
        preferences.availability = availability;
        preferences.mine = mine; preferences.copycat = copycat; preferences.query = query;
        preferences.savedAvailability = savedAvailability == null ? GuidebookCatalog.Availability.ENABLED : savedAvailability;
        preferences.savedQuery = savedQuery;
        preferences.selected = selected == null ? "" : selected.key();
        preferences.save();
        super.removed();
    }

    private GuidebookFilterButton filterButton(int y, String key, int color, Runnable action) {
        var button = new GuidebookFilterButton(filterX, y, filterWidth, 19, label(key + ".short"), color, action);
        button.setTooltip(Tooltip.of(label(key))); addDrawableChild(button); return button;
    }

    private void setAvailability(GuidebookCatalog.Availability choice) { leaveQuickFilter(); relatedView = false; availability = choice; refreshList(); }
    private void leaveQuickFilter() {
        if (!mine && !copycat) return;
        mine = false; copycat = false; availability = savedAvailability;
        search.setText(savedQuery);
    }
    private void toggleQuickFilter(boolean choices) {
        relatedView = false;
        if (choices ? copycat : mine) leaveQuickFilter();
        else {
            leaveQuickFilter();
            savedAvailability = availability; savedQuery = query;
            mine = !choices; copycat = choices; availability = GuidebookCatalog.Availability.ALL;
            search.setText(""); listPanel.offset = 0;
        }
        refreshList();
    }

    private void reloadCatalogue() {
        catalogue = GuidebookCatalog.entries(); generation = GuidebookDefinitions.INSTANCE.generation();
        language = client.options.language; refreshList();
    }

    private void refreshList() {
        List<GuidebookEntry> result = GuidebookCatalog.filter(catalogue, query, availability, mine, copycat);
        String signature = result.stream().map(entry -> entry.name().toString()).collect(java.util.stream.Collectors.joining("\n"))
                + ":" + collapsed + ":" + (mine || copycat || !query.isBlank());
        if (!filtered.equals(result) || !signature.equals(listSignature) || rows.isEmpty()) {
            listSignature = signature; filtered = result; rows.clear();
            int y = 0; GuidebookEntry.Category category = null;
            for (GuidebookEntry entry : filtered) {
                if (entry.category() != category) {
                    category = entry.category(); if (y > 0) y += 5;
                    var heading = textRenderer.wrapLines(font(category.title()), listPanel.contentWidth() - 14);
                    int h = heading.size() * 10 + 6;
                    rows.add(new ListRow(null, category, heading, y, h)); y += h;
                }
                if (isCollapsed(category)) continue;
                var name = colored(entry.name(), readable(entry.color()));
                var lines = textRenderer.wrapLines(name, listPanel.contentWidth() - entryInset - 12);
                int h = Math.min(2, lines.size()) * 10 + 8;
                rows.add(new ListRow(entry, category, lines, y, h)); y += h;
            }
            listPanel.contentHeight(y);
        }
        if (!relatedView && (selected == null || filtered.stream().noneMatch(entry -> entry.key().equals(selected.key())))) {
            select(filtered.isEmpty() ? null : filtered.getFirst());
        }
        updateFilterButtons();
    }

    private boolean isCollapsed(GuidebookEntry.Category category) { return !mine && !copycat && query.isBlank() && collapsed.contains(category); }
    private void toggleCategory(GuidebookEntry.Category category) {
        if (!collapsed.remove(category)) collapsed.add(category);
        refreshList();
    }
    private void updateFilterButtons() {
        if (allButton == null) return;
        allButton.selected(!mine && !copycat && availability == GuidebookCatalog.Availability.ALL);
        enabledButton.selected(!mine && !copycat && availability == GuidebookCatalog.Availability.ENABLED);
        disabledButton.selected(!mine && !copycat && availability == GuidebookCatalog.Availability.DISABLED);
        mineButton.selected(mine);
        copycatButton.visible = GuidebookCatalog.isCopycat(); copycatButton.selected(copycat);
        backgroundButton.selected(transparentBackground);
    }

    private void select(GuidebookEntry entry) {
        if (Objects.equals(selected, entry)) return;
        relatedView = false; selected = entry; pagePanel.offset = 0; pageSignature = ""; refreshPage();
    }

    @Override public void tick() {
        super.tick();
        if (client.world == null || client.player == null) { close(); return; }
        if (copycat && !GuidebookCatalog.isCopycat()) leaveQuickFilter();
        if (generation != GuidebookDefinitions.INSTANCE.generation() || !language.equals(client.options.language)) reloadCatalogue();
        if (selected != null && GuidebookCatalog.permanentlyExcluded(selected)) { selected = null; relatedView = false; }
        refreshList(); refreshPage();
    }

    private void refreshPage() {
        if (selected == null) { parts.clear(); pagePanel.contentHeight(0); pageSignature = ""; return; }
        Text subtitle = selected.title(), summary = selected.definition().field("summary", null), lore = selected.lore();
        List<Text> abilities = new ArrayList<>(selected.definition().abilities());
        var basic = selected.role() != null ? WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(selected.role()) : null;
        if (basic != null && basic.seePoison()) abilities.add(label("poison"));
        List<GuidebookItems.Offer> starting = GuidebookItems.starting(selected), shop = GuidebookItems.shop(selected);
        var status = GuidebookCatalog.status(selected);
        String signature = selected.key() + ":" + width + ":" + generation + ":" + language + ":" + status
                + ":" + selected.name() + ":" + subtitle + ":" + summary + ":" + lore + ":" + abilities + ":" + starting + ":" + shop;
        if (signature.equals(pageSignature)) return;
        pageSignature = signature; parts.clear();
        int w = pagePanel.contentWidth(), accent = readable(selected.color());
        int artSize = w >= 180 ? 40 : 32;
        int requirementWidth = status.minimum() == null ? 0 : textRenderer.getWidth(label("requirement.badge", status.minimum(), status.maximum()));
        int sideWidth = Math.max(artSize, requirementWidth), bodyWidth = Math.max(42, w - sideWidth - 9);
        Text name = colored(selected.name(), accent).copy().formatted(Formatting.BOLD);
        boolean longName = textRenderer.wrapLines(font(name), (int) (bodyWidth / 1.2f)).size() > 3;
        int y = paragraph(name, 0, 0, longName ? w : bodyWidth, TEXT, 1.2f) + 3;
        int artY = longName ? y : 0;
        parts.add(new ArtPart(w - sideWidth + (sideWidth - artSize) / 2, artY, artSize, GuidebookImages.get(selected)));
        int sideEnd = artY + artSize;
        if (selected.role() != null && status.minimum() != null) {
            parts.add(new RequirementPart(w - sideWidth, artY + artSize + 5, sideWidth, status)); sideEnd += 17;
        }
        if (subtitle != null && !subtitle.getString().isBlank()) y = paragraph(font(subtitle), 0, y, bodyWidth, MUTED, .9f) + 4;
        if (summary != null) y = paragraph(summary, 0, y, y < sideEnd ? bodyWidth : w, TEXT, 1) + 5;
        y = Math.max(y, sideEnd) + 5;
        if (!status.ready()) y = paragraph(label("loading"), 0, y, w, MUTED, 1) + 4;
        else if (!status.enabled()) y = paragraph(label("disabled"), 0, y, w, MUTED, 1) + 4;
        else if (status.blocked()) y = paragraph(label("blocked"), 0, y, w, MUTED, 1) + 4;
        if (selected.role() != null || !abilities.isEmpty()) {
            y = heading("ability", y, w, accent);
            if (abilities.isEmpty()) y = paragraph(label("ability.unavailable"), 0, y, w, MUTED, 1) + 5;
            else for (Text ability : abilities) y = paragraph(ability, 0, y, w, TEXT, 1) + 5;
        }
        if (selected.role() != null || !starting.isEmpty()) {
            y = heading("starting", y, w, accent);
            if (starting.isEmpty()) y = paragraph(label(GuidebookItems.startingKnown(selected) ? "starting.none" : "starting.unavailable"), 0, y, w, MUTED, 1) + 5;
            else y = itemGrid(starting, y, w);
        }
        if (selected.role() != null) {
            y = heading("shop", y, w, accent);
            if (shop.isEmpty()) y = paragraph(label(GuidebookItems.shopKnown(selected) ? "shop.none" : "shop.unavailable"), 0, y, w, MUTED, 1) + 5;
            else y = itemGrid(shop, y, w);
        }
        if (lore != null && !lore.getString().isBlank()) {
            y = heading("lore", y, w, accent); y = paragraph(lore, 0, y, w, TEXT, 1) + 5;
        }
        if (selected.role() == WyspiaExpressRoles.LICH || selected.role() == WyspiaExpressRoles.CULT_LEADER) {
            var child = selected.role() == WyspiaExpressRoles.LICH ? WyspiaExpressRoles.LICH_GHOUL : WyspiaExpressRoles.CULTIST;
            parts.add(new LinkPart(y, GuidebookEntry.role(child))); y += 20;
        }
        pagePanel.contentHeight(y + 4);
    }

    private int heading(String key, int y, int width, int color) {
        parts.add(new RulePart(y + 2, color));
        return paragraph(colored(label(key), color).copy().formatted(Formatting.BOLD), 0, y + 7, width, color, 1) + 4;
    }
    private int paragraph(Text text, int x, int y, int width, int color, float scale) {
        List<OrderedText> lines = new ArrayList<>();
        var paragraphs = new ArrayList<net.minecraft.text.MutableText>();
        font(text).visit((style, value) -> {
            String[] chunks = value.split("\n", -1);
            for (int i = 0; i < chunks.length; i++) {
                if (paragraphs.isEmpty()) paragraphs.add(Text.empty());
                paragraphs.getLast().append(Text.literal(chunks[i]).setStyle(style));
                if (i + 1 < chunks.length) paragraphs.add(Text.empty());
            }
            return java.util.Optional.empty();
        }, net.minecraft.text.Style.EMPTY);
        for (Text line : paragraphs) {
            if (line.getString().isEmpty()) lines.add(OrderedText.EMPTY);
            else lines.addAll(textRenderer.wrapLines(line, Math.max(20, (int) (width / scale))));
        }
        parts.add(new TextPart(x, y, lines, color, scale));
        return y + lines.size() * Math.round(11 * scale);
    }

    private int itemGrid(List<GuidebookItems.Offer> offers, int y, int width) {
        int columns = Math.min(3, Math.max(1, width / 105)), gap = 5, cellWidth = (width - gap * (columns - 1)) / columns;
        for (int first = 0; first < offers.size(); first += columns) {
            int height = 29;
            for (int i = first; i < Math.min(first + columns, offers.size()); i++) {
                int lines = Math.min(2, textRenderer.wrapLines(font(offers.get(i).stack().getName()), cellWidth - 26).size());
                height = Math.max(height, lines * 10 + 15);
            }
            for (int col = 0; col < columns && first + col < offers.size(); col++) {
                parts.add(new ItemPart(col * (cellWidth + gap), y, cellWidth, height, offers.get(first + col)));
            }
            y += height + 4;
        }
        return y + 2;
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hoveredTooltip = null;
        context.fill(0, 0, width, height, transparentBackground ? 0x20000000 : 0x68000000);
        context.fill(left + 3, top + 3, right + 3, bottom + 3, transparentBackground ? 0x40000000 : 0xA0000000);
        context.fill(left, top, right, bottom, transparentBackground ? 0xB81B2025 : 0xFF1B2025);
        context.fill(left + 3, top + 22, detailX, bottom - 3, transparentBackground ? 0xA0151A1E : 0xFF151A1E);
        context.drawBorder(left, top, right - left, bottom - top, 0xFF806E50);
        context.drawBorder(left + 2, top + 2, right - left - 4, bottom - top - 4, 0xFF38382F);
        for (int cornerX : new int[]{left + 4, right - 12}) for (int cornerY : new int[]{top + 4, bottom - 5})
            context.fill(cornerX, cornerY, cornerX + 8, cornerY + 1, 0xFFB79C67);
        context.fill(detailX, top + 23, detailX + 1, bottom - 6, 0xFF806E50);
        bookMark(context, left + 8, top + 7);
        context.drawText(textRenderer, colored(title, GOLD), left + 23, top + 8, GOLD, false);
        renderList(context, mouseX, mouseY); renderPage(context, mouseX, mouseY);
        context.drawText(textRenderer, label("entries", filtered.size()), listPanel.x, bottom - 12, MUTED, false);
        super.render(context, mouseX, mouseY, delta);
        if (hoveredTooltip != null) context.drawTooltip(textRenderer, hoveredTooltip.stream().map(GuidebookStyle::font).toList(), mouseX, mouseY);
    }

    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { }

    private void renderList(DrawContext context, int mouseX, int mouseY) {
        listPanel.begin(context);
        if (filtered.isEmpty()) {
            var lines = textRenderer.wrapLines(label(mine ? "mine.empty" : copycat ? "copycat.empty" : "empty"), listPanel.contentWidth());
            int y = listPanel.y + 5;
            for (var line : lines) { context.drawText(textRenderer, line, listPanel.x + 3, y, MUTED, false); y += 11; }
        }
        var own = GuidebookCatalog.ownEntries();
        for (var row : rows) {
            int y = listPanel.drawY(row.y);
            if (y + row.height < listPanel.y || y > listPanel.y + listPanel.height) continue;
            boolean hover = listPanel.contains(mouseX, mouseY) && mouseX < listPanel.x + listPanel.contentWidth()
                    && mouseY >= y && mouseY < y + row.height;
            if (row.entry == null) {
                int color = category(row.category);
                int boxX = listPanel.x + 1, boxY = y + 4;
                context.drawBorder(boxX, boxY, 8, 8, 0xFF000000 | color);
                context.fill(boxX + 2, boxY + 3, boxX + 6, boxY + 4, 0xFF000000 | color);
                if (isCollapsed(row.category)) context.fill(boxX + 3, boxY + 2, boxX + 4, boxY + 6, 0xFF000000 | color);
                int offset = 3;
                for (var line : row.lines) { context.drawText(textRenderer, line, listPanel.x + 12, y + offset, color, false); offset += 10; }
                if (hover && !mine && !copycat && query.isBlank()) hoveredTooltip = List.of(label(isCollapsed(row.category) ? "expand" : "collapse", row.category.title()));
            } else {
                int accent = readable(row.entry.color());
                boolean chosen = selected != null && row.entry.key().equals(selected.key());
                boolean yours = own.contains(row.entry.key());
                if (chosen || hover) context.fill(listPanel.x + entryInset, y, listPanel.x + listPanel.contentWidth(), y + row.height - 1, chosen ? 0xFF333638 : 0xFF262B30);
                if (chosen) context.fill(listPanel.x + entryInset, y + 1, listPanel.x + entryInset + 2, y + row.height - 2, 0xFF000000 | accent);
                if (yours) context.drawBorder(listPanel.x + entryInset + 1, y + 1, listPanel.contentWidth() - entryInset - 2, row.height - 2, 0xFF000000 | accent);
                int offset = 4;
                for (var line : row.lines.stream().limit(2).toList()) { context.drawText(textRenderer, line, listPanel.x + entryInset + 6, y + offset, accent, false); offset += 10; }
                if (hover) hoveredTooltip = yours ? List.of(colored(row.entry.name(), accent), label("yours"), label(statusKey(GuidebookCatalog.status(row.entry))))
                        : List.of(colored(row.entry.name(), accent), label(statusKey(GuidebookCatalog.status(row.entry))));
            }
        }
        listPanel.end(context); listPanel.scrollbar(context);
    }

    private static String statusKey(GuidebookCatalog.Status status) {
        return !status.ready() ? "loading" : !status.enabled() ? "disabled" : status.blocked() ? "blocked"
                : !status.meetsRequirement() ? "requirement.failed" : "enabled";
    }

    private void renderPage(DrawContext context, int mouseX, int mouseY) {
        pagePanel.begin(context);
        if (selected == null) context.drawText(textRenderer, label("select"), pagePanel.x + 3, pagePanel.y + 5, MUTED, false);
        for (PagePart part : parts) {
            int y = pagePanel.drawY(part.y());
            if (part instanceof TextPart text) {
                context.getMatrices().push(); context.getMatrices().translate(pagePanel.x + text.x, y, 0);
                context.getMatrices().scale(text.scale, text.scale, 1);
                int offset = 0;
                for (var line : text.lines) { context.drawText(textRenderer, line, 0, offset, text.color, false); offset += 11; }
                context.getMatrices().pop();
            } else if (part instanceof ArtPart art) {
                float scale = Math.min((float) art.size / art.art.width(), (float) art.size / art.art.height());
                int iw = Math.max(1, Math.round(art.art.width() * scale)), ih = Math.max(1, Math.round(art.art.height() * scale));
                context.drawTexture(art.art.texture(), pagePanel.x + art.x + (art.size - iw) / 2, y + (art.size - ih) / 2,
                        iw, ih, 0, 0, art.art.width(), art.art.height(), art.art.width(), art.art.height());
            } else if (part instanceof RequirementPart requirement) {
                var status = requirement.status;
                int x = pagePanel.x + requirement.x;
                int color = status.meetsRequirement() ? 0xA6DCA0 : 0xE8A77D;
                Text min = font(Text.literal(status.minimum() + " ≤ "));
                int iconX = x + textRenderer.getWidth(min);
                context.drawText(textRenderer, min, x, y + 2, color, false);
                playerIcon(context, iconX, y, color);
                context.drawText(textRenderer, font(Text.literal(" ≤ " + status.maximum())), iconX + 8, y + 2, color, false);
                if (pagePanel.contains(mouseX, mouseY) && mouseX >= x && mouseX < x + requirement.width && mouseY >= y && mouseY < y + 12)
                    hoveredTooltip = List.of(label("requirement", status.minimum(), status.maximum()));
            } else if (part instanceof ItemPart item) {
                int x = pagePanel.x + item.x;
                boolean hover = pagePanel.contains(mouseX, mouseY) && mouseX >= x && mouseX < x + item.width && mouseY >= y && mouseY < y + item.height;
                context.fill(x, y, x + item.width, y + item.height, hover ? 0xFF343A3D : transparentBackground ? 0x90242A2E : 0xFF242A2E);
                context.drawItem(item.offer.stack(), x + 3, y + 4);
                int offset = 3;
                for (var line : textRenderer.wrapLines(font(item.offer.stack().getName()), item.width - 26).stream().limit(2).toList()) {
                    context.drawText(textRenderer, line, x + 24, y + offset, TEXT, false); offset += 10;
                }
                Text extra = item.offer.price() == null ? label("quantity", item.offer.amount()) : label("price", item.offer.price());
                if (item.offer.price() != null && item.offer.amount() > 1) extra = extra.copy().append("  ").append(label("quantity", item.offer.amount()));
                context.drawText(textRenderer, extra, x + 24, y + offset + 1, item.offer.price() == null ? MUTED : 0xF0C66E, false);
                if (hover) {
                    var tooltip = new ArrayList<>(item.offer.stack().getTooltip(Item.TooltipContext.create(client.world), client.player, TooltipType.BASIC));
                    Integer cooldown = org.cat.express.wyspiaexpress.WyspiaExpressItems.configuredCooldowns().get(item.offer.stack().getItem());
                    if (cooldown != null && cooldown >= 0) tooltip.add(label("cooldown", cooldown).copy().withColor(0xC5ADFF));
                    if (item.offer.action()) tooltip.add(label("action").copy().withColor(GOLD));
                    hoveredTooltip = tooltip;
                }
            } else if (part instanceof LinkPart link) {
                context.drawText(textRenderer, colored(label("related", link.entry.name()), readable(link.entry.color())), pagePanel.x, y + 4, GOLD, false);
            } else if (part instanceof RulePart rule) {
                context.fill(pagePanel.x, y, pagePanel.x + pagePanel.contentWidth(), y + 1, 0x60000000 | rule.color);
            }
        }
        pagePanel.end(context); pagePanel.scrollbar(context);
    }

    private static void bookMark(DrawContext context, int x, int y) {
        context.drawBorder(x, y, 11, 9, 0xFFB79C67);
        context.fill(x + 5, y + 1, x + 6, y + 9, 0xFFB79C67);
        context.fill(x + 1, y + 2, x + 4, y + 3, 0xFFDBC18B);
        context.fill(x + 7, y + 2, x + 10, y + 3, 0xFFDBC18B);
    }
    private static void playerIcon(DrawContext context, int x, int y, int color) {
        color |= 0xFF000000;
        context.fill(x + 2, y, x + 6, y + 3, color);
        context.fill(x + 1, y + 4, x + 7, y + 7, color);
        context.fill(x + 2, y + 7, x + 4, y + 10, color);
        context.fill(x + 5, y + 7, x + 7, y + 10, color);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (listPanel.click(mouseX, mouseY) || pagePanel.click(mouseX, mouseY)) return true;
            if (listPanel.contains(mouseX, mouseY) && mouseX < listPanel.x + listPanel.contentWidth()) for (var row : rows) {
                if (mouseY >= listPanel.drawY(row.y) && mouseY < listPanel.drawY(row.y + row.height)) {
                    if (row.entry != null) { select(row.entry); setFocused(null); }
                    else if (!mine && !copycat && query.isBlank()) toggleCategory(row.category);
                    return true;
                }
            }
            if (pagePanel.contains(mouseX, mouseY)) for (var part : parts) if (part instanceof LinkPart link) {
                if (mouseY >= pagePanel.drawY(link.y) && mouseY < pagePanel.drawY(link.y + 20)) {
                    selected = link.entry; relatedView = true; pagePanel.offset = 0; pageSignature = ""; refreshPage(); return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        return button == 0 && (listPanel.drag(mx, my) || pagePanel.drag(mx, my)) || super.mouseDragged(mx, my, button, dx, dy);
    }
    @Override public boolean mouseReleased(double mx, double my, int button) { listPanel.release(); pagePanel.release(); return super.mouseReleased(mx, my, button); }
    @Override public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        return listPanel.scroll(mx, my, vertical) || pagePanel.scroll(mx, my, vertical) || super.mouseScrolled(mx, my, horizontal, vertical);
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (search.isFocused()) return super.keyPressed(key, scan, modifiers);
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
            List<GuidebookEntry> visible = rows.stream().filter(row -> row.entry != null).map(ListRow::entry).toList();
            if (!visible.isEmpty()) {
                int index = MathHelper.clamp(visible.indexOf(selected) + (key == GLFW.GLFW_KEY_DOWN ? 1 : -1), 0, visible.size() - 1);
                select(visible.get(index));
                for (var row : rows) if (selected.equals(row.entry)) listPanel.reveal(row.y, row.y + row.height);
                return true;
            }
        }
        if (key == GLFW.GLFW_KEY_PAGE_DOWN || key == GLFW.GLFW_KEY_PAGE_UP) {
            pagePanel.offset += pagePanel.height * (key == GLFW.GLFW_KEY_PAGE_DOWN ? 1 : -1); pagePanel.clamp(); return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }
    @Override public boolean shouldPause() { return false; }

    private record ListRow(GuidebookEntry entry, GuidebookEntry.Category category, List<OrderedText> lines, int y, int height) {}
    private sealed interface PagePart permits TextPart, ArtPart, RequirementPart, ItemPart, LinkPart, RulePart { int y(); }
    private record TextPart(int x, int y, List<OrderedText> lines, int color, float scale) implements PagePart {}
    private record ArtPart(int x, int y, int size, GuidebookImages.Art art) implements PagePart {}
    private record RequirementPart(int x, int y, int width, GuidebookCatalog.Status status) implements PagePart {}
    private record ItemPart(int x, int y, int width, int height, GuidebookItems.Offer offer) implements PagePart {}
    private record LinkPart(int y, GuidebookEntry entry) implements PagePart {}
    private record RulePart(int y, int color) implements PagePart {}
}
