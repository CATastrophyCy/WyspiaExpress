package org.cat.express.wyspiaexpress.config;

import com.google.gson.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** One schema drives file validation, typed decoding, guidebook paths and network verification. */
public final class ConfigSchema {
    static final Gson JSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Map<String, Leaf> leaves = new TreeMap<>();
    private final Map<String, Object> roots;
    private final Map<String, String> modeSettings = new HashMap<>();
    private final Map<String, Set<String>> branches = new HashMap<>();

    ConfigSchema(Map<String, Object> roots) {
        this.roots = Collections.unmodifiableMap(new LinkedHashMap<>(roots));
        roots.forEach((name, root) -> collect(name, root));
    }

    void registerModeSettings(String id, Object model) {
        if (!ConfigBundle.validModeId(id) || modeSettings.containsKey(id)) throw new IllegalArgumentException("Invalid or duplicate mode settings: " + id);
        String prefix = "modeSettings." + id;
        collect(prefix, model);
        if (!Collections.disjoint(branches.get("mode"), branches.get(prefix))) throw new IllegalArgumentException("Mode settings shadow common mode fields: " + id);
        modeSettings.put(id, prefix);
    }

    void overlayMode(String id, JsonObject input, Map<String, JsonElement> target, String source) {
        String prefix = modeSettings.get(id);
        var common = new JsonObject();
        var specific = new JsonObject();
        input.entrySet().forEach(entry -> {
            if (branches.get("mode").contains(entry.getKey())) common.add(entry.getKey(), entry.getValue());
            else if (prefix != null && branches.get(prefix).contains(entry.getKey())) specific.add(entry.getKey(), entry.getValue());
            else throw invalid(source, "mode." + entry.getKey(), "unknown setting for " + id);
        });
        walk("mode", common, target, source);
        if (prefix != null) walk(prefix, specific, target, source);
    }

    private void collect(String path, Object node) {
        branches.put(path, new HashSet<>());
        for (Field field : node.getClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
            String key = path + "." + field.getName();
            branches.get(path).add(field.getName());
            try {
                Object value = field.get(node);
                if (value == null) throw new IllegalStateException("Null default: " + key);
                if (field.getType().isPrimitive() || value instanceof String || value instanceof Enum<?> || value instanceof List<?>) {
                    leaves.put(key, new Leaf(field.getGenericType(), field.getAnnotation(ConfigRange.class), JSON.toJsonTree(value)));
                    ConfigValues.bind(node, field.getName(), key);
                } else collect(key, value);
            } catch (IllegalAccessException e) { throw new IllegalStateException(key, e); }
        }
    }

    Map<String, JsonElement> defaults() {
        var result = new TreeMap<String, JsonElement>();
        leaves.forEach((key, leaf) -> result.put(key, leaf.defaultValue.deepCopy()));
        return result;
    }

    Set<String> domains() { return roots.keySet(); }
    Object root(String domain) { return roots.get(domain); }
    public boolean contains(String key) { return leaves.containsKey(key); }

    void overlay(String domain, JsonObject input, Map<String, JsonElement> target, String source) {
        walk(domain, input, target, source);
    }

    private void walk(String path, JsonObject input, Map<String, JsonElement> target, String source) {
        for (var entry : input.entrySet()) {
            String key = path + "." + entry.getKey();
            JsonElement value = entry.getValue();
            if (leaves.containsKey(key)) {
                decode(key, value, source);
                target.put(key, value.deepCopy());
            } else if (branches.containsKey(key) && value.isJsonObject()) {
                walk(key, value.getAsJsonObject(), target, source);
            } else throw invalid(source, key, "unknown field or wrong object type");
        }
    }

    Map<String, Object> decodeComplete(Map<String, JsonElement> input, String source) {
        if (!input.keySet().equals(leaves.keySet())) throw invalid(source, "schema", "missing or unexpected settings");
        var result = new TreeMap<String, Object>();
        input.forEach((key, value) -> result.put(key, decode(key, value, source)));
        validateRelationships(result, source);
        return Collections.unmodifiableMap(result);
    }

    private Object decode(String key, JsonElement value, String source) {
        Leaf leaf = leaves.get(key);
        if (leaf == null) throw invalid(source, key, "unknown setting");
        try {
            Object decoded = convert(leaf.type, value);
            if (decoded instanceof Number n && leaf.range != null &&
                    (n.doubleValue() < leaf.range.min() || n.doubleValue() > leaf.range.max())) {
                throw new IllegalArgumentException("expected " + leaf.range.min() + " to " + leaf.range.max());
            }
            if (key.endsWith(".shopEntries")) {
                for (Object item : (List<?>) decoded) validateShop((String) item);
            }
            if (key.endsWith(".itemAmount") && ((List<?>) decoded).stream().anyMatch(n -> ((Integer)n) < 0))
                throw new IllegalArgumentException("item amounts cannot be negative");
            return decoded;
        } catch (RuntimeException e) { throw invalid(source, key, e.getMessage()); }
    }

    private void validateShop(String entry) {
        String[] parts = entry.split(";", -1);
        if (parts.length != 4) throw new IllegalArgumentException("shop entries require ITEM;PRICE;LIMIT;TYPE");
        org.cat.express.wyspiaexpress.shop.EnumShopEntry.valueOf(parts[0].trim().toUpperCase(Locale.ROOT));
        if (Integer.parseInt(parts[1].trim()) < 0 || Integer.parseInt(parts[2].trim()) < -1)
            throw new IllegalArgumentException("shop price must be nonnegative and limit at least -1");
        if (!Set.of("WEAPON", "TOOL", "POISON").contains(parts[3].trim().toUpperCase(Locale.ROOT)))
            throw new IllegalArgumentException("unknown shop item type");
    }

    private Object convert(Type type, JsonElement value) {
        if (value == null || value.isJsonNull()) throw new IllegalArgumentException("null is not supported; omit an override to inherit");
        if (type instanceof ParameterizedType parameterized && parameterized.getRawType() == List.class) {
            if (!value.isJsonArray()) throw new IllegalArgumentException("expected a list");
            var result = new ArrayList<>();
            for (var element : value.getAsJsonArray()) result.add(convert(parameterized.getActualTypeArguments()[0], element));
            return List.copyOf(result);
        }
        if (!(type instanceof Class<?> cls) || !value.isJsonPrimitive()) throw new IllegalArgumentException("wrong value type");
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (cls == boolean.class || cls == Boolean.class) {
            if (!primitive.isBoolean()) throw new IllegalArgumentException("expected boolean");
            return primitive.getAsBoolean();
        }
        if (cls == String.class || cls.isEnum()) {
            if (!primitive.isString()) throw new IllegalArgumentException("expected text");
            if (cls == String.class) return primitive.getAsString();
            for (Object constant : cls.getEnumConstants()) if (((Enum<?>) constant).name().equals(primitive.getAsString())) return constant;
            throw new IllegalArgumentException("unknown " + cls.getSimpleName() + ": " + primitive.getAsString());
        }
        if (!primitive.isNumber()) throw new IllegalArgumentException("expected number");
        var number = primitive.getAsBigDecimal();
        if (cls == int.class || cls == Integer.class) return number.intValueExact();
        if (cls == long.class || cls == Long.class) return number.longValueExact();
        if (cls == float.class || cls == Float.class) {
            float result = number.floatValue();
            if (!Float.isFinite(result)) throw new IllegalArgumentException("number must be finite");
            return result;
        }
        if (cls == double.class || cls == Double.class) {
            double result = number.doubleValue();
            if (!Double.isFinite(result)) throw new IllegalArgumentException("number must be finite");
            return result;
        }
        throw new IllegalArgumentException("unsupported type " + cls.getName());
    }

    private void validateRelationships(Map<String, Object> values, String source) {
        ordered(values, source, "tasks.minTaskDelaySeconds", "tasks.maxTaskDelaySeconds");
        for (String key : leaves.keySet()) {
            if (key.endsWith(".minimumPlayerSpawn")) ordered(values, source, key, key.replace(".minimumPlayerSpawn", ".maximumPlayerSpawn"));
            if (key.endsWith(".minimumDistance")) ordered(values, source, key, key.replace(".minimumDistance", ".maximumDistance"));
        }
        for (String key : List.of("mode.killerDividend", "mode.vigilanteDividend")) {
            int value = ((Number)values.get(key)).intValue();
            if (value != -1 && value < 1) throw invalid(source, key, "expected -1 or a positive dividend");
        }
    }

    private void ordered(Map<String, Object> values, String source, String minimum, String maximum) {
        if (((Number) values.get(minimum)).doubleValue() > ((Number) values.get(maximum)).doubleValue())
            throw invalid(source, minimum, "must not exceed " + maximum);
    }

    public String fingerprint() {
        var text = new StringBuilder();
        leaves.forEach((key, leaf) -> text.append(key).append(':').append(leaf.type.getTypeName()).append(':')
                .append(enumSignature(leaf.type)).append(':')
                .append(leaf.range == null ? "" : leaf.range.min() + ":" + leaf.range.max()).append(':').append(leaf.defaultValue).append('\n'));
        return hash(text.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String enumSignature(Type type) {
        if (type instanceof ParameterizedType p) return enumSignature(p.getActualTypeArguments()[0]);
        if (type instanceof Class<?> c && c.isEnum()) return Arrays.toString(c.getEnumConstants());
        return "";
    }

    static String hash(byte[] data) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    static IllegalArgumentException invalid(String source, String key, String reason) {
        return new IllegalArgumentException(source + " [" + key + "]: " + reason);
    }

    private record Leaf(Type type, ConfigRange range, JsonElement defaultValue) {}
}
