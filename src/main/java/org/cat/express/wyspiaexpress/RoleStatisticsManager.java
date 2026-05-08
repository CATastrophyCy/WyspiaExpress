package org.cat.express.wyspiaexpress;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class RoleStatisticsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WyspiaExpress.MOD_ID + "/role_stats");
    private static RoleStatisticsManager INSTANCE;

    private final Path statsDir;
    private final List<RoleSelectionEvent> eventLog = new ArrayList<>();
    private LocalDate currentDate;

    private RoleStatisticsManager(MinecraftServer server) {
        Path worldRoot = server.getSavePath(WorldSavePath.ROOT);
        this.statsDir = worldRoot.resolve("wyspia_stats");
        this.currentDate = LocalDate.now(ZoneId.systemDefault());

        try {
            java.nio.file.Files.createDirectories(statsDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create stats directory", e);
        }
    }

    public static void init(MinecraftServer server) {
        INSTANCE = new RoleStatisticsManager(server);
        INSTANCE.load();
        LOGGER.info("Role statistics initialized. Using file: {}", INSTANCE.getFile().getAbsolutePath());
    }

    public static void shutdown() {
        if (INSTANCE != null) {
            INSTANCE.save();
            LOGGER.info("Role statistics saved. Total events logged: {}", INSTANCE.eventLog.size());
            INSTANCE = null;
        }
    }

    public static RoleStatisticsManager getInstance() {
        return INSTANCE;
    }

    public void recordSelection(UUID playerUuid, String playerName, String selectedRole, List<String> availableChoices) {
        RoleSelectionEvent event = new RoleSelectionEvent(
                Instant.now().toEpochMilli(),
                playerUuid,
                playerName,
                selectedRole,
                new ArrayList<>(availableChoices)
        );
        eventLog.add(event);
        /*
        LOGGER.info(
                "[RolePick] UUID={} Name={} Selected={} Choices={}",
                playerUuid, playerName, selectedRole, availableChoices
        );
        */

    }

    private File getFile() {
        String fileName = String.format("role_events_%s.json", currentDate.toString());
        return statsDir.resolve(fileName).toFile();
    }

    public void save() {
        File file = getFile();

        JsonObject root = new JsonObject();
        root.addProperty("date", currentDate.toString());
        root.add("events", new Gson().toJsonTree(eventLog));

        Map<String, Integer> frequency = new HashMap<>();
        for (RoleSelectionEvent event : eventLog) {
            frequency.merge(event.selectedRole(), 1, Integer::sum);
        }

        JsonObject summary = new JsonObject();
        JsonObject freqJson = new JsonObject();
        frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEachOrdered(e -> freqJson.addProperty(e.getKey(), e.getValue()));
        summary.add("roleFrequency", freqJson);
        summary.addProperty("totalSelections", eventLog.size());
        long uniquePlayers = eventLog.stream().map(RoleSelectionEvent::playerUuid).distinct().count();
        summary.addProperty("uniquePlayers", uniquePlayers);

        root.add("summary", summary);

        try (Writer writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save role statistics", e);
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
            JsonArray eventsArray = root.getAsJsonArray("events");

            if (eventsArray != null) {
                Type listType = new TypeToken<List<RoleSelectionEvent>>(){}.getType();
                List<RoleSelectionEvent> loaded = new Gson().fromJson(eventsArray, listType);
                if (loaded != null) {
                    eventLog.addAll(loaded);
                }
            }

            LOGGER.info("Loaded {} prior events for {}.", eventLog.size(), currentDate);
        } catch (Exception e) {
            LOGGER.error("Failed to load role statistics", e);
        }
    }

    private record RoleSelectionEvent(long timestamp, UUID playerUuid, String playerName,
                                      String selectedRole, List<String> availableChoices) {}
}