package org.cat.express.wyspiaexpress.client.mixin.items;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.client.roles.technician.CaptureDeviceEntityRenderer;
import org.BsXinQin.kinswathe.entities.CaptureDeviceEntity;
import org.cat.express.wyspiaexpress.components.CaptureDeviceEntityComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CaptureDeviceEntityRenderer.class)
public abstract class CaptureDeviceEntityMixin {

    @WrapOperation(
            method = "render(Lorg/BsXinQin/kinswathe/entities/CaptureDeviceEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/client/WatheClient;isPlayerSpectatingOrCreative()Z"
            )
    )
    private boolean wyspiaexpress$allowOwnerToSeeDevice(Operation<Boolean> original, CaptureDeviceEntity entity) {
        if (original.call()) {
            return true;
        }

        PlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        if (clientPlayer != null) {
            CaptureDeviceEntityComponent ownerComponent = CaptureDeviceEntityComponent.KEY.get(entity);

            return ownerComponent.getOwner().equals(clientPlayer.getUuid());
        }
        return false;
    }
}