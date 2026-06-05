package org.cat.express.wyspiaexpress.mixin.roles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.BsXinQin.kinswathe.entities.CaptureDeviceEntity;
import org.BsXinQin.kinswathe.items.CaptureDeviceItem;
import org.cat.express.wyspiaexpress.components.CaptureDeviceEntityComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.UUID;

@Mixin(CaptureDeviceItem.class)
public abstract class CaptureDeviceMixin {
    @WrapOperation(
            method = "useOnBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/BsXinQin/kinswathe/entities/CaptureDeviceEntity;setOwner(Ljava/util/UUID;)V"
            )
    )
    private void wyspiaexpress$wrapSetOwner(
            CaptureDeviceEntity entity,
            UUID technicianUUID,
            Operation<Void> original
    ) {
        original.call(entity, technicianUUID);
        CaptureDeviceEntityComponent component = CaptureDeviceEntityComponent.KEY.get(entity);
        component.setOwner(technicianUUID);
    }

}
