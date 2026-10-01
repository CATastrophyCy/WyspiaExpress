package org.cat.express.wyspiaexpress.client.guidebook;

import dev.doctor4t.wathe.api.Role;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;

import java.util.Arrays;
import java.util.stream.Collectors;

public record GuidebookEntry(Identifier id, Role role, Modifier modifier, Category category, int color) {
    public enum Category {
        KILLER, KILLER_NEUTRAL, CIVILIAN, TRUE_NEUTRAL, MODIFIER;
        public Text title() { return Text.translatable("gui.wyspiaexpress.guidebook.category." + name().toLowerCase(java.util.Locale.ROOT)); }
    }

    public static GuidebookEntry role(Role role) {
        Category category = WyspiaExpressRoles.TRUE_NEUTRALS.contains(role) ? Category.TRUE_NEUTRAL
                : WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role) ? Category.KILLER_NEUTRAL
                : role.canUseKiller() ? Category.KILLER : role.isInnocent() ? Category.CIVILIAN : Category.TRUE_NEUTRAL;
        return new GuidebookEntry(role.identifier(), role, null, category, role.color());
    }

    public static GuidebookEntry modifier(Modifier modifier) {
        return new GuidebookEntry(modifier.identifier(), null, modifier, Category.MODIFIER, modifier.color());
    }

    public String key() { return (role != null ? "role:" : "modifier:") + id; }
    public GuidebookDefinition definition() { return GuidebookDefinitions.INSTANCE.get(this); }

    public Text name() {
        Text name = definition().field("name", "guidebook.role." + id);
        if (name != null && !name.getString().isBlank()) return name;
        String key = "announcement.role." + id.getNamespace() + "." + id.getPath();
        if (Language.getInstance().hasTranslation(key)) return Text.translatable(key);
        if (modifier != null) {
            Text fallback = modifier.getName(false);
            if (!fallback.getString().contains("announcement.") && !fallback.getString().contains("modifier.")) return fallback;
        }
        return Text.literal(Arrays.stream(id.getPath().split("[_/]")).filter(s -> !s.isEmpty())
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1)).collect(Collectors.joining(" ")));
    }

    public Text title() { return definition().field("title", "guidebook.role.title." + id); }
    public Text lore() { return definition().field("lore", "guidebook.role.description." + id); }
}
