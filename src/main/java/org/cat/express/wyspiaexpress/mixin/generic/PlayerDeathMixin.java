package org.cat.express.wyspiaexpress.mixin.generic;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.components.PlayerHearDeadComponent;
import org.cat.express.wyspiaexpress.components.PlayerSenseDeadComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class PlayerDeathMixin {
        @Inject(
                method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
                at = @At("TAIL")
        )
        private static void wyspiaexpress$addKillStat(PlayerEntity victim, boolean spawnBody, PlayerEntity killer, Identifier deathReason, CallbackInfo ci) {

            var component = GameWorldComponent.KEY.get(victim.getWorld());
            if (component.canUseKillerFeatures(victim)) {
                var reviveComponent = LichReviveComponent.KEY.get(victim.getWorld());
                // count killers that are still alive
                if(victim.getWorld().getPlayers().stream().filter(component::canUseKillerFeatures).filter(GameFunctions::isPlayerAliveAndSurvival).count()
                        + reviveComponent.getAvailableRevives() < reviveComponent.getMaxRevives())
                    reviveComponent.incrementAvailableRevives();
            }
            var world_component = WorldComponent.KEY.get(victim.getWorld());
            world_component.addPlayerDead(victim.getUuid());

            if( killer== null) return;
            // remove phantom invisibility depending on the config
            if(component.isRole(killer, Noellesroles.PHANTOM) && WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.phantomConfig.loseInvisibilityWhenKill()){
                if(deathReason != null) {
                    if( !deathReason.equals(GameConstants.DeathReasons.POISON)) {
                        killer.removeStatusEffect(StatusEffects.INVISIBILITY);
                    }
                }

            }
             /*
                FIX: fix neutral roles get punished for killing players with KinsWathe, this happens because kinswathe only checks if the player is not innocent to give them coin
                    , but with IncreaseMoneyWhenKill <= 100 it ends up punishing them. Either way the code doesn't give them correct reward
             */
            if (!component.isInnocent(killer) && !component.canUseKillerFeatures(killer) && KinsWatheConfig.HANDLER.instance().EnableWatheModify) {
                ShopUtil.addCoin(killer, - (KinsWatheConfig.HANDLER.instance().IncreaseMoneyWhenKill - 100));
            }
            PlayerHearDeadComponent.KEY.get(victim).reset();
            PlayerSenseDeadComponent.KEY.get(victim).reset();

        }

        @Inject(
                method = "finalizeGame",
                at = @At("TAIL")
        )
        private static void wyspiaexpress$resetStat(ServerWorld world, CallbackInfo ci) {
            var component = LichReviveComponent.KEY.get(world);
            component.reset();
            var world_component = WorldComponent.KEY.get(world);
            world_component.reset();
        }


}
