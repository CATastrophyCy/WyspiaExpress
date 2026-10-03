package org.cat.express.wyspiaexpress.client.guidebook;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.cat.express.wyspiaexpress.WyspiaExpress;

import java.util.*;
import java.util.regex.Pattern;

public final class GuidebookDefinition {
    private static final Pattern TOKEN = Pattern.compile("\\{([a-zA-Z0-9_.-]+)}");
    public static final GuidebookDefinition EMPTY = new GuidebookDefinition(new JsonObject(), "legacy");
    private final JsonObject data;
    private final String resource;
    private final Map<String, GuidebookValues.Binding> values = new LinkedHashMap<>();
    private final Set<String> reported = new HashSet<>();

    public GuidebookDefinition(JsonObject data, String resource) {
        this.data = data;
        this.resource = resource;
        if (data.has("version") && data.get("version").getAsInt() != 1) {
            throw new IllegalArgumentException("Unsupported guidebook definition version");
        }
        if (data.has("values") && !data.get("values").isJsonObject()) {
            report("values", new IllegalArgumentException("Values must be an object"));
        } else if (data.has("values")) {
            for (var entry : data.getAsJsonObject("values").entrySet()) {
                try {
                    values.put(entry.getKey(), GuidebookValues.bind(entry.getValue().getAsJsonObject()));
                } catch (RuntimeException error) {
                    report(entry.getKey(), error);
                }
            }
        }
    }

    public Text field(String field, String fallback) {
        if (data.has(field)) {
            try {
                Text result = text(data.get(field));
                if (result != null) return result;
            } catch (RuntimeException error) { report(field, error); }
        }
        if (fallback == null || !Language.getInstance().hasTranslation(fallback)) return null;
        String translated = Language.getInstance().get(fallback);
        return legacy(fallback.startsWith("guidebook.role.description.") ? GuidebookLegacy.description(translated) : translated);
    }

    public Identifier image() {
        try { return data.has("image") ? Identifier.tryParse(data.get("image").getAsString()) : null; }
        catch (RuntimeException error) { report("image", error); return null; }
    }

    public List<Text> abilities() {
        List<Text> result = new ArrayList<>();
        if (!data.has("abilities")) return result;
        if (!data.get("abilities").isJsonArray()) {
            report("abilities", new IllegalArgumentException("Abilities must be an array"));
            return List.of(Text.translatable("gui.wyspiaexpress.guidebook.unavailable"));
        }
        for (JsonElement element : data.getAsJsonArray("abilities")) {
            try {
                var block = element.getAsJsonObject();
                if (block.has("when")) {
                    JsonObject condition = block.getAsJsonObject("when");
                    Object actual = binding(condition.get("value").getAsString()).value();
                    var expected = condition.get("equals");
                    if (!(actual instanceof Boolean) || !expected.isJsonPrimitive() || !expected.getAsJsonPrimitive().isBoolean()) {
                        throw new IllegalArgumentException("Conditions require a boolean binding and equals value");
                    }
                    if (!actual.equals(expected.getAsBoolean())) continue;
                }
                Text resolved = text(block.get("text"));
                if (resolved != null && !resolved.getString().isEmpty()) result.add(resolved);
            } catch (RuntimeException error) {
                report("abilities", error);
                result.add(Text.translatable("gui.wyspiaexpress.guidebook.unavailable"));
            }
        }
        return result;
    }

    private GuidebookValues.Binding binding(String name) {
        var binding = values.get(name);
        if (binding == null) throw new IllegalArgumentException("Unknown or invalid value alias: " + name);
        return binding;
    }

    public Text text(JsonElement element) {
        if (element == null || element.isJsonNull()) return Text.empty();
        if (element.isJsonPrimitive()) return literal(element.getAsString());
        if (element.isJsonArray()) {
            MutableText text = Text.empty();
            for (var part : element.getAsJsonArray()) { Text child = text(part); if (child != null) text.append(child); }
            return text;
        }
        JsonObject spec = element.getAsJsonObject();
        if (spec.has("value")) return binding(spec.get("value").getAsString()).text();
        MutableText result;
        if (spec.has("translate")) {
            String key = spec.get("translate").getAsString();
            if (!Language.getInstance().hasTranslation(key)) return null;
            List<Object> args = new ArrayList<>();
            if (spec.has("with")) for (var arg : spec.getAsJsonArray("with")) {
                Text resolved = text(arg);
                if (resolved == null) throw new IllegalArgumentException("Missing argument translation");
                args.add(resolved);
            }
            result = Text.translatable(key, args.toArray());
        } else if (spec.has("text")) {
            result = literal(spec.get("text").getAsString());
        } else throw new IllegalArgumentException("Text requires text, translate or value");
        if (spec.has("bold")) result.styled(style -> style.withBold(spec.get("bold").getAsBoolean()));
        if (spec.has("italic")) result.styled(style -> style.withItalic(spec.get("italic").getAsBoolean()));
        if (spec.has("color")) {
            String color = spec.get("color").getAsString();
            if (color.startsWith("#")) result.withColor(Integer.parseInt(color.substring(1), 16));
            else {
                Formatting formatting = Formatting.byName(color);
                if (formatting == null) throw new IllegalArgumentException("Unknown text colour: " + color);
                result.formatted(formatting);
            }
        }
        if (spec.has("extra")) for (var extra : spec.getAsJsonArray("extra")) {
            Text child = text(extra); if (child != null) result.append(child);
        }
        return result;
    }

    private MutableText literal(String string) {
        var matcher = TOKEN.matcher(string);
        MutableText result = Text.empty();
        int end = 0;
        while (matcher.find()) {
            result.append(legacy(string.substring(end, matcher.start())));
            result.append(binding(matcher.group(1)).text());
            end = matcher.end();
        }
        return result.append(legacy(string.substring(end)));
    }

    public static MutableText legacy(String string) {
        string = string.replace("\\n", "\n");
        MutableText result = Text.empty();
        Style style = Style.EMPTY;
        int start = 0;
        for (int i = 0; i + 1 < string.length(); i++) {
            if (string.charAt(i) != '§') continue;
            Formatting formatting = Formatting.byCode(string.charAt(i + 1));
            if (formatting == null) continue;
            result.append(Text.literal(string.substring(start, i)).setStyle(style));
            style = formatting == Formatting.RESET ? Style.EMPTY
                    : formatting.isColor() ? Style.EMPTY.withFormatting(formatting) : style.withFormatting(formatting);
            start = i + 2;
            i++;
        }
        return result.append(Text.literal(string.substring(start)).setStyle(style));
    }

    private void report(String field, RuntimeException error) {
        String key = field + ":" + error.getMessage();
        if (reported.add(key)) WyspiaExpress.LOGGER.warn("Guidebook {} [{}]: {}", resource, field, error.getMessage());
    }
}
