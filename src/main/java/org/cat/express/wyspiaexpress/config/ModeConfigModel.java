package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("ModeConfig")
public class ModeConfigModel {
    @Comment("Don't hide roles from the guidebook just because they don't belong to this gamemode")
    public boolean hideGamemode = false;

    @Comment("Amount of players needed for each killer. -1 uses Wathe's setting")
    @ConfigRange(min = -1, max = 99)
    public int killerDividend = -1;

    @Comment("Amount of players needed for each vigilante. -1 uses Wathe's setting")
    @ConfigRange(min = -1, max = 99)
    public int vigilanteDividend = -1;

    @Comment("Amount of players needed for each killer sided neutral")
    @ConfigRange(min = 3, max = 99)
    public int killerAlignedNeutralDividend = 9;

    @Comment("Amount of players needed for each true neutral in true neutral mode")
    @ConfigRange(min = 3, max = 99)
    public int trueNeutralDividend = 6;

}
