package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import com.google.gson.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Reads authored JSON5 without saving resolved profiles back over sparse overrides. */
final class ConfigFiles {
    private static final Jankson JSON5 = Jankson.builder().build();
    private static final Set<String> OVERRIDABLE = Set.of("rules", "tasks", "roles", "items", "modifiers", "mode");
    private ConfigFiles() {}

    static Map<String, Map<String, Object>> load(Path directory, ConfigSchema schema) {
        try {
            Files.createDirectories(directory);
            Map<String, JsonElement> defaults = schema.defaults();
            var shared = new TreeMap<>(defaults);
            for (String domain : schema.domains()) {
                if (domain.equals("mode")) continue;
                Path file = directory.resolve(domain + ".json5");
                if (Files.exists(file)) schema.overlay(domain, read(file), shared, file.toString());
            }
            // Validate the current format before generating missing files.
            var profiles = new TreeMap<String, Map<String, Object>>();
            profiles.put("shared", schema.decodeComplete(shared, directory.toString()));
            Path modes = directory.resolve("gamemodes");
            Set<String> modeIds = new TreeSet<>(List.of("wyspiaexpress:killer_modded", "wyspiaexpress:true_neutral_modded"));
            if (Files.isDirectory(modes)) try (var namespaces = Files.list(modes)) {
                for (Path namespace : namespaces.filter(Files::isDirectory).toList()) try (var entries = Files.walk(namespace)) {
                    for (Path entry : entries.filter(Files::isDirectory).filter(path -> !path.equals(namespace)).toList()) {
                        boolean authored;
                        try (var files = Files.list(entry)) {
                            var children = files.toList();
                            authored = children.isEmpty() || children.stream().anyMatch(path -> path.getFileName().toString().endsWith(".json5"));
                        }
                        if (authored) modeIds.add(namespace.getFileName() + ":" + namespace.relativize(entry).toString().replace(java.io.File.separatorChar, '/'));
                    }
                }
            }
            for (String mode : modeIds) {
                if (!ConfigBundle.validModeId(mode)) throw new IllegalArgumentException("Invalid config mode folder: " + mode);
                Path folder = modes.resolve(mode.replace(':', '/'));
                var resolved = new TreeMap<>(shared);
                if (Files.isDirectory(folder)) try (var files = Files.list(folder)) {
                    for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".json5")).toList()) {
                        String name = file.getFileName().toString().replaceFirst("\\.json5$", "");
                        if (!OVERRIDABLE.contains(name)) throw new IllegalArgumentException("Unsupported mode config: " + file);
                        if (name.equals("mode")) schema.overlayMode(mode, read(file), resolved, file.toString());
                        else schema.overlay(name, read(file), resolved, file.toString());
                    }
                }
                profiles.put(mode, schema.decodeComplete(resolved, folder.toString()));
            }
            for (String domain : schema.domains()) {
                if (domain.equals("mode")) continue;
                Path file = directory.resolve(domain + ".json5");
                if (!Files.exists(file)) Files.writeString(file, JSON5.toJson(schema.root(domain)).toJson(JsonGrammar.JANKSON), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }
            for (String mode : modeIds) {
                Path folder = modes.resolve(mode.replace(':', '/')); Files.createDirectories(folder);
                for (String domain : OVERRIDABLE) {
                    Path file = folder.resolve(domain + ".json5");
                    if (!Files.exists(file)) {
                        Files.writeString(file, "// Omitted settings inherit the shared configuration.\n{}\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                    }
                }
            }
            return Collections.unmodifiableMap(profiles);
        } catch (IOException e) { throw new IllegalStateException("Could not load Wyspia config directory " + directory, e); }
    }

    static JsonObject read(Path file) {
        try {
            return JsonParser.parseString(JSON5.load(file.toFile()).toJson(JsonGrammar.STRICT)).getAsJsonObject();
        } catch (Exception e) { throw new IllegalArgumentException("Invalid JSON5 in " + file + ": " + e.getMessage(), e); }
    }

    /** Local client files are disposable across releases; server files never use this reset path. */
    static void prepareClient(Path directory, ConfigSchema schema, String version) {
        Path marker = directory.resolve(".schema");
        String expected = version + "\n" + schema.fingerprint() + "\n";
        try {
            if (Files.exists(marker) && Files.readString(marker).equals(expected)) return;
            Files.createDirectories(directory);
            // Invalidate first so an interrupted reset is retried on the next launch.
            Files.deleteIfExists(marker);
            for (String domain : schema.domains()) {
                if (!domain.equals("mode")) Files.deleteIfExists(directory.resolve(domain + ".json5"));
            }
            Path modes = directory.resolve("gamemodes");
            if (Files.isDirectory(modes)) try (var files = Files.walk(modes)) {
                for (Path file : files.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".json5")).toList()) Files.delete(file);
            }
            load(directory, schema);
            Files.writeString(marker, expected, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not refresh Wyspia client configs in " + directory, e);
        }
    }
}
