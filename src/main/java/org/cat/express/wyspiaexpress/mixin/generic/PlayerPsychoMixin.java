package org.cat.express.wyspiaexpress.mixin.generic;

import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.components.PsychoModeState;
import org.cat.express.wyspiaexpress.packets.StaminaS2CPacket;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.cat.express.wyspiaexpress.util.ItemUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerPsychoComponent.class)
public abstract class PlayerPsychoMixin implements PsychoModeState {
    @Shadow @Final private PlayerEntity player;
    @Shadow public int psychoTicks;
    @Shadow public abstract void stopPsycho();
    @Shadow public abstract void sync();
    @Unique private int wyspiaexpress$blockedHits;

    @Override
    public int getBlockedHits() { return wyspiaexpress$blockedHits; }

    @Override
    public void blockHit() {
        if (player.getWorld().isClient || psychoTicks <= 0) return;
        wyspiaexpress$blockedHits++;
        if (WyspiaExpress.RULES_CONFIG.psycho.rework() && getPenalizedHits() > 0) {
            psychoTicks = (int) (psychoTicks * (1 - WyspiaExpress.RULES_CONFIG.psycho.timeLostPerHit()));
        }
        if (psychoTicks == 0) stopPsycho();
        else sync();
    }

    @Inject(method = "startPsycho", at = @At("RETURN"))
    private void wyspiaexpress$start(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || player.getWorld().isClient) return;
        wyspiaexpress$blockedHits = 0;
        psychoTicks = WyspiaExpress.RULES_CONFIG.psycho.duration() * 20;
        StaminaS2CPacket.send(player, GameUtil.refillStamina(player));
        sync();
    }

    @Inject(method = "stopPsycho", at = @At("RETURN"))
    private void wyspiaexpress$stop(CallbackInfo ci) {
        if (player.getWorld().isClient) return;
        if (wyspiaexpress$blockedHits > 0 && GameFunctions.isPlayerAliveAndSurvival(player)) {
            ItemUtil.setWeaponCooldown(player, WyspiaExpress.RULES_CONFIG.psycho.weaponCooldown() * 20);
        }
        wyspiaexpress$blockedHits = 0;
        StaminaS2CPacket.send(player, GameUtil.refillStamina(player));
        sync();
    }

    @Inject(method = "reset", at = @At("HEAD"))
    private void wyspiaexpress$reset(CallbackInfo ci) {
        wyspiaexpress$blockedHits = 0;
    }

    @Inject(method = "writeToNbt", at = @At("TAIL"))
    private void wyspiaexpress$save(NbtCompound tag, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        tag.putInt("wyspiaexpressBlockedHits", wyspiaexpress$blockedHits);
    }

    @Inject(method = "readFromNbt", at = @At("TAIL"))
    private void wyspiaexpress$load(NbtCompound tag, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        wyspiaexpress$blockedHits = tag.getInt("wyspiaexpressBlockedHits");
    }
}
