package org.cat.express.wyspiaexpress.client.items;


import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.wathe.client.util.WatheItemTooltips;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemToolTip {

    private static final Map<Item, Integer> presetCooldowns = new HashMap<>();

    private static boolean initialized = false;
    public static int getItemCooldownTicks(@NotNull Item item) {return presetCooldowns.getOrDefault(item, 0);}

    public static void initItemCooldown() {
        if(initialized) return;
        WyspiaExpressItems.registerItemsCooldown();
        presetCooldowns.putAll(GameConstants.ITEM_COOLDOWNS);
        initialized = true;
    }

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
            initItemCooldown();
            ItemCooldownManager itemCooldown = MinecraftClient.getInstance().player.getItemCooldownManager();
            if (itemCooldown != null && itemCooldown.isCoolingDown(item)) {
                float progress = itemCooldown.getCooldownProgress(item, 0);
                int totalTicks = getItemCooldownTicks(item);
                if (totalTicks > 0) {
                    int remainingTicks = (int) (totalTicks * progress) + 19;
                    int totalSeconds = remainingTicks / 20;
                    int minutes = totalSeconds / 60;
                    int seconds = totalSeconds % 60;
                    String countdown = (minutes > 0 ? minutes + "m" : "") + (seconds > 0 ? seconds + "s" : "");
                    list.add(Text.translatable("tip.cooldown", countdown).withColor(WatheItemTooltips.COOLDOWN_COLOR));

                }
            }
        }
    }
}