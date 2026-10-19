package org.cat.express.wyspiaexpress.client.items;

import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.wathe.client.util.WatheItemTooltips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.util.ItemUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemToolTip {

    public static void addItemtip(@NotNull Item item, @NotNull ItemStack itemStack, @NotNull List<Text> list) {
        if (itemStack.isOf(item)) {
            addCooldowntip(item, itemStack, list);
            addTooltip(item, itemStack, list);
        }
    }

    public static void addTooltip(@NotNull Item item, @NotNull ItemStack itemStack, @NotNull List<Text> list) {
        if (itemStack.isOf(item)) {
            list.addAll(TextUtils.getTooltipForItem(item, Style.EMPTY.withColor(WatheItemTooltips.REGULAR_TOOLTIP_COLOR)));
        }
    }

    public static void addCooldowntip(@NotNull Item item, @NotNull ItemStack itemStack, @NotNull List<Text> list) {
        if (MinecraftClient.getInstance().player == null) return;
        if (itemStack.isOf(item)) {
            ItemCooldownManager itemCooldown = MinecraftClient.getInstance().player.getItemCooldownManager();
            if (itemCooldown != null && itemCooldown.isCoolingDown(item)) {
                int totalTicks = ItemUtil.getItemCooldownTicks(item);
                if (totalTicks > 0) {
                    int remainingTicks = itemCooldown.entries.get(item).endTick - itemCooldown.tick;
                    int totalSeconds = (remainingTicks + 19) / 20;
                    int minutes = totalSeconds / 60;
                    int seconds = totalSeconds % 60;
                    String countdown = (minutes > 0 ? minutes + "m" : "") + (seconds > 0 ? seconds + "s" : "");
                    list.add(Text.translatable("tip.cooldown", countdown).withColor(WatheItemTooltips.COOLDOWN_COLOR));

                }
            }
        }
    }
}
