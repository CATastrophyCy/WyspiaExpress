package org.cat.express.wyspiaexpress.mixin.noellesroles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.agmas.noellesroles.recaller.RecallerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecallerPlayerComponent.class)
public abstract class RecallerSyncMixin {

    // we are skipping the serverTick sync since other gameplay parts that changes the component already calls sync
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lorg/agmas/noellesroles/recaller/RecallerPlayerComponent;sync()V"))
    private void wyspiaexpress$skipTickSync(RecallerPlayerComponent component, Operation<Void> original) {
    }
}
