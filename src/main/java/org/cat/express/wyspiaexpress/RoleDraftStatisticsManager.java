package org.cat.express.wyspiaexpress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Selection history for the new modes; legacy/HML reporting and weights remain independent. */
public final class RoleDraftStatisticsManager {
    public enum Pool { KILLER, TRUE_NEUTRAL, KILLER_ALIGNED_NEUTRAL, VIGILANTE }

    public record History(double expectedAssignments, long assignments) {
        public History {
            if (!Double.isFinite(expectedAssignments) || expectedAssignments < 0 || assignments < 0) {
                throw new IllegalArgumentException("Invalid role draft history");
            }
        }

        public double credit() { return expectedAssignments - assignments; }

        private History plus(History other) {
            return new History(expectedAssignments + other.expectedAssignments, assignments + other.assignments);
        }
    }

    private static final History EMPTY = new History(0, 0);
    private static RoleDraftStatisticsManager instance;
    private final Path file;
    private final Map<UUID, EnumMap<Pool, History>> histories = new HashMap<>();

    private RoleDraftStatisticsManager(MinecraftServer server) {
        file = server.getSavePath(WorldSavePath.ROOT).resolve("wyspia_player_stats")
                .resolve("player_role_draft_stats.json");
    }

    public static void init(MinecraftServer server) {
        instance = new RoleDraftStatisticsManager(server);
        instance.load();
    }

    public static RoleDraftStatisticsManager getInstance() { return instance; }

    public static void shutdown() {
        if (instance != null) instance.save();
        instance = null;
    }

    public History getHistory(UUID playerId, Pool pool) {
        var player = histories.get(playerId);
        return player == null ? EMPTY : player.getOrDefault(pool, EMPTY);
    }

    public double computeWeight(UUID playerId, Pool pool) {
        History history = getHistory(playerId, pool);
        if (WyspiaExpress.GAMEMODES_CONFIG.combineKillerAndTrueNeutralHistory()
                && (pool == Pool.KILLER || pool == Pool.TRUE_NEUTRAL)) {
            history = getHistory(playerId, Pool.KILLER).plus(getHistory(playerId, Pool.TRUE_NEUTRAL));
        }
        return WyspiaExpress.GAMEMODES_CONFIG.baseWeight() + Math.max(history.credit(), 0);
    }

    public void record(UUID playerId, Pool pool, double expectedShare, boolean assigned) {
        var player = histories.computeIfAbsent(playerId, ignored -> new EnumMap<>(Pool.class));
        History previous = player.getOrDefault(pool, EMPTY);
        player.put(pool, new History(previous.expectedAssignments + expectedShare,
                previous.assignments + (assigned ? 1 : 0)));
    }

    public void recordRound(List<ServerPlayerEntity> participants, GameWorldComponent gameWorld,
                            Pool mainPool, boolean alignedDraft) {
        if (participants.isEmpty()) return;
        var activePools = EnumSet.of(mainPool, Pool.VIGILANTE);
        if (alignedDraft) activePools.add(Pool.KILLER_ALIGNED_NEUTRAL);
        var assignedCounts = new EnumMap<Pool, Integer>(Pool.class);
        for (ServerPlayerEntity player : participants) {
            Pool assigned = assignedPool(gameWorld.getRole(player));
            if (assigned != null) assignedCounts.merge(assigned, 1, Integer::sum);
        }
        for (ServerPlayerEntity player : participants) {
            Pool assigned = assignedPool(gameWorld.getRole(player));
            for (Pool pool : activePools) {
                // Actual allocations account for rounded quotas, capacity and forced picks.
                // All participants earn their share, including those who opted out.
                double expectedShare = assignedCounts.getOrDefault(pool, 0) / (double) participants.size();
                record(player.getUuid(), pool, expectedShare, assigned == pool);
            }
        }
        save();
    }

    private static Pool assignedPool(Role role) {
        if (role == null) return null;
        if (WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) return Pool.KILLER_ALIGNED_NEUTRAL;
        if (role.canUseKiller()) return Pool.KILLER;
        if (role == WatheRoles.VIGILANTE) return Pool.VIGILANTE;
        if (WyspiaExpressRoles.TRUE_NEUTRALS.contains(role) && role != SERoles.AMNESIAC) return Pool.TRUE_NEUTRAL;
        return null;
    }

    public void reset() {
        histories.clear();
        save();
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            try (var writer = Files.newBufferedWriter(file)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(histories, writer);
            }
        } catch (IOException error) {
            WyspiaExpress.LOGGER.error("Failed to save role draft history", error);
        }
    }

    private void load() {
        if (!Files.exists(file)) return;
        try (var reader = Files.newBufferedReader(file)) {
            Map<UUID, EnumMap<Pool, History>> loaded = new Gson().fromJson(reader,
                    new TypeToken<Map<UUID, EnumMap<Pool, History>>>() {}.getType());
            if (loaded != null) histories.putAll(loaded);
        } catch (IOException | RuntimeException error) {
            WyspiaExpress.LOGGER.error("Failed to load role draft history", error);
        }
    }
}
