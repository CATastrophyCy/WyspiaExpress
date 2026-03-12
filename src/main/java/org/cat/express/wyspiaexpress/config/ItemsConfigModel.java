package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.Sync;

@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "wyspiaexpress/items", wrapperName = "WyspiaExpressItemsConfig")
public class ItemsConfigModel {

    @Comment("""
    === ITEM CONFIGURATION GUIDE ===
    - renderItemOnHand: Render Item on hand?
    - canPunchPlayers: Can punch players
    - dropOnDeath: Should drop on death
    """)
    @Nest public ItemConfig itemConfig = new ItemConfig();
}
