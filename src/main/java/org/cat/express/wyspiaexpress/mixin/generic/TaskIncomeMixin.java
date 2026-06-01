package org.cat.express.wyspiaexpress.mixin.generic;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerMoodComponent.class)
public abstract class TaskIncomeMixin {
    @Shadow public abstract float getMood();

    @Shadow @Final private PlayerEntity player;

    @Inject(method = "setMood", at = @At("HEAD"))
    void wyspiaexpress$giveCoinsForMood(float mood, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());

        /**
         *  role with FAKE MOOD just have setmood to always set the mood to 1, making so it never decreases
         *  and getMood to always return one
         *  but when they complete a task this is still getting called with mood value greater than the getMood() returns
         *  so role with fake mood should still get money
          */
        if (mood > getMood()) {
            Role role = gameWorldComponent.getRole(player);
            if (role != null) {
                var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
                if (config != null) {
                    ShopUtil.addCoin(player, config.taskIncome());

                }
            }
        }
    }

}
