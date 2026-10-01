package org.cat.express.wyspiaexpress.guidebook;

import dev.doctor4t.wathe.api.Role;
import net.minecraft.item.ItemStack;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.shop.ShopUtil;

import java.util.ArrayList;
import java.util.List;

/** The configurable, additive part of a role's assignment loadout. Never fires assignment events. */
public final class RoleLoadoutResolver {
    private RoleLoadoutResolver() {}

    public static List<ItemStack> configured(Role role) {
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if (config == null) return List.of();
        List<ItemStack> result = new ArrayList<>();
        var items = config.items();
        var amounts = config.itemAmount();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = ShopUtil.fromEnumShopEntry(items.get(i)).getDefaultStack();
            stack.setCount(i < amounts.size() ? amounts.get(i) : 1);
            result.add(stack);
        }
        return result;
    }
}
