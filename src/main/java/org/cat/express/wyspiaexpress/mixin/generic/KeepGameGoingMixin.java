package org.cat.express.wyspiaexpress.mixin.generic;

import org.cat.express.wyspiaexpress.components.roles.LoversComponent;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.BsXinQin.kinswathe.component.CustomWinnerComponent;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MurderGameMode.class)
public abstract class KeepGameGoingMixin {
    @Inject(method = "tickServerGameLoop", at = @At(value = "FIELD", target = "Ldev/doctor4t/wathe/game/GameFunctions$WinStatus;NONE:Ldev/doctor4t/wathe/game/GameFunctions$WinStatus;", ordinal = 3, opcode = Opcodes.GETSTATIC), cancellable = true)
    private void keepLicensedvillainGame(@NotNull ServerWorld world, @NotNull GameWorldComponent gameWorld, @NotNull CallbackInfo ci, @Local(name = "winStatus") @NotNull GameFunctions.@NotNull WinStatus winStatus) {
        List<ServerPlayerEntity> players = world.getPlayers();
        List<ServerPlayerEntity> alivePlayers = players.stream().filter(GameFunctions::isPlayerAliveAndSurvival).toList();
        boolean hitmanAlive = alivePlayers.stream().anyMatch(player -> gameWorld.isRole(player, WyspiaExpressRoles.HITMAN));
        if (winStatus != GameFunctions.WinStatus.TIME && hitmanAlive && alivePlayers.size() == 1) {
            CustomWinnerComponent winner = CustomWinnerComponent.KEY.get(world);
            winner.setWinningTextId("hitman");
            winner.setWinners(alivePlayers);
            winner.setColor(WyspiaExpressRoles.HITMAN.color());
            winner.sync();
            GameRoundEndComponent.KEY.get(world).setRoundEndData(players, GameFunctions.WinStatus.KILLERS);
            GameFunctions.stopGame(world);
            ci.cancel();
            return;
        }
        LoversComponent lovers = LoversComponent.KEY.get(world);
        var winningPair = lovers.winningPair(alivePlayers);
        if (winStatus != GameFunctions.WinStatus.TIME && !winningPair.isEmpty()) {
            RoundResultComponent.KEY.get(world).setVictory(
                    RoundResultComponent.Outcome.LOVERS,
                    WyspiaExpressRoles.LOVER_1.color(), winningPair);
            CustomWinnerComponent winner = CustomWinnerComponent.KEY.get(world);
            winner.setWinningTextId("lovers");
            winner.setWinners(alivePlayers);
            winner.setColor(WyspiaExpressRoles.LOVER_1.color());
            winner.sync();
            GameRoundEndComponent.KEY.get(world).setRoundEndData(players, GameFunctions.WinStatus.KILLERS);
            GameFunctions.stopGame(world);
            ci.cancel();
            return;
        }
        if ((hitmanAlive || alivePlayers.stream().anyMatch(player -> LoversComponent.isLover(gameWorld.getRole(player))))
                && (winStatus == GameFunctions.WinStatus.KILLERS || winStatus == GameFunctions.WinStatus.PASSENGERS)) {
            ci.cancel();
            return;
        }
        boolean eddieAlive = false;
        boolean villainLicensedAlive = false;
        boolean onlyCultist = true;
        boolean cultistAlive = false;
        for (ServerPlayerEntity player : alivePlayers) {
            if (gameWorld.isRole(player, WyspiaExpressRoles.EDDIE_WAFFLES)) {
                eddieAlive = true;
            }
            if (gameWorld.isRole(player, WyspiaExpressRoles.VILLAIN_LICENSED)){
                villainLicensedAlive = true;
            }
            if(!gameWorld.isRole(player, WyspiaExpressRoles.CULTIST) && !gameWorld.isRole(player, WyspiaExpressRoles.CULT_LEADER)) {
                onlyCultist = false;
            }
            else{
                cultistAlive = true;
            }
            if(eddieAlive && villainLicensedAlive && !onlyCultist && cultistAlive ){
                break;
            }
        }

        if (alivePlayers.size() == 1 && eddieAlive) {
            CustomWinnerComponent customWinner = CustomWinnerComponent.KEY.get(world);
            customWinner.setWinningTextId("eddie_waffles");
            customWinner.setWinners(alivePlayers);
            customWinner.setColor(WyspiaExpressRoles.EDDIE_WAFFLES.color());
            customWinner.sync();
            GameRoundEndComponent gameRoundEnd = GameRoundEndComponent.KEY.get(world);
            gameRoundEnd.setRoundEndData(players, GameFunctions.WinStatus.KILLERS);
            GameFunctions.stopGame(world);
        }
        if (alivePlayers.size() == 1 && villainLicensedAlive) {
            CustomWinnerComponent customWinner = CustomWinnerComponent.KEY.get(world);
            customWinner.setWinningTextId("villain_licensed");
            customWinner.setWinners(alivePlayers);
            customWinner.setColor(WyspiaExpressRoles.VILLAIN_LICENSED.color());
            customWinner.sync();
            GameRoundEndComponent gameRoundEnd = GameRoundEndComponent.KEY.get(world);
            gameRoundEnd.setRoundEndData(players, GameFunctions.WinStatus.KILLERS);
            GameFunctions.stopGame(world);
        }
        if(cultistAlive && onlyCultist){
            CustomWinnerComponent customWinner = CustomWinnerComponent.KEY.get(world);
            customWinner.setWinningTextId("cult");
            customWinner.setWinners(alivePlayers);
            customWinner.setColor(WyspiaExpressRoles.CULT_LEADER.color());
            customWinner.sync();
            GameRoundEndComponent gameRoundEnd = GameRoundEndComponent.KEY.get(world);
            gameRoundEnd.setRoundEndData(players, GameFunctions.WinStatus.KILLERS);
            GameFunctions.stopGame(world);
        }
        if ( (eddieAlive || villainLicensedAlive || cultistAlive)  && (winStatus == GameFunctions.WinStatus.KILLERS || winStatus == GameFunctions.WinStatus.PASSENGERS)) {
            ci.cancel();
        }

    }
}
