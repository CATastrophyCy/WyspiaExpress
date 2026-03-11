package org.cat.express.wyspiaexpress.items;

import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;

public class FakeRevolverItem extends Item {
    public FakeRevolverItem(@NotNull Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
        WyspiaExpressItems.setItemCooldown(user, WyspiaExpressItems.FAKE_REVOLVER, null);
        // only make sound when clicked
        user.playSound(WatheSounds.ITEM_REVOLVER_SHOOT, 5f, 1f + user.getRandom().nextFloat() * .1f - .05f);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

}
