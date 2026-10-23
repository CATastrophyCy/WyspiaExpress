package org.cat.express.wyspiaexpress.config;

import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Map;

/** Stable typed model nodes resolve leaves against the current side's cached profile. */
public final class ConfigValues {
    private static final Map<Object, Map<String, String>> KEYS = new IdentityHashMap<>();
    private ConfigValues() {}

    static void bind(Object owner, String field, String key) {
        var fields = KEYS.computeIfAbsent(owner, ignored -> new HashMap<>());
        String previous = fields.putIfAbsent(field, key);
        if (previous != null && !previous.equals(key))
            throw new IllegalArgumentException("Reuse of one model instance at " + previous + " and " + key + "; create separate model instances");
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object owner, String field, T defaultValue) {
        var fields = KEYS.get(owner);
        if (fields == null) return defaultValue;
        return (T) GameConfigs.value(fields.get(field));
    }
}
