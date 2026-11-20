package org.cat.express.wyspiaexpress;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.cat.express.wyspiaexpress.util.RoleUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.fazeclan.river.stupid_express.constants.SERoles;

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

    public void recordRoundRole(ServerPlayerEntity player, Role role) {
        MutablePlayerStats stats = getPlayerStats(player);
        stats.rolesReceived.add(RoleUtil.getRoleId(role));
        if (role == SERoles.AMNESIAC) {
            stats.amnesiacCount++;
        } else if (WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) {
            stats.killerAlignedNeutralCount++;
        } else if (role.canUseKiller()) {
            stats.killerCount++;
        } else if (role == WatheRoles.VIGILANTE) {
            stats.vigilanteCount++;
        } else if (role.isInnocent() || role == WyspiaExpressRoles.LOVER_2) {
            stats.civilianCount++;
        } else {
            stats.trueNeutralCount++;
        }
    }

    private MutablePlayerStats getPlayerStats(ServerPlayerEntity player) {
        MutablePlayerStats stats = playerStats.computeIfAbsent(player.getUuid(), MutablePlayerStats::new);
        String playerName = player.getName().getString();
        stats.lastKnownName = playerName;
        if (!stats.namesUsed.contains(playerName)) {
            stats.namesUsed.add(playerName);
        }
        return stats;
    }

    public PlayerAggregateStats getStats(@NotNull UUID playerUuid) {
        MutablePlayerStats stats = playerStats.get(playerUuid);
        return (stats == null ? new MutablePlayerStats(playerUuid) : stats).toImmutable();
    }

    public double computeCategoryWeight(int dividend, @NotNull UUID playerUuid, @NotNull RoleCategory roleCategory) {
        MutablePlayerStats stats = playerStats.get(playerUuid);
        double frequency = 0;
        if (stats != null && stats.roundsPlayed() > 0) {
            int count = switch (roleCategory) {
                case VIGILANTE -> stats.vigilanteCount;
                case KILLER -> stats.killerCount;
                case CIVILIAN -> stats.civilianCount;
                case NEUTRAL -> stats.legacyNeutralCount + stats.killerAlignedNeutralCount
                        + stats.trueNeutralCount;
            };
            frequency = count / (double) stats.roundsPlayed();
        }
        return WyspiaExpress.GAMEMODES_CONFIG.baseWeight() + Math.max(1.0 / dividend - frequency, 0.0);
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
                        mutable.rolesReceived.addAll(entry.rolesReceived());
                        mutable.vigilanteCount = entry.vigilanteCount();
                        mutable.killerCount = entry.killerCount();
                        mutable.civilianCount = entry.civilianCount();
                        mutable.killerAlignedNeutralCount = entry.killerAlignedNeutralCount();
                        mutable.trueNeutralCount = entry.trueNeutralCount();
                        mutable.amnesiacCount = entry.amnesiacCount();
                        mutable.legacyNeutralCount = entry.legacyNeutralCount();
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
        private final List<String> rolesReceived = new ArrayList<>();
        private int vigilanteCount;
        private int killerCount;
        private int civilianCount;
        private int killerAlignedNeutralCount;
        private int trueNeutralCount;
        private int amnesiacCount;
        private int legacyNeutralCount;

        private MutablePlayerStats(UUID playerUuid) {
            this.playerUuid = playerUuid;
        }

        private int roundsPlayed() {
            return killerCount + vigilanteCount + civilianCount + killerAlignedNeutralCount
                    + trueNeutralCount + amnesiacCount + legacyNeutralCount;
        }

        private PlayerAggregateStats toImmutable() {
            int total = roundsPlayed();
            return new PlayerAggregateStats(
                    playerUuid,
                    lastKnownName,
                    namesUsed,
                    total,
                    killerCount,
                    vigilanteCount,
                    civilianCount,
                    killerAlignedNeutralCount,
                    trueNeutralCount,
                    amnesiacCount,
                    legacyNeutralCount,
                    percentage(killerCount, total),
                    percentage(vigilanteCount, total),
                    percentage(civilianCount, total),
                    percentage(killerAlignedNeutralCount, total),
                    percentage(trueNeutralCount, total),
                    percentage(amnesiacCount, total),
                    rolesReceived
            );
        }
    }

    private static double percentage(int count, int total) {
        return total == 0 ? 0 : count * 100.0 / total;
    }

    public record PlayerAggregateStats(
            @NotNull UUID playerUuid,
            @Nullable String lastKnownName,
            @Nullable List<String> namesUsed,
            int roundsPlayed,
            int killerCount,
            int vigilanteCount,
            int civilianCount,
            int killerAlignedNeutralCount,
            int trueNeutralCount,
            int amnesiacCount,
            @SerializedName(value = "legacyNeutralCount", alternate = "neutralCount") int legacyNeutralCount,
            double killerPercentage,
            double vigilantePercentage,
            double civilianPercentage,
            double killerAlignedNeutralPercentage,
            double trueNeutralPercentage,
            double amnesiacPercentage,
            List<String> rolesReceived
    ) {
        public PlayerAggregateStats {
            namesUsed = namesUsed == null ? List.of() : List.copyOf(namesUsed);
            rolesReceived = rolesReceived == null ? List.of() : List.copyOf(rolesReceived);
        }
    }
}
