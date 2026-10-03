package org.cat.express.wyspiaexpress.client.guidebook;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.RoleComponent;
import org.cat.express.wyspiaexpress.components.PlayerRolePickingComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class GuidebookCatalog {
    private GuidebookCatalog() {}

    public enum Availability { ALL, CURRENT, UNAVAILABLE, DISABLED }

    public static List<GuidebookEntry> entries() {
        List<GuidebookEntry> entries = new ArrayList<>();
        WatheRoles.ROLES.forEach(role -> entries.add(GuidebookEntry.role(role)));
        HMLModifiers.MODIFIERS.forEach(modifier -> entries.add(GuidebookEntry.modifier(modifier)));
        return entries.stream().filter(entry -> !permanentlyExcluded(entry)).distinct().sorted(Comparator.comparing(GuidebookEntry::category)
                .thenComparing(entry -> entry.name().getString().toLowerCase(Locale.ROOT))
                .thenComparing(GuidebookEntry::key)).toList();
    }

    public static Set<String> ownEntries() {
        var client = MinecraftClient.getInstance();
        Set<String> keys = new HashSet<>();
        if (client.world == null || client.player == null) return keys;
        var role = GameWorldComponent.KEY.get(client.world).getRole(client.player);
        if (role != null) keys.add("role:" + role.identifier());
        var modifiers = WorldModifierComponent.KEY.get(client.world).getModifiers(client.player);
        if (modifiers != null) modifiers.forEach(mod -> keys.add("modifier:" + mod.identifier()));
        return keys;
    }

    public static boolean isCopycat() {
        var client = MinecraftClient.getInstance();
        return client.world != null && client.player != null
                && GameWorldComponent.KEY.get(client.world).isRole(client.player, WyspiaExpressRoles.COPYCAT);
    }

    public static Set<String> copycatChoices() {
        if (!isCopycat()) return Set.of();
        var component = PlayerRolePickingComponent.KEY.get(MinecraftClient.getInstance().player);
        Set<String> keys = new HashSet<>();
        for (String choice : component.getRoles()) {
            // The existing picker uses path IDs; resolve through the same map as its server handler.
            var role = WyspiaExpressRoles.STRING_ROLES.get(choice);
            if (role == null && choice.equals(WatheRoles.KILLER.identifier().getPath())) role = WatheRoles.KILLER;
            if (role != null) keys.add("role:" + role.identifier());
        }
        return keys;
    }

    public static boolean normallyVisible(GuidebookEntry entry) {
        if (entry.role() != null) return !WyspiaExpressRoles.HIDDEN_ROLES.contains(entry.role());
        return entry.modifier() == WyspiaExpressRoles.GUESSER || entry.modifier() == WyspiaExpressRoles.BOMBER
                || !WyspiaExpressRoles.HIDDEN_MODIFIERS.contains(entry.modifier());
    }

    /** Obsolete replacements and always-hidden modifiers never get a guide, even through Me/Pick. */
    public static boolean permanentlyExcluded(GuidebookEntry entry) {
        if (entry.role() == WatheRoles.DISCOVERY_CIVILIAN || entry.role() == WatheRoles.CIVILIAN
                || entry.role() == WatheRoles.KILLER || entry.role() == WatheRoles.LOOSE_END) return true;
        if (entry.role() != null) return WyspiaExpressRoles.HIDDEN_ROLES.contains(entry.role())
                && entry.role() != WyspiaExpressRoles.COPYCAT && entry.role() != WyspiaExpressRoles.CULTIST
                && entry.role() != WyspiaExpressRoles.LICH_GHOUL;
        return WyspiaExpressRoles.HIDDEN_MODIFIERS.contains(entry.modifier())
                && entry.modifier() != WyspiaExpressRoles.GUESSER && entry.modifier() != WyspiaExpressRoles.BOMBER;
    }

    public static Status status(GuidebookEntry entry) {
        var client = MinecraftClient.getInstance();
        if (client.world == null) return new Status(false, false, false, null, null);
        var context = RoleComponent.KEY.get(client.world);
        var basic = entry.role() != null ? WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(entry.role()) : null;
        Integer min = basic != null ? basic.minimumPlayerSpawn() : null;
        Integer max = basic != null ? basic.maximumPlayerSpawn() : null;
        boolean current = entry.role() != null ? !context.disabledRoles.contains(entry.id().toString())
                : !context.disabledModifiers.contains(entry.id().toString());
        boolean serverDisabled = entry.role() != null ? context.configuredDisabledRoles.contains(entry.id().toString())
                : context.disabledModifiers.contains(entry.id().toString());
        boolean blocked = !normallyVisible(entry);
        if (basic != null && basic.maximumSpawn() <= 0) blocked = true;
        if (entry.role() == WyspiaExpressRoles.COPYCAT && !WyspiaExpress.ROLES_CONFIG.enableRolePicking()) blocked = true;
        if (entry.modifier() == WyspiaExpressRoles.BOMBER) {
            blocked = !WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.enabled();
        }
        return new Status(current, serverDisabled, blocked, min, max);
    }

    public static List<GuidebookEntry> filter(List<GuidebookEntry> entries, String search, Availability availability,
                                            boolean mine) {
        return filter(entries, search, availability, mine, false);
    }

    public static List<GuidebookEntry> filter(List<GuidebookEntry> entries, String search, Availability availability,
                                            boolean mine, boolean copycat) {
        String query = search.strip().toLowerCase(Locale.ROOT);
        Set<String> own = ownEntries();
        Set<String> choices = copycat ? copycatChoices() : Set.of();
        return entries.stream().filter(entry -> {
            if (permanentlyExcluded(entry)) return false;
            if (copycat) return choices.contains(entry.key()) && entry.name().getString().toLowerCase(Locale.ROOT).contains(query);
            if (mine) return own.contains(entry.key()) && entry.name().getString().toLowerCase(Locale.ROOT).contains(query);
            if (!normallyVisible(entry) || !entry.name().getString().toLowerCase(Locale.ROOT).contains(query)) return false;
            Status status = status(entry);
            return switch (availability) {
                case ALL -> true;
                case CURRENT -> status.current();
                case DISABLED -> status.serverDisabled();
                case UNAVAILABLE -> !status.current() && !status.serverDisabled();
            };
        }).toList();
    }

    public record Status(boolean current, boolean serverDisabled, boolean blocked, Integer minimum, Integer maximum) {}
}
