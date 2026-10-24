package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("GamemodesConfig")
public class GamemodesConfigModel {
    @Comment("Use custom weighted assignment")
    public boolean useCustomWeightedAssignment = true;

    @Comment("Base weight value used for customWeight assigment. The lower the value the more balanced the system is. High values add more variance (it is not recommended to go above 0.01!)." +
            "Don't change this value unless you know what you are doing!")
    public double baseWeight = 0.002;

    @Comment("Neutral role dividend")
    @ConfigRange(min = 3, max = 99)
    public int neutralDividend = 6;


    @Comment("Use the same weights for killer and true neutral roles. Killer sided neutrals still have separate weights")
    public boolean shareKillerNeutralWeights = false;

    @Comment("Minimum amount of players for alternate mode to start a true neutral round. At least 12 players are still needed")
    @ConfigRange(min = 1, max = 99)
    public int minimumPlayersForNeutralMode = 12;

    @Comment("Chance of alternate mode starting a true neutral round. 0.5 means 50%")
    @ConfigRange(min = 0, max = 1)
    public double neutralModeChance = 0.5;

}
