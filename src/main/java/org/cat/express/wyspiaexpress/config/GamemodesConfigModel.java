package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("GamemodesConfig")
public class GamemodesConfigModel {
    @Comment("Use category-statistics weighting in HML and the legacy Wyspia mode. New modes use separate draft histories whenever game weights are enabled.")
    public boolean useCustomWeightedAssignment = true;

    @Comment("Base weight value used for customWeight assigment. The lower the value the more balanced the system is. High values add more variance (it is not recommended to go above 0.01!)." +
            "Don't change this value unless you know what you are doing!")
    public double baseWeight = 0.002;

    @Comment("Neutral role dividend")
    @ConfigRange(min = 3, max = 99)
    public int neutralDividend = 6;


    @Comment("Combine killer and true-neutral assignment histories. Killer-aligned history remains separate.")
    public boolean combineKillerAndTrueNeutralHistory = false;

    @Comment("Minimum participants for choosing a true-neutral round once that mode is implemented.")
    @ConfigRange(min = 1, max = 99)
    public int trueNeutralMinimumPlayers = 12;

    @Comment("Chance of choosing a true-neutral round once that mode is implemented. Does not enable the mode.")
    @ConfigRange(min = 0, max = 1)
    public double trueNeutralChance = 0.5;

}
