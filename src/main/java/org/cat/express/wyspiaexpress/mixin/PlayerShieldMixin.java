package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameFunctions.class, priority = 500)
public class PlayerShieldMixin {
    // force to check if the player can die first, before running any logic that gets injected
    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V", at = @At("HEAD"), cancellable = true)
    private static void wyspiaexpress$checkAllowPlayerDeath(PlayerEntity victim, boolean spawnBody, @Nullable PlayerEntity killer, Identifier deathReason, CallbackInfo ci) {
        if (!AllowPlayerDeath.EVENT.invoker().allowDeath(victim, killer, deathReason)) {
            // right now hardcoded the slowness duration, need to change in the next version bump
            victim.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,2,0,true,true, false));
            ci.cancel();
        }
    }
}