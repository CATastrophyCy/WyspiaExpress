package org.cat.express.wyspiaexpress.client.mixin.items.outlawRevolver;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.index.tag.WatheItemTags;
import dev.doctor4t.wathe.util.MatrixParticleManager;
import dev.doctor4t.wathe.util.MatrixUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class OutlawRevolverMixin {
    @Shadow
    private ItemStack mainHand;
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
                    shift = At.Shift.AFTER))
    private void wyspiaexpress$itemVFX(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {

        if (entity instanceof PlayerEntity playerEntity && stack.isOf(WyspiaExpressItems.OUTLAW_REVOLVER)) {
            if (playerEntity.getUuid() != MinecraftClient.getInstance().player.getUuid()) {
                MatrixParticleManager.setMuzzlePosForPlayer(playerEntity, MatrixUtils.matrixToVec(matrices));
            } else if (!renderMode.isFirstPerson()) {
                MatrixParticleManager.setMuzzlePosForPlayer(playerEntity, MatrixUtils.matrixToVec(matrices));
            }
        }
    }
    @ModifyExpressionValue(
            method = "updateHeldItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean wyspiaexpress$ignoreNbtUpdateForRevolver(boolean original, @Local(ordinal = 0) ItemStack newItemStack) {
        if (!original) {
            if ( (this.mainHand.isIn(WatheItemTags.GUNS) && newItemStack.isOf(WyspiaExpressItems.OUTLAW_REVOLVER)) ||
                    (this.mainHand.isOf(WyspiaExpressItems.OUTLAW_REVOLVER)) && newItemStack.isIn(WatheItemTags.GUNS)) {
                return true;
            }
            if ( (this.mainHand.isOf(WyspiaExpressItems.FAKE_REVOLVER) && newItemStack.isOf(WyspiaExpressItems.OUTLAW_REVOLVER)) ||
                    (this.mainHand.isOf(WyspiaExpressItems.OUTLAW_REVOLVER) && newItemStack.isOf(WyspiaExpressItems.FAKE_REVOLVER))) {
                return true;
            }
        }
        return original;
    }

}
