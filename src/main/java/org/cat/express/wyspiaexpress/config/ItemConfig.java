package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

public class ItemConfig {

    @Comment("Cooldown for PSYCHO_MODE, in seconds")
    public int psychoModeCooldown = 300;

    @Comment("Cooldown for BLACKOUT, in seconds")
    public int blackOutCooldown = 165;

    @Comment("Cooldown for POWER_RESTORE, in seconds")
    public int powerRestoreCooldown = 180;


    // mine
    @Comment("Configuration for FAKE_REVOLVER")
    public FakeRevolverConfig fakeRevolverConfig = new FakeRevolverConfig();

    @Comment("Configuration for MEGAPHONE")
    public MegaphoneConfig megaphoneConfig = new MegaphoneConfig();

    @Comment("Configuration for TAPE")
    public TapeConfig tapeConfig = new TapeConfig();

    @Comment("Configuration for OUTLAW_REVOLVER")
    public OutlawRevolverConfig outlawRevolverConfig = new OutlawRevolverConfig();

    @Comment("Configuration for RITUAL_DAGGER")
    public RitualDaggerConfig ritualDaggerConfig = new RitualDaggerConfig();

    @Comment("Configuration for SENSE_DEAD")
    public SenseDeadConfig senseDeadConfig = new SenseDeadConfig();

    @Comment("Configuration for SMOKE_BOMB")
    public SmokeBombConfig smokeBombConfig = new SmokeBombConfig();

    // wathe
    @Comment("Configuration for REVOLVER")
    public RevolverConfig revolverConfig = new RevolverConfig();

    @Comment("Configuration for KNIFE")
    public KnifeConfig knifeConfig = new KnifeConfig();

    @Comment("Configuration for LOCK_PICK")
    public LockPickConfig lockPickConfig = new LockPickConfig();

    @Comment("Configuration for BODY_BAG")
    public BodyBagConfig bodyBagConfig = new BodyBagConfig();

    @Comment("Configuration for GRENADE")
    public GrenadeConfig grenadeConfig = new GrenadeConfig();

    @Comment("Configuration for DERRINGER")
    public DerringerConfig derringerConfig = new DerringerConfig();

    // noelles roles
    @Comment("Configuration for FAKE_KNIFE")
    public FakeKnifeConfig fakeKnifeConfig = new FakeKnifeConfig();

    // kin's wathe
    @Comment("Configuration for HUNTING_KNIFE")
    public HuntingKnifeConfig huntingKnifeConfig = new HuntingKnifeConfig();

    @Comment("Configuration for POISON_INJECTOR")
    public PoisonInjectorConfig poisonInjectorConfig = new PoisonInjectorConfig();

    @Comment("Configuration for BLOWGUN")
    public BlowgunConfig blowgunConfig = new BlowgunConfig();

    @Comment("Configuration for KNOCKOUT_DRUG")
    public KnockoutDrugConfig knockoutDrugConfig = new KnockoutDrugConfig();

    @Comment("Configuration for CAPTURE_DEVICE")
    public CaptureDeviceConfig captureDeviceConfig = new CaptureDeviceConfig();

    @Comment("Configuration for PAN")
    public PanConfig panConfig = new PanConfig();

    @Comment("Configuration for PILL")
    public PillConfig pillConfig = new PillConfig();

    @Comment("Configuration for ACID_BARREL")
    public AcidBarrelConfig acidBarrelConfig = new AcidBarrelConfig();

    @Comment("Configuration for MEDICAL_KIT")
    public MedicalKitConfig medicalKitConfig = new MedicalKitConfig();

    @Comment("Configuration for WRENCH")
    public WrenchConfig wrenchConfig = new WrenchConfig();


    // mine
    public static class FakeRevolverConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Enable fake revolver particles and recoil")
        public boolean enableShotEffects = true;

        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 20;


    }
    public static class MegaphoneConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 60;

        @Comment("Duration, in seconds, of the glowing effect on the reported body")
        public int duration = 60;

        @Comment("Reporting sound volume")
        public float volume = 1.0f;

    }
    public static class TapeConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 30;

        @Comment("Enable sound")
        public boolean enableSound = false;

        @Comment("Countdown, in ticks, before you can remove the tape")
        public int removeCountdown = 100;

        @Comment("Enable passive mood lost")
        public boolean enableMoodLost = false;

        @Comment("Mood lost amount")
        public float moodLostAmount = 0.05f;

    }
    public static class OutlawRevolverConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 60;

        @Comment("Cooldown when hitting nothing, in seconds, minimum 0")
        public int missCooldown = 90;

    }
    public static class RitualDaggerConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 120;

        @Comment("Duration of the stun, in ticks")
        public int duration = 50;

    }
    public static class SenseDeadConfig{
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 45;

        @Comment("Duration, in seconds, of seeing dead bodies")
        public int duration = 20;

    }
    public static class SmokeBombConfig {
        @Comment("Show the predicted explosion area and highlight affected players while holding a smoke bomb")
        public boolean showTrajectory = true;

        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 60;

        @Comment("Radius of the explosion")
        @ConfigRange(min = 0.0, max = 10.0)
        public double radius = 3.0;

        @Comment("Radius of the explosion to the owner.")
        @ConfigRange(min = 0.0, max = 10.0)
        public double ownerRadius = 2.1;

        @Comment("Duration of the effect, in ticks")
        public int duration = 120;

        @ConfigRange(min = 0.0, max = 10.0)
        public double smokeRadius = 3.0;

        @Comment("Duration of the effect, in ticks")
        public int smokeDuration = 160;

        @Comment("Enable smoke apply linger effect")
        public boolean linger = true;

        @Comment("Duration of the lingering effects, in ticks, after entering the smoke")
        public int lingerDuration = 30;

        @Comment("Player movement speed slowdown percent")
        public double slowness = 0.50;

        @Comment("Amount of particles to be spawned per tick frequency")
        public int particles = 1500;

    }
    // wathe
    public static class RevolverConfig {
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 10;

    }
    public static class KnifeConfig {
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 60;

    }
    public static class LockPickConfig {
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 90;

    }
    public static class BodyBagConfig {
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 30;

    }
    public static class GrenadeConfig {
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 120;

        @Comment("Show the predicted explosion area and highlight affected players while holding a grenade")
        public boolean showTrajectory = true;

        @Comment("Radius of the explosion")
        @ConfigRange(min = 0.0, max = 10.0)
        public double radius = 3.0;

        @Comment("Radius of the explosion to the owner.")
        @ConfigRange(min = 0.0, max = 10.0)
        public double ownerRadius = 2.1;

    }
    public static class DerringerConfig {
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 30;

    }
    // noelles roles
    public static class FakeKnifeConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
    }
    // mine
    public static class HuntingKnifeConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 45;

    }
    public static class PoisonInjectorConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 60;

    }
    public static class BlowgunConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 60;

    }
    public static class KnockoutDrugConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 45;

    }
    public static class CaptureDeviceConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 45;

    }
    public static class PanConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 45;

    }
    public static class PillConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 300;

    }
    public static class AcidBarrelConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 60;

    }
    public static class MedicalKitConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 60;

    }

    public static class WrenchConfig{
        @Comment("Basic item configuration")
        public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 30;

        @Comment("Whether to enable light restoration function")
        public boolean lightRestoration = true;

        @Comment("Radius of light restoration")
        public int radius = 20;

        @Comment("Cooldown of light restoration")
        public int lightRestorationCooldown = 120;

    }
}
