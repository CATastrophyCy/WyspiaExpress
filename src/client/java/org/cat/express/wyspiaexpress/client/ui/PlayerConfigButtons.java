package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;
import org.cat.express.wyspiaexpress.packets.PlayerConfigC2SPacket;

import java.util.Locale;

public final class PlayerConfigButtons extends ButtonWidget {
    private final PlayerConfigComponent.Option option;
    private final Tooltip description;
    private Boolean displayedState;

    private PlayerConfigButtons(int x, int y, int width, PlayerConfigComponent.Option option) {
        super(x, y, width, 20, Text.empty(), button -> {
            var player = MinecraftClient.getInstance().player;
            if (player != null && PlayerConfigComponent.canEdit(player)) {
                ClientPlayNetworking.send(new PlayerConfigC2SPacket(option.ordinal()));
            }
        }, DEFAULT_NARRATION_SUPPLIER);
        this.option = option;
        description = Tooltip.of(Text.translatable(key() + ".tooltip"));
        updateState();
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof InventoryScreen || screen instanceof LimitedInventoryScreen
                    || screen instanceof CreativeInventoryScreen)) return;
            if (client.player == null || !PlayerConfigComponent.canEdit(client.player)) return;
            int buttonWidth = Math.clamp((width - 24) / 3, 1, 164);
            int x = (width - buttonWidth * 3 - 8) / 2;
            int inventoryHeight = screen instanceof LimitedInventoryScreen ? 32
                    : screen instanceof CreativeInventoryScreen ? 136 : 166;
            int inventoryBottom = (height - inventoryHeight) / 2 + inventoryHeight;
            if (screen instanceof CreativeInventoryScreen) inventoryBottom += 28; // Bottom creative tabs.
            int y = Math.max(0, Math.min(height - 24, inventoryBottom + 4));
            for (var option : PlayerConfigComponent.Option.values()) {
                Screens.getButtons(screen).add(new PlayerConfigButtons(x, y, buttonWidth, option));
                x += buttonWidth + 4;
            }
            ScreenEvents.afterRender(screen).register((current, context, mouseX, mouseY, delta) -> {
                for (var child : current.children()) {
                    if (!(child instanceof PlayerConfigButtons button) || !button.visible || !button.isHovered()) continue;
                    // Finish the shop's queued price text before drawing our description above it.
                    context.draw();
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, 600);
                    context.drawOrderedTooltip(client.textRenderer, button.description.getLines(client), mouseX, mouseY);
                    context.draw();
                    context.getMatrices().pop();
                    break;
                }
            });
        });
    }

    private String key() { return "gui.wyspiaexpress.player_config." + option.name().toLowerCase(Locale.ROOT); }

    private void updateState() {
        var player = MinecraftClient.getInstance().player;
        visible = active = player != null && PlayerConfigComponent.canEdit(player);
        if (!visible) return;
        boolean enabled = PlayerConfigComponent.KEY.get(player).isEnabled(option);
        if (displayedState == null || displayedState != enabled) {
            setMessage(Text.literal(enabled ? "[x] " : "[ ] ").append(Text.translatable(key()))
                    .withColor(enabled ? 0x8DE6A0 : 0xD1D5DB));
            displayedState = enabled;
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        updateState();
        if (visible) super.renderWidget(context, mouseX, mouseY, delta);
    }
}
