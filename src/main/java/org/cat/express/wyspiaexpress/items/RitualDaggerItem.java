package org.cat.express.wyspiaexpress.items;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.packet.items.HuntingKnifeC2SPacket;
import org.BsXinQin.kinswathe.roles.hunter.HunterComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.packets.RitualDaggerC2SPacket;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RitualDaggerItem extends Item {

    public RitualDaggerItem(@NotNull Settings settings) {super(settings);}

    @Override
    public @NotNull TypedActionResult<@NotNull ItemStack> use(@NotNull World world, @NotNull PlayerEntity player, @NotNull Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);

        player.setCurrentHand(hand);
        player.playSound(WatheSounds.ITEM_KNIFE_PREPARE, 1.0f, 1.0f);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(@NotNull ItemStack stack, @NotNull World world, @NotNull LivingEntity livingEntity, int remainingUseTicks) {
        if (!(livingEntity instanceof @NotNull PlayerEntity player) || player.isSpectator() || remainingUseTicks >= this.getMaxUseTime(stack, player) - 10  || !world.isClient) return;

        if (remainingUseTicks > 5) {
            HitResult hitResult = ProjectileUtil.getCollision(player, entity -> entity instanceof @NotNull PlayerEntity target && GameFunctions.isPlayerAliveAndSurvival(target), 3.0f);
            if (hitResult instanceof @NotNull EntityHitResult entityHitResult) {
                try {
                    Class<?> networkingClass = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
                    Method getMethod = networkingClass.getMethod("send", net.minecraft.network.packet.CustomPayload.class);
                    getMethod.invoke(null, new RitualDaggerC2SPacket(entityHitResult.getEntity().getId()));
                } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException reason) {
                    WyspiaExpress.LOGGER.warn("Failed to send RitualDaggerC2SPacket\t\n Reason {}", reason.getMessage());
                }
            }
        }
    }

    @Override
    public void usageTick(@NotNull World world, @NotNull LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks <= 5 && entity instanceof PlayerEntity player) {
            player.stopUsingItem();
        }
    }

    @Override
    public UseAction getUseAction(@NotNull ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity livingEntity) {
        return 100;
    }

}