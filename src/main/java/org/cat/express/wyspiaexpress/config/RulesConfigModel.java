package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("RulesConfig")
public class RulesConfigModel {
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

}
