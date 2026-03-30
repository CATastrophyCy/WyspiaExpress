package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import org.agmas.harpymodloader.modded_murder.ModdedWeights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScoreboardRoleSelectorComponent.class)
public class WeightsResetMixin {

    @Inject(method = "reset", at = @At("TAIL"), cancellable = false)
    public void wyspiaexpress$reset(CallbackInfoReturnable<Integer> cir) {
        ModdedWeights.roleRounds.clear();
        ModdedWeights.roleWeights.clear();
    }
}