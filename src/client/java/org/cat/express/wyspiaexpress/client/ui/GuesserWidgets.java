package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.guidebook.GuidebookEntry;
import org.cat.express.wyspiaexpress.components.GuesserComponent;
import org.cat.express.wyspiaexpress.components.RoleComponent;
import org.cat.express.wyspiaexpress.modifiers.GuesserAbility;
import org.cat.express.wyspiaexpress.packets.GuessC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class GuesserWidgets {
    private final LimitedInventoryScreen screen;
    private final InventoryAbilityPanel.Group group;
    private final List<Role> roles;
    private InventoryAbilityPanel panel;
    private UUID target;
    private TextFieldWidget input;

    public GuesserWidgets(LimitedInventoryScreen screen) {
        this.screen = screen;
        this.group = new InventoryAbilityPanel.Group("guesser", Text.translatable("modifier.wyspiaexpress.guesser"), new ArrayList<>(), true);
        var roundRoles = RoleComponent.KEY.get(screen.player.getWorld());

        this.roles = WatheRoles.ROLES.stream().filter(GuesserAbility::civilian)
                .filter(role -> !roundRoles.disabledRoles.contains(WyspiaExpressRoles.getRoleId(role)))
                .sorted(Comparator.comparing(r -> GuidebookEntry.role(r).name().getString())).toList();
        players();
    }
    public InventoryAbilityPanel.Group group() { return group; }
    public void attach(InventoryAbilityPanel panel) { this.panel = panel; }
    private void players() {
        target = null;
        group.rowBreakBefore = null;
        var game = GameWorldComponent.KEY.get(screen.player.getWorld());
        var entries = screen.player.networkHandler.getPlayerList().stream()
                .filter(entry -> entry.getGameMode() != GameMode.SPECTATOR && entry.getGameMode() != GameMode.CREATIVE)
                .filter(entry -> game.getRoles().containsKey(entry.getProfile().getId()))
                .sorted(Comparator.comparing(entry -> entry.getProfile().getName(), String.CASE_INSENSITIVE_ORDER)).toList();
        long civilians = entries.stream().filter(entry -> GuesserAbility.civilian(game.getRoles().get(entry.getProfile().getId()))).count();
        var widgets = new ArrayList<ClickableWidget>();
        if (civilians >= WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.minPlayer()) for (var entry : entries) {
            if (entry.getProfile().getId().equals(screen.player.getUuid())
                    || !GuesserAbility.canTarget(game.getRoles().get(entry.getProfile().getId()))) continue;
            var button = new ButtonWidget(0, 0, 16, 16, Text.literal(entry.getProfile().getName()),
                    b -> { if (GuesserComponent.KEY.get(screen.player).cooldown() == 0) edit(entry.getProfile().getId()); }, supplier -> supplier.get()) {
                @Override protected void renderWidget(net.minecraft.client.gui.DrawContext context, int x, int y, float delta) {
                    int cooldown = GuesserComponent.KEY.get(screen.player).cooldown();
                    if (cooldown > 0) context.setShaderColor(.25f, .25f, .25f, .5f);
                    context.drawGuiTexture(ShopEntry.Type.TOOL.getTexture(), getX() - 7, getY() - 7, 30, 30);
                    PlayerSkinDrawer.draw(context, entry.getSkinTextures().texture(), getX(), getY(), 16);
                    if (isHovered()) {
                        context.fill(getX(), getY(), getX() + 16, getY() + 14, 0x90FFBF49);
                        context.fill(getX(), getY() + 14, getX() + 15, getY() + 15, 0x90FFBF49);
                        context.fill(getX(), getY() + 15, getX() + 14, getY() + 16, 0x90FFBF49);
                    }
                    context.setShaderColor(1, 1, 1, 1);
                    if (isHovered()) context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                            getMessage(), getX() + 8, getY() - 9, 0xFFBF49);
                    if (cooldown > 0) context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                            Text.literal(String.valueOf((cooldown + 19) / 20)), getX(), getY(), 0xFF7777);
                }
            };
            widgets.add(button);
        }
        group.widgets = widgets;
        if (panel != null) { panel.focusWidget(null); panel.refresh(); }
    }
    private void edit(UUID selected) {
        target = selected;
        input = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 180, 20,
                Text.translatable("gui.wyspiaexpress.guesser.role")) {
            @Override public boolean keyPressed(int key, int scan, int modifiers) {
                if (isFocused() && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) { submit(); return true; }
                return super.keyPressed(key, scan, modifiers);
            }
        };
        input.setMaxLength(128);
        input.setPlaceholder(Text.translatable("gui.wyspiaexpress.guesser.role"));
        input.setChangedListener(text -> editor());
        editor();
        if (panel != null) { panel.select("guesser"); panel.focusWidget(null); }
    }
    private void editor() {
        var widgets = new ArrayList<ClickableWidget>();
        widgets.add(ButtonWidget.builder(Text.translatable("gui.back"), b -> players()).dimensions(0, 0, 45, 20).build());
        widgets.add(input);
        group.rowBreakBefore = null;
        String search = input.getText().strip().toLowerCase(Locale.ROOT);
        for (Role role : roles) {
            Text name = GuidebookEntry.role(role).name();
            if (!search.isEmpty() && !name.getString().toLowerCase(Locale.ROOT).contains(search)
                    && !WyspiaExpressRoles.getRoleId(role).toLowerCase(Locale.ROOT).contains(search)) continue;
            var button = ButtonWidget.builder(name.copy().withColor(role.color()), b -> {
                        input.setText(WyspiaExpressRoles.getRoleId(role));
                        submit();
                    })
                    .dimensions(0, 0, 140, 20).build();
            button.setTooltip(Tooltip.of(Text.literal(WyspiaExpressRoles.getRoleId(role))));
            if (group.rowBreakBefore == null) group.rowBreakBefore = button;
            widgets.add(button);
        }
        group.widgets = widgets;
        if (panel != null) panel.refresh();
    }
    private void submit() {
        if (target == null || GuesserComponent.KEY.get(screen.player).cooldown() > 0) return;
        String text = input.getText().strip();
        var guess = GuesserAbility.resolve(text);
        if (guess.role() == null) {
            String typed = text;
            var translated = WatheRoles.ROLES.stream().filter(r -> GuidebookEntry.role(r).name().getString().equalsIgnoreCase(typed)).toList();
            if (translated.size() == 1) { text = WyspiaExpressRoles.getRoleId(translated.getFirst()); guess = GuesserAbility.resolve(text); }
        }
        if (guess.forbidden()) {
            // Report an invalid attempt without transmitting a non-civilian role guess.
            text = "";
        }
        ClientPlayNetworking.send(new GuessC2SPacket(target, text));
        screen.close();
    }
}
