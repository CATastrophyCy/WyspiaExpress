package org.cat.express.wyspiaexpress.mixin.items;

import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import dev.doctor4t.wathe.index.WatheProperties;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.items.WrenchItem;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(WrenchItem.class)
public abstract class WrenchMixin {
    @Inject(
            method = "useOnBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void wathe$useLightRestoration(@NotNull ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if(player == null) return;

        var config = WyspiaExpress.ITEMS_CONFIG.itemConfig.wrenchConfig;
        if(!config.lightRestoration()) return;
        if (player.getItemCooldownManager().isCoolingDown((WrenchItem)(Object)this)) return;
        if(world.isClient || !context.shouldCancelInteraction()) return;
        if(WorldBlackoutComponent.KEY.get(world).isBlackoutActive()) {
            player.sendMessage(Text.literal("Can't restore nearby lights during a BlackOut!").setStyle(
                    Style.EMPTY.withColor(Formatting.RED)), true);
            return;
        }

        WyspiaExpressItems.setItemCooldown(player, (Item)(Object)this, null, config.lightRestorationCooldown());
        BlockPos center = player.getBlockPos();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        world.playSound(null, center, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.8F, 1.0F);

        int minY = Math.max(center.getY() - config.radius(), world.getBottomY());
        int maxY = Math.min(center.getY() + config.radius(), world.getTopY() - 1);

        for (int x = center.getX() - config.radius(); x <= center.getX() + config.radius(); x++) {
            for (int z = center.getZ() - config.radius(); z <= center.getZ() + config.radius(); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (!state.contains(Properties.LIT) || !state.contains(WatheProperties.ACTIVE)) continue;

                    BlockState newState = state.with(Properties.LIT, true).with(WatheProperties.ACTIVE, true);
                    if (newState == state) continue;
                    world.setBlockState(pos.toImmutable(), newState, Block.NOTIFY_ALL);
                    world.playSound(null, pos, WatheSounds.BLOCK_LIGHT_TOGGLE, SoundCategory.BLOCKS, 0.5f, 0.5f);
                }
            }
        }
        cir.setReturnValue(ActionResult.SUCCESS);
        cir.cancel();

    }
}
