package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import dev.doctor4t.wathe.api.WatheRoles;
import io.wispforest.owo.config.annotation.RangeConstraint;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;

import java.util.ArrayList;
import java.util.List;

// defines the basic configs shared to all roles
public class RoleBasicConfig {

    @Comment("Whether this role has shop.")
    public boolean enableShop = false;
    @Comment("Shop entries.")
    public List<String> shopEntries = new ArrayList<>();
    @Comment("Has passive income. WIP, doesn't have any effect on other mods role that has passive income enabled")
    public boolean passiveIncome = false;
    @Comment("Amount of income from task. WIP, for any non custom roles please set this to 0, unless they didn't earn money originally")
    public int taskIncome = 0;
    @Comment("Starting items.")
    public List<EnumShopEntry> items = new ArrayList<>();
    @Comment("Starting item amount")
    public List<Integer> itemAmount = new ArrayList<>();

    // still WIP, doesn't have any effect on other mods roles
    @Comment("Maximum sprint time. -1 to have infinite stamina. WIP, doesn't have any effect on other mods role")
    @RangeConstraint(min = -1, max = 6000)
    public int maxSprintTime = WatheRoles.CIVILIAN.getMaxSprintTime();

    @Comment("Minimum number of players to spawn this role. Can only take values from 1 to 20")
    @RangeConstraint(min = 1, max = 20)
    public int minimumPlayerSpawn = 5;

    @Comment("Maximum number of this role spawning. Can only take values from 0 to 20")
    @RangeConstraint(min = 0, max = 20)
    public int maximumSpawn = 1;
    @Comment("Can see the game timer. WIP, doesn't have any effect on other mods role")
    public boolean seeTimer = false;

}
