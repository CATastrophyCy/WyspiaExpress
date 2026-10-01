package org.cat.express.wyspiaexpress;

import dev.doctor4t.wathe.api.event.AllowPlayerPunching;
import dev.doctor4t.wathe.api.event.ShouldDropOnDeath;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.agmas.noellesroles.ModItems;
import org.cat.express.wyspiaexpress.config.WyspiaExpressItemsConfig;
import org.cat.express.wyspiaexpress.items.*;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

public class WyspiaExpressItems {

    public static final HashMap<Item, WyspiaExpressItemsConfig.ItemBasicConfig> ITEMS_BASIC_CONFIG = new HashMap<>();

    public static void init(){
        registerItemConfig();

        registerItems();
        registerEvents();
    }

    public static final Item FAKE_REVOLVER = registerItem(new FakeRevolverItem(new Item.Settings().maxCount(1)), "fake_revolver");
    public static final Item FUN_BOX = registerItem( new Item(new Item.Settings()), "fun_box");
    public static final Item SENSE_DEAD = registerItem( new Item(new Item.Settings()), "sense_dead");
    public static final Item TP_READY = registerItem(new TpReadyItem(new Item.Settings().maxCount(1)), "tp_ready");
    public static final Item MEGAPHONE = registerItem(new MegaphoneItem(new Item.Settings().maxCount(4)), "megaphone");
    public static final Item OUTLAW_REVOLVER = registerItem(new OutlawRevolverItem(new Item.Settings().maxCount(1)), "outlaw_revolver");
    public static final Item RITUAL_DAGGER = registerItem(new RitualDaggerItem(new Item.Settings().maxCount(1)), "ritual_dagger");
    public static final Item TAPE = registerItem(new TapeItem(new Item.Settings().maxCount(4)), "tape");
    public static final Item SMOKE_BOMB = registerItem(new SmokeBombItem(new Item.Settings().maxCount(1)), "smoke_bomb");
    public static void registerItemConfig(){
        // custom items
        ITEMS_BASIC_CONFIG.put(FAKE_REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeRevolverConfig.basic);
        ITEMS_BASIC_CONFIG.put(MEGAPHONE, WyspiaExpress.ITEMS_CONFIG.itemConfig.megaphoneConfig.basic);
        ITEMS_BASIC_CONFIG.put(OUTLAW_REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.outlawRevolverConfig.basic);
        ITEMS_BASIC_CONFIG.put(TAPE, WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.basic);
        ITEMS_BASIC_CONFIG.put(RITUAL_DAGGER, WyspiaExpress.ITEMS_CONFIG.itemConfig.ritualDaggerConfig.basic);
        ITEMS_BASIC_CONFIG.put(SMOKE_BOMB, WyspiaExpress.ITEMS_CONFIG.itemConfig.smokeBombConfig.basic);
        // wathe
        ITEMS_BASIC_CONFIG.put(WatheItems.REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.revolverConfig.basic);
        ITEMS_BASIC_CONFIG.put(WatheItems.KNIFE, WyspiaExpress.ITEMS_CONFIG.itemConfig.knifeConfig.basic);
        ITEMS_BASIC_CONFIG.put(WatheItems.LOCKPICK, WyspiaExpress.ITEMS_CONFIG.itemConfig.lockPickConfig.basic);
        ITEMS_BASIC_CONFIG.put(WatheItems.BODY_BAG, WyspiaExpress.ITEMS_CONFIG.itemConfig.bodyBagConfig.basic);
        ITEMS_BASIC_CONFIG.put(WatheItems.GRENADE, WyspiaExpress.ITEMS_CONFIG.itemConfig.grenadeConfig.basic);
        ITEMS_BASIC_CONFIG.put(WatheItems.DERRINGER, WyspiaExpress.ITEMS_CONFIG.itemConfig.derringerConfig.basic);

        // noelles roles
        ITEMS_BASIC_CONFIG.put(ModItems.FAKE_KNIFE, WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeKnifeConfig.basic);

        // kin's wathe
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.HUNTING_KNIFE, WyspiaExpress.ITEMS_CONFIG.itemConfig.huntingKnifeConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.POISON_INJECTOR, WyspiaExpress.ITEMS_CONFIG.itemConfig.poisonInjectorConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.BLOWGUN, WyspiaExpress.ITEMS_CONFIG.itemConfig.blowgunConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.KNOCKOUT_DRUG, WyspiaExpress.ITEMS_CONFIG.itemConfig.knockoutDrugConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.CAPTURE_DEVICE, WyspiaExpress.ITEMS_CONFIG.itemConfig.captureDeviceConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.PAN, WyspiaExpress.ITEMS_CONFIG.itemConfig.panConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.PILL, WyspiaExpress.ITEMS_CONFIG.itemConfig.pillConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.SULFURIC_ACID_BARREL, WyspiaExpress.ITEMS_CONFIG.itemConfig.acidBarrelConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.MEDICAL_KIT, WyspiaExpress.ITEMS_CONFIG.itemConfig.medicalKitConfig.basic);
        ITEMS_BASIC_CONFIG.put(KinsWatheItems.WRENCH, WyspiaExpress.ITEMS_CONFIG.itemConfig.wrenchConfig.basic);

    }

    public static void registerItems(){
        registerItemsCooldown();
        // registerItemGroups
        registerItemGroup(FAKE_REVOLVER, WatheItems.EQUIPMENT_GROUP);
        registerItemGroup(MEGAPHONE, WatheItems.EQUIPMENT_GROUP);
        registerItemGroup(OUTLAW_REVOLVER, WatheItems.EQUIPMENT_GROUP);
        registerItemGroup(RITUAL_DAGGER, WatheItems.EQUIPMENT_GROUP);
        registerItemGroup(TAPE, WatheItems.EQUIPMENT_GROUP);
        registerItemGroup(SMOKE_BOMB, WatheItems.EQUIPMENT_GROUP);
    }
    public static Item registerItem(Item item, String id) {
        Identifier itemID = Identifier.of(WyspiaExpress.MOD_ID, id);

        return Registry.register(Registries.ITEM, itemID, item);
    }
    public static void setItemCooldown(@NotNull PlayerEntity player, @NotNull Item item, Hand hand) {
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            player.getItemCooldownManager().set(item, GameConstants.ITEM_COOLDOWNS.get(item));
            if (hand != null) player.getStackInHand(hand).decrement(1);
        }
    }
    public static void setItemCooldown(@NotNull PlayerEntity player, @NotNull Item item, Hand hand, int seconds) {
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            player.getItemCooldownManager().set(item, GameConstants.getInTicks(0,seconds));
            if (hand != null) player.getStackInHand(hand).decrement(1);
        }
    }
    public static void registerItemCooldown(Item item, int minutes, int seconds) {
        GameConstants.ITEM_COOLDOWNS.put(item, GameConstants.getInTicks(minutes, seconds));
    }
    public static void registerItemCooldown(Item item, int seconds) {
        if(seconds < 0) return;
        registerItemCooldown(item,0,seconds);
    }
    public static void registerItemGroup(Item item, RegistryKey<ItemGroup> itemGroup){
        ItemGroupEvents.modifyEntriesEvent(itemGroup).register( entries -> {
            entries.add(item);
        });
    }
    public static void registerEvents(){
        registerPunchEvent();
        registerDropEvent();
    }
    public static void registerPunchEvent(){
        AllowPlayerPunching.EVENT.register(((playerEntity, playerEntity1) -> {
            Item item = playerEntity.getMainHandStack().getItem();
            var config = ITEMS_BASIC_CONFIG.get(item);
            return config != null && config.canPunchPlayers();
        }));
    }
    public static void registerDropEvent(){
        ShouldDropOnDeath.EVENT.register(((itemStack, identifier) -> {
            var config = ITEMS_BASIC_CONFIG.get(itemStack.getItem());
            return config != null && config.dropOnDeath();
        }));
    }
    public static java.util.Map<Item, Integer> configuredCooldowns() {
        return java.util.Map.ofEntries(
                java.util.Map.entry(FAKE_REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeRevolverConfig.cooldown()),
                java.util.Map.entry(MEGAPHONE, WyspiaExpress.ITEMS_CONFIG.itemConfig.megaphoneConfig.cooldown()),
                java.util.Map.entry(OUTLAW_REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.outlawRevolverConfig.cooldown()),
                java.util.Map.entry(RITUAL_DAGGER, WyspiaExpress.ITEMS_CONFIG.itemConfig.ritualDaggerConfig.cooldown()),
                java.util.Map.entry(TAPE, WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.cooldown()),
                java.util.Map.entry(SENSE_DEAD, WyspiaExpress.ITEMS_CONFIG.itemConfig.senseDeadConfig.cooldown()),
                java.util.Map.entry(SMOKE_BOMB, WyspiaExpress.ITEMS_CONFIG.itemConfig.smokeBombConfig.cooldown()),
                java.util.Map.entry(WatheItems.REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.revolverConfig.cooldown()),
                java.util.Map.entry(WatheItems.KNIFE, WyspiaExpress.ITEMS_CONFIG.itemConfig.knifeConfig.cooldown()),
                java.util.Map.entry(WatheItems.LOCKPICK, WyspiaExpress.ITEMS_CONFIG.itemConfig.lockPickConfig.cooldown()),
                java.util.Map.entry(WatheItems.BODY_BAG, WyspiaExpress.ITEMS_CONFIG.itemConfig.bodyBagConfig.cooldown()),
                java.util.Map.entry(WatheItems.GRENADE, WyspiaExpress.ITEMS_CONFIG.itemConfig.grenadeConfig.cooldown()),
                java.util.Map.entry(WatheItems.DERRINGER, WyspiaExpress.ITEMS_CONFIG.itemConfig.derringerConfig.cooldown()),
                java.util.Map.entry(WatheItems.PSYCHO_MODE, WyspiaExpress.ITEMS_CONFIG.itemConfig.psychoModeCooldown()),
                java.util.Map.entry(WatheItems.BLACKOUT, WyspiaExpress.ITEMS_CONFIG.itemConfig.blackOutCooldown()),
                java.util.Map.entry(KinsWatheItems.HUNTING_KNIFE, WyspiaExpress.ITEMS_CONFIG.itemConfig.huntingKnifeConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.POISON_INJECTOR, WyspiaExpress.ITEMS_CONFIG.itemConfig.poisonInjectorConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.BLOWGUN, WyspiaExpress.ITEMS_CONFIG.itemConfig.blowgunConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.KNOCKOUT_DRUG, WyspiaExpress.ITEMS_CONFIG.itemConfig.knockoutDrugConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.CAPTURE_DEVICE, WyspiaExpress.ITEMS_CONFIG.itemConfig.captureDeviceConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.PAN, WyspiaExpress.ITEMS_CONFIG.itemConfig.panConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.PILL, WyspiaExpress.ITEMS_CONFIG.itemConfig.pillConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.SULFURIC_ACID_BARREL, WyspiaExpress.ITEMS_CONFIG.itemConfig.acidBarrelConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.WRENCH, WyspiaExpress.ITEMS_CONFIG.itemConfig.wrenchConfig.cooldown()),
                java.util.Map.entry(KinsWatheItems.ICON_POWER_RESTORATION, WyspiaExpress.ITEMS_CONFIG.itemConfig.powerRestoreCooldown()));
    }

    public static void registerItemsCooldown() {
        configuredCooldowns().forEach(WyspiaExpressItems::registerItemCooldown);
    }
}
