package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Nest;

public class ItemConfig {
    // mine
    @Comment("REVOLVER cooldown, in seconds")
    public int revolverCooldown = 10;
    @Comment("KNIFE cooldown, in seconds")
    public int knifeCooldown = 60;
    @Comment("BODY_BAG cooldown, in seconds")
    public int bodyBagCooldown = 180;
    @Comment("PSYCHO_MODE, in seconds")
    public int psychoModeCooldown = 300;
    @Comment("BLACKOUT cooldown, in seconds")
    public int blackoutCooldown = 180;
    @Comment("GRENADE cooldown, in seconds")
    public int grenadeCooldown = 180;
    @Comment("DERRINGER cooldown, in seconds")
    public int derringerCooldown = 20;


    @Comment("HUNTING_KNIFE cooldown, in seconds")
    public int huntingKnifeCooldown = 45;
    @Comment("POISON_INJECTOR cooldown, in seconds")
    public int poisonInjectorCooldown = 60;
    @Comment("BLOW_GUN cooldown, in seconds")
    public int blowGunCooldown = 60;
    @Comment("KNOCKOUT_DRUG cooldown, in seconds")
    public int knockOutDrugCooldown = 45;
    @Comment("CAPTURE_DEVICE cooldown, in seconds")
    public int captureDeviceCooldown = 60;
    @Comment("PAN cooldown, in seconds")
    public int panCooldown = 45;
    @Comment("PILL cooldown, in seconds")
    public int pillCooldown = 180;
    @Comment("ACID_BARREL, in seconds")
    public int acidBarrelCooldown = 60;

    @Comment("POWER_RESTORE cooldown, in seconds")
    public int powerRestoreCooldown = 180;



    @Comment("Configuration for FAKE_REVOLVER")
    @Nest public FakeRevolverConfig fakeRevolverConfig = new FakeRevolverConfig();

    @Comment("Configuration for MEGAPHONE")
    @Nest public MegaphoneConfig megaphoneConfig = new MegaphoneConfig();
    // other mods
    @Comment("Configuration for FAKE_KNIFE")
    @Nest public FakeKnifeConfig fakeKnifeConfig = new FakeKnifeConfig();

    @Comment("Configuration for MEDICAL_KIT")
    @Nest public MedicalKitConfig medicalKitConfig = new MedicalKitConfig();

    @Comment("Configuration for WRENCH")
    @Nest public WrenchConfig wrenchConfig = new WrenchConfig();

    // mine
    public static class FakeRevolverConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 20;

    }

    public static class MegaphoneConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 60;
        @Comment("Duration, in seconds, of the glowing effect on the reported body")
        public int duration = 60;
        @Comment("Reporting sound volume")
        public float volume = 1.0f;
    }

    // other mods
    public static class FakeKnifeConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
    }

    public static class MedicalKitConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 60;
    }

    public static class WrenchConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 120;
    }
}
