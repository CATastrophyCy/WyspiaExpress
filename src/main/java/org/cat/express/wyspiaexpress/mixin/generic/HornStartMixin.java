package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.MapEffect;
import dev.doctor4t.wathe.block.HornBlock;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.server.world.ServerWorld;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HornBlock.class)
public abstract class HornStartMixin {
    @WrapOperation(method = "onUse", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/game/GameFunctions;startGame(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/wathe/api/GameMode;Ldev/doctor4t/wathe/api/MapEffect;I)V"))
    private void wyspiaexpress$keepSelectedMode(ServerWorld world, GameMode mode, MapEffect effect,
                                               int time, Operation<Void> original) {
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (game.getGameMode() instanceof WyspiaExpressModdedGameMode selected) {
            original.call(world, selected, game.getMapEffect(), GameConstants.getInTicks(selected.defaultStartTime, 0));
        } else {
            original.call(world, mode, effect, time);
        }
    }
}
