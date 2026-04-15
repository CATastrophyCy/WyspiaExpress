package org.cat.express.wyspiaexpress.items;

import dev.doctor4t.wathe.game.GameFunctions;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.aussiebox.starexpress.ModSounds;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;

public class TapeItem extends Item {
    public TapeItem(Item.Settings properties) {
        super(properties);
    }

    public @NotNull ActionResult useOnEntity(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, @NotNull LivingEntity livingEntity, @NotNull Hand interactionHand) {
        super.useOnEntity(itemStack, player, livingEntity, interactionHand);
        if (!(livingEntity instanceof PlayerEntity victim)
            || !GameFunctions.isPlayerAliveAndSurvival(victim)
            || player.getItemCooldownManager().isCoolingDown(itemStack.getItem()))
            return ActionResult.FAIL;

        SilenceComponent victimSilence = SilenceComponent.KEY.get(victim);
        if (victimSilence.isSilenced()) return ActionResult.FAIL;

        WyspiaExpressItems.setItemCooldown(player, WyspiaExpressItems.TAPE, interactionHand);
        if (WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.enableSound()) {
            player.playSound(ModSounds.ITEM_TAPE_APPLY, 1.0F, 1.0F);
        } else {
            player.playSoundToPlayer(ModSounds.ITEM_TAPE_APPLY, SoundCategory.PLAYERS,1.0F, 1.0F);
        }
        victimSilence.setSilenced(true);
        victimSilence.setTearChecks(0);
        victimSilence.setSilencer(player.getUuid());
        victimSilence.sync();
        return ActionResult.SUCCESS;
    }
}