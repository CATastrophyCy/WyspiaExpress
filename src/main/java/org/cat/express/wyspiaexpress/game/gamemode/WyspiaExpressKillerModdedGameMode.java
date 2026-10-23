package org.cat.express.wyspiaexpress.game.gamemode;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.List;

/** Killer main draft with a separate killer-aligned neutral draft; no ordinary true-neutral slots. */
public final class WyspiaExpressKillerModdedGameMode extends WyspiaExpressModdedGameMode {
    public WyspiaExpressKillerModdedGameMode(Identifier identifier) { super(identifier); }

    public static boolean allowsRole(Role role) {
        // Amnesiac remains the intentional fallback rather than an ordinary neutral draft.
        return role != null && role != WyspiaExpressRoles.EDDIE_WAFFLES
                && (!WyspiaExpressRoles.TRUE_NEUTRALS.contains(role) || role == SERoles.AMNESIAC);
    }

    @Override
    public void initializeGame(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> players) {
        new WyspiaExpressRoleAssignment(true).initializeRound(world, gameWorld, players);
    }
}
