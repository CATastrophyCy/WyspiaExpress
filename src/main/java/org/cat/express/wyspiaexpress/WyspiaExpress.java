package org.cat.express.wyspiaexpress;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.packets.VersionCheckNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cat.express.wyspiaexpress.config.WyspiaExpressServerConfig;

public class WyspiaExpress implements ModInitializer {
    public static final String MOD_ID = "wyspiaexpress";

    // Create the global logger
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final WyspiaExpressServerConfig CONFIG = WyspiaExpressServerConfig.createAndLoad();

    @Override
    public void onInitialize() {
        LOGGER.info("WyspiaExpress is initializing...");
        WyspiaExpressRoles.init();
        WyspiaExpressItems.init();
        registerVersionCheck();

        LOGGER.info("WyspiaExpress finished initializing.");
    }

    public static String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("UNKNOWN");
    }

    private void registerVersionCheck() {
        // 1) When login queries start, send server version to the client
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            String serverVersion = WyspiaExpress.getVersion();
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(serverVersion);
            sender.sendPacket(VersionCheckNetwork.VERSION_QUERY_ID, buf);
        });

        // 2) Handle client's response and kick if mismatch / no mod
        ServerLoginNetworking.registerGlobalReceiver(
                VersionCheckNetwork.VERSION_QUERY_ID,
                (server, handler, understood, buf, synchronizer, responseSender) -> {
                    if (!understood) {
                        handler.disconnect(Text.literal(
                                "You must install " + WyspiaExpress.MOD_ID + " to join this server."));
                        return;
                    }

                    String clientVersion = buf.readString(64);
                    String serverVersion = WyspiaExpress.getVersion();
                    if (!clientVersion.equals(serverVersion)) {
                        handler.disconnect(Text.literal(
                                "Incompatible " + WyspiaExpress.MOD_ID + " version.\n" +
                                        "Server: " + serverVersion + ", Client: " + clientVersion));
                    }
                }
        );
    }
}
