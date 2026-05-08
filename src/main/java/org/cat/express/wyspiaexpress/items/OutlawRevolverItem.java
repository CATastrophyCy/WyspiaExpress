package org.cat.express.wyspiaexpress.items;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.item.RevolverItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.packets.OutlawRevolverC2SPacket;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OutlawRevolverItem extends Item {
    public OutlawRevolverItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);

        if (world.isClient) {
            HitResult hitResult = ProjectileUtil.getCollision(user, entity -> entity instanceof @NotNull PlayerEntity target && GameFunctions.isPlayerAliveAndSurvival(target), 15.0F);

            try {
                Class<?> networkingClass = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
                Method getMethod = networkingClass.getMethod("send", net.minecraft.network.packet.CustomPayload.class);
                if (hitResult instanceof @NotNull EntityHitResult entityHitResult) {
                    Entity target = entityHitResult.getEntity();
                    getMethod.invoke(null, new OutlawRevolverC2SPacket(target.getId()));
                }
                else{
                    getMethod.invoke(null, new OutlawRevolverC2SPacket(-1));
                }
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException reason) {
                WyspiaExpress.LOGGER.warn("Failed to send OutlawRevolverC2SPacket\t\n Reason {}", reason.getMessage());
            }

            user.setPitch(user.getPitch() - 4);
            RevolverItem.spawnHandParticle();
        }
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

}