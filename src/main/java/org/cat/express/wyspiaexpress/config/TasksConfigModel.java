package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("TasksConfig")
public class TasksConfigModel {

        @Comment("Seconds before the first task after a reset")
        @ConfigRange(min = 0, max = 107374182)
        public int firstTaskDelaySeconds = 30;

        @Comment("Minimum seconds to wait for a new task after completion.")
        @ConfigRange(min = 0, max = 107374182)
        public int minTaskDelaySeconds = 30;

        @Comment("Maximum seconds to wait for a new task after completion.")
        @ConfigRange(min = 0, max = 107374182)
        public int maxTaskDelaySeconds = 45;

        @Comment("Fraction of full mood drained per second. Default: a full bar in 240 seconds, is a drain of '0.00416666666' ")
        @ConfigRange(min = 0, max = 1)
        public double passiveDrainPerSecond = 1.0 / 240.0;

        @Comment("Enable mood drain while no active task.")
        public boolean passiveMoodDrain = false;

        @Comment("Prevent rolling the same task twice in a row.")
        public boolean preventConsecutiveTasks = true;

        @Comment("Amount of mood restored per completed task.")
        @ConfigRange(min = 0, max = 1)
        public float completionRestoration = 0.5F;

}
