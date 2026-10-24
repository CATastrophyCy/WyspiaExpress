package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("GamemodesConfig")
public class GamemodesConfigModel {
    @Comment("Use category-statistics weighting in HML and the legacy Wyspia mode. New modes use separate draft histories whenever game weights are enabled.")
    public boolean useCustomWeightedAssignment = true;

    @Comment("Positive minimum lottery weight. New Wyspia modes add accumulated assignment credit; HML/legacy modes add their frequency deficit. Lower values favor balancing more strongly.")
    public double baseWeight = 0.002;

    @Comment("Neutral role dividend")
    @ConfigRange(min = 3, max = 99)
    public int neutralDividend = 6;


    @Comment("Combine killer and true-neutral assignment histories. Killer-aligned history remains separate.")
    public boolean combineKillerAndTrueNeutralHistory = false;

    @Comment("Minimum participants for an Alternate launcher round to choose true neutrals. The neutral mode's twelve-player minimum also applies.")
    @ConfigRange(min = 1, max = 99)
    public int trueNeutralMinimumPlayers = 12;

    @Comment("Chance of choosing a true-neutral round through the Alternate launcher when its player requirement is met.")
    @ConfigRange(min = 0, max = 1)
    public double trueNeutralChance = 0.5;

}
