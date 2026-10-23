package org.cat.express.wyspiaexpress.util;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.HashMap;

public final class ItemUtil {
    private static Map<Item, Integer> nativeCooldowns;
    private ItemUtil() {}

    public static void captureNativeCooldowns() {
        if (nativeCooldowns == null) nativeCooldowns = new HashMap<>(GameConstants.ITEM_COOLDOWNS);
    }

    public static int nativeCooldownTicks(Item item) {
        captureNativeCooldowns();
        return nativeCooldowns.getOrDefault(item, 0);
    }

    public static void refreshConfiguredCooldowns() {
        captureNativeCooldowns();
        configuredCooldowns().forEach((item, seconds) -> GameConstants.ITEM_COOLDOWNS.put(item,
                seconds < 0 ? nativeCooldownTicks(item) : GameConstants.getInTicks(0, seconds)));
    }

    public static int getItemCooldownTicks(@NotNull Item item) {
        Integer seconds = configuredCooldowns().get(item);
        return seconds != null && seconds >= 0 ? GameConstants.getInTicks(0, seconds)
                : nativeCooldownTicks(item);
    }

    public static void setItemCooldown(@NotNull PlayerEntity player, @NotNull Item item, Hand hand) {
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            player.getItemCooldownManager().set(item, getItemCooldownTicks(item));
            if (hand != null) player.getStackInHand(hand).decrement(1);
        }
    }

    public static void setItemCooldown(@NotNull PlayerEntity player, @NotNull Item item, Hand hand, int seconds) {
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            player.getItemCooldownManager().set(item, GameConstants.getInTicks(0, seconds));
            if (hand != null) player.getStackInHand(hand).decrement(1);
        }
    }

    public static void registerItemCooldown(Item item, int minutes, int seconds) {
        GameConstants.ITEM_COOLDOWNS.put(item, GameConstants.getInTicks(minutes, seconds));
    }

    public static void registerItemCooldown(Item item, int seconds) {
        if (seconds < 0) return;
        registerItemCooldown(item, 0, seconds);
    }

    public static Map<Item, Integer> configuredCooldowns() {
        return Map.ofEntries(
                Map.entry(WyspiaExpressItems.FAKE_REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeRevolverConfig.cooldown()),
                Map.entry(WyspiaExpressItems.MEGAPHONE, WyspiaExpress.ITEMS_CONFIG.itemConfig.megaphoneConfig.cooldown()),
                Map.entry(WyspiaExpressItems.OUTLAW_REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.outlawRevolverConfig.cooldown()),
                Map.entry(WyspiaExpressItems.RITUAL_DAGGER, WyspiaExpress.ITEMS_CONFIG.itemConfig.ritualDaggerConfig.cooldown()),
                Map.entry(WyspiaExpressItems.TAPE, WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.cooldown()),
                Map.entry(WyspiaExpressItems.SENSE_DEAD, WyspiaExpress.ITEMS_CONFIG.itemConfig.senseDeadConfig.cooldown()),
                Map.entry(WyspiaExpressItems.SMOKE_BOMB, WyspiaExpress.ITEMS_CONFIG.itemConfig.smokeBombConfig.cooldown()),
                Map.entry(WatheItems.REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.revolverConfig.cooldown()),
                Map.entry(WatheItems.KNIFE, WyspiaExpress.ITEMS_CONFIG.itemConfig.knifeConfig.cooldown()),
                Map.entry(WatheItems.LOCKPICK, WyspiaExpress.ITEMS_CONFIG.itemConfig.lockPickConfig.cooldown()),
                Map.entry(WatheItems.BODY_BAG, WyspiaExpress.ITEMS_CONFIG.itemConfig.bodyBagConfig.cooldown()),
                Map.entry(WatheItems.GRENADE, WyspiaExpress.ITEMS_CONFIG.itemConfig.grenadeConfig.cooldown()),
                Map.entry(WatheItems.DERRINGER, WyspiaExpress.ITEMS_CONFIG.itemConfig.derringerConfig.cooldown()),
                Map.entry(WatheItems.PSYCHO_MODE, WyspiaExpress.ITEMS_CONFIG.itemConfig.psychoModeCooldown()),
                Map.entry(WatheItems.BLACKOUT, WyspiaExpress.ITEMS_CONFIG.itemConfig.blackOutCooldown()),
                Map.entry(KinsWatheItems.HUNTING_KNIFE, WyspiaExpress.ITEMS_CONFIG.itemConfig.huntingKnifeConfig.cooldown()),
                Map.entry(KinsWatheItems.POISON_INJECTOR, WyspiaExpress.ITEMS_CONFIG.itemConfig.poisonInjectorConfig.cooldown()),
                Map.entry(KinsWatheItems.BLOWGUN, WyspiaExpress.ITEMS_CONFIG.itemConfig.blowgunConfig.cooldown()),
                Map.entry(KinsWatheItems.KNOCKOUT_DRUG, WyspiaExpress.ITEMS_CONFIG.itemConfig.knockoutDrugConfig.cooldown()),
                Map.entry(KinsWatheItems.CAPTURE_DEVICE, WyspiaExpress.ITEMS_CONFIG.itemConfig.captureDeviceConfig.cooldown()),
                Map.entry(KinsWatheItems.PAN, WyspiaExpress.ITEMS_CONFIG.itemConfig.panConfig.cooldown()),
                Map.entry(KinsWatheItems.PILL, WyspiaExpress.ITEMS_CONFIG.itemConfig.pillConfig.cooldown()),
                Map.entry(KinsWatheItems.SULFURIC_ACID_BARREL, WyspiaExpress.ITEMS_CONFIG.itemConfig.acidBarrelConfig.cooldown()),
                Map.entry(KinsWatheItems.WRENCH, WyspiaExpress.ITEMS_CONFIG.itemConfig.wrenchConfig.cooldown()),
                Map.entry(KinsWatheItems.ICON_POWER_RESTORATION, WyspiaExpress.ITEMS_CONFIG.itemConfig.powerRestoreCooldown()));
    }

    public static ItemStack getVisibleHandStack(ItemStack stack) {
        var config = WyspiaExpressItems.ITEMS_BASIC_CONFIG.get(stack.getItem());
        return config != null && !config.renderItemOnHand() ? ItemStack.EMPTY : stack;
    }
    public static boolean isHoldingItem(PlayerEntity viewer, LivingEntity entity, Item item) {
        ItemStack psychosisItem = PlayerMoodComponent.KEY.get(viewer).getPsychosisItems().get(entity.getUuid());
        return psychosisItem != null ? psychosisItem.isOf(item) : entity.getMainHandStack().isOf(item);
    }
}
