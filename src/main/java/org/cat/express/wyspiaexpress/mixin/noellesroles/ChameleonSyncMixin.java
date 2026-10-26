package org.cat.express.wyspiaexpress.mixin.noellesroles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.agmas.noellesroles.chameleon.ChameleonPlayerComponent;

@Mixin(ChameleonPlayerComponent.class)
public abstract class ChameleonSyncMixin {
    @Shadow public int hidingTicks;
    @Unique private int wyspiaexpress$broadcastRenderedTicks;

    // Body alpha saturates at 400, but the first-person hand keeps shrinking until 1000.
    // only sync if the hiding tick has actually changed! we set a cap on the hiding tick so it doesn't infinitely increase
    @Unique private int wyspiaexpress$renderedTicks() {
        return Math.clamp(hidingTicks, 0, 1000);
    }
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lorg/agmas/noellesroles/chameleon/ChameleonPlayerComponent;sync()V"))
    private void wyspiaexpress$syncChanges(ChameleonPlayerComponent component, Operation<Void> original) {
        if (wyspiaexpress$renderedTicks() != wyspiaexpress$broadcastRenderedTicks) {
            original.call(component);
        }
    }
    @Inject(method = "sync", at = @At("RETURN"))
    private void wyspiaexpress$rememberBroadcast(CallbackInfo ci) {
        wyspiaexpress$broadcastRenderedTicks = wyspiaexpress$renderedTicks();
    }
}
