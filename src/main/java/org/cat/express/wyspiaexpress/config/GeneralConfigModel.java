package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("GeneralConfig")
public class GeneralConfigModel {
    @Comment("Hide StarryExpress's old inventory guidebook button. The new guide remains available through its keybind.")
    public boolean suppressOldGuidebook = true;

    @Comment("Enable spectator special instinct showing more information")
    public boolean spectatorSpecialInstinct = true;

    @Comment("Enable spectator special hud showing more information")
    public boolean spectatorSpecialHud = true;

}
