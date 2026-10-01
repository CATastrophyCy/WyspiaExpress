package org.cat.express.wyspiaexpress.guidebook;

import org.BsXinQin.kinswathe.KinsWatheConfig;

import java.util.Map;

/** Additional options omitted by Kin's existing config component, carried on that same CCA channel. */
public interface KinsGuideConfig {
    java.util.Set<String> KEYS = java.util.Set.of("BellringerAbilityCooldown", "BodymakerAbilityCooldown",
            "CleanerAbilityCooldown", "CleanerGetCoins", "DetectiveAbilityCooldown", "DreamerInitialItemQuantity",
            "HunterAbilityCooldown", "JudgeAbilityCooldown", "JudgeAbilityGlowing", "RobotAbilityCooldown", "RobotAbilityDuration", "KidnapperGetAdditionalCoins");

    Map<String, Double> wyspiaexpress$guideValues();

    static Map<String, Double> serverValues() {
        var config = KinsWatheConfig.HANDLER.instance();
        return Map.ofEntries(
                Map.entry("BellringerAbilityCooldown", (double) config.BellringerAbilityCooldown),
                Map.entry("BodymakerAbilityCooldown", (double) config.BodymakerAbilityCooldown),
                Map.entry("CleanerAbilityCooldown", (double) config.CleanerAbilityCooldown),
                Map.entry("CleanerGetCoins", (double) config.CleanerGetCoins),
                Map.entry("DetectiveAbilityCooldown", (double) config.DetectiveAbilityCooldown),
                Map.entry("DreamerInitialItemQuantity", (double) config.DreamerInitialItemQuantity),
                Map.entry("HunterAbilityCooldown", (double) config.HunterAbilityCooldown),
                Map.entry("JudgeAbilityCooldown", (double) config.JudgeAbilityCooldown),
                Map.entry("JudgeAbilityGlowing", (double) config.JudgeAbilityGlowing),
                Map.entry("RobotAbilityCooldown", (double) config.RobotAbilityCooldown),
                Map.entry("RobotAbilityDuration", (double) config.RobotAbilityDuration),
                Map.entry("KidnapperGetAdditionalCoins", (double) config.KidnapperGetAdditionalCoins));
    }
}
