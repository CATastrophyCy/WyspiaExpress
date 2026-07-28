package org.cat.express.wyspiaexpress.mixin.generic;

import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameFunctions.class, priority = 100)
public class PlayerShieldMixin {
    // force to check if the player can die first, before running any logic that gets injected
    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V", at = @At("HEAD"), cancellable = true)
    private static void wyspiaexpress$checkAllowPlayerDeath(PlayerEntity victim, boolean spawnBody, @Nullable PlayerEntity killer, Identifier deathReason, CallbackInfo ci) {
        PlayerEffectComponent effectComponent = PlayerEffectComponent.KEY.get(victim);
        if (!AllowPlayerDeath.EVENT.invoker().allowDeath(victim, killer, deathReason)) {

            if(WyspiaExpress.SERVER_CONFIG.blockStunTicks() > effectComponent.stunTicks)
                effectComponent.setStunTicks(WyspiaExpress.SERVER_CONFIG.blockStunTicks());
            ci.cancel();
            return;
        }
        // check psycho shield, as that doesn't get checked on AllowPlayerDeath
        PlayerPsychoComponent component = PlayerPsychoComponent.KEY.get(victim);
        if (component.getPsychoTicks() > 0) {
            // they still have psycho protection
            if (component.getArmour() > 0) {

                if(WyspiaExpress.SERVER_CONFIG.psychoStunTicks()> effectComponent.stunTicks)
                    effectComponent.setStunTicks(WyspiaExpress.SERVER_CONFIG.psychoStunTicks()); // use a different stun duration
                component.setArmour(component.getArmour() - 1);
                component.sync();
                victim.playSoundToPlayer(WatheSounds.ITEM_PSYCHO_ARMOUR, SoundCategory.MASTER, 5F, 1F);
                ci.cancel();
                return;
            }
        }

    }
}