package org.cat.express.wyspiaexpress.config;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Atomic, self-contained transfer: schema + complete resolved profiles, verified before publication. */
public final class ConfigBundle {
    public static final int PROTOCOL = 1;
    public static final int MAX_BYTES = 2 * 1024 * 1024;
    // Leave room for the envelope inside Minecraft's login query packet limit.
    public static final int MAX_COMPRESSED_BYTES = 1_000_000;
    private final Map<String, Map<String, Object>> profiles;
    private final byte[] compressed;
    private final String digest;

    private ConfigBundle(Map<String, Map<String, Object>> profiles, byte[] compressed, String digest) {
        this.profiles = Collections.unmodifiableMap(new TreeMap<>(profiles));
        this.compressed = compressed.clone();
        this.digest = digest;
    }

    static boolean validModeId(String mode) {
        if (!mode.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) return false;
        return Arrays.stream(mode.substring(mode.indexOf(':') + 1).split("/", -1))
                .noneMatch(part -> part.isEmpty() || part.equals(".") || part.equals(".."));
    }

    public Map<String, Object> profile(String mode) { return profiles.getOrDefault(mode, profiles.get("shared")); }
    public String digest() { return digest; }
    public byte[] compressed() { return compressed.clone(); }
    public Set<String> modes() { return profiles.keySet(); }

    static ConfigBundle fromProfiles(Map<String, Map<String, Object>> profiles, ConfigSchema schema) {
        var json = new JsonObject();
        json.addProperty("protocol", PROTOCOL);
        json.addProperty("schema", schema.fingerprint());
        json.add("profiles", ConfigSchema.JSON.toJsonTree(profiles));
        byte[] raw = ConfigSchema.JSON.toJson(json).getBytes(StandardCharsets.UTF_8);
        if (raw.length > MAX_BYTES) throw new IllegalArgumentException("Wyspia configuration exceeds " + MAX_BYTES + " bytes");
        try {
            var bytes = new ByteArrayOutputStream();
            try (var gzip = new GZIPOutputStream(bytes)) { gzip.write(raw); }
            byte[] compressed = bytes.toByteArray();
            if (compressed.length > MAX_COMPRESSED_BYTES) throw new IllegalArgumentException("Compressed configuration exceeds the login packet budget");
            return new ConfigBundle(profiles, compressed, ConfigSchema.hash(raw));
        } catch (IOException e) { throw new IllegalStateException(e); }
    }

    public static ConfigBundle decode(byte[] compressed, String expectedDigest, ConfigSchema schema) {
        if (compressed.length > MAX_COMPRESSED_BYTES) throw new IllegalArgumentException("Compressed configuration too large");
        try {
            byte[] raw;
            try (var gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                raw = gzip.readNBytes(MAX_BYTES + 1);
                if (raw.length > MAX_BYTES) throw new IllegalArgumentException("Expanded configuration too large");
            }
            String digest = ConfigSchema.hash(raw);
            if (!digest.equals(expectedDigest)) throw new IllegalArgumentException("Configuration content checksum mismatch");
            var object = JsonParser.parseString(new String(raw, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!object.keySet().equals(Set.of("protocol", "schema", "profiles"))) throw new IllegalArgumentException("Unexpected configuration envelope");
            if (object.get("protocol").getAsInt() != PROTOCOL) throw new IllegalArgumentException("Configuration protocol mismatch");
            if (!schema.fingerprint().equals(object.get("schema").getAsString())) throw new IllegalArgumentException("Configuration schema mismatch; install the matching WyspiaExpress build");
            var jsonProfiles = object.getAsJsonObject("profiles");
            if (!jsonProfiles.has("shared") || jsonProfiles.size() > 128) throw new IllegalArgumentException("Missing shared profile or too many profiles");
            var profiles = new TreeMap<String, Map<String, Object>>();
            for (var entry : jsonProfiles.entrySet()) {
                if (!entry.getKey().equals("shared") && !validModeId(entry.getKey())) throw new IllegalArgumentException("Invalid mode ID in config");
                var values = new TreeMap<String, JsonElement>();
                entry.getValue().getAsJsonObject().entrySet().forEach(e -> values.put(e.getKey(), e.getValue()));
                profiles.put(entry.getKey(), schema.decodeComplete(values, "server profile " + entry.getKey()));
            }
            return new ConfigBundle(profiles, compressed, digest);
        } catch (IOException | IllegalStateException | NullPointerException e) {
            throw new IllegalArgumentException("Malformed configuration snapshot", e);
        }
    }
}
