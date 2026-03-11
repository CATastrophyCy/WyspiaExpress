package org.cat.express.wyspiaexpress;

import dev.doctor4t.wathe.api.event.AllowPlayerPunching;
import dev.doctor4t.wathe.api.event.ShouldDropOnDeath;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.cat.express.wyspiaexpress.config.WyspiaExpressServerConfig;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;
import org.cat.express.wyspiaexpress.shop.ShopUtil;

import java.util.HashMap;

public class WyspiaExpressItems {

    public static final HashMap<Item, WyspiaExpressServerConfig.ItemBasicConfig> ITEMS_BASIC_CONFIG = new HashMap<>();
    public static void init(){
        registerItemConfig();

        registerEvents();
    }
    public static void registerItemConfig(){
        ITEMS_BASIC_CONFIG.put(ShopUtil.fromEnumShopEntry(EnumShopEntry.FAKE_KNIFE).getItem(), WyspiaExpress.CONFIG.itemConfig.fakeKnifeConfig.basic);
        ITEMS_BASIC_CONFIG.put(ShopUtil.fromEnumShopEntry(EnumShopEntry.MEDICAL_KIT).getItem(), WyspiaExpress.CONFIG.itemConfig.medicalKitConfig.basic);
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
}
