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
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

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
        PlayerEffectComponent stunComponent = PlayerEffectComponent.KEY.get(player);
        if(stunComponent.stunTicks > 0) return null;
        if (WyspiaExpress.SERVER_CONFIG.disableProtectionGunDrop()) {
            if (targetEntity instanceof PlayerEntity target && target.distanceTo(player) < 65.0) {
                GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
                boolean backfire = false;

                // backfire: if you kill an innocent you have a chance of shooting yourself instead
                if (shouldBackFire(game,player, target, mainHandStack)) {
                    backfire = true;
                    GameFunctions.killPlayer(player, true, player, GameConstants.DeathReasons.GUN);
                }
                // no backfire
                if(!backfire ) {
                    boolean isExecutionerTarget = isExecutionerTarget(game, target);
                    GameFunctions.killPlayer(target, true, player, GameConstants.DeathReasons.GUN);

                    if( !isExecutionerTarget && shouldDropGun(game,player,target)) {
                            Scheduler.schedule(() -> {
                                if (!context.player().getInventory().contains((s) -> s.isIn(WatheItemTags.GUNS))) return;
                                player.getInventory().remove((s) -> s.isOf(WatheItems.REVOLVER), 1, player.getInventory());
                                ItemEntity item = player.dropItem(WatheItems.REVOLVER.getDefaultStack(), false, false);
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
    @Unique
    private static boolean shouldBackFire(GameWorldComponent game, PlayerEntity killer, PlayerEntity victim, ItemStack mainHandStack) {
        // first check if the victim is executioner target
        if(isExecutionerTarget(game,victim)) return false;
        // now check if the victim is a voodoo and the voodoo config is enabled
        if (game.isRole(victim, Noellesroles.VOODOO) && NoellesRolesConfig.HANDLER.instance().voodooShotLikeEvil) {
            return false;
        }
        return game.isInnocent(victim) && game.isInnocent(killer) && GameFunctions.isPlayerAliveAndSurvival(killer) && mainHandStack.isOf(WatheItems.REVOLVER)
                && killer.getRandom().nextFloat() <= game.getBackfireChance();
    }
    @Unique
    private static boolean shouldDropGun(GameWorldComponent game, PlayerEntity killer, PlayerEntity victim){

        if(game.isInnocent(victim) && GameFunctions.isPlayerSpectatingOrCreative(victim) && GameFunctions.isPlayerAliveAndSurvival(killer) ){
            if(game.isRole(killer, KinsWatheRoles.LICENSED_VILLAIN) || game.isRole(killer, WyspiaExpressRoles.VILLAIN_LICENSED)) return false;

            return true;
        }
        return false;
    }
    @Unique
    private static boolean isExecutionerTarget(GameWorldComponent game, PlayerEntity player){
        for (UUID uuid : game.getAllWithRole(Noellesroles.EXECUTIONER)) {
            PlayerEntity executioner = player.getWorld().getPlayerByUuid(uuid);
            if (!GameFunctions.isPlayerAliveAndSurvival(executioner)) continue;
            ExecutionerPlayerComponent executionerPlayerComponent = ExecutionerPlayerComponent.KEY.get(executioner);
            if (executionerPlayerComponent.target.equals(player.getUuid())) {
                return true;
            }
        }
        return false;
    }
}