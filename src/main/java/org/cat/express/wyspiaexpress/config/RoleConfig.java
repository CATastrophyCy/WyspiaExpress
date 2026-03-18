package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Nest;
import org.jetbrains.annotations.Range;

public class RoleConfig {
    // mine
    @Comment("Config options for BANDIT.")
    @Nest public BanditConfig banditConfig = new BanditConfig();
    @Comment("Config options for AWESOME_BINGLUS")
    @Nest public NoteTakerConfig noteTakerConfig = new NoteTakerConfig();
    @Comment("Config options for EDGE_LORD")
    @Nest public EdgeLordConfig edgeLordConfig = new EdgeLordConfig();
    @Comment("Config options for GAMBLER")
    @Nest public GamblerConfig  gamblerConfig = new GamblerConfig();
    @Comment("Config options for REANIMATOR.")
    @Nest public ReanimatorConfig reanimatorConfig = new ReanimatorConfig();
    // Starry Express
    @Nest public StarryExpressRoles starryExpress = new StarryExpressRoles();
    // Stupid Express
    @Nest public StupidExpressRoles stupidExpress = new StupidExpressRoles();
    // Noelle's roles
    @Nest public NoellesRoles noellesRoles = new NoellesRoles();
    // Kin's wathe
    @Nest public KinsWatheRoles kinsWatheRoles = new KinsWatheRoles();

    // Mine
    public static class BanditConfig {
        @Comment("Basic role configuration.")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
    }
    public static class NoteTakerConfig {
        @Comment("Basic role configuration.")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
    }
    public static class EdgeLordConfig {
        @Comment("Basic role configuration.")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
    }
    public static class GamblerConfig {
        @Comment("Basic role configuration.")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        @Comment("Good pool chance.")
        public double goodPoolChance = 0.30;
        @Comment("Bad pool miss chance.")
        public double badPoolMissChance = 0.40;
    }
    public static class ReanimatorConfig {
        @Comment("Basic role configuration.")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        @Comment("Additional revive. Note the Reanimator can't revive when the number of alive killers is greater or equal to {Starting killer amount + this number}")
        public int additionalRevive = 1;
        @Comment("Cooldown for the ability, in seconds.")
        public int cooldown = 180;
        @Comment("Range to activate ability.")
        public double range = 3.0;
        @Comment("The amount of coin revived player starts with")
        public int startingCoin = 75;
        @Comment("Configuration for their GHOUL")
        @Nest public ReanimatorGhoulConfig ghoulConfig = new ReanimatorGhoulConfig();

        // Reanimator revived roles
        public static class ReanimatorGhoulConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }
    // Starry Express
    public static class StarryExpressRoles{
        @Comment("Config options for MUZZLER.")
        @Nest public MuzzlerConfig muzzlerConfig = new MuzzlerConfig();

        public static class MuzzlerConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }

    // Stupid Express
    public static class StupidExpressRoles{
        @Comment("Config options for NECROMANCER, from Stupid Express.")
        @Nest public NecromancerConfig necromancerConfig = new NecromancerConfig();

        @Comment("Config options for AVARICIOUS.")
        @Nest public AvariciousConfig avariciousConfig = new AvariciousConfig();

        @Comment("Config options for AMNESIAC")
        @Nest public AmnesiacConfig amnesiacConfig = new AmnesiacConfig();

        @Comment("Config options for THIEF")
        @Nest public ThiefConfig thiefConfig = new ThiefConfig();

        public static class NecromancerConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        public static class AvariciousConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        public static class AmnesiacConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        public static class ThiefConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }
    // Noelle's role
    public static class NoellesRoles{
        @Comment("Config options for MORPHLING.")
        @Nest public MorphlingConfig morphlingConfig = new MorphlingConfig();

        @Comment("Config options for PHANTOM.")
        @Nest public PhantomConfig phantomConfig = new PhantomConfig();

        @Comment("Config options for SWAPPER.")
        @Nest public SwapperConfig swapperConfig = new SwapperConfig();

        @Comment("Config options for NOISE_MAKER.")
        @Nest public NoiseMakerConfig noiseMakerConfig = new NoiseMakerConfig();

        public static class MorphlingConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class PhantomConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class SwapperConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class NoiseMakerConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }


    // Kin's wathe

    public static class KinsWatheRoles{
        @Comment("Config options for BODYMAKER.")
        @Nest public BodymakerConfig bodymakerConfig = new BodymakerConfig();

        @Comment("Config options for CLEANER.")
        @Nest public CleanerConfig cleanerConfig = new CleanerConfig();

        @Comment("Config options for HUNTER.")
        @Nest public HunterConfig hunterConfig = new HunterConfig();

        @Comment("Config options for KIDNAPPER.")
        @Nest public KidnapperConfig kidnapperConfig = new KidnapperConfig();

        @Comment("Config options for DRUGMAKER.")
        @Nest public DrugmakerConfig drugmakerConfig = new DrugmakerConfig();

        public static class BodymakerConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class CleanerConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class HunterConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class KidnapperConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class DrugmakerConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }

}
