package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import java.util.ArrayList;
import java.util.List;

@ConfigModel("RulesConfig")
public class RulesConfigModel {
    public InventoryTrashConfig inventoryTrash = new InventoryTrashConfig();

    public static class InventoryTrashConfig {
        @Comment("Allow players to delete selected items from their inventory during a game")
        public boolean enabled;

        @Comment("Items that can be deleted. Gamemode overrides replace this list")
        public List<String> items = new ArrayList<>();
    }

    @Comment("Multiply to crawling speed. i.e. 1.5 means crawling is 50% faster")
    @ConfigRange(min = 0, max = 100)
    public double crawlSpeedMultiplier = 2.0;

    @Comment("By setting this to true shooting a protected player won't drop your revolver regardless if they are innocent or not")
    public boolean disableProtectionGunDrop = true;

    @Comment("Maximum time left, in minutes, for a civilian death or conversion to add time. -1 disables the limit")
    @ConfigRange(min = -1, max = 1789569)
    public int maxTimeForDeathBonus = -1;

    @Comment("Player stun tick after blocking damage")
    @ConfigRange(min = 0, max = 2147483647)
    public int blockStunTicks = 60;

    @Comment("Player stun tick after blocking damage with psycho mode")
    @ConfigRange(min = 0, max = 2147483647)
    public int psychoStunTicks = 20;

    public PsychoConfig psycho = new PsychoConfig();

    public static class PsychoConfig {
        @Comment("Enable infinite psycho shield, slow and time loss from hits. Duration and cooldowns always apply")
        public boolean rework = true;

        @Comment("Psycho duration, in seconds")
        @ConfigRange(min = 1, max = 107374182)
        public int duration = 60;

        @Comment("Blocked hits before the slow and duration penalties start")
        @ConfigRange(min = 0, max = 2147483647)
        public int freeHits = 1;

        @Comment("Movement slow per hit after the free hits. 0.15 = 15%")
        @ConfigRange(min = 0, max = 1)
        public double slowPerHit = 0.15;

        @Comment("Maximum movement slow. 0.4 = 40%")
        @ConfigRange(min = 0, max = 1)
        public double maxSlow = 0.4;

        @Comment("Fraction of remaining psycho time removed per hit after the free hits. 0.35 = 35%")
        @ConfigRange(min = 0, max = 1)
        public double timeLostPerHit = 0.35;

        @Comment("Weapon cooldown after getting hit in psycho, in seconds. Longer cooldowns are kept")
        @ConfigRange(min = 0, max = 107374182)
        public int weaponCooldown = 15;

        @Comment("Extra bat recharge time per hit after the free hits, in seconds")
        @ConfigRange(min = 0, max = 60)
        public double batDelayPerHit = 0.5;

        @Comment("Maximum extra bat recharge time, in seconds")
        @ConfigRange(min = 0, max = 60)
        public double maxBatDelay = 2.5;
    }

    @Comment("Enable freezing")
    public boolean freeze = false;

    @Comment("The time, in seconds, it takes for a player to die when staying outside for too long")
    @ConfigRange(min = 1, max = 107374182)
    public int freezeTimer = 60;

    @Comment("Enable depression killing")
    public boolean depressionKilling = false;

    @Comment("The time, in seconds, it takes for a player to die when staying depressed for too long")
    @ConfigRange(min = 1, max = 107374182)
    public int depressedTimer = 90;

    @Comment("Whether to enable item bound checking, this will make out of bound item tp to closest dead body")
    public boolean enableItemBoundChecking = true;

    @Comment("Enable killer having role color instinct on killer-aligned neutrals")
    public boolean killerSpecialInstinct = false;

    @Comment("Killer instinct brightness. 0 keeps normal darkness, 1 matches spectators")
    @ConfigRange(min = 0, max = 1)
    public double killerInstinctBrightness = 0.4;

}
