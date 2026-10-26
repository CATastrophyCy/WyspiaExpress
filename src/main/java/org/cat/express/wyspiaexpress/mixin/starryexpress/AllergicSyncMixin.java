package org.cat.express.wyspiaexpress.mixin.starryexpress;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.aussiebox.starexpress.cca.AllergicComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin(AllergicComponent.class)
public abstract class AllergicSyncMixin {
    @Shadow public int armor;
    @Shadow private int glowTicks;
    @Unique private int wyspiaexpress$broadcastArmor;
    @Unique private boolean wyspiaexpress$broadcastGlowing;

    // only sync if actual changes to those values are done
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lorg/aussiebox/starexpress/cca/AllergicComponent;sync()V"))
    private void wyspiaexpress$syncChanges(AllergicComponent component, Operation<Void> original) {
        if (armor != wyspiaexpress$broadcastArmor
                || (glowTicks > 0) != wyspiaexpress$broadcastGlowing) {
            original.call(component);
        }
    }

    @Inject(method = "sync", at = @At("RETURN"))
    private void wyspiaexpress$rememberBroadcast(CallbackInfo ci) {
        wyspiaexpress$broadcastArmor = armor;
        wyspiaexpress$broadcastGlowing = glowTicks > 0;
    }
}
