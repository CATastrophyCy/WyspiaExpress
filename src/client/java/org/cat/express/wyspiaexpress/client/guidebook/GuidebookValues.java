package org.cat.express.wyspiaexpress.client.guidebook;

import com.google.gson.JsonObject;
import net.minecraft.text.Text;
import org.aussiebox.starexpress.StarryExpress;
import org.cat.express.wyspiaexpress.config.GameConfigs;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;
import org.cat.express.wyspiaexpress.shop.ShopUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Reads resolved settings, including server overrides; never accesses backing models or setters. */
public final class GuidebookValues {
    private static final Map<String, Supplier<?>> DERIVED = new HashMap<>();

    private GuidebookValues() {}

    public static void registerDerived(String id, Supplier<?> getter) {
        DERIVED.put(id, getter);
    }

    public static Binding bind(JsonObject spec) {
        String source = string(spec, "config", "");
        String path = string(spec, "path", "");
        Supplier<?> getter;
        if (source.equals("derived")) {
            getter = DERIVED.get(path);
            if (getter == null) throw new IllegalArgumentException("Unknown derived value: " + path);
        } else if (!source.equals("starexpress")) {
            String key = source + "." + path;
            if (!GameConfigs.schema().contains(key)) throw new IllegalArgumentException("Unknown option: " + source + ":" + path);
            getter = () -> GameConfigs.value(key);
        } else {
            getter = bindStarryValue(path);
        }
        String format = string(spec, "format", "number");
        String unit = string(spec, "unit", "");
        if (!List.of("number", "coins", "duration", "percent", "multiplier", "boolean", "text", "items").contains(format)) {
            throw new IllegalArgumentException("Unknown format: " + format);
        }
        if (format.equals("duration") && !List.of("seconds", "ticks").contains(unit)) {
            throw new IllegalArgumentException("Duration requires unit seconds or ticks");
        }
        return new Binding(getter, format, unit);
    }

    /** Starry owns its config implementation; bind its public accessors without importing that library. */
    private static Supplier<?> bindStarryValue(String path) {
        try {
            String[] parts = path.split("\\.", -1);
            Object section = StarryExpress.class.getField("CONFIG").get(null);
            for (int i = 0; i < parts.length - 1; i++) {
                section = section.getClass().getField(parts[i]).get(section);
            }
            Object target = section;
            var getter = target.getClass().getMethod(parts[parts.length - 1]);
            return () -> {
                try {
                    return getter.invoke(target);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Cannot read StarryExpress guidebook value: " + path, exception);
                }
            };
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("Unknown StarryExpress guidebook value: " + path, exception);
        }
    }

    public record Binding(Supplier<?> getter, String format, String unit) {
        public Object value() { return getter.get(); }

        public Text text() {
            Text text = formattedText();
            int color = switch (format) {
                case "coins" -> 0xF0C66E;
                case "duration" -> 0xC5ADFF;
                case "boolean" -> Boolean.TRUE.equals(value()) ? 0xA6DCA0 : 0xF09C92;
                default -> 0xA6DCA0;
            };
            return text.copy().withColor(color);
        }

        private Text formattedText() {
            Object value = value();
            if (value == null) return Text.translatable("gui.wyspiaexpress.guidebook.unavailable");
            if (format.equals("boolean")) {
                if (!(value instanceof Boolean flag)) throw new IllegalArgumentException("Boolean format requires boolean option");
                return Text.translatable("gui.wyspiaexpress.guidebook." + (flag ? "on" : "off"));
            }
            if (format.equals("text")) return Text.literal(value.toString());
            if (format.equals("items")) {
                if (!(value instanceof List<?> list)) throw new IllegalArgumentException("Items format requires a list");
                var text = Text.empty();
                for (Object item : list) {
                    if (!text.getString().isEmpty()) text.append(", ");
                    text.append(item instanceof EnumShopEntry entry ? ShopUtil.fromEnumShopEntry(entry).getName()
                            : Text.literal(String.valueOf(item)));
                }
                return text;
            }
            if (!(value instanceof Number number) || !Double.isFinite(number.doubleValue())) {
                throw new IllegalArgumentException("Numeric format requires a finite number");
            }
            double amount = number.doubleValue();
            return switch (format) {
                case "coins" -> Text.translatable("gui.wyspiaexpress.guidebook.coins", decimal(amount));
                case "duration" -> Text.translatable("gui.wyspiaexpress.guidebook.seconds", decimal(unit.equals("ticks") ? amount / 20 : amount));
                case "percent" -> Text.literal(decimal(amount * 100) + "%");
                case "multiplier" -> Text.literal(decimal(amount) + "×");
                default -> Text.literal(decimal(amount));
            };
        }
    }

    public static String decimal(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    static String string(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }
}
