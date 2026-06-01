package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.tag.WatheItemTags;
import dev.doctor4t.wathe.util.GunDropPayload;
import dev.doctor4t.wathe.util.GunShootPayload;
import dev.doctor4t.wathe.util.Scheduler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GunShootPayload.Receiver.class)
public abstract class GunShootPayloadMixin {


    @WrapOperation(
            method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getEntityById(I)Lnet/minecraft/entity/Entity;"
            )
    )
    private Entity interceptShootingBlock(
            ServerWorld world,
            int targetId,
            Operation<Entity> original,
            GunShootPayload payload,
            ServerPlayNetworking.Context context
    ) {
        ServerPlayerEntity player = context.player();
        ItemStack mainHandStack = player.getMainHandStack();
        Entity targetEntity = original.call(world, targetId);

        if (WyspiaExpress.SERVER_CONFIG.disableProtectionGunDrop()) {
            if (targetEntity instanceof PlayerEntity target && target.distanceTo(player) < 65.0) {
                GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
                Item revolver = WatheItems.REVOLVER;
                boolean backfire = false;

                // backfire: if you kill an innocent you have a chance of shooting yourself instead
                if (game.isInnocent(target) && game.isInnocent(player) && GameFunctions.isPlayerAliveAndSurvival(player) && mainHandStack.isOf(revolver)
                    && player.getRandom().nextFloat() <= game.getBackfireChance()) {
                    backfire = true;
                    GameFunctions.killPlayer(player, true, player, GameConstants.DeathReasons.GUN);
                }
                // no backfire
                if(!backfire ) {
                    GameFunctions.killPlayer(target, true, player, GameConstants.DeathReasons.GUN);

                    // target is innocent and dead (this makes it so shooting someone protected will not drop the gun nor remove mood)
                    if(game.isInnocent(target) && GameFunctions.isPlayerSpectatingOrCreative(target) && GameFunctions.isPlayerAliveAndSurvival(player) ) {
                        Scheduler.schedule(() -> {
                            if (!context.player().getInventory().contains((s) -> s.isIn(WatheItemTags.GUNS))) return;
                            player.getInventory().remove((s) -> s.isOf(revolver), 1, player.getInventory());
                            ItemEntity item = player.dropItem(revolver.getDefaultStack(), false, false);
                            if (item != null) {
                                item.setPickupDelay(10);
                                item.setThrower(player);
                            }
                            ServerPlayNetworking.send(player, new GunDropPayload());
                            PlayerMoodComponent.KEY.get(player).setMood(0);
                        }, 4);
                    }
                }
            }
            return null;
        }
        return targetEntity;
    }
}