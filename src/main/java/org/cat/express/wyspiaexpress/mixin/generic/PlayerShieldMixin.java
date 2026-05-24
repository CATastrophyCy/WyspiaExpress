package org.cat.express.wyspiaexpress.mixin.generic;

import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.agmas.noellesroles.Noellesroles;
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
        if (!AllowPlayerDeath.EVENT.invoker().allowDeath(victim, killer, deathReason)) {
            // right now hardcoded the slowness duration, need to change in the next version bump
            PlayerEffectComponent.KEY.get(victim).setStunTicks(WyspiaExpress.SERVER_CONFIG.blockStunTicks());
            ci.cancel();
            return;
        }
        // check psycho shield, as that doesn't get checked on AllowPlayerDeath
        PlayerPsychoComponent component = PlayerPsychoComponent.KEY.get(victim);
        if (component.getPsychoTicks() > 0) {
            // they still have psycho protection
            if (component.getArmour() > 0) {
                PlayerEffectComponent.KEY.get(victim).setStunTicks(WyspiaExpress.SERVER_CONFIG.blockStunTicks());
                component.setArmour(component.getArmour() - 1);
                component.sync();
                victim.playSoundToPlayer(WatheSounds.ITEM_PSYCHO_ARMOUR, SoundCategory.MASTER, 5F, 1F);
                ci.cancel();
                return;
            }
        }

        // all other AllowDeath event failed, so the player is destined to die
        if(killer== null) return;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(killer.getWorld());
        // remove phantom invisibility depending on the config
        if(gameWorldComponent.isRole(killer, Noellesroles.PHANTOM) && WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.phantomConfig.loseInvisibilityWhenKill()){
            if(deathReason != null) {
                if( !deathReason.equals(GameConstants.DeathReasons.POISON)) {
                    killer.removeStatusEffect(StatusEffects.INVISIBILITY);
                }
            }

        }
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.getWorld());
        /*
        FIX: fix neutral roles get punished for killing players with KinsWathe, this happens because kinswathe only checks if the player is not innocent to give them coin
            , but with IncreaseMoneyWhenKill <= 100 it ends up punishing them. Either way the code doesn't give them correct reward
         */
        if (!gameWorld.isInnocent(killer) && !gameWorld.canUseKillerFeatures(killer)) {
            PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(killer);
            playerShop.addToBalance(- (KinsWatheConfig.HANDLER.instance().IncreaseMoneyWhenKill - 100));
        }

    }
}