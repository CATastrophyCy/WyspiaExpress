package org.cat.express.wyspiaexpress.client.guidebook;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.cat.express.wyspiaexpress.WyspiaExpress;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;

/** Local presentation preferences, independent of server configuration and world state. */
final class GuidebookPreferences {
    private static GuidebookPreferences instance;
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("wyspiaexpress-guidebook.json");
    boolean transparent = true, navigationSaved, mine, copycat;
    GuidebookCatalog.Availability availability = GuidebookCatalog.Availability.ENABLED;
    GuidebookCatalog.Availability savedAvailability = GuidebookCatalog.Availability.ENABLED;
    String query = "", savedQuery = "", selected = "";
    final EnumSet<GuidebookEntry.Category> collapsed = EnumSet.noneOf(GuidebookEntry.Category.class);

    static GuidebookPreferences get() {
        if (instance == null) instance = load(FILE);
        return instance;
    }

    static GuidebookPreferences load(Path file) {
        var preferences = new GuidebookPreferences();
        if (!Files.exists(file)) return preferences;
        try (var reader = Files.newBufferedReader(file)) {
            var data = JsonParser.parseReader(reader).getAsJsonObject();
            if (data.get("version").getAsInt() != 1) return preferences;
            preferences.transparent = bool(data, "transparent", true);
            preferences.navigationSaved = bool(data, "navigationSaved", false);
            // Version-1 files may contain the removed Now filter; ignore its fields.
            preferences.mine = bool(data, "mine", false);
            preferences.copycat = !preferences.mine && bool(data, "copycat", false);
            preferences.availability = availability(data, "availability");
            preferences.savedAvailability = availability(data, "savedAvailability");
            preferences.query = string(data, "query"); preferences.savedQuery = string(data, "savedQuery");
            preferences.selected = string(data, "selected");
            if (data.has("collapsed")) for (var category : data.getAsJsonArray("collapsed")) {
                try { preferences.collapsed.add(GuidebookEntry.Category.valueOf(category.getAsString())); }
                catch (IllegalArgumentException ignored) { /* Ignore categories removed by a future version. */ }
            }
        } catch (Exception error) {
            WyspiaExpress.LOGGER.warn("Cannot read local guidebook preferences: {}", error.getMessage());
            return new GuidebookPreferences();
        }
        return preferences;
    }

    void save() { save(FILE); }
    void save(Path file) {
        try {
            var data = new JsonObject(); data.addProperty("version", 1);
            data.addProperty("transparent", transparent); data.addProperty("navigationSaved", navigationSaved);
            data.addProperty("availability", availability.name());
            data.addProperty("mine", mine); data.addProperty("copycat", copycat);
            data.addProperty("savedAvailability", savedAvailability.name());
            data.addProperty("query", query); data.addProperty("savedQuery", savedQuery); data.addProperty("selected", selected);
            var categories = new com.google.gson.JsonArray(); collapsed.forEach(category -> categories.add(category.name()));
            data.add("collapsed", categories);
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().create().toJson(data) + "\n");
            try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (Exception error) {
            WyspiaExpress.LOGGER.warn("Cannot save local guidebook preferences: {}", error.getMessage());
        }
    }

    private static boolean bool(JsonObject data, String key, boolean fallback) { return data.has(key) ? data.get(key).getAsBoolean() : fallback; }
    private static String string(JsonObject data, String key) { return data.has(key) ? data.get(key).getAsString() : ""; }
    private static GuidebookCatalog.Availability availability(JsonObject data, String key) {
        try { return GuidebookCatalog.Availability.valueOf(string(data, key)); }
        catch (IllegalArgumentException ignored) { return GuidebookCatalog.Availability.ENABLED; }
    }
}
