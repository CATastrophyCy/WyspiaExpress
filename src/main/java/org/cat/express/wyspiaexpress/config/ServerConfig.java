package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.Sync;

import java.util.ArrayList;
import java.util.List;

@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "wyspiaexpress/general", wrapperName = "WyspiaExpressServerConfig")
public class ServerConfig {
    @Comment("Add this percentage to crawling speed. i.e. 0.5 means crawling is 50% faster")
    public double crawlSpeedMultiplier = 1.0;
    @Comment("Wheter killer role will always have guesser added. With this enabled it is recommended to have guesser disabled.")
    public boolean killerAlwaysGuesser = false;
    @Comment("Maximum amount of guessers when killerAlwaysGuesser is enabled.")
    public int maximumGuessers = 1;
    @Comment("Whether to enable item bound checking, this will make out of bound item tp to closest dead body.")
    public boolean enableItemBoundChecking = true;

    @Comment("Whether to enable random start tp.")
    public boolean enableRandomStartTp = true;
    @Comment("The starting positions to tp player to, must be inside the ready area.")
    public List<String> randomStartTp = new ArrayList<>(List.of(
            "-1002.5, 2.0, -361.5, -90.0, 0.0"
    ));
    @Comment("The position the TP item tps player to.")
    @Nest public TpTargetPosition readyTrainTp = new TpTargetPosition();

    public static class TpTargetPosition {
        public double x, y, z;
        public float yaw, pitch;
        public TpTargetPosition() {}
        public TpTargetPosition(double x, double y, double z, float yaw, float pitch) {
            this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.pitch = pitch;
        }
    }
}