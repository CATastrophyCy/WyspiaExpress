package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.Sync;

@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "wyspiaexpress/modifiers", wrapperName = "WyspiaExpressModifiersConfig")
public class ModifiersConfig {

    @Comment("Config options for PROFESIONAL_VENT_CRAWLER")
    @Nest public VentCrawlerConfig ventCrawlerConfig = new VentCrawlerConfig();
    public static class VentCrawlerConfig{
        @Comment("Crawling speed boost. Is multiplicative with general crawl speed boost")
        public double crawlSpeedModifier = 1.5;

    }
}
