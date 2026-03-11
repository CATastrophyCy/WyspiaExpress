package org.cat.express.wyspiaexpress.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.client.items.ItemToolTip;

public class WyspiaexpressClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerItemToolTips();
    }

    public static void registerItemToolTips(){
        ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
            ItemToolTip.addItemtip(WyspiaExpressItems.FAKE_REVOLVER, itemStack, list);
        }));
    }
}
