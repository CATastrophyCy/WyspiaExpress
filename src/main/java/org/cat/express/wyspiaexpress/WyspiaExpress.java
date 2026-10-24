package org.cat.express.wyspiaexpress;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.WatheMapEffects;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.ModItems;
import org.cat.express.wyspiaexpress.config.ItemsConfig;
import org.cat.express.wyspiaexpress.config.ModifiersConfig;
import org.cat.express.wyspiaexpress.config.RolesConfig;
import org.cat.express.wyspiaexpress.config.*;
import org.cat.express.wyspiaexpress.items.InvisibleArmor;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedGameMode;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressKillerModdedGameMode;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressTrueNeutralModdedGameMode;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedAlternate;
import org.cat.express.wyspiaexpress.game.mapeffect.WyspiaExpressMapEffect;
import org.cat.express.wyspiaexpress.packets.*;
import org.cat.express.wyspiaexpress.particles.ParticleManager;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.cat.express.wyspiaexpress.util.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WyspiaExpress implements ModInitializer {
    public static final String MOD_ID = "wyspiaexpress";

    // Create the global logger
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RolesConfig ROLES_CONFIG = new RolesConfig();
    public static final ModifiersConfig MODIFIERS_CONFIG = new ModifiersConfig();
    public static final ItemsConfig ITEMS_CONFIG = new ItemsConfig();
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();
    public static final GeneralConfig GENERAL_CONFIG = new GeneralConfig();
    public static final RulesConfig RULES_CONFIG = new RulesConfig();
    public static final TasksConfig TASKS_CONFIG = new TasksConfig();
    public static final GamemodesConfig GAMEMODES_CONFIG = new GamemodesConfig();
    public static final ModeConfig MODE_CONFIG = new ModeConfig();
    public static final WyspiaExpressModdedGameMode MODDED_GAMEMODE =
            new WyspiaExpressModdedGameMode(Identifier.of(MOD_ID, "modded"));
    public static final WyspiaExpressKillerModdedGameMode KILLER_MODDED_GAMEMODE =
            new WyspiaExpressKillerModdedGameMode(Identifier.of(MOD_ID, "killer_modded"));
    public static final WyspiaExpressTrueNeutralModdedGameMode TRUE_NEUTRAL_MODDED_GAMEMODE =
            new WyspiaExpressTrueNeutralModdedGameMode(Identifier.of(MOD_ID, "true_neutral_modded"));
    public static final WyspiaExpressModdedAlternate ALTERNATE_GAMEMODE =
            new WyspiaExpressModdedAlternate(Identifier.of(MOD_ID, "modded_alternate"));
    public static final WyspiaExpressMapEffect MAP_EFFECT =
            new WyspiaExpressMapEffect(Identifier.of(MOD_ID, "train"));

    public final List<Item> NO_DESPAWN_ITEMS = List.of(
             ModItems.MASTER_KEY, WatheItems.REVOLVER
    );
    public static int TICK = 0;
    public static long ROUND_TICKS = -1;
    @Override
    public void onInitialize() {
        LOGGER.info("WyspiaExpress is initializing...");
        GameConfigs.initialize();
        ConfigSync.initializeServer();
        WatheGameModes.registerGameMode(MODDED_GAMEMODE.identifier, MODDED_GAMEMODE);
        WatheGameModes.registerGameMode(KILLER_MODDED_GAMEMODE.identifier, KILLER_MODDED_GAMEMODE);
        WatheGameModes.registerGameMode(TRUE_NEUTRAL_MODDED_GAMEMODE.identifier, TRUE_NEUTRAL_MODDED_GAMEMODE);
        WatheGameModes.registerGameMode(ALTERNATE_GAMEMODE.identifier, ALTERNATE_GAMEMODE);
        WatheMapEffects.registerMapEffect(MAP_EFFECT.identifier, MAP_EFFECT);
        WyspiaExpressRoles.init();
        WyspiaExpressItems.init();
        InvisibleArmor.initialize();
        WyspiaExpressSounds.init();
        WyspiaExpressEntities.init();
        WyspiaExpressGameFunctions.init();
        WyspiaExpressCommands.init();
        ParticleManager.init();
        registerVersionCheck();
        registerPackets();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> ROUND_TICKS = -1);
        GameEvents.ON_GAME_START.register(gameMode -> ROUND_TICKS = 0);
        GameEvents.ON_FINISH_INITIALIZE.register((world, gameWorld) ->
                LOGGER.info("Round started: gamemode {} and map effect {}.",
                        gameWorld.getGameMode().identifier, gameWorld.getMapEffect().identifier));
        GameEvents.ON_GAME_STOP.register(gameMode -> {
            if (ROUND_TICKS >= 0) {
                LOGGER.info("Round has finished after {}.", GameUtil.formatRoundTime(ROUND_TICKS));
            }
        });
        ServerLifecycleEvents.SERVER_STARTING.register(RoleStatisticsManager::init);
        ServerLifecycleEvents.SERVER_STARTING.register(RoleCategoryStatisticsManager::init);
        ServerLifecycleEvents.SERVER_STARTING.register(RoleDraftStatisticsManager::init);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> RoleStatisticsManager.shutdown());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> RoleCategoryStatisticsManager.shutdown());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> RoleDraftStatisticsManager.shutdown());
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (GameWorldComponent.KEY.get(server.getOverworld()).getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
                ++ROUND_TICKS;
            }
            if(TICK >= 1200)
                TICK = 0;
            else{
                TICK++;
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof ItemEntity itemEntity)) {
                return;
            }
            if(!GameWorldComponent.KEY.get(world).isRunning())
                return;
            var itemStack = itemEntity.getStack();

            if (NO_DESPAWN_ITEMS.stream().anyMatch(itemStack::isOf)) {
                itemEntity.setNeverDespawn();
            }

        });
        LOGGER.info("WyspiaExpress finished initializing.");
    }

    private void registerVersionCheck() {
        // 1) When login queries start, send server version to the client
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            String serverVersion = VersionUtil.getVersion();
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
                                "You must install " + WyspiaExpress.MOD_ID + " version " + VersionUtil.getVersion() +  " to join this server."));
                        return;
                    }

                    String clientVersion = buf.readString(64);
                    String serverVersion = VersionUtil.getVersion();
                    if (!clientVersion.equals(serverVersion)) {
                        handler.disconnect(Text.literal(
                                "Incompatible " + WyspiaExpress.MOD_ID + " version.\n" +
                                        "Server: " + serverVersion + ", Client: " + clientVersion));
                    }
                }
        );

        String forceCrawlVersion = VersionUtil.getForceCrawlVersion();
        if (!forceCrawlVersion.equals("UNKNOWN")) {
            ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
                String serverVersion = VersionUtil.getForceCrawlVersion();
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(serverVersion);
                sender.sendPacket(VersionCheckNetwork.VERSION_QUERY_FORCE_CRAWL_ID, buf);
            });
            ServerLoginNetworking.registerGlobalReceiver(
                    VersionCheckNetwork.VERSION_QUERY_FORCE_CRAWL_ID,
                    (server, handler, understood, buf, synchronizer, responseSender) -> {
                        if (!understood) {
                            handler.disconnect(Text.literal(
                                    "You must install " + "ForceCrawl"+ " version " + VersionUtil.getForceCrawlVersion() +  " to join this server."));
                            return;
                        }

                        String clientVersion = buf.readString(64);
                        String serverVersion = VersionUtil.getForceCrawlVersion();
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
        PlayerConfigC2SPacket.register();
        LichReviveC2SPacket.register();
        CultLeaderReviveC2SPacket.register();
        OutlawRevolverC2SPacket.register();
        NoTargetAbilityC2SPacket.register();
        RolePickC2SPacket.register();
        GuessC2SPacket.register();
        RitualDaggerC2SPacket.register();
    }
}
