package org.cat.express.wyspiaexpress.mixin.items;

import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.item.GrenadeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.Entity.GrenadeEntity;
import org.cat.express.wyspiaexpress.WyspiaExpressEntities;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrenadeItem.class)
public abstract class GrenadeMixin {
    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true
    )
    private void wathe$useCustomGrenade(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(
                null,
                user.getX(), user.getY(), user.getZ(),
                WatheSounds.ITEM_GRENADE_THROW,
                SoundCategory.NEUTRAL,
                0.5F,
                1F + (world.random.nextFloat() - .5f) / 10f
        );

        if (!world.isClient) {
            GrenadeEntity grenade = new GrenadeEntity(WyspiaExpressEntities.GRENADE, world); // spawn Wyspia Grenade

            grenade.setOwner(user);
            grenade.setPos(user.getX(), user.getEyeY() - 0.1, user.getZ());
            grenade.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 0.5F, 1.0F);

            world.spawnEntity(grenade);
        }

        if (!user.isCreative()) {
            WyspiaExpressItems.setItemCooldown(user, (Item)(Object)this, hand);
        }

        user.incrementStat(Stats.USED.getOrCreateStat((GrenadeItem)(Object)this));
        cir.setReturnValue(TypedActionResult.success(itemStack, world.isClient()));
        cir.cancel();
    }
}
