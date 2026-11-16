package org.cat.express.wyspiaexpress.mixin.kinswathe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.BsXinQin.kinswathe.roles.hacker.HackerPhoneComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HackerPhoneComponent.class)
public class HackerPhoneComponentMixin {
    @Shadow @Final private PlayerEntity player;

    @ModifyReturnValue(method = "getPhone", at = @At("RETURN"))
    private ItemStack restoreReveals(ItemStack phone) {
        WorldComponent.KEY.get(player.getWorld()).restoreHackerPhone(player, phone);
        return phone;
    }
}
