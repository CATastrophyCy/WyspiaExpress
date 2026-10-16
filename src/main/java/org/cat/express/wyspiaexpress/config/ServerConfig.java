package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.Sync;

import java.util.ArrayList;
import java.util.List;

@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "wyspiaexpress/general", wrapperName = "WyspiaExpressServerConfig")
public class ServerConfig {
    @Comment("Config options for mood and tasks")
    @Nest public MoodTasks moodTasks = new MoodTasks();

    public static class MoodTasks {
        @Comment("Seconds before the first task after a reset")
        @RangeConstraint(min = 0, max = 107374182)
        public int firstTaskDelaySeconds = 30;
        @Comment("Minimum seconds to wait for a new task after completion.")
        @RangeConstraint(min = 0, max = 107374182)
        public int minTaskDelaySeconds = 30;
        @Comment("Maximum seconds to wait for a new task after completion.")
        @RangeConstraint(min = 0, max = 107374182)
        public int maxTaskDelaySeconds = 45;
        @Comment("Fraction of full mood drained per second. Default: a full bar in 240 seconds, is a drain of '0.00416666666' ")
        @RangeConstraint(min = 0, max = 1)
        public double passiveDrainPerSecond = 1.0 / 240.0;
        @Comment("Enable mood drain while no active task.")
        public boolean passiveMoodDrain = false;
        @Comment("Prevent rolling the same task twice in a row.")
        public boolean preventConsecutiveTasks = true;
        @Comment("Amount of mood restored per completed task.")
        @RangeConstraint(min = 0, max = 1)
        public float completionRestoration = 0.5F;
    }

    @Comment("Hide StarryExpress's old inventory guidebook button. The new guide remains available through its keybind.")
    public boolean suppressOldGuidebook = true;
    @Comment("Use custom weighted assignment")
    public boolean useCustomWeightedAssignment = true;
    @Comment("Base weight value used for customWeight assigment. The lower the value the more balanced the system is. High values add more variance (it is not recommended to go above 0.01!)." +
            "Don't change this value unless you know what you are doing!")
    public double baseWeight = 0.002;
    @Comment("Neutral role dividend")
    @RangeConstraint(min = 3, max = 99)
    public int neutralDividend = 6;
    @Comment("Number of extra spectator voicechats, minimum 5")
    @RangeConstraint(min = 5, max = 30)
    public int extraSpectatorsVoicechat = 5;
    @Comment("Enable spectator special instinct showing more information")
    public boolean spectatorSpecialInstinct = true;
    @Comment("Enable spectator special hud showing more information")
    public boolean spectatorSpecialHud = true;
    @Comment("Enable killer having role color instinct on killer-aligned neutrals")
    public boolean killerSpecialInstinct = false;
    @Comment("Multiply to crawling speed. i.e. 1.5 means crawling is 50% faster")
    public double crawlSpeedMultiplier = 2.0;
    @Comment("By setting this to true shooting a protected player won't drop your revolver regardless if they are innocent or not")
    public boolean disableProtectionGunDrop = true;
    @Comment("Player stun tick after blocking damage")
    public int blockStunTicks = 60;
    @Comment("Player stun tick after blocking damage with psycho mode")
    public int psychoStunTicks = 20;
    @Comment("Enable freezing")
    public boolean freeze = false;
    @Comment("The time, in seconds, it takes for a player to die when staying outside for too long")
    public int freezeTimer = 60;
    @Comment("Enable depression killing")
    public boolean depressionKilling = false;
    @Comment("The time, in seconds, it takes for a player to die when staying depressed for too long")
    public int depressedTimer = 90;
    @Comment("Whether to enable item bound checking, this will make out of bound item tp to closest dead body")
    public boolean enableItemBoundChecking = true;
    @Comment("Whether to enable random start tp")
    public boolean enableRandomStartTp = true;
    @Comment("The starting positions to tp player to, must be inside the ready area")
    public List<String> randomStartTp = new ArrayList<>(List.of(
            "-1002.5, 2.0, -361.5, -90.0, 0.0"
    ));
    @Comment("The position the TP item tps player to")
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
