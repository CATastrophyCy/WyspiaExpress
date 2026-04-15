package org.cat.express.wyspiaexpress.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @WrapWithCondition(
            method = "doItemUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;resetEquipProgress(Lnet/minecraft/util/Hand;)V"
            )
    )
    private boolean wyspiaexpress$cancelRevolverUpdateAnimation(HeldItemRenderer instance, Hand hand) {
        ItemStack stackInHand = MinecraftClient.getInstance().player.getStackInHand(hand);
        boolean isRevolver = stackInHand.isOf(WyspiaExpressItems.OUTLAW_REVOLVER) ||
                stackInHand.isOf(WyspiaExpressItems.FAKE_REVOLVER);
        return !isRevolver;
    }
}