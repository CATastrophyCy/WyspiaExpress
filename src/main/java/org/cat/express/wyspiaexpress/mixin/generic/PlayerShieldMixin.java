package org.cat.express.wyspiaexpress.mixin.generic;

import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.components.PsychoModeState;
import org.cat.express.wyspiaexpress.components.roles.LoversComponent;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameFunctions.class, priority = 100)
public class PlayerShieldMixin {
    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private static void wyspiaexpress$checkAllowPlayerDeath(PlayerEntity victim, boolean spawnBody,
            @Nullable PlayerEntity killer, Identifier deathReason, CallbackInfo ci) {
        if (killer != null && GameConstants.DeathReasons.BAT.equals(deathReason)
                && PlayerPsychoComponent.KEY.get(killer).getPsychoTicks() > 0) {
            killer.resetLastAttackedTicks();
        }
        if (!AllowPlayerDeath.EVENT.invoker().allowDeath(victim, killer, deathReason)) {
            wyspiaexpress$showProtection(victim, deathReason, WyspiaExpress.RULES_CONFIG.blockStunTicks());
            ci.cancel();
            return;
        }

        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(victim);
        if (psycho.getPsychoTicks() > 0) {
            if (GameConstants.DeathReasons.POISON.equals(deathReason)
                    || GameConstants.DeathReasons.KNIFE.equals(deathReason)
                    || GameConstants.DeathReasons.BAT.equals(deathReason)
                    || GameConstants.DeathReasons.GRENADE.equals(deathReason)
                    || GameConstants.DeathReasons.GUN.equals(deathReason)) {
                if (WyspiaExpress.RULES_CONFIG.psycho.rework() || psycho.getArmour() > 0) {
                    if (!WyspiaExpress.RULES_CONFIG.psycho.rework()) psycho.setArmour(psycho.getArmour() - 1);
                    PsychoModeState.get(victim).blockHit();
                    victim.playSoundToPlayer(WatheSounds.ITEM_PSYCHO_ARMOUR, SoundCategory.MASTER, 5F, 1F);
                    wyspiaexpress$showProtection(victim, deathReason, WyspiaExpress.RULES_CONFIG.psychoStunTicks());
                    ci.cancel();
                    return;
                }
            }
            // Otherwise Wathe's own armour check would still absorb this death.
            psycho.stopPsycho();
        }

        if (GameFunctions.isPlayerAliveAndSurvival(victim)
                && LoversComponent.KEY.get(victim.getWorld()).trySurviveLethalHit(victim)) ci.cancel();
    }

    @Unique
    private static void wyspiaexpress$showProtection(PlayerEntity victim, Identifier deathReason, int stunTicks) {
        PlayerEffectComponent effects = PlayerEffectComponent.KEY.get(victim);
        if (effects.stunTicks < stunTicks) effects.setStunTicks(stunTicks);
        victim.sendMessage(Text.literal("Your protection saved you from ")
                .append(GameUtil.getDeathReason(deathReason)), true);
    }
}
