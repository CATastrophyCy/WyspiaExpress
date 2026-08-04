package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.RangeConstraint;

public class ItemConfig {

    @Comment("Cooldown for PSYCHO_MODE, in seconds")
    public int psychoModeCooldown = 300;
    @Comment("Cooldown for BLACKOUT, in seconds")
    public int blackOutCooldown = 165;
    @Comment("Cooldown for POWER_RESTORE, in seconds")
    public int powerRestoreCooldown = 180;

    // mine
    @Comment("Configuration for FAKE_REVOLVER")
    @Nest public FakeRevolverConfig fakeRevolverConfig = new FakeRevolverConfig();

    @Comment("Configuration for MEGAPHONE")
    @Nest public MegaphoneConfig megaphoneConfig = new MegaphoneConfig();

    @Comment("Configuration for TAPE")
    @Nest public TapeConfig tapeConfig = new TapeConfig();

    @Comment("Configuration for OUTLAW_REVOLVER")
    @Nest public OutlawRevolverConfig outlawRevolverConfig = new OutlawRevolverConfig();

    @Comment("Configuration for RITUAL_DAGGER")
    @Nest public RitualDaggerConfig ritualDaggerConfig = new RitualDaggerConfig();

    @Comment("Configuration for SENSE_DEAD")
    @Nest public SenseDeadConfig senseDeadConfig = new SenseDeadConfig();

    @Comment("Configuration for SMOKE_BOMB")
    @Nest public SmokeBombConfig smokeBombConfig = new SmokeBombConfig();

    // wathe
    @Comment("Configuration for REVOLVER")
    @Nest public RevolverConfig revolverConfig = new RevolverConfig();

    @Comment("Configuration for KNIFE")
    @Nest public KnifeConfig knifeConfig = new KnifeConfig();

    @Comment("Configuration for LOCK_PICK")
    @Nest public LockPickConfig lockPickConfig = new LockPickConfig();

    @Comment("Configuration for BODY_BAG")
    @Nest public BodyBagConfig bodyBagConfig = new BodyBagConfig();

    @Comment("Configuration for GRENADE")
    @Nest public GrenadeConfig grenadeConfig = new GrenadeConfig();

    @Comment("Configuration for DERRINGER")
    @Nest public DerringerConfig derringerConfig = new DerringerConfig();

    // noelles roles
    @Comment("Configuration for FAKE_KNIFE")
    @Nest public FakeKnifeConfig fakeKnifeConfig = new FakeKnifeConfig();

    // kin's wathe
    @Comment("Configuration for HUNTING_KNIFE")
    @Nest public HuntingKnifeConfig huntingKnifeConfig = new HuntingKnifeConfig();

    @Comment("Configuration for POISON_INJECTOR")
    @Nest public PoisonInjectorConfig poisonInjectorConfig = new PoisonInjectorConfig();

    @Comment("Configuration for BLOWGUN")
    @Nest public BlowgunConfig blowgunConfig = new BlowgunConfig();

    @Comment("Configuration for KNOCKOUT_DRUG")
    @Nest public KnockoutDrugConfig knockoutDrugConfig = new KnockoutDrugConfig();

    @Comment("Configuration for CAPTURE_DEVICE")
    @Nest public CaptureDeviceConfig captureDeviceConfig = new CaptureDeviceConfig();

    @Comment("Configuration for PAN")
    @Nest public PanConfig panConfig = new PanConfig();

    @Comment("Configuration for PILL")
    @Nest public PillConfig pillConfig = new PillConfig();

    @Comment("Configuration for ACID_BARREL")
    @Nest public AcidBarrelConfig acidBarrelConfig = new AcidBarrelConfig();

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
    public static class TapeConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
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
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 60;
        @Comment("Cooldown when hitting nothing, in seconds, minimum 0")
        public int missCooldown = 90;
    }
    public static class RitualDaggerConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
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
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 60;
        @Comment("Radius of the explosion")
        @RangeConstraint(min = 0.0, max = 10.0)
        public double radius = 2.0;
        @Comment("Radius of the explosion to the owner.")
        @RangeConstraint(min = 0.0, max = 10.0)
        public double ownerRadius = 1.5;
        @Comment("Duration of the effect, in ticks")
        public int duration = 100;
    }
    // wathe
    public static class RevolverConfig {
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 10;
    }
    public static class KnifeConfig {
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 60;
    }
    public static class LockPickConfig {
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 30;
    }
    public static class BodyBagConfig {
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 30;
    }
    public static class GrenadeConfig {
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 120;
        @Comment("Radius of the explosion")
        @RangeConstraint(min = 0.0, max = 10.0)
        public double radius = 3.0;
        @Comment("Radius of the explosion to the owner.")
        @RangeConstraint(min = 0.0, max = 10.0)
        public double ownerRadius = 1.8;
    }
    public static class DerringerConfig {
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds, minimum 0")
        public int cooldown = 30;
    }
    // noelles roles
    public static class FakeKnifeConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
    }
    // mine
    public static class HuntingKnifeConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 45;
    }
    public static class PoisonInjectorConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 60;
    }
    public static class BlowgunConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 60;
    }
    public static class KnockoutDrugConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 45;
    }
    public static class CaptureDeviceConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 45;
    }
    public static class PanConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 45;
    }
    public static class PillConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 300;
    }
    public static class AcidBarrelConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
        @Comment("Cooldown, in seconds")
        public int cooldown = 60;
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
        public int cooldown = 30;
    }
}
