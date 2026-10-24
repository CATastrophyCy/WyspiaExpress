package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.UUID;

/** Outer wrappers keep other mods' result rules intact outside Wyspia rounds. */
@Mixin(value = GameRoundEndComponent.class, priority = 100)
public abstract class RoundResultMixin {
    @Shadow @Final private World world;

    @WrapMethod(method = "didWin")
    private boolean wyspiaexpress$useCapturedWinners(UUID player, Operation<Boolean> original) {
        RoundResultComponent result = RoundResultComponent.KEY.get(world);
        return result.ownsResult() ? result.didWin(player) : original.call(player);
    }

    @WrapMethod(method = "setRoundEndData")
    private void wyspiaexpress$captureResult(List<ServerPlayerEntity> players, GameFunctions.WinStatus status,
                                           Operation<Void> original) {
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (!world.isClient && game.getGameMode() instanceof WyspiaExpressModdedGameMode) {
            RoundResultComponent result = RoundResultComponent.KEY.get(world);
            if (status == GameFunctions.WinStatus.NONE) result.reset();
            else {
                if (result.hasResult() && game.getGameStatus() == GameWorldComponent.GameStatus.STOPPING) return;
                result.capture(players, status);
            }
        }
        original.call(players, status);
    }
}
