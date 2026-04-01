package org.cat.express.wyspiaexpress.mixin.items.grenade;

import dev.doctor4t.wathe.item.GrenadeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrenadeItem.class)
public abstract class GrenadeCooldownMixin {
    @Inject(method = "use", at = @At("TAIL"))
    private void wyspiaexpress$applyGrenadeCooldown(@NotNull World world, @NotNull PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!user.isCreative()) {
            WyspiaExpressItems.setItemCooldown(user, (Item)(Object)this, null);
        }
    }
}
