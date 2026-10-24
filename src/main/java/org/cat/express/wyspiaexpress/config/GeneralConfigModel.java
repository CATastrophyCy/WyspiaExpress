package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("GeneralConfig")
public class GeneralConfigModel {
    @Comment("Disable the old StarryExpress guidebook button")
    public boolean disableOldGuidebook = true;

    @Comment("Enable spectator special instinct showing more information")
    public boolean spectatorSpecialInstinct = true;

    @Comment("Enable spectator special hud showing more information")
    public boolean spectatorSpecialHud = true;

}
