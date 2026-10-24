package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.cca.AutoStartComponent;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedAlternate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AutoStartComponent.class)
public abstract class AutoStartMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;getGameMode()Ldev/doctor4t/wathe/api/GameMode;"))
    private GameMode wyspiaexpress$useAlternateLauncher(GameMode selected) {
        // This local controls both the minimum-player check and the eventual start call.
        return WyspiaExpressModdedAlternate.automaticStartMode(selected);
    }
}
