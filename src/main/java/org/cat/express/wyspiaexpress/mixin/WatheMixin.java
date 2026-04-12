package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.Wathe;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wathe.class)
public abstract class WatheMixin {
    @Inject(method = "isSupporter", at = @At("HEAD"), cancellable = true)
    private static void supporter(PlayerEntity player, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(true);
    }
}
