package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.text.Text;
import org.aussiebox.starexpress.StarryExpress;

import java.util.regex.Pattern;

/** The ten positional arguments supported by StarryExpress's original guidebook. */
public final class GuidebookLegacy {
    private static final Pattern ARGUMENT = Pattern.compile("%(?:(\\d+)\\$)?s|%%");
    private GuidebookLegacy() {}

    public static String description(String text) {
        if (!text.contains("%")) return text;
        var allergic = StarryExpress.CONFIG.allergicConfig;
        Object[] args = {
                Text.translatable("guidebook.parameter.setting").getString(),
                StarryExpress.CONFIG.starstruckConfig.abilityCooldown(),
                StarryExpress.CONFIG.starstruckConfig.abilityDuration(),
                allergic.nothingChance() + allergic.instinctChance() + allergic.armorChance() + allergic.poisonChance(),
                allergic.nothingChance(), allergic.instinctChance(), allergic.armorChance(), allergic.poisonChance(),
                StarryExpress.CONFIG.muzzlerConfig.suffocationTime(), StarryExpress.CONFIG.muzzlerConfig.tapeTearCheckCount()
        };
        var matcher = ARGUMENT.matcher(text);
        var result = new StringBuilder();
        int implicit = 0;
        while (matcher.find()) {
            String replacement = "%";
            if (!matcher.group().equals("%%")) {
                int index;
                try { index = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) - 1 : implicit++; }
                catch (NumberFormatException invalid) { index = -1; }
                replacement = index >= 0 && index < args.length ? args[index].toString() : matcher.group();
            }
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        return matcher.appendTail(result).toString();
    }
}
