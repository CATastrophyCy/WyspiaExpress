package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.RangeConstraint;
public class ItemBasicConfig {
    @Comment("Render Item on hand?")
    public boolean renderItemOnHand = true;
    @Comment("Can punch players")
    public boolean canPunchPlayers = true;
}
