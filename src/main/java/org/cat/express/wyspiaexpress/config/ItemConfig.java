package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Nest;

public class ItemConfig {
    // mine
    @Comment("Configuration for FAKE_REVOLVER")
    @Nest public FakeRevolverConfig fakeRevolverConfig = new FakeRevolverConfig();
    // other mods
    @Comment("Configuration for FAKE_KNIFE")
    @Nest public FakeKnifeConfig fakeKnifeConfig = new FakeKnifeConfig();

    @Comment("Configuration for MEDICAL_KIT")
    @Nest public MedicalKitConfig medicalKitConfig = new MedicalKitConfig();

    // mine
    public static class FakeRevolverConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
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
