package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Sync;

@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "wyspiaexpress/general", wrapperName = "WyspiaExpressServerConfig")
public class ServerConfig {
    @Comment("Add this percentage to crawling speed")
    public float crawlSpeedMultiplier = 0.5F;
    @Comment("Wheter killer role will always have guesser added. With this enabled it is recommended to have guesser disabled.")
    public boolean killerAlwaysGuesser = false;
}