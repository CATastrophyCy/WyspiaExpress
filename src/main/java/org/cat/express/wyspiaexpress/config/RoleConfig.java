package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Nest;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;

import java.util.List;

public class RoleConfig {
    // mine
    @Comment("Config options for COPYCAT")
    @Nest public CopycatConfig copycatConfig = new CopycatConfig();
    @Comment("Config options for NOTE_TAKER")
    @Nest public NoteTakerConfig noteTakerConfig = new NoteTakerConfig();
    @Comment("Config options for OUTLAW")
    @Nest public OutlawConfig outlawConfig = new OutlawConfig();
    @Comment("Config options for EDGE_LORD")
    @Nest public EdgeLordConfig edgeLordConfig = new EdgeLordConfig();
    @Comment("Config options for GAMBLER")
    @Nest public GamblerConfig  gamblerConfig = new GamblerConfig();
    @Comment("Config options for LICH")
    @Nest public LichConfig lichConfig = new LichConfig();
    @Comment("Config options for CULT_LEADER")
    @Nest public CultLeaderConfig cultLeaderConfig = new CultLeaderConfig();
    @Comment("Config options for EDDIE_WAFFLES")
    @Nest public EddieWafflesConfig eddieWafflesConfig = new EddieWafflesConfig();
    // wathe
    @Comment("Config options for VIGILANTE")
    @Nest public VigilanteConfig vigilanteConfig = new VigilanteConfig();

    // Starry Express
    @Nest public StarryExpressRoles starryExpress = new StarryExpressRoles();
    // Stupid Express
    @Nest public StupidExpressRoles stupidExpress = new StupidExpressRoles();
    // Noelle's roles
    @Nest public NoellesRoles noellesRoles = new NoellesRoles();
    // Kin's wathe
    @Nest public KinsWatheRoles kinsWatheRoles = new KinsWatheRoles();

    // Mine
    public static class CopycatConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
    }
    public static class NoteTakerConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
    }
    public static class OutlawConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        @Comment("Ability cooldown, in seconds")
        public int cooldown = 90;
        @Comment("Enable ability sound effect. If disabled, only the Outlaw can hear it")
        public boolean enableAbilitySound = true;
        @Comment("Ability sound effect volumen")
        public float volume = 1.0f;
        @Comment("Ability cost, in coins")
        public int cost = 75;
        @Comment("Ability self stun duration, in ticks")
        public int selfStunDuration = 40;
    }
    public static class EdgeLordConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
    }
    public static class GamblerConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        @Comment("Miss chance")
        public double missChance = 0.33;
        @Comment("Good pool chance")
        public double goodPoolChance = 0.30;
        @Comment("Good pool")
        public List<EnumShopEntry> goodPool = List.of( EnumShopEntry.KNIFE,
                EnumShopEntry.MEDICAL_KIT, EnumShopEntry.MASTER_KEY, EnumShopEntry.PAN, EnumShopEntry.WRENCH, EnumShopEntry.CAPTURE_DEVICE);
        @Comment("Bad pool")
        public List<EnumShopEntry> badPool = List.of( EnumShopEntry.NOTE,
                EnumShopEntry.FIRECRACKER, EnumShopEntry.MEGAPHONE);
        @Comment("Miss compensation coin")
        public int missCompensationCoin = 0;
    }
    public static class LichConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        @Comment("Additional revive. Note the LICH can't revive when the number of alive killers is greater or equal to {Starting killer amount + this number}")
        public int additionalRevive = 1;
        @Comment("Cooldown for the ability, in seconds")
        public int cooldown = 180;
        @Comment("Range to activate ability")
        public double range = 3.0;
        @Comment("The amount of coin revived player starts with")
        public int startingCoin = 75;
        @Comment("Configuration for their GHOUL")
        @Nest public LichGhoulConfig ghoulConfig = new LichGhoulConfig();

        // Lich revived roles
        public static class LichGhoulConfig {
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }
    public static class CultLeaderConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        @Comment("Enable conversion by staying close at players")
        public boolean enableConversion = true;
        @Comment("Cooldown for the revive ability, in seconds")
        public int cooldown = 120;
        @Comment("Amount of cumulative time, in ticks, it takes to convert a player")
        public int conversionTime = 600;
        @Comment("Conversion range, in blocks")
        public double conversionRange = 3.0;
        @Comment("The amount of coin converted player starts with")
        public int convertCoin = 150;
        @Comment("Configuration for their CULTIST")
        @Nest public CultistConfig cultistConfig = new CultistConfig();

        // Lich revived roles
        public static class CultistConfig {
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }
    public static class EddieWafflesConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        @Comment("Cooldown for the ability, in seconds")
        public int cooldown = 5;
    }
    // wathe

    public static class VigilanteConfig {
        @Comment("Basic role configuration")
        @Nest public RoleBasicConfig basic = new RoleBasicConfig();
    }

    // Starry Express
    public static class StarryExpressRoles{
        @Comment("Config options for MUZZLER")
        @Nest public MuzzlerConfig muzzlerConfig = new MuzzlerConfig();

        @Comment("Config options for STARSTRUCK")
        @Nest public StarstruckConfig starstruckConfig = new StarstruckConfig();

        public static class MuzzlerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
            @Comment("Enable payout per muzzled players")
            public boolean enablePayout = false;
            @Comment("Amount of coin Muzzled player add to Muzler every 10 seconds")
            public int muzzledPayout = 5;
        }
        public static class StarstruckConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
            @Comment("Enable ability sound")
            public boolean enableAbilitySound = false;
            @Comment("Enable ability particle effects")
            public boolean enableAbilityParticleEffects = false;
            @Comment("Enable persistent particle effects during ability")
            public boolean enablePersistentParticleEffects = false;
        }
    }

    // Stupid Express
    public static class StupidExpressRoles{
        @Comment("Config options for NECROMANCER, from Stupid Express")
        @Nest public NecromancerConfig necromancerConfig = new NecromancerConfig();

        @Comment("Config options for AVARICIOUS")
        @Nest public AvariciousConfig avariciousConfig = new AvariciousConfig();

        @Comment("Config options for AMNESIAC")
        @Nest public AmnesiacConfig amnesiacConfig = new AmnesiacConfig();

        @Comment("Config options for THIEF")
        @Nest public ThiefConfig thiefConfig = new ThiefConfig();

        @Comment("Config options for ARSONIST")
        @Nest public ArsonistConfig arsonistConfig = new ArsonistConfig();

        public static class NecromancerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        public static class AvariciousConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        public static class AmnesiacConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        public static class ThiefConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
            @Comment("Cooldown, in seconds, when failing a steal (target has no stealable item)")
            public int failCooldown = 30;
            @Comment("Cooldown, in seconds, when stealing an item")
            public int cooldown = 45;
            @Comment("Distance, for client, to start rendering thief crosshair")
            public double clientDistance = 1.5;
            @Comment("Distance, for server, to validate a steal")
            public double serverDistance = 1.8;
        }
        public static class ArsonistConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }
    // Noelle's role
    public static class NoellesRoles{
        @Comment("Config options for MORPHLING")
        @Nest public MorphlingConfig morphlingConfig = new MorphlingConfig();

        @Comment("Config options for PHANTOM")
        @Nest public PhantomConfig phantomConfig = new PhantomConfig();

        @Comment("Config options for SWAPPER")
        @Nest public SwapperConfig swapperConfig = new SwapperConfig();

        @Comment("Config options for NOISE_MAKER")
        @Nest public NoiseMakerConfig noiseMakerConfig = new NoiseMakerConfig();

        @Comment("Config options for CORONER")
        @Nest public CoronerConfig coronerConfig = new CoronerConfig();

        @Comment("Config options for CONDUCTOR")
        @Nest public ConductorConfig conductorConfig = new ConductorConfig();

        @Comment("Config options for TRAPPER")
        @Nest public TrapperConfig trapperConfig = new TrapperConfig();

        @Comment("Config options for MIMIC")
        @Nest public MimicConfig mimicConfig = new MimicConfig();

        @Comment("Config options for VULTURE")
        @Nest public VultureConfig vultureConfig = new VultureConfig();

        @Comment("Config options for EXECUTIONER")
        @Nest public ExecutionerConfig executionerConfig = new ExecutionerConfig();

        public static class MorphlingConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class PhantomConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
            @Comment("Ability cooldown, in seconds")
            public int cooldown = 150;
            @Comment("Ability duration, in seconds")
            public int duration = 30;
            @Comment("Whether phantom would lose invisibility after killing (poison won't trigger)")
            public boolean loseInvisibilityWhenKill = true;

        }

        public static class SwapperConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class NoiseMakerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class CoronerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class ConductorConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class TrapperConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class MimicConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class VultureConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class ExecutionerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
            @Comment("Executioner coin reward after their target dies from revolver by non killer")
            public int reward = 150;
        }

    }


    // Kin's wathe

    public static class KinsWatheRoles{
        @Comment("Config options for BODYMAKER")
        @Nest public BodymakerConfig bodymakerConfig = new BodymakerConfig();

        @Comment("Config options for CLEANER")
        @Nest public CleanerConfig cleanerConfig = new CleanerConfig();

        @Comment("Config options for HUNTER")
        @Nest public HunterConfig hunterConfig = new HunterConfig();

        @Comment("Config options for KIDNAPPER")
        @Nest public KidnapperConfig kidnapperConfig = new KidnapperConfig();

        @Comment("Config options for DRUGMAKER")
        @Nest public DrugmakerConfig drugmakerConfig = new DrugmakerConfig();

        @Comment("Config options for DETECTIVE")
        @Nest public DetectiveConfig detectiveConfig = new DetectiveConfig();

        @Comment("Config options for JUDGE")
        @Nest public JudgeConfig judgeConfig = new JudgeConfig();

        @Comment("Config options for BELLRINGER")
        @Nest public BellringerConfig bellringerConfig = new BellringerConfig();

        @Comment("Config options for ROBOT")
        @Nest public RobotConfig robotConfig = new RobotConfig();

        @Comment("Config options for PHYSICIAN")
        @Nest public PhysicianConfig physicianConfig = new PhysicianConfig();

        @Comment("Config options for TECHNICIAN")
        @Nest public TechnicianConfig technicianConfig = new TechnicianConfig();

        @Comment("Config options for COOK")
        @Nest public CookConfig cookConfig = new CookConfig();

        @Comment("Config options for HACKER")
        @Nest public HackerConfig hackerConfig = new HackerConfig();

        @Comment("Config options for DREAMER")
        @Nest public DreamerConfig dreamerConfig = new DreamerConfig();

        @Comment("Config options for LICENSED_VILLAIN")
        @Nest public LicensedVillainConfig licensedVillainConfig = new LicensedVillainConfig();

        public static class BodymakerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class CleanerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class HunterConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class KidnapperConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class DrugmakerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class DetectiveConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        public static class JudgeConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class BellringerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class RobotConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class PhysicianConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class TechnicianConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class CookConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        public static class HackerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
            @Comment("Enable instinct")
            public boolean enableInstinct = false;
        }

        public static class DreamerConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
            @Comment("Enable dream imprint tp to its applier")
            public boolean enableDreamImprintTeleport = false;
            @Comment("Minimum delusion requirement")
            public int minimumRequirement = 2;
            @Comment("Maximum delusion requirement")
            public int maximumRequirement = 4;
            @Comment("Enable instinct")
            public boolean enableInstinct = false;
        }
        public static class LicensedVillainConfig{
            @Comment("Basic role configuration")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
    }

}
