package org.cat.express.wyspiaexpress.config;


import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import org.cat.express.wyspiaexpress.Wyspiaexpress;
import pro.fazeclan.river.stupid_express.mixin.role.avaricious.AvariciousGoldPayout;

import java.util.ArrayList;
import java.util.List;


// uses owo-lib annotation system to auto generate config file and also syncing
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "wyspiaexpress-server", wrapperName = "WyspiaExpressServerConfig")
public class ServerConfig {

    @SectionHeader("role_config")

    @Comment("""
            Valid shop entries:
                Format: ITEM_NAME;PRICE;PURCHASE_LIMIT;TYPE
                Example: KNIFE;50;1;WEAPON
                Type can take values from: WEAPON, TOOL, POISON; it only affects the ui in shop
                PURCHASE_LIMIT is still WIP, not implemented yet
           \s
            Valid items:\s
            KNIFE, REVOLVER, GRENADE, PSYCHO_MODE, POISON_VIAL, SCORPION, FIRECRACKER, LOCKPICK,
            CROWBAR, BODY_BAG, BLACKOUT, NOTE,
            TAPE,
            FAKE_KNIFE, DELUSION_VIAL, DEFENSE_VIAL, ROLE_MINE, MASTER_KEY, FAKE_REVOLVER,
            PAN, COOKED_PORKCHOP, COOKED_CHIKEN, COOKED_BEEF, PILL, MEDICAL_KIT, ACID_BARREL, HUNTING_KNIFE, KNOCKOUT_DRUG, DREAM_IMPRINT, BLOWGUN, POISON_INJECTOR
           \s""")
    public List<String> exampleShopEntries = new ArrayList<>(List.of("KNIFE;50;-1;WEAPON", "PSYCHO_MODE;300;-1;WEAPON"));

    @Comment("Configuration for roles.")
    @Nest public RoleConfig roleConfig = new RoleConfig();

    public static class RoleConfig {
        @Comment("Config options for BANDIT.")
        @Nest public BanditConfig banditConfig = new BanditConfig();


        // Starry Express
        @Comment("Config options for MUZZLER.")
        @Nest public MuzzlerConfig muzzlerConfig = new MuzzlerConfig();

        // Stupid Express
        @Comment("Config options for NECROMANCERG.")
        @Nest public NecromancerConfig necromancerConfig = new NecromancerConfig();

        @Comment("Config options for AVARICIOUS.")
        @Nest public AvariciousConfig avariciousConfig = new AvariciousConfig();
        // Noelle's role

        @Comment("Config options for MORPHLING.")
        @Nest public MorphlingConfig morphlingConfig = new MorphlingConfig();

        @Comment("Config options for PHANTOM.")
        @Nest public PhantomConfig phantomConfig = new PhantomConfig();

        @Comment("Config options for SWAPPER.")
        @Nest public SwapperConfig swapperConfig = new SwapperConfig();

        // Kin's wathe

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


        public static class BanditConfig {
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        // Starry Express
        public static class MuzzlerConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        // Stupid Express
        public static class NecromancerConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }
        public static class AvariciousConfig{
            @Comment("Basic role configuration.")
            @Nest public RoleBasicConfig basic = new RoleBasicConfig();
        }

        // Noelle's role
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
        // Kin's wathe
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
