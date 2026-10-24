package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("ModeConfig")
public class ModeConfigModel {
    @Comment("Players per killer. -1 inherits Wathe's native setting. Used by the Wyspia assignment implementation.")
    @ConfigRange(min = -1, max = 99)
    public int killerDividend = -1;

    @Comment("Players per vigilante. -1 inherits Wathe's native setting.")
    @ConfigRange(min = -1, max = 99)
    public int vigilanteDividend = -1;

    @Comment("Players per killer-aligned neutral, independently of true-neutral slots.")
    @ConfigRange(min = 3, max = 99)
    public int killerAlignedNeutralDividend = 9;

    @Comment("Players per main true-neutral slot in the true-neutral mode.")
    @ConfigRange(min = 3, max = 99)
    public int trueNeutralDividend = 6;

}
