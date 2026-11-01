package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.cat.express.wyspiaexpress.components.roles.LoversComponent;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class PlayerDeathMixin {
    @WrapOperation(
            method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameTimeComponent;addTime(I)V"))
    private static void wyspiaexpress$limitCivilianDeathTimeBonus(GameTimeComponent timer, int amount, Operation<Void> original) {
        if (amount > 0 && !GameUtil.canAddCivilianDeathTime(timer)) return;
        original.call(timer, amount);
    }

    @Inject(
            method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At("TAIL"))
    private static void wyspiaexpress$addKillStat(PlayerEntity victim, boolean spawnBody, PlayerEntity killer, Identifier deathReason, CallbackInfo ci) {
        GameUtil.onPlayerDeath(victim, killer, deathReason);
    }

    @Inject(method = "finalizeGame", at = @At("TAIL"))
    private static void wyspiaexpress$resetStat(ServerWorld world, CallbackInfo ci) {
        LichReviveComponent.KEY.get(world).reset();
        WorldComponent.KEY.get(world).reset();
        LoversComponent.KEY.get(world).reset();
    }
}
