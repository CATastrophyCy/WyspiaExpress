package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressGameFunctions;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameFunctions.class)
public abstract class ReadyPlayersMixin {
    @ModifyExpressionValue(method = "startGame", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;count()J"))
    private static long wyspiaexpress$countAutoJoinOnStart(long count, ServerWorld world) {
        return count + GameUtil.countAutoJoinPlayersOutsideReadyArea(world);
    }

    @ModifyReturnValue(method = "getReadyPlayerCount", at = @At("RETURN"))
    private static int wyspiaexpress$countAutoJoin(int count, World world) {
        return count + Math.toIntExact(GameUtil.countAutoJoinPlayersOutsideReadyArea(world));
    }

    @ModifyReturnValue(method = "getReadyPlayerList", at = @At("RETURN"))
    private static List<ServerPlayerEntity> wyspiaexpress$includeAutoJoin(List<ServerPlayerEntity> ready, ServerWorld world) {
        List<ServerPlayerEntity> players = new ArrayList<>(ready);
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (PlayerConfigComponent.KEY.get(player).autoJoin() && !players.contains(player)) players.add(player);
        }
        return players;
    }

    @WrapOperation(method = "initializeGame", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/game/GameFunctions;getReadyPlayerList(Lnet/minecraft/server/world/ServerWorld;)Ljava/util/List;"))
    private static List<ServerPlayerEntity> wyspiaexpress$preparePlayers(ServerWorld world,
                                                                       Operation<List<ServerPlayerEntity>> original) {
        List<ServerPlayerEntity> participants = original.call(world);
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (game.getGameMode() == WyspiaExpress.ALTERNATE_GAMEMODE) {
            WyspiaExpress.ALTERNATE_GAMEMODE.selectRound(world, game, participants.size());
        }
        WorldComponent.KEY.get(world).startRound();
        // Capture first: configured destinations need not be inside the ready-area box.
        WyspiaExpressGameFunctions.prepareRoundPlayers(world, participants);
        return participants;
    }
}
