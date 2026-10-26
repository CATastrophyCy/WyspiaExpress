package org.cat.express.wyspiaexpress.mixin.starryexpress;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SilenceComponent.class)
public abstract class SilenceSyncMixin {
    @Shadow private boolean silenced;
    @Unique private boolean wyspiaexpress$broadcastSilenced;


    // only sync if the player is actually silenced!
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lorg/aussiebox/starexpress/cca/SilenceComponent;sync()V"))
    private void wyspiaexpress$syncWhileSilenced(SilenceComponent component, Operation<Void> original) {
        if (silenced || wyspiaexpress$broadcastSilenced) original.call(component);
    }

    @Inject(method = "sync", at = @At("RETURN"))
    private void wyspiaexpress$rememberSilence(CallbackInfo ci) {
        wyspiaexpress$broadcastSilenced = silenced;
    }
}
