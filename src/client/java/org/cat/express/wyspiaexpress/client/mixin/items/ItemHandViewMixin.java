package org.cat.express.wyspiaexpress.client.mixin.items;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(HeldItemFeatureRenderer.class)
public class ItemHandViewMixin {

    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack wyspiaexpress$view(LivingEntity instance, Operation<ItemStack> original)  {

        ItemStack ret = original.call(instance);

        var config = WyspiaExpressItems.ITEMS_BASIC_CONFIG.get(ret.getItem());
        if (config != null && !config.renderItemOnHand()) {
            ret = ItemStack.EMPTY;
        }
        return ret;
    }

}
