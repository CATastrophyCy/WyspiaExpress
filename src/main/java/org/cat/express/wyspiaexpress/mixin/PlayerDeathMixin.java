package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
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
        private static void addKillStat(PlayerEntity victim, boolean spawnBody, PlayerEntity killer, Identifier deathReason, CallbackInfo ci) {

            var component = GameWorldComponent.KEY.get(victim.getWorld());
            if (component.canUseKillerFeatures(victim)) {
                var reviveComponent = LichReviveComponent.KEY.get(victim.getWorld());
                // count killers that are still alive
                if(victim.getWorld().getPlayers().stream().filter(component::canUseKillerFeatures).filter(GameFunctions::isPlayerAliveAndSurvival).count()
                        + reviveComponent.getAvailableRevives() >= reviveComponent.getMaxRevives())
                    return;
                reviveComponent.incrementAvailableRevives();
            }
            var world_component = WorldComponent.KEY.get(victim.getWorld());
            world_component.addPlayerDead(victim.getUuid());
        }

        @Inject(
                method = "finalizeGame",
                at = @At("TAIL")
        )
        private static void resetStat(ServerWorld world, CallbackInfo ci) {
            var component = LichReviveComponent.KEY.get(world);
            component.reset();
            var world_component = WorldComponent.KEY.get(world);
            world_component.reset();
        }


}
