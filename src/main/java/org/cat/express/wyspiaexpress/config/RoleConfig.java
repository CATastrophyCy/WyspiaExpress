package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Nest;

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
        @Comment("Config options for NECROMANCERG.")
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
