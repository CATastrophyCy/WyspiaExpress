package org.cat.express.wyspiaexpress.items;

import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpressEntities;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.entity.SmokeBombEntity;
import org.jetbrains.annotations.NotNull;

public class SmokeBombItem extends Item {
    public SmokeBombItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), WatheSounds.ITEM_GRENADE_THROW, SoundCategory.NEUTRAL, 0.5F, 1F + (world.random.nextFloat() - .5f) / 10f);
        if (!world.isClient) {
            SmokeBombEntity smokeBomb = new SmokeBombEntity(WyspiaExpressEntities.SMOKE_BOMB, world);
            smokeBomb.setOwner(user);
            smokeBomb.setPos(user.getX(), user.getEyeY() - 0.1, user.getZ());
            smokeBomb.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 0.5F, 1.0F);
            world.spawnEntity(smokeBomb);
        }

        WyspiaExpressItems.setItemCooldown(user, this, hand);

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);
        return TypedActionResult.success(itemStack, world.isClient());
    }
}