package org.cat.express.wyspiaexpress.client.mixin.items;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import org.cat.express.wyspiaexpress.util.ItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public class ItemArmPoseMixin {
    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack wyspiaexpress$view(AbstractClientPlayerEntity instance, Hand hand, Operation<ItemStack> original) {
        return ItemUtil.getVisibleHandStack(original.call(instance, hand));
    }

    @ModifyExpressionValue(method = "getArmPose", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getItemUseTimeLeft()I"))
    private static int wyspiaexpress$remoteSpearPose(int remaining,
                                                   @Local(argsOnly = true) AbstractClientPlayerEntity player,
                                                   @Local ItemStack stack) {
        if (player instanceof OtherClientPlayerEntity && stack.getUseAction() == UseAction.SPEAR) {
            return player.isUsingItem() ? 1 : 0;
        }
        return remaining;
    }
}
