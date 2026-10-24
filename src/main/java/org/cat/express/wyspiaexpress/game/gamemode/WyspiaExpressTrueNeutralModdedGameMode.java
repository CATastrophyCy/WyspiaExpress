package org.cat.express.wyspiaexpress.game.gamemode;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.RoleDraftStatisticsManager;
import org.cat.express.wyspiaexpress.RoleDraftStatisticsManager.Pool;
import org.cat.express.wyspiaexpress.config.GameConfigs;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.List;

public final class WyspiaExpressTrueNeutralModdedGameMode extends WyspiaExpressModdedGameMode {
    public WyspiaExpressTrueNeutralModdedGameMode(Identifier identifier) { super(identifier, 12); }

    public static boolean isMainRole(Role role) {
        return role != null && (role == WyspiaExpressRoles.CULT_LEADER
                || role == WyspiaExpressRoles.VILLAIN_LICENSED || role == SERoles.THIEF);
    }

    public static boolean allowsRole(Role role) {
        return role != null && role != KinsWatheRoles.HACKER && role != WyspiaExpressRoles.LICH
                && (isMainRole(role) || role == SERoles.AMNESIAC
                || WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)
                || (role.isInnocent() && !role.canUseKiller()));
    }

    @Override
    public boolean isRoleAllowed(Role role) { return allowsRole(role); }

    @Override
    public boolean isKillerRoleAllowed(Role role) { return role != WyspiaExpressRoles.LICH; }

    @Override
    public void initializeGame(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> players) {
        RoundBuilder round = new RoundBuilder(world, gameWorld, players)
                .prepareRound()
                .discardInvalidForcedRoles(this::isRoleAllowed);
        ScoreboardRoleSelectorComponent.KEY.get(world.getScoreboard()).forcedKillers.clear();
        List<Role> eligibleRoles = round.eligibleRoles(role -> !WyspiaExpressRoles.HIDDEN_ROLES.contains(role) && isRoleAllowed(role));

        // Main threats and aligned neutrals have separate quotas and histories.
        var trueNeutralDraft = round.assignNeutralPool(eligibleRoles.stream().filter(WyspiaExpressTrueNeutralModdedGameMode::isMainRole).toList(),
                players.size() / WyspiaExpress.MODE_CONFIG.trueNeutralDividend(), Pool.TRUE_NEUTRAL);
        round.assignVigilantes(players.size() / GameConfigs.vigilanteDividend(gameWorld));
        var alignedNeutralDraft = round.assignNeutralPool(eligibleRoles.stream().filter(WyspiaExpressRoles.KILLER_SIDED_NEUTRALS::contains).toList(),
                players.size() / WyspiaExpress.MODE_CONFIG.killerAlignedNeutralDividend(), Pool.KILLER_ALIGNED_NEUTRAL);
        round.assignCivilianReplacements(eligibleRoles.stream()
                .filter(role -> role.isInnocent() && !role.canUseKiller()).toList())
                .assignAmnesiacFallback();
        RoleDraftStatisticsManager.getInstance().recordRound(players, gameWorld, Pool.TRUE_NEUTRAL, alignedNeutralDraft.hadEligibleCapacity());
        round.completeAssignments(trueNeutralDraft.assignedCount(), WyspiaExpressTrueNeutralModdedGameMode::isMainRole);
    }
}
