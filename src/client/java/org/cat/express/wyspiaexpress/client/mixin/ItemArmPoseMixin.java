package org.cat.express.wyspiaexpress.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(PlayerEntityRenderer.class)
public class ItemArmPoseMixin {
    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack view(AbstractClientPlayerEntity instance, Hand hand, Operation<ItemStack> original) {

        ItemStack ret = original.call(instance, hand);
        var config = WyspiaExpressItems.ITEMS_BASIC_CONFIG.get(ret.getItem());
        if (config != null && !config.renderItemOnHand()) {
            ret = ItemStack.EMPTY;
        }
        return ret;
    }
}
