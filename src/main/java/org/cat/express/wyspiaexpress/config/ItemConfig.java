package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Nest;

public class ItemConfig {
    // mine
    @Comment("Configuration for FAKE_REVOLVER")
    @Nest public FakeRevolverConfig fakeRevolverConfig = new FakeRevolverConfig();

    @Comment("Configuration for MEGAPHONE")
    @Nest public MegaphoneConfig megaphoneConfig = new MegaphoneConfig();
    // other mods
    @Comment("Configuration for FAKE_KNIFE")
    @Nest public FakeKnifeConfig fakeKnifeConfig = new FakeKnifeConfig();

    @Comment("Configuration for MEDICAL_KIT")
    @Nest public MedicalKitConfig medicalKitConfig = new MedicalKitConfig();


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
    }

}
