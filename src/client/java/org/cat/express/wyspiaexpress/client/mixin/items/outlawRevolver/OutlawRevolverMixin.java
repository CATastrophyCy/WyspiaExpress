package org.cat.express.wyspiaexpress.client.mixin.items.outlawRevolver;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.util.MatrixParticleManager;
import dev.doctor4t.wathe.util.MatrixUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
public class OutlawRevolverMixin {
    @Shadow
    private ItemStack mainHand;

    @Shadow
    @Final
    private MinecraftClient client;
    @WrapOperation(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V")
    )
    private void wyspiaexpress$wrapItemVFX(
            ItemRenderer instance,
            LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world,
            int light, int overlay, int seed,
            Operation<Void> original
    ) {

        original.call(instance, entity, stack, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
        boolean isRevolver = stack.isOf(WyspiaExpressItems.OUTLAW_REVOLVER);

        if (entity instanceof PlayerEntity playerEntity && isRevolver) {
            if (!playerEntity.getUuid().equals(MinecraftClient.getInstance().player.getUuid()) || !renderMode.isFirstPerson()) {
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
    private boolean wyspiaexpress$ignoreNbtUpdate(boolean original, @Local(ordinal = 0) ItemStack newItemStack) {
        if (!original) {
            // Check the mainHand directly like Wathe does, instead of the method arguments
            boolean oldIsRevolver = this.mainHand.isOf(WyspiaExpressItems.FAKE_REVOLVER) ||
                    this.mainHand.isOf(WyspiaExpressItems.OUTLAW_REVOLVER);

            boolean newIsRevolver = newItemStack.isOf(WyspiaExpressItems.FAKE_REVOLVER) ||
                    newItemStack.isOf(WyspiaExpressItems.OUTLAW_REVOLVER);
            if (oldIsRevolver && newIsRevolver) {
                return true;
            }
        }
        return original;
    }
}

