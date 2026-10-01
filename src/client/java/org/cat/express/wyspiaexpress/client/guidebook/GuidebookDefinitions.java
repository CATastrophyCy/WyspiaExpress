package org.cat.express.wyspiaexpress.client.guidebook;

import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class GuidebookDefinitions implements SimpleSynchronousResourceReloadListener {
    public static final GuidebookDefinitions INSTANCE = new GuidebookDefinitions();
    private Map<String, GuidebookDefinition> definitions = Map.of();
    private int generation;

    private GuidebookDefinitions() {}

    @Override public Identifier getFabricId() { return Identifier.of(WyspiaExpress.MOD_ID, "guidebook"); }

    @Override public void reload(ResourceManager manager) {
        Map<String, GuidebookDefinition> loaded = new HashMap<>();
        for (var entry : manager.findResources("guidebook", id -> id.getPath().endsWith(".json")).entrySet()) {
            Identifier resource = entry.getKey();
            String path = resource.getPath();
            String prefix;
            String kind;
            if (path.startsWith("guidebook/roles/")) { prefix = "guidebook/roles/"; kind = "role"; }
            else if (path.startsWith("guidebook/modifiers/")) { prefix = "guidebook/modifiers/"; kind = "modifier"; }
            else continue;
            try (Reader reader = entry.getValue().getReader()) {
                Identifier id = Identifier.of(resource.getNamespace(), path.substring(prefix.length(), path.length() - 5));
                loaded.put(kind + ":" + id, new GuidebookDefinition(JsonParser.parseReader(reader).getAsJsonObject(), resource.toString()));
            } catch (Exception error) {
                WyspiaExpress.LOGGER.warn("Cannot load guidebook definition {}: {}", resource, error.getMessage());
            }
        }
        definitions = Map.copyOf(loaded);
        generation++;
    }

    public GuidebookDefinition get(GuidebookEntry entry) { return definitions.getOrDefault(entry.key(), GuidebookDefinition.EMPTY); }
    public int generation() { return generation; }
}
