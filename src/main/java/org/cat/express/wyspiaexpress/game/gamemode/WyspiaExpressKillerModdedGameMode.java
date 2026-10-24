package org.cat.express.wyspiaexpress.game.gamemode;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.RoleDraftStatisticsManager;
import org.cat.express.wyspiaexpress.RoleDraftStatisticsManager.Pool;
import org.cat.express.wyspiaexpress.config.GameConfigs;
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
    public boolean isRoleAllowed(Role role) { return allowsRole(role); }

    @Override
    public void initializeGame(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> players) {
        RoundBuilder round = new RoundBuilder(world, gameWorld, players)
                .prepareRound()
                .discardInvalidForcedRoles(this::isRoleAllowed);
        int killerQuota = players.size() / GameConfigs.killerDividend(gameWorld);
        int assignedKillerCount = round.assignBaseKillers(killerQuota);
        round.assignVigilantes(players.size() / GameConfigs.vigilanteDividend(gameWorld));
        if (WyspiaExpress.ROLES_CONFIG.enableRolePicking()) round.assignCopycatRoles();
        else {
            round.assignForcedRoles(round.unassignedKillers(), List.of(WyspiaExpressRoles.COPYCAT));
            round.assignKillerReplacements(role -> !WyspiaExpressRoles.HIDDEN_ROLES.contains(role) && isRoleAllowed(role));
        }

        List<Role> eligibleRoles = round.eligibleRoles(role -> !WyspiaExpressRoles.HIDDEN_ROLES.contains(role) && isRoleAllowed(role));
        var alignedNeutralDraft = round.assignNeutralPool(eligibleRoles.stream().filter(WyspiaExpressRoles.KILLER_SIDED_NEUTRALS::contains).toList(),
                players.size() / WyspiaExpress.MODE_CONFIG.killerAlignedNeutralDividend(), Pool.KILLER_ALIGNED_NEUTRAL);
        round.assignCivilianReplacements(eligibleRoles.stream()
                .filter(role -> role.isInnocent() && !role.canUseKiller()).toList())
                .assignAmnesiacFallback()
                .configureLichRevives(killerQuota);
        RoleDraftStatisticsManager.getInstance().recordRound(players, gameWorld, Pool.KILLER, alignedNeutralDraft.hadEligibleCapacity());
        round.completeAssignments(assignedKillerCount);
    }
}
