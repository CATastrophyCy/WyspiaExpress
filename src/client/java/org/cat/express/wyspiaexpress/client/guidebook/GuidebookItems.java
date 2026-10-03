package org.cat.express.wyspiaexpress.client.guidebook;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.guidebook.RoleLoadoutResolver;
import org.cat.express.wyspiaexpress.guidebook.RoleShopResolver;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.ArrayList;
import java.util.List;

public final class GuidebookItems {
    private GuidebookItems() {}
    public record Offer(ItemStack stack, int amount, Integer price, boolean action) {}

    public static List<Offer> starting(GuidebookEntry entry) {
        List<ItemStack> stacks = new ArrayList<>();
        var role = entry.role();
        if (role != null) {
            stacks.addAll(RoleLoadoutResolver.configured(role));
            if (role == WatheRoles.VIGILANTE) add(stacks, WatheItems.REVOLVER, 1);
            if (role == Noellesroles.BETTER_VIGILANTE) add(stacks, WatheItems.GRENADE, 1);
            if (role == Noellesroles.MIMIC || role == Noellesroles.JESTER) add(stacks, ModItems.FAKE_KNIFE, 1);
            if (role == Noellesroles.JESTER) add(stacks, ModItems.FAKE_REVOLVER, 1);
            if (role == Noellesroles.CONDUCTOR) add(stacks, ModItems.MASTER_KEY, 1);
            if (role == Noellesroles.AWESOME_BINGLUS) add(stacks, WatheItems.NOTE, 16);
            if (role == KinsWatheRoles.CLEANER) add(stacks, KinsWatheItems.SULFURIC_ACID_BARREL, 1);
            if (role == KinsWatheRoles.DREAMER) {
                Number quantity = GuidebookSources.kinsExtra("DreamerInitialItemQuantity");
                if (quantity != null) add(stacks, KinsWatheItems.DREAM_IMPRINT, quantity.intValue());
            }
            if (role == KinsWatheRoles.HACKER) add(stacks, KinsWatheItems.PHONE, 1);
            if (role == KinsWatheRoles.KIDNAPPER) add(stacks, KinsWatheItems.KNOCKOUT_DRUG, 1);
            if (role == KinsWatheRoles.LICENSED_VILLAIN) add(stacks, WatheItems.LOCKPICK, 1);
            if (role == KinsWatheRoles.PHYSICIAN) add(stacks, KinsWatheItems.MEDICAL_KIT, 1);
            if (role == SERoles.ARSONIST) {
                add(stacks, SEItems.JERRY_CAN, 1);
                add(stacks, SEItems.LIGHTER, 1);
            }
        } else if (entry.modifier() == WyspiaExpressRoles.EMPLOYEE) {
            var key = WatheItems.KEY.getDefaultStack();
            key.set(DataComponentTypes.LORE, new LoreComponent(Text.literal("Employee Key")
                    .getWithStyle(Style.EMPTY.withItalic(false).withColor(0xFF8C00))));
            stacks.add(key);
        } else if (entry.modifier() == WyspiaExpressRoles.BOMBER) {
            add(stacks, WatheItems.GRENADE, WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.grenadeAmount());
            add(stacks, WyspiaExpressItems.SMOKE_BOMB, WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.smokeBombAmount());
        }
        // Combine identical stacks for readable amounts without changing components or actual grants.
        List<Offer> result = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            int index = -1;
            for (int i = 0; i < result.size(); i++) if (ItemStack.areItemsAndComponentsEqual(stack, result.get(i).stack())) { index = i; break; }
            if (index < 0) result.add(new Offer(stack.copyWithCount(1), stack.getCount(), null, false));
            else {
                Offer old = result.get(index);
                result.set(index, new Offer(old.stack(), old.amount() + stack.getCount(), null, false));
            }
        }
        return List.copyOf(result);
    }

    private static void add(List<ItemStack> stacks, Item item, int count) { if (count > 0) stacks.add(new ItemStack(item, count)); }

    public static boolean startingKnown(GuidebookEntry entry) {
        var role = entry.role();
        return role == null || WyspiaExpressRoles.ROLES_BASIC_CONFIG.containsKey(role)
                || role == WatheRoles.CIVILIAN || role == WatheRoles.KILLER || role == WatheRoles.VIGILANTE
                || role == Noellesroles.JESTER || role == Noellesroles.AWESOME_BINGLUS || role == Noellesroles.BARTENDER
                || role == Noellesroles.VOODOO || role == Noellesroles.RECALLER || role == Noellesroles.BETTER_VIGILANTE
                || role == Noellesroles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES
                || role == SERoles.INITIATE;
    }

    public static boolean shopKnown(GuidebookEntry entry) {
        return entry.role() != null && (WyspiaExpressRoles.ROLES_BASIC_CONFIG.containsKey(entry.role())
                || entry.role() == Noellesroles.BARTENDER || entry.role() == SERoles.INITIATE
                || entry.role() == WatheRoles.CIVILIAN || entry.role().canUseKiller());
    }

    public static List<Offer> shop(GuidebookEntry entry) {
        if (entry.role() == null) return List.of();
        if (entry.role() == Noellesroles.BARTENDER) {
            var world = net.minecraft.client.MinecraftClient.getInstance().world;
            return world == null ? List.of() : List.of(new Offer(ModItems.DEFENSE_VIAL.getDefaultStack(), 1,
                    org.agmas.noellesroles.ConfigWorldComponent.KEY.get(world).defenseVialPrice, false));
        }
        var entries = WyspiaExpressRoles.ROLES_BASIC_CONFIG.containsKey(entry.role())
                ? RoleShopResolver.configured(entry.role()) : entry.role() == SERoles.INITIATE ? SERoles.INITIATE_SHOP
                : entry.role().canUseKiller() ? GameConstants.SHOP_ENTRIES : List.<dev.doctor4t.wathe.util.ShopEntry>of();
        return entries.stream().map(offer -> new Offer(offer.stack().copyWithCount(1),
                RoleShopResolver.purchaseQuantity(offer.stack().getItem()), offer.price(), RoleShopResolver.isAction(offer.stack().getItem()))).toList();
    }
}
