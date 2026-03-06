package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import dev.doctor4t.wathe.api.WatheRoles;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.RangeConstraint;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;

import java.util.ArrayList;
import java.util.List;

// defines the basic configs shared to all roles

public class RoleBasicConfig {

    @Comment("Whether this civilian role has shop.")
    public boolean enableShop = true;

    @Comment("Shop options.")
    @Nest public ShopConfig shopConfig = new ShopConfig();


    // still WIP, doesn't have any effect
    @Comment("Starting items.")
    public List<EnumShopEntry> items = new ArrayList<>();
    // still WIP, doesn't have any effect
    @Comment("Starting item amount")
    public List<Integer> itemAmount = new ArrayList<>();

    // still WIP, doesn't have any effect on other mods roles
    @Comment("Maximum sprint time. -1 to have infinite stamina.")
    @RangeConstraint(min = -1, max = 6000)
    public int maxSprintTime = WatheRoles.CIVILIAN.getMaxSprintTime();

    @Comment("Minimum number of players to spawn this role. Can only take values from 1 to 20")
    @RangeConstraint(min = 1, max = 20)
    public int minimumPlayerSpawn = 5;

    @Comment("Maximum number of this role spawning. Can only take values from 0 to 20")
    @RangeConstraint(min = 0, max = 20)
    public int maximumSpawn = 1;
    @Comment("Can see the game timer")
    public boolean seeTimer = false;

}
