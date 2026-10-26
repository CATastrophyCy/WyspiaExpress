package org.cat.express.wyspiaexpress.mixin.noellesroles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.agmas.noellesroles.vulture.VulturePlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulturePlayerComponent.class)
public abstract class VultureSyncMixin {

    // we are skipping the serverTick sync since other gameplay parts that changes the component already calls sync
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lorg/agmas/noellesroles/vulture/VulturePlayerComponent;sync()V"))
    private void wyspiaexpress$skipTickSync(VulturePlayerComponent component, Operation<Void> original) {
    }
}
