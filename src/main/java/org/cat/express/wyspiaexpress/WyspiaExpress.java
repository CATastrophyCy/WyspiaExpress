package org.cat.express.wyspiaexpress;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.config.WyspiaExpressItemsConfig;
import org.cat.express.wyspiaexpress.config.WyspiaExpressModifiersConfig;
import org.cat.express.wyspiaexpress.config.WyspiaExpressRolesConfig;
import org.cat.express.wyspiaexpress.config.WyspiaExpressServerConfig;
import org.cat.express.wyspiaexpress.packets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WyspiaExpress implements ModInitializer {
    public static final String MOD_ID = "wyspiaexpress";

    // Create the global logger
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final WyspiaExpressRolesConfig ROLES_CONFIG = WyspiaExpressRolesConfig.createAndLoad();
    public static final WyspiaExpressModifiersConfig MODIFIERS_CONFIG = WyspiaExpressModifiersConfig.createAndLoad();
    public static final WyspiaExpressItemsConfig ITEMS_CONFIG = WyspiaExpressItemsConfig.createAndLoad();
    public static final WyspiaExpressServerConfig SERVER_CONFIG = WyspiaExpressServerConfig.createAndLoad();
    public static int TICK = 0;
    @Override
    public void onInitialize() {
        LOGGER.info("WyspiaExpress is initializing...");
        ROLES_CONFIG.save();
        MODIFIERS_CONFIG.save();
        ITEMS_CONFIG.save();
        SERVER_CONFIG.save();
        WyspiaExpressRoles.init();
        WyspiaExpressItems.init();
        WyspiaExpressSounds.init();
        WyspiaExpressGameFunctions.init();
        WyspiaExpressCommands.init();
        registerVersionCheck();
        registerPackets();
        ServerLifecycleEvents.SERVER_STARTING.register(RoleStatisticsManager::init);
        ServerLifecycleEvents.SERVER_STARTING.register(RoleCategoryStatisticsManager::init);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> RoleStatisticsManager.shutdown());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> RoleCategoryStatisticsManager.shutdown());
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if(TICK >= 1200)
                TICK = 0;
            else{
                TICK++;
            }
        });
        LOGGER.info("WyspiaExpress finished initializing.");
    }

    public static String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("UNKNOWN");
    }
    public static String getForceCrawlVersion() {
        return FabricLoader.getInstance()
                .getModContainer("forcecrawl")
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
                                "You must install " + WyspiaExpress.MOD_ID + " version " + WyspiaExpress.getVersion() +  " to join this server."));
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

        String forceCrawlVersion = WyspiaExpress.getForceCrawlVersion();
        if (!forceCrawlVersion.equals("UNKNOWN")) {
            ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
                String serverVersion = WyspiaExpress.getForceCrawlVersion();
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(serverVersion);
                sender.sendPacket(VersionCheckNetwork.VERSION_QUERY_FORCE_CRAWL_ID, buf);
            });
            ServerLoginNetworking.registerGlobalReceiver(
                    VersionCheckNetwork.VERSION_QUERY_FORCE_CRAWL_ID,
                    (server, handler, understood, buf, synchronizer, responseSender) -> {
                        if (!understood) {
                            handler.disconnect(Text.literal(
                                    "You must install " + "ForceCrawl"+ " version " + WyspiaExpress.getForceCrawlVersion() +  " to join this server."));
                            return;
                        }

                        String clientVersion = buf.readString(64);
                        String serverVersion = WyspiaExpress.getForceCrawlVersion();
                        if (!clientVersion.equals(serverVersion)) {
                            handler.disconnect(Text.literal(
                                    "Incompatible " + "ForceCrawl" + " version.\n" +
                                            "Server: " + serverVersion + ", Client: " + clientVersion));
                        }
                    }
            );
        }

    }
    private void registerPackets(){
        LichReviveC2SPacket.register();
        CultLeaderReviveC2SPacket.register();
        OutlawRevolverC2SPacket.register();
        NoTargetAbilityC2SPacket.register();
        RolePickC2SPacket.register();
        RitualDaggerC2SPacket.register();
    }
}
