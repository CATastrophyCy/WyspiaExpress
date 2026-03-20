package org.cat.express.wyspiaexpress.items;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.components.PlayerBodyEntityComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MegaphoneItem extends Item{
    public MegaphoneItem(@NotNull Item.Settings settings) {super(settings);}

    @Override
    public ActionResult useOnEntity(ItemStack stack, @NotNull PlayerEntity player, @NotNull LivingEntity entity, @NotNull Hand hand) {
        if (player.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;
        if (!player.getWorld().isClient && entity instanceof @NotNull PlayerBodyEntity playerBody) {
            var component = PlayerBodyEntityComponent.KEY.get(playerBody);
            if(component.isReported()) return ActionResult.FAIL;
            WyspiaExpressItems.setItemCooldown(player, this, hand); // passing hand will make the item removed
            for(PlayerEntity playerEntity : player.getWorld().getPlayers()) {
                if(GameFunctions.isPlayerAliveAndSurvival(playerEntity)) {
                    playerEntity.sendMessage(Text.translatable("tip.wyspiaexpress.items.megaphone"), true);
                    //playerEntity.playSoundToPlayer(SoundEvents., SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
                else if(GameFunctions.isPlayerSpectatingOrCreative(playerEntity) ) {
                    playerEntity.sendMessage(Text.translatable("tip.wyspiaexpress.items.megaphone_spectator", playerEntity.getName()
                            ), true);
                    //
                }
            }
            component.setReported(true);
            playerBody.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.GLOWING,
                    GameConstants.getInTicks(0,WyspiaExpress.ITEMS_CONFIG.itemConfig.megaphoneConfig.duration()),
                    0, true, false, false));
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}



