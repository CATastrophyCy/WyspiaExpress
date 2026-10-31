package org.cat.express.wyspiaexpress.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.components.roles.LoversComponent;

public final class LoversPhoneItem extends Item {
    public LoversPhoneItem(Settings settings) { super(settings); }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.wyspiaexpress.lovers_phone.tooltip"));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !LoversComponent.KEY.get(world).togglePhone(player)) {
            return TypedActionResult.fail(user.getStackInHand(hand));
        }
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }
}
