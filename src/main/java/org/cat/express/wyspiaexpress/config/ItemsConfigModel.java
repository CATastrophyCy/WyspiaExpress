package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("ItemsConfig")
public class ItemsConfigModel {

    @Comment("""
    === ITEM CONFIGURATION GUIDE ===
    - renderItemOnHand: Render Item on hand?
    - canPunchPlayers: Can punch players
    - dropOnDeath: Should drop on death
    """)
    public ItemConfig itemConfig = new ItemConfig();
}
