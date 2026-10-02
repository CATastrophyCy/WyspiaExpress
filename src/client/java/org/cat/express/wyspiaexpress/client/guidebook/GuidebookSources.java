package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.client.MinecraftClient;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.cat.express.wyspiaexpress.guidebook.KinsGuideConfig;

import java.util.function.Function;

/** Adapters for existing synced components and gameplay-derived values, not local YACL defaults. */
public final class GuidebookSources {
    private GuidebookSources() {}

    public static void init() {
        // Fixed mechanics in the installed Noelle version; these are not server config options.
        GuidebookValues.registerDerived("noelles.morphDuration", () -> 35);
        GuidebookValues.registerDerived("noelles.morphRecovery", () -> 20);
        GuidebookValues.registerDerived("noelles.recallMarkCooldown", () -> 10);
        GuidebookValues.registerDerived("noelles.recallCost", () -> 100);
        GuidebookValues.registerDerived("noelles.recallReturnCooldown", () -> 30);
        GuidebookValues.registerDerived("noelles.swapCooldown", () -> 60);
        GuidebookValues.registerDerived("noelles.vultureCooldown", () -> 20);
        dev.doctor4t.wathe.api.WatheRoles.ROLES.forEach(role ->
                GuidebookValues.registerDerived("role." + role.identifier() + ".sprint", role::getMaxSprintTime));
        kins("BellringerAbilityPrice", config -> config.BellringerAbilityPrice);
        kins("BodymakerAbilityFakeRole", config -> config.BodymakerAbilityFakeRole);
        kins("CleanerAbilityPrice", config -> config.CleanerAbilityPrice);
        kins("DetectiveAbilityPrice", config -> config.DetectiveAbilityPrice);
        kins("HunterAbilityPrice", config -> config.HunterAbilityPrice);
        kins("JudgeAbilityPrice", config -> config.JudgeAbilityPrice);
        kins("DrugmakerGetCoins", config -> config.DrugmakerGetCoins);
        kins("HackerHackingTime", config -> config.HackerHackingTime);
        GuidebookValues.registerDerived("noelles.maximumDefenseVials", () -> {
            var world = MinecraftClient.getInstance().world;
            return world == null ? null : org.agmas.noellesroles.ConfigWorldComponent.KEY.get(world).maximumDefenseVials;
        });
        for (String key : KinsGuideConfig.KEYS) {
            GuidebookValues.registerDerived("kins." + key, () -> kinsExtra(key));
        }

    }

    private static void kins(String key, Function<ConfigWorldComponent, ?> getter) {
        GuidebookValues.registerDerived("kins." + key, () -> {
            var world = MinecraftClient.getInstance().world;
            return world == null ? null : getter.apply(ConfigWorldComponent.KEY.get(world));
        });
    }

    public static Number kinsExtra(String key) {
        var world = MinecraftClient.getInstance().world;
        if (world == null) return null;
        return ((KinsGuideConfig) ConfigWorldComponent.KEY.get(world)).wyspiaexpress$guideValues().get(key);
    }
}
