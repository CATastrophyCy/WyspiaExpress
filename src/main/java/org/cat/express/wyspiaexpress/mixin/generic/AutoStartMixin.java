package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.cca.AutoStartComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedAlternate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AutoStartComponent.class)
public abstract class AutoStartMixin {
    @Shadow @Final public World world;
    @Shadow public int startTime;
    @Shadow public int time;
    @Shadow public abstract void reset();

    @Inject(method = "setStartTime", at = @At("TAIL"))
    private void wyspiaexpress$restartCountdown(CallbackInfo ci) {
        this.reset();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void wyspiaexpress$countDownOnlyInLobby(CallbackInfo ci) {
        if (this.startTime <= 0
                || GameWorldComponent.KEY.get(this.world).getGameStatus() != GameWorldComponent.GameStatus.INACTIVE) {
            if (this.time != this.startTime) this.reset();
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;getGameMode()Ldev/doctor4t/wathe/api/GameMode;"))
    private GameMode wyspiaexpress$useAlternateLauncher(GameMode selected) {
        // This local controls both the minimum-player check and the eventual start call.
        return WyspiaExpressModdedAlternate.automaticStartMode(selected);
    }
}
