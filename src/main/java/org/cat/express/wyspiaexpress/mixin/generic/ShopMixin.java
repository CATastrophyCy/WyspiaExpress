package org.cat.express.wyspiaexpress.mixin.generic;

import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.components.PlayerShopComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(dev.doctor4t.wathe.cca.PlayerShopComponent.class)
public abstract class ShopMixin {
    @Shadow @Final private PlayerEntity player;

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    private void wyspiaexpress$tryBuy(int index, CallbackInfo ci) {
        var shop = PlayerShopComponent.KEY.get(player);
        if (!shop.hasCustomShop()) return;
        shop.tryBuy(index);
        ci.cancel();
    }
}
