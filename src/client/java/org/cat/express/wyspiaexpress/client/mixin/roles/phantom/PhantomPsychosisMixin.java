package org.cat.express.wyspiaexpress.client.mixin.roles.phantom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = HeldItemFeatureRenderer.class, priority = 1500)
public abstract class PhantomPsychosisMixin {

    @WrapOperation(
            method = {"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"
            )}
    )
    public ItemStack hidePsychosisItems(LivingEntity instance, Operation<ItemStack> original) {
        ItemStack orig = instance.getMainHandStack();
        if (instance.isInvisible()) return orig;
        return original.call(instance);
    }
}