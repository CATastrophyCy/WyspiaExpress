package org.cat.express.wyspiaexpress.game.gamemode;

import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.config.GameConfigs;

import java.util.List;

public final class WyspiaExpressModdedAlternate extends WyspiaExpressModdedGameMode {
    public WyspiaExpressModdedAlternate(Identifier identifier) { super(identifier); }

    public void selectRound(ServerWorld world, GameWorldComponent gameWorld, int participantCount) {
        boolean neutral = participantCount >= Math.max(WyspiaExpress.TRUE_NEUTRAL_MODDED_GAMEMODE.minPlayerCount,
                WyspiaExpress.GAMEMODES_CONFIG.trueNeutralMinimumPlayers())
                && world.getRandom().nextDouble() < WyspiaExpress.GAMEMODES_CONFIG.trueNeutralChance();
        gameWorld.setGameMode(neutral ? WyspiaExpress.TRUE_NEUTRAL_MODDED_GAMEMODE
                : WyspiaExpress.KILLER_MODDED_GAMEMODE);
        GameConfigs.refreshDerivedValues();
    }

    @Override
    public void initializeGame(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> players) {
        selectRound(world, gameWorld, players.size());
        gameWorld.getGameMode().initializeGame(world, gameWorld, players);
    }

    public static GameMode automaticStartMode(GameMode selected) {
        return selected == WyspiaExpress.KILLER_MODDED_GAMEMODE
                || selected == WyspiaExpress.TRUE_NEUTRAL_MODDED_GAMEMODE
                ? WyspiaExpress.ALTERNATE_GAMEMODE : selected;
    }
}
