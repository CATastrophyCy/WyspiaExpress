package org.cat.express.wyspiaexpress.config;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.BooleanSupplier;

/** Owns immutable snapshots. Server and client state are independent even in singleplayer. */
public final class GameConfigs {
    private static final Map<String, Object> MODE_SETTINGS = new LinkedHashMap<>();
    private static ConfigSchema schema;
    private static ConfigBundle defaults;
    private static volatile ServerState serverState;
    private static volatile ConfigBundle clientState;
    private static volatile String lastServerMode;
    private static BooleanSupplier clientThread = () -> false;
    private static Supplier<String> clientMode = () -> "shared";
    private GameConfigs() {}

    /** Register a new mode's typed settings from its common constructor, before config initialization. */
    public static <T extends ConfigView<?>> T registerModeSettings(net.minecraft.util.Identifier id, T view) {
        if (schema != null) throw new IllegalStateException("Register mode settings before GameConfigs.initialize()");
        if (MODE_SETTINGS.putIfAbsent(id.toString(), Objects.requireNonNull(view).model()) != null)
            throw new IllegalArgumentException("Duplicate mode settings: " + id);
        return view;
    }

    public static void initialize() {
        if (schema != null) throw new IllegalStateException("Wyspia configs already initialized");
        var roots = new LinkedHashMap<String, Object>();
        roots.put("general", WyspiaExpress.GENERAL_CONFIG.model());
        roots.put("server", WyspiaExpress.SERVER_CONFIG.model());
        roots.put("rules", WyspiaExpress.RULES_CONFIG.model());
        roots.put("tasks", WyspiaExpress.TASKS_CONFIG.model());
        roots.put("gamemodes", WyspiaExpress.GAMEMODES_CONFIG.model());
        roots.put("roles", WyspiaExpress.ROLES_CONFIG.model());
        roots.put("items", WyspiaExpress.ITEMS_CONFIG.model());
        roots.put("modifiers", WyspiaExpress.MODIFIERS_CONFIG.model());
        roots.put("mode", WyspiaExpress.MODE_CONFIG.model());
        schema = new ConfigSchema(roots);
        MODE_SETTINGS.forEach(schema::registerModeSettings);
        defaults = ConfigBundle.fromProfiles(Map.of("shared", schema.decodeComplete(schema.defaults(), "Java defaults")), schema);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            var profiles = ConfigFiles.load(FabricLoader.getInstance().getConfigDir().resolve("wyspiaexpress"), schema);
            serverState = new ServerState(server, ConfigBundle.fromProfiles(profiles, schema));
            refreshDerivedValues();
            WyspiaExpress.LOGGER.info("Loaded Wyspia configuration: {} profiles, schema {}", profiles.size(), schema.fingerprint());
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            var state = serverState;
            if (state != null && state.server == server) { serverState = null; lastServerMode = null; }
        });
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            var state = serverState;
            if (state == null || state.server != server) return;
            var world = server.getOverworld();
            if (world == null) return;
            String mode = GameWorldComponent.KEY.get(world).getGameMode().identifier.toString();
            if (!mode.equals(lastServerMode)) { lastServerMode = mode; refreshDerivedValues(); }
        });
    }

    public static ConfigSchema schema() { return schema; }

    public static void initializeClientFiles() {
        ConfigFiles.prepareClient(FabricLoader.getInstance().getConfigDir().resolve("wyspiaexpress"),
                schema, org.cat.express.wyspiaexpress.util.VersionUtil.getVersion());
    }


    public static void refreshDerivedValues() {
        // In singleplayer the native static table is shared. Let the server thread own it.
        var state = serverState;
        if (state == null || state.server.isOnThread()) {
            WyspiaExpressItems.registerItemsCooldown();
            WyspiaExpressRoles.registerRoleLimit();
            org.cat.express.wyspiaexpress.shop.ShopUtil.FUN_BOX_RARE_POOL.clear();
            org.cat.express.wyspiaexpress.shop.ShopUtil.FUN_BOX_NORMAL_POOL.clear();
        }
    }

    public static Object value(String key) {
        var state = serverState;
        Map<String, Object> values;
        if (state != null && (state.server.isOnThread() || !clientThread.getAsBoolean())) {
            String mode = lastServerMode == null ? "shared" : lastServerMode;
            if (state.server.isOnThread()) {
                var world = state.server.getOverworld();
                mode = world == null ? "shared" : GameWorldComponent.KEY.get(world).getGameMode().identifier.toString();
            }
            values = state.bundle.profile(mode);
        } else {
            var remote = clientState;
            values = remote == null ? defaults.profile("shared") : remote.profile(clientMode.get());
        }
        if (!values.containsKey(key)) throw new IllegalArgumentException("Unknown Wyspia configuration setting: " + key);
        return values.get(key);
    }

    public static ConfigBundle serverBundle() {
        var state = serverState;
        if (state == null) throw new IllegalStateException("Server configuration is not initialized");
        return state.bundle;
    }

    public static void installClient(ConfigBundle bundle) { clientState = Objects.requireNonNull(bundle); }
    public static boolean hasClientSnapshot() { return clientState != null; }
    public static void clearClient() { clientState = null; }
    public static void setClientThread(BooleanSupplier predicate) { clientThread = Objects.requireNonNull(predicate); }
    public static void setClientMode(Supplier<String> supplier) { clientMode = Objects.requireNonNull(supplier); }

    public static int killerDividend(GameWorldComponent game) {
        int value = WyspiaExpress.MODE_CONFIG.killerDividend();
        return value == -1 ? game.getKillerDividend() : value;
    }
    public static int vigilanteDividend(GameWorldComponent game) {
        int value = WyspiaExpress.MODE_CONFIG.vigilanteDividend();
        return value == -1 ? game.getVigilanteDividend() : value;
    }

    private record ServerState(MinecraftServer server, ConfigBundle bundle) {}
}
