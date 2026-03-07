package org.cat.express.wyspiaexpress.config;


import dev.doctor4t.wathe.util.ShopEntry;
import org.cat.express.wyspiaexpress.Wyspiaexpress;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;

import java.util.ArrayList;
import java.util.List;

// I'll change this in the future so shopEntries list becomes a variable of the RoleBasicConfig's wrapper
public class ShopConfig {

    // decode the formatted string to a list of ShopEntryConfig
    public static List<ShopEntryConfig> fromStrings(List<String> shopEntries) {
        List<ShopEntryConfig> entries = new ArrayList<>();
        for (String entry : shopEntries){
            entries.add(ShopEntryConfig.fromString(entry));
        }
        return entries;
    }

    public static class ShopEntryConfig{
        public EnumShopEntry item = EnumShopEntry.NOTE;
        public int price = 5;
        public int purchaseLimit = -1;
        public ShopEntry.Type type = ShopEntry.Type.TOOL;

        public static ShopEntryConfig fromString(String s){
            String[] parts = s.split(";");
            ShopEntryConfig shopEntryConfig = new ShopEntryConfig();
            // Skip invalidly formatted entries
            if (parts.length != 4) {
                Wyspiaexpress.LOGGER.warn("Invalid shop entry format: {}", s);
                return shopEntryConfig;
            }
            try {
                EnumShopEntry item = EnumShopEntry.valueOf(parts[0].trim().toUpperCase());
                int price = Integer.parseInt(parts[1].trim());
                int limit = Integer.parseInt(parts[2].trim());
                ShopEntry.Type type = ShopEntry.Type.valueOf(parts[3].trim().toUpperCase());

                shopEntryConfig.item = item;
                shopEntryConfig.price = price;
                shopEntryConfig.purchaseLimit = limit;
                shopEntryConfig.type = type;
            } catch (IllegalArgumentException e) {
                Wyspiaexpress.LOGGER.warn("Failed to parse shop entry: {}", s);
            }
            return shopEntryConfig;
        }
    }

}
