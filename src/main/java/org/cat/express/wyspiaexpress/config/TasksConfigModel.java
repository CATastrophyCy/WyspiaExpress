package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;

@ConfigModel("TasksConfig")
public class TasksConfigModel {

        @Comment("Seconds before the first task after a reset")
        @ConfigRange(min = 0, max = 107374182)
        public int firstTaskDelaySeconds = 30;

        @Comment("Minimum time, in seconds, to get a new task after completing one")
        @ConfigRange(min = 0, max = 107374182)
        public int minTaskDelaySeconds = 30;

        @Comment("Maximum time, in seconds, to get a new task after completing one")
        @ConfigRange(min = 0, max = 107374182)
        public int maxTaskDelaySeconds = 45;

        @Comment("Fraction of full mood drained per second. Default: a full bar in 240 seconds, is a drain of '0.00416666666' ")
        @ConfigRange(min = 0, max = 1)
        public double moodDrainPerSecond = 1.0 / 240.0;

        @Comment("Enable passive mood drain when the player doesn't have a task")
        public boolean drainMoodWithoutTask = false;

        @Comment("Prevent players from getting the same task twice in a row")
        public boolean preventRepeatedTasks = true;

        @Comment("Amount of mood restored when completing a task, from 0.0 to 1.0")
        @ConfigRange(min = 0, max = 1)
        public float taskMoodReward = 0.5F;

}
