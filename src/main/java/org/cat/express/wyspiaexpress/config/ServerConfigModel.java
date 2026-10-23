package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import java.util.ArrayList;
import java.util.List;

@ConfigModel("ServerConfig")
public class ServerConfigModel {
    @Comment("Number of extra spectator voicechats, minimum 5")
    @ConfigRange(min = 5, max = 30)
    public int extraSpectatorsVoicechat = 5;

    @Comment("Whether to enable random start tp")
    public boolean enableRandomStartTp = true;

    @Comment("The starting positions to tp player to, must be inside the ready area")
    public List<String> randomStartTp = new ArrayList<>(List.of(
            "-1002.5, 2.0, -361.5, -90.0, 0.0"
    ));

    @Comment("The position the TP item tps player to")
    public TpTargetPosition readyTrainTp = new TpTargetPosition();

    public static class TpTargetPosition {
        public double x, y, z;



        public float yaw, pitch;


        public TpTargetPosition() {}
        public TpTargetPosition(double x, double y, double z, float yaw, float pitch) {
            this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.pitch = pitch;
        }
    }
}
