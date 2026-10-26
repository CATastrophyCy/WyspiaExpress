package org.cat.express.wyspiaexpress.mixin.noellesroles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;


@Mixin(BartenderPlayerComponent.class)
public abstract class BartenderSyncMixin {
    @Shadow public int glowTicks;
    @Shadow public int armor;
    @Unique private boolean wyspiaexpress$broadcastGlowing;
    @Unique private int wyspiaexpress$broadcastArmor;

    // only sync after armor has changed since there are some parts that change armor without calling sync
    // and sync if we change the state of glowing
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lorg/agmas/noellesroles/bartender/BartenderPlayerComponent;sync()V"))
    private void wyspiaexpress$syncChanges(BartenderPlayerComponent component, Operation<Void> original) {
        if ((glowTicks > 0) != wyspiaexpress$broadcastGlowing
                || armor != wyspiaexpress$broadcastArmor) {
            original.call(component);
        }
    }

    @Inject(method = "sync", at = @At("RETURN"))
    private void wyspiaexpress$rememberBroadcast(CallbackInfo ci) {
        wyspiaexpress$broadcastGlowing = glowTicks > 0;
        wyspiaexpress$broadcastArmor = armor;
    }
}
