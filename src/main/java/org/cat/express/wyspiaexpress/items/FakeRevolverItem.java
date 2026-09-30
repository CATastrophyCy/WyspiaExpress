package org.cat.express.wyspiaexpress.items;

import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.item.RevolverItem;
import dev.doctor4t.wathe.util.ShootMuzzleS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
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
        user.playSound(WatheSounds.ITEM_REVOLVER_SHOOT, 5f, 1f + user.getRandom().nextFloat() * .1f - .05f);
        if (WyspiaExpress.ITEMS_CONFIG.itemConfig.fakeRevolverConfig.enableShotEffects()) {
            if (world.isClient) {
                user.setPitch(user.getPitch() - 4);
                RevolverItem.spawnHandParticle();
            } else if (user instanceof ServerPlayerEntity player) {
                ShootMuzzleS2CPayload muzzle = new ShootMuzzleS2CPayload(player.getUuidAsString());
                for (ServerPlayerEntity tracking : PlayerLookup.tracking(player)) {
                    ServerPlayNetworking.send(tracking, muzzle);
                }
                ServerPlayNetworking.send(player, muzzle);
            }
        }
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

}
