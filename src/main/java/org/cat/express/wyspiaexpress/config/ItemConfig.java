package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Nest;

public class ItemConfig {

    @Comment("Configuration for FAKE_KNIFE")
    @Nest public FakeKnifeConfig fakeKnifeConfig = new FakeKnifeConfig();

    @Comment("Configuration for MEDICAL_KIT")
    @Nest public MedicalKitConfig medicalKitConfig = new MedicalKitConfig();


    public static class FakeKnifeConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
    }

    public static class MedicalKitConfig{
        @Comment("Basic item configuration")
        @Nest public ItemBasicConfig basic = new ItemBasicConfig();
    }
}
