package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.packets.TrashC2SPacket;
import org.cat.express.wyspiaexpress.util.ItemUtil;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public final class InventoryTrash extends ClickableWidget {
    private final LimitedInventoryScreen screen;
    private final ButtonWidget confirm;
    private final ButtonWidget cancel;
    private final Map<Integer, ItemStack> selected = new LinkedHashMap<>();
    private boolean selecting;

    private InventoryTrash(LimitedInventoryScreen screen) {
        super((screen.width + 176) / 2 + 4, (screen.height - 32) / 2 + 6, 20, 20,
                Text.translatable("gui.wyspiaexpress.trash"));
        this.screen = screen;
        setTooltip(Tooltip.of(Text.translatable("gui.wyspiaexpress.trash.tooltip")));
        confirm = ButtonWidget.builder(Text.literal("✓").withColor(0x8DE6A0), button -> confirm())
                .dimensions(getX(), getY(), 20, 20).build();
        boolean sideBySide = getX() + 44 <= screen.width - 4;
        if (!sideBySide) { confirm.setY(getY() - 5); confirm.setHeight(14); }
        cancel = ButtonWidget.builder(Text.literal("×").withColor(0xFF9292), button -> cancel())
                .dimensions(getX() + (sideBySide ? 24 : 0), getY() + (sideBySide ? 0 : 11), 20, sideBySide ? 20 : 14)
                .tooltip(Tooltip.of(Text.translatable("gui.cancel"))).build();
        update();
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, current, width, height) -> {
            if (!(current instanceof LimitedInventoryScreen screen)) return;
            var trash = new InventoryTrash(screen);
            var buttons = Screens.getButtons(screen);
            buttons.add(trash);
            buttons.add(trash.confirm);
            buttons.add(trash.cancel);
            ScreenEvents.afterTick(screen).register(ignored -> trash.update());
            ScreenEvents.remove(screen).register(ignored -> trash.cancel());
            ScreenEvents.afterRender(screen).register((ignored, context, mouseX, mouseY, delta) -> trash.highlight(context));
            ScreenMouseEvents.allowMouseClick(screen).register((ignored, x, y, button) -> trash.allowClick(x, y, button));
            ScreenMouseEvents.allowMouseRelease(screen).register((ignored, x, y, button) -> !trash.selecting);
            ScreenKeyboardEvents.allowKeyPress(screen).register((ignored, key, scan, modifiers) -> {
                if (!trash.selecting) return true;
                if (client.options.dropKey.matchesKey(key, scan) || client.options.swapHandsKey.matchesKey(key, scan)) return false;
                for (var hotbar : client.options.hotbarKeys) if (hotbar.matchesKey(key, scan)) return false;
                return true;
            });
        });
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        selecting = true;
        selected.clear();
        screen.setFocused(null);
        for (var child : screen.children()) if (child instanceof InventoryAbilityPanel panel) panel.blurForClick(-1, -1);
        update();
    }

    private void cancel() {
        selecting = false;
        selected.clear();
        screen.setDragging(false);
        update();
    }

    private void confirm() {
        update();
        if (!selecting || selected.isEmpty()) return;
        ClientPlayNetworking.send(new TrashC2SPacket(
                selected.entrySet().stream().map(entry -> new TrashC2SPacket.Selection(entry.getKey(), entry.getValue().copy())).toList()));
        cancel();
    }

    private void update() {
        boolean available = ItemUtil.canUseInventoryTrash(screen.player);
        if (!available) { selecting = false; selected.clear(); }
        selected.entrySet().removeIf(entry -> !ItemUtil.canTrash(screen.player.getInventory().getStack(entry.getKey()))
                || !ItemStack.areEqual(entry.getValue(), screen.player.getInventory().getStack(entry.getKey())));
        visible = available && !selecting;
        active = available && screen.getScreenHandler().getCursorStack().isEmpty();
        confirm.visible = cancel.visible = available && selecting;
        confirm.active = !selected.isEmpty();
        confirm.setTooltip(Tooltip.of(Text.translatable("gui.wyspiaexpress.trash.confirm", selected.size())));
    }

    private int slotX(Slot slot) { return (screen.width - 176) / 2 + slot.x; }
    private int slotY(Slot slot) { return (screen.height - 32) / 2 + slot.y - LimitedHandledScreen.getYOffset(); }
    private boolean inventorySlot(Slot slot) { return slot.inventory == screen.player.getInventory() && slot.getIndex() >= 0 && slot.getIndex() < 9; }

    private boolean allowClick(double x, double y, int button) {
        update();
        if (!selecting) return true;
        if (confirm.isMouseOver(x, y) || cancel.isMouseOver(x, y)) return true;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (var slot : screen.getScreenHandler().slots) {
                if (!inventorySlot(slot) || x < slotX(slot) || x >= slotX(slot) + 16
                        || y < slotY(slot) || y >= slotY(slot) + 16 || !ItemUtil.canTrash(slot.getStack())) continue;
                if (selected.remove(slot.getIndex()) == null) selected.put(slot.getIndex(), slot.getStack().copy());
                playDownSound(MinecraftClient.getInstance().getSoundManager());
                update();
                break;
            }
        }
        return false;
    }

    private void highlight(DrawContext context) {
        if (!selecting) return;
        // Flush the shop's queued prices before drawing slot overlays.
        context.draw();
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 400);
        for (var slot : screen.getScreenHandler().slots) {
            if (!inventorySlot(slot) || !ItemUtil.canTrash(slot.getStack())) continue;
            boolean marked = selected.containsKey(slot.getIndex());
            int x = slotX(slot), y = slotY(slot);
            context.fill(x, y, x + 16, y + 16, marked ? 0x66E5484D : 0x2275CFA5);
            context.drawBorder(x - 1, y - 1, 18, 18, marked ? 0xFFFF6970 : 0xFF75CFA5);
        }
        context.draw();
        context.getMatrices().pop();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getRight(), getBottom(), isHovered() ? 0xE6404754 : 0xC0191E28);
        context.drawBorder(getX(), getY(), width, height, active ? 0xFF8993A3 : 0xFF505664);
        int x = getX() + 5, y = getY() + 4, color = active ? 0xFFE3E7EE : 0xFF707782;
        context.fill(x + 3, y, x + 7, y + 1, color);
        context.fill(x, y + 2, x + 10, y + 3, color);
        context.fill(x + 1, y + 4, x + 2, y + 11, color);
        context.fill(x + 8, y + 4, x + 9, y + 11, color);
        context.fill(x + 2, y + 11, x + 8, y + 12, color);
        context.fill(x + 4, y + 4, x + 5, y + 9, color);
        context.fill(x + 6, y + 4, x + 7, y + 9, color);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { appendDefaultNarrations(builder); }
}
