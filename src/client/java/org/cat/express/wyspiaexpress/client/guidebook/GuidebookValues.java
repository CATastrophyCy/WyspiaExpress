package org.cat.express.wyspiaexpress.client.guidebook;

import com.google.gson.JsonObject;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import net.minecraft.text.Text;
import org.aussiebox.starexpress.StarryExpress;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;
import org.cat.express.wyspiaexpress.shop.ShopUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Reads option mirrors, including server overrides; never accesses backing fields or setters. */
public final class GuidebookValues {
    private static final Map<String, ConfigWrapper<?>> CONFIGS = new HashMap<>();
    private static final Map<String, Supplier<?>> DERIVED = new HashMap<>();

    static {
        CONFIGS.put("roles", WyspiaExpress.ROLES_CONFIG);
        CONFIGS.put("items", WyspiaExpress.ITEMS_CONFIG);
        CONFIGS.put("modifiers", WyspiaExpress.MODIFIERS_CONFIG);
        CONFIGS.put("general", WyspiaExpress.SERVER_CONFIG);
        CONFIGS.put("starexpress", StarryExpress.CONFIG);
    }

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
        } else {
            var config = CONFIGS.get(source);
            if (config == null) throw new IllegalArgumentException("Unknown config source: " + source);
            Option<?> option = config.optionForKey(new Option.Key(path));
            if (option == null) throw new IllegalArgumentException("Unknown option: " + source + ":" + path);
            getter = option::value;
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
