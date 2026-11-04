package org.cat.express.wyspiaexpress.guidebook;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.item.Item;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.config.ShopConfig;

import java.util.List;

public final class RoleShopResolver {
    private RoleShopResolver() {}

    public static List<ShopConfig.ShopEntryConfig> getShopConfig(Role role) {
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if (config == null || !config.enableShop()) return List.of();
        return ShopConfig.fromStrings(config.shopEntries());
    }

    public static boolean isAction(Item item) {
        return item == WatheItems.BLACKOUT || item == WatheItems.PSYCHO_MODE
                || item == KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH
                || item == KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH
                || item == KinsWatheItems.ICON_POTION_EFFECT_REFRESH
                || item == KinsWatheItems.ICON_POWER_RESTORATION
                || item == WyspiaExpressItems.FUN_BOX || item == WyspiaExpressItems.SENSE_DEAD;
    }

    public static int purchaseQuantity(Item item) {
        return isAction(item) ? 0 : item == WatheItems.NOTE ? 2 : 1;
    }
}
