package org.cat.express.wyspiaexpress.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.client.items.ItemToolTip;
import org.cat.express.wyspiaexpress.packets.VersionCheckNetwork;

import java.util.concurrent.CompletableFuture;


public class WyspiaexpressClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerItemToolTips();
        ClientLoginNetworking.registerGlobalReceiver(
                VersionCheckNetwork.VERSION_QUERY_ID,
                (client, handler, buf, listenerAdder) -> {

                    String clientVersion = WyspiaExpress.getVersion();
                    PacketByteBuf reply = PacketByteBufs.create();
                    reply.writeString(clientVersion);

                    return CompletableFuture.completedFuture(reply);
                }
        );
    }

    public static void registerItemToolTips(){
        ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
            ItemToolTip.addItemtip(WyspiaExpressItems.FAKE_REVOLVER, itemStack, list);
        }));
    }
}
