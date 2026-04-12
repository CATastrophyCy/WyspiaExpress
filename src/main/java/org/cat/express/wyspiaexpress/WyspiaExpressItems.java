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
import org.cat.express.wyspiaexpress.config.WyspiaExpressItemsConfig;
import org.cat.express.wyspiaexpress.items.FakeRevolverItem;
import org.cat.express.wyspiaexpress.items.MegaphoneItem;
import org.cat.express.wyspiaexpress.items.TpReadyItem;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
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
    public static final Item TP_READY = registerItem(new TpReadyItem(new Item.Settings().maxCount(1)), "tp_ready");
    public static final Item MEGAPHONE = registerItem(new MegaphoneItem(new Item.Settings().maxCount(1)), "megaphone");

    public static void registerItemConfig(){
        // custom items
        ITEMS_BASIC_CONFIG.put(FAKE_REVOLVER, WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeRevolverConfig.basic);
        ITEMS_BASIC_CONFIG.put(MEGAPHONE, WyspiaExpress.ITEMS_CONFIG.itemConfig.megaphoneConfig.basic);
        // other mods
        ITEMS_BASIC_CONFIG.put(ShopUtil.fromEnumShopEntry(EnumShopEntry.FAKE_KNIFE), WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeKnifeConfig.basic);
        ITEMS_BASIC_CONFIG.put(ShopUtil.fromEnumShopEntry(EnumShopEntry.MEDICAL_KIT), WyspiaExpress.ITEMS_CONFIG.itemConfig.medicalKitConfig.basic);
        ITEMS_BASIC_CONFIG.put(ShopUtil.fromEnumShopEntry(EnumShopEntry.WRENCH), WyspiaExpress.ITEMS_CONFIG.itemConfig.wrenchConfig.basic);

    }

    public static void registerItems(){
        registerItemsCooldown();
        // registerItemGroups
        registerItemGroup(FAKE_REVOLVER, WatheItems.EQUIPMENT_GROUP);
        registerItemGroup(MEGAPHONE, WatheItems.EQUIPMENT_GROUP);
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
    public static void registerItemCooldown(Item item, int minutes, int seconds) {
        GameConstants.ITEM_COOLDOWNS.put(item, GameConstants.getInTicks(minutes, seconds));
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
    public static void registerItemsCooldown(){
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeRevolverConfig.cooldown() >= 0) {
            registerItemCooldown(FAKE_REVOLVER, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeRevolverConfig.cooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.megaphoneConfig.cooldown() >= 0) {
            registerItemCooldown(MEGAPHONE, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.megaphoneConfig.cooldown());
        }
        // base wathe
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.revolverCooldown() >= 0) {
            registerItemCooldown(WatheItems.REVOLVER, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.revolverCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.knifeCooldown() >= 0) {
            registerItemCooldown(WatheItems.KNIFE, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.knifeCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.bodyBagCooldown() >= 0) {
            registerItemCooldown(WatheItems.BODY_BAG, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.bodyBagCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.psychoModeCooldown() >= 0) {
            registerItemCooldown(WatheItems.PSYCHO_MODE, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.psychoModeCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.blackoutCooldown() >= 0) {
            registerItemCooldown(WatheItems.BLACKOUT, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.blackoutCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.grenadeCooldown() >= 0) {
            registerItemCooldown(WatheItems.GRENADE, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.grenadeCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.derringerCooldown() >= 0) {
            registerItemCooldown(WatheItems.DERRINGER, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.derringerCooldown());
        }
        // kin's wathe
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.huntingKnifeCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.HUNTING_KNIFE, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.huntingKnifeCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.poisonInjectorCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.POISON_INJECTOR, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.poisonInjectorCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.blowGunCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.BLOWGUN, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.blowGunCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.knockOutDrugCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.KNOCKOUT_DRUG, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.knockOutDrugCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.captureDeviceCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.CAPTURE_DEVICE, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.captureDeviceCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.panCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.PAN, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.panCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.pillCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.PILL, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.pillCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.acidBarrelCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.SULFURIC_ACID_BARREL, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.acidBarrelCooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.wrenchConfig.cooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.WRENCH, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.wrenchConfig.cooldown());
        }
        if(WyspiaExpress.ITEMS_CONFIG.itemConfig.powerRestoreCooldown() >= 0) {
            registerItemCooldown(KinsWatheItems.ICON_POWER_RESTORATION, 0, WyspiaExpress.ITEMS_CONFIG.itemConfig.powerRestoreCooldown());
        }
    }
}
