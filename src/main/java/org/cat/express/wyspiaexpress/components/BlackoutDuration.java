package org.cat.express.wyspiaexpress.components;

/** The synchronized duration of Wathe's current blackout. */
public interface BlackoutDuration {
    int getBlackoutRemainingTicks();

    int getBlackoutDurationTicks();

    /** Remaining time until the first light restores; stays zero after that point. */
    int getBlackoutMinimumRemainingTicks();
}
