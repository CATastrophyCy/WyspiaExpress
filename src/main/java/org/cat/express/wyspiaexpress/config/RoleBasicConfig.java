package org.cat.express.wyspiaexpress.config;

import io.wispforest.owo.config.annotation.RangeConstraint;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;

import java.util.ArrayList;
import java.util.List;

// defines the basic configs shared to all roles
public class RoleBasicConfig {

    public boolean enableShop = false;
    public List<String> shopEntries = new ArrayList<>();
    public boolean passiveIncome = false;
    public int taskIncome = 0;
    public List<EnumShopEntry> items = new ArrayList<>();
    public List<Integer> itemAmount = new ArrayList<>();
    public int minimumPlayerSpawn = 1;
    public int maximumPlayerSpawn = 99;

    @RangeConstraint(min = 0, max = 20)
    public int maximumSpawn = 1;

}
