package org.cat.express.wyspiaexpress.mixin;


import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.config.ShopConfig;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerShopComponent.class)
public abstract class ShopMixin {

    @Shadow public int balance;
    @Shadow public abstract void sync();
    @Shadow @Final @NotNull private PlayerEntity player;

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    void wyspiaexpress$tryBuy(int index, @NotNull CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        Role playerRole = gameWorld.getRole(this.player);
        var basicConfig = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(playerRole);
        if (basicConfig != null) {
            if(basicConfig.enableShop()) {

                List<ShopEntry> shop = ShopUtil.fromShopEntryConfigs(ShopConfig.fromStrings(basicConfig.shopEntries()));
                if (index < 0 || index >= shop.size()) return;
                ShopEntry entries = shop.get(index);

                if (ShopUtil.handlePurchase(this.player, this.balance, entries.stack().getItem(), entries.price())) {
                    this.balance -= entries.price();
                    this.sync();
                }
            }
            ci.cancel();
        }
    }

}
