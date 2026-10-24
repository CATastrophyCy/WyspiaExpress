package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("ModifiersConfig")
public class ModifiersConfigModel {


    @Comment("Config options for GUESSER")
    public GuesserConfig guesserConfig = new GuesserConfig();
    @Comment("Config options for PROFESIONAL_VENT_CRAWLER")
    public VentCrawlerConfig ventCrawlerConfig = new VentCrawlerConfig();

    @Comment("Config options for BOMBER")
    public BomberConfig bomberConfig = new BomberConfig();

    @Comment("Config options for ELUSIVE")
    public ElusiveConfig elusiveConfig = new ElusiveConfig();

    public static class GuesserConfig{
        @Comment("Automatically give Guesser to killers, or main true neutrals in the true-neutral mode. With this enabled it is recommended to have Guesser disabled in the random modifier pool.")
        public boolean killerAlwaysGuesser = true;

        @Comment("Maximum amount of guessers when killerAlwaysGuesser is enabled")
        public int maximumGuessers = 1;

        @Comment("Minimum amount of living civilian players for guesser to be able to guess")
        public int minPlayer = 3;

        @Comment("Guesser cooldown after a valid civilian role guess, in seconds. Zero disables the cooldown")
        public int cooldown = 120;

        @Comment("Guesser cooldown after unknown, misspelled or non-civilian input, in seconds. Zero disables the cooldown")
        public int invalidGuessCooldown = 5;

    }
    public static class VentCrawlerConfig{
        @Comment("Crawling speed boost. Is multiplicative with general crawl speed boost")
        public double crawlSpeedModifier = 1.5;

    }

    public static class BomberConfig{
        @Comment("Enable bomber")
        public boolean enabled = false;

        @Comment("1 out of chance of bomber appearing on a random civilian")
        public int chance = 1000;

        @Comment("Amount of grenade to give to player")
        public int grenadeAmount = 3;

        @Comment("Amount of smoke bomb to give to player")
        public int smokeBombAmount = 3;

    }

    public static class ElusiveConfig{
        @Comment("Minimum distance to start hiding from instinct")
        @ConfigRange(min = 0, max = 500)
        public int minimumDistance = 10;

        @Comment("Maximum distance to hide from instinct")
        @ConfigRange(min = 0, max = 500)
        public int maximumDistance = 50;

    }
}
