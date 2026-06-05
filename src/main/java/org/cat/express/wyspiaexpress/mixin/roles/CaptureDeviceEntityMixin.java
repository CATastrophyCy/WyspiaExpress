package org.cat.express.wyspiaexpress.mixin.roles;

import org.BsXinQin.kinswathe.entities.CaptureDeviceEntity;
import org.cat.express.wyspiaexpress.components.CaptureDeviceEntityComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

@Mixin(CaptureDeviceEntity.class)
public abstract class CaptureDeviceEntityMixin {
    @Inject(method = "setOwner", at = @At("TAIL"))
    private void onSetOwner(UUID technicianUUID, CallbackInfo ci) {
        CaptureDeviceEntityComponent component = CaptureDeviceEntityComponent.KEY.get(this);
        component.setOwner(technicianUUID);
    }
}
