package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.WyspiaExpress;

/** Extra state on Wathe's psycho component; saved and synced with that component. */
public interface PsychoModeState {
    static PsychoModeState get(PlayerEntity player) {
        return (PsychoModeState) PlayerPsychoComponent.KEY.get(player);
    }

    int getPsychoTicks();
    int getBlockedHits();
    void blockHit();

    default int getPenalizedHits() {
        return Math.max(0, getBlockedHits() - WyspiaExpress.RULES_CONFIG.psycho.freeHits());
    }

    default float movementMultiplier() {
        var config = WyspiaExpress.RULES_CONFIG.psycho;
        return config.rework() && getPsychoTicks() > 0
                ? (float) (1 - Math.min(config.maxSlow(), getPenalizedHits() * config.slowPerHit())) : 1;
    }

    default float batDelayTicks() {
        var config = WyspiaExpress.RULES_CONFIG.psycho;
        return getPsychoTicks() > 0
                ? (float) (Math.min(config.maxBatDelay(), getPenalizedHits() * config.batDelayPerHit()) * 20) : 0;
    }
}
