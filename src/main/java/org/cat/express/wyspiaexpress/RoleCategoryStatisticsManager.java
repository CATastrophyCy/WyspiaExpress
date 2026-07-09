package org.cat.express.wyspiaexpress;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class RoleCategoryStatisticsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WyspiaExpress.MOD_ID + "/player_role_category_stats");
    private static RoleCategoryStatisticsManager INSTANCE;

    private final Path statsDir;
    private final Map<UUID, MutablePlayerStats> playerStats = new HashMap<>();
    private LocalDate currentDate;

    public enum RoleCategory {
        KILLER, VIGILANTE, NEUTRAL, CIVILIAN
    }

    private RoleCategoryStatisticsManager(MinecraftServer server) {
        Path worldRoot = server.getSavePath(WorldSavePath.ROOT);
        this.statsDir = worldRoot.resolve("wyspia_player_stats");
        this.currentDate = LocalDate.now(ZoneId.systemDefault());

        try {
            java.nio.file.Files.createDirectories(statsDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create player stats directory", e);
        }
    }

    public static void init(MinecraftServer server) {
        INSTANCE = new RoleCategoryStatisticsManager(server);
        INSTANCE.load();
        LOGGER.info("Player role category statistics initialized. Using file: {}", INSTANCE.getFile().getAbsolutePath());
    }

    public static void shutdown() {
        if (INSTANCE != null) {
            INSTANCE.save();
            LOGGER.info("Player role category statistics saved. Total players tracked: {}", INSTANCE.playerStats.size());
            INSTANCE = null;
        }
    }

    public static RoleCategoryStatisticsManager getInstance() {
        return INSTANCE;
    }

    /**
     * Direct way to register a role-category assignment by UUID.
     */
    public void recordRoleCategory(@NotNull UUID playerUuid,@NotNull String playerName,@NotNull RoleCategory roleCategory) {

        MutablePlayerStats stats = playerStats.computeIfAbsent(playerUuid, ignored -> new MutablePlayerStats(playerUuid));

        stats.lastKnownName = playerName;
        if (!stats.namesUsed.contains(playerName)) {
            stats.namesUsed.add(playerName);
        }
        switch (roleCategory) {
            case VIGILANTE -> stats.vigilanteCount++;
            case KILLER -> stats.killerCount++;
            case CIVILIAN -> stats.civilianCount++;
            case NEUTRAL -> stats.neutralCount++;
        }
    }
    public PlayerAggregateStats getStats(@NotNull UUID playerUuid) {
        MutablePlayerStats stats = playerStats.get(playerUuid);
        return stats == null ? new PlayerAggregateStats(playerUuid, null, null,
                0, 0, 0, 0, 0, 0, 0, 0, 0)
                : stats.toImmutable();
    }

    public double computeCategoryWeight(int dividend,@NotNull UUID playerUuid,@NotNull RoleCategory roleCategory) {
        PlayerAggregateStats stats = getStats(playerUuid);
        double target = 1.0 / dividend;
        double percentage = switch (roleCategory){
            case VIGILANTE -> stats.vigilantePercentage;
            case KILLER -> stats.killerPercentage;
            case NEUTRAL -> stats.neutralPercentage;
            case CIVILIAN -> stats.civilianPercentage;
        };
        double deficit = Math.max(target - percentage, 0.0);
        return WyspiaExpress.SERVER_CONFIG.baseWeight() + deficit;
    }
    private File getFile() {
        String fileName = String.format("player_role_category_stats_%s.json", currentDate.toString());
        return statsDir.resolve(fileName).toFile();
    }

    public void save() {
        File file = getFile();

        JsonObject root = new JsonObject();
        root.addProperty("date", currentDate.toString());

        List<PlayerAggregateStats> exported = playerStats.values().stream()
                .map(MutablePlayerStats::toImmutable)
                .sorted(Comparator.comparing(PlayerAggregateStats::roundsPlayed).reversed()
                        .thenComparing(PlayerAggregateStats::lastKnownName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        root.add("players", new Gson().toJsonTree(exported));

        JsonObject summary = new JsonObject();
        summary.addProperty("totalPlayers", playerStats.size());
        int totalRounds = exported.stream().mapToInt(PlayerAggregateStats::roundsPlayed).max().orElse(0);
        summary.addProperty("totalRounds", totalRounds);

        root.add("summary", summary);

        try (Writer writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save player role category statistics", e);
        }
    }

    public void load() {
        File file = getFile();
        if (!file.exists()) {
            LOGGER.info("No prior stats for {}. Starting fresh.", currentDate);
            return;
        }

        try (Reader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray playersArray = root.getAsJsonArray("players");

            if (playersArray != null) {
                Type listType = new TypeToken<List<PlayerAggregateStats>>() {}.getType();
                List<PlayerAggregateStats> loaded = new Gson().fromJson(playersArray, listType);

                if (loaded != null) {
                    for (PlayerAggregateStats entry : loaded) {
                        MutablePlayerStats mutable = new MutablePlayerStats(entry.playerUuid());
                        mutable.lastKnownName = entry.lastKnownName();
                        mutable.namesUsed.addAll(entry.namesUsed());
                        mutable.vigilanteCount = entry.vigilanteCount();
                        mutable.killerCount = entry.killerCount();
                        mutable.civilianCount = entry.civilianCount();
                        mutable.neutralCount = entry.neutralCount();
                        playerStats.put(entry.playerUuid(), mutable);
                    }
                }
            }

            LOGGER.info("Loaded stats for {} players for {}.", playerStats.size(), currentDate);
        } catch (Exception e) {
            LOGGER.error("Failed to load player role category statistics", e);
        }
    }

    private static class MutablePlayerStats {
        private final UUID playerUuid;
        private String lastKnownName;
        private final List<String> namesUsed = new ArrayList<>();
        private int vigilanteCount;
        private int killerCount;
        private int civilianCount;
        private int neutralCount;

        private MutablePlayerStats(UUID playerUuid) {
            this.playerUuid = playerUuid;
        }

        private PlayerAggregateStats toImmutable() {
            int total = vigilanteCount + killerCount + civilianCount + neutralCount;

            double vigilantePct = total == 0 ? 0.0 : (vigilanteCount * 100.0) / total;
            double killerPct = total == 0 ? 0.0 : (killerCount * 100.0) / total;
            double civilianPct = total == 0 ? 0.0 : (civilianCount * 100.0) / total;
            double neutralPct = total == 0 ? 0.0 : (neutralCount * 100.0) / total;
            return new PlayerAggregateStats(
                    playerUuid,
                    lastKnownName,
                    new ArrayList<>(namesUsed),
                    total,
                    vigilanteCount,
                    killerCount,
                    civilianCount,
                    neutralCount,
                    vigilantePct,
                    killerPct,
                    neutralPct,
                    civilianPct
            );
        }
    }

    public record PlayerAggregateStats(
            @NotNull UUID playerUuid,
            @Nullable String lastKnownName,
            @Nullable List<String> namesUsed,
            int roundsPlayed,
            int vigilanteCount,
            int killerCount,
            int civilianCount,
            int neutralCount,
            double vigilantePercentage,
            double killerPercentage,
            double neutralPercentage,
            double civilianPercentage
    ) {
        public PlayerAggregateStats {
            namesUsed = namesUsed == null ? List.of() : List.copyOf(namesUsed);
        }
    }
}