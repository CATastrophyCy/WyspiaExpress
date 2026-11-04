package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.config.ShopConfig;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.List;

public final class PlayerShopComponent implements AutoSyncedComponent {
    public static final ComponentKey<PlayerShopComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "player_shop"), PlayerShopComponent.class);

    public record ShopItem(ShopEntry entry, int purchaseLimit, int purchases) {
        public boolean isSoldOut() { return purchaseLimit >= 0 && purchases >= purchaseLimit; }
        public int getRemainingPurchases() { return purchaseLimit < 0 ? -1 : Math.max(0, purchaseLimit - purchases); }
        private static ShopItem fromConfig(ShopConfig.ShopEntryConfig config) {
            return new ShopItem(ShopUtil.fromShopEntryConfig(config), config.purchaseLimit, 0);
        }
    }

    private final PlayerEntity player;
    private List<ShopItem> items = List.of();
    private boolean customShop;
    private int version;

    public PlayerShopComponent(PlayerEntity player) { this.player = player; }
    public List<ShopItem> getItems() { return items; }
    public boolean hasCustomShop() { return customShop; }
    public int getVersion() { return version; }

    public void setRoleShop(Role role) {
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        customShop = config != null;
        items = config != null && config.enableShop()
                ? ShopConfig.fromStrings(config.shopEntries()).stream().map(ShopItem::fromConfig).toList() : List.of();
        sync();
    }

    public void setShop(List<ShopConfig.ShopEntryConfig> entries) {
        customShop = true;
        items = entries.stream().map(ShopItem::fromConfig).toList();
        sync();
    }

    public void addEntry(ShopConfig.ShopEntryConfig entry) {
        var newItems = new ArrayList<>(items);
        newItems.add(ShopItem.fromConfig(entry));
        customShop = true;
        items = List.copyOf(newItems);
        sync();
    }

    public void setEntry(int index, ShopConfig.ShopEntryConfig entry) {
        var newItems = new ArrayList<>(items);
        newItems.set(index, ShopItem.fromConfig(entry));
        items = List.copyOf(newItems);
        sync();
    }

    public void removeEntry(int index) {
        var newItems = new ArrayList<>(items);
        newItems.remove(index);
        items = List.copyOf(newItems);
        sync();
    }

    public void tryBuy(int index) {
        if (!GameWorldComponent.KEY.get(player.getWorld()).isRunning() || index < 0 || index >= items.size()) return;
        ShopItem item = items.get(index);
        if (item.isSoldOut()) {
            player.sendMessage(Text.translatable("shop.wyspiaexpress.sold_out"), true);
            return;
        }
        var coins = dev.doctor4t.wathe.cca.PlayerShopComponent.KEY.get(player);
        ShopEntry entry = item.entry();
        if (ShopUtil.handlePurchase(player, coins.balance, entry.stack().getItem(), entry.price())) {
            coins.addToBalance(-entry.price());
            var newItems = new ArrayList<>(items);
            newItems.set(index, new ShopItem(entry, item.purchaseLimit(), item.purchases() + 1));
            items = List.copyOf(newItems);
            sync();
        }
    }

    private void sync() {
        version++;
        KEY.sync(player);
    }

    @Override public boolean shouldSyncWith(ServerPlayerEntity recipient) { return recipient == player; }

    @Override public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putBoolean("customShop", customShop);
        var entries = new NbtList();
        for (ShopItem item : items) {
            var entry = new NbtCompound();
            entry.put("stack", item.entry().stack().encode(lookup));
            entry.putInt("price", item.entry().price());
            entry.putString("type", item.entry().type().name());
            entry.putInt("purchaseLimit", item.purchaseLimit());
            entry.putInt("purchases", item.purchases());
            entries.add(entry);
        }
        tag.put("items", entries);
    }

    @Override public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        customShop = tag.getBoolean("customShop");
        var entries = tag.getList("items", NbtElement.COMPOUND_TYPE);
        var loadedItems = new ArrayList<ShopItem>();
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            var stack = ItemStack.fromNbt(lookup, entry.getCompound("stack")).orElseThrow();
            loadedItems.add(new ShopItem(new ShopEntry(stack, entry.getInt("price"), ShopEntry.Type.valueOf(entry.getString("type"))),
                    entry.getInt("purchaseLimit"), entry.getInt("purchases")));
        }
        items = List.copyOf(loadedItems);
        version++;
    }
}
