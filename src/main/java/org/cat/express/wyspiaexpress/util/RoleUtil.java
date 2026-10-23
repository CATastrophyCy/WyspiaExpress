package org.cat.express.wyspiaexpress.util;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modded_murder.ModdedWeights;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class RoleUtil {
    private RoleUtil() {}

    public static @NotNull ArrayList<Role> getKillerRoles() {
        ArrayList<Role> killerRoles = new ArrayList<>(WatheRoles.ROLES);

        killerRoles.removeIf(role -> !isEligibleKillerRole(role));
        return killerRoles;
    }

    public static boolean roleMeetPlayerRequirement(Role role) {
        return roleMeetPlayerRequirement(role, WyspiaExpressRoles.ROUND_PLAYER_COUNT);
    }

    public static boolean roleMeetPlayerRequirement(Role role, int playerCount) {
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if (config == null) return true;
        return playerCount >= config.minimumPlayerSpawn()
                && playerCount <= config.maximumPlayerSpawn();
    }

    public static int getRoleSpawnLimit(Role role) {
        int limit = Harpymodloader.ROLE_MAX.getOrDefault(role.identifier(), Integer.MAX_VALUE);
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if (config == null) return limit;
        if (!roleMeetPlayerRequirement(role)) return 0;

        limit = Math.min(limit, config.maximumSpawn());
        int interval = config.playersPerAdditionalSpawn();
        if (interval > 0) {
            long allowed = 1 + ((long) WyspiaExpressRoles.ROUND_PLAYER_COUNT - config.minimumPlayerSpawn()) / interval;
            limit = (int) Math.min(limit, allowed);
        }
        return Math.max(0, limit);
    }

    public static int getRemainingRoleSpawnLimit(Role role, GameWorldComponent gameWorld) {
        long assignedCount = gameWorld.getRoles().values().stream().filter(role::equals).count();
        return (int) Math.max(0, getRoleSpawnLimit(role) - assignedCount);
    }

    public static List<ServerPlayerEntity> selectPlayersByRoleHistory(List<ServerPlayerEntity> candidates,
            List<Role> roles, int count, boolean useWeights, Random random) {
        List<ServerPlayerEntity> selected = new ArrayList<>();
        if (roles.isEmpty() || count <= 0) return selected;
        List<ServerPlayerEntity> remaining = new ArrayList<>(candidates);
        for (int draw = 0; draw < count && !remaining.isEmpty(); draw++) {
            int minimumHistory = Integer.MAX_VALUE;
            if (useWeights) {
                for (Role role : roles) {
                    for (ServerPlayerEntity player : remaining) {
                        minimumHistory = Math.min(minimumHistory, getRoleHistoryCount(role, player));
                    }
                }
            }

            double[] weights = new double[remaining.size()];
            double totalWeight = 0;
            for (int index = 0; index < remaining.size(); index++) {
                if (useWeights) {
                    for (Role role : roles) {
                        long relativeHistory = (long) getRoleHistoryCount(role, remaining.get(index)) - minimumHistory;
                        weights[index] += Math.exp(-4.0 * relativeHistory);
                    }
                } else {
                    weights[index] = 1;
                }
                totalWeight += weights[index];
            }

            double threshold = random.nextDouble() * totalWeight;
            int selectedIndex = 0;
            for (int index = 0; index < weights.length; index++) {
                if (weights[index] <= 0) continue;
                selectedIndex = index; // Last positive entry also handles rounding at the end of the sum.
                threshold -= weights[index];
                if (threshold <= 0) break;
            }
            selected.add(remaining.remove(selectedIndex));
            // Recompute the baseline after removal so even large history gaps cannot empty the next draw.
        }
        return selected;
    }

    private static int getRoleHistoryCount(Role role, PlayerEntity player) {
        var counts = ModdedWeights.roleRounds.get(role);
        return counts == null ? 1 : counts.getOrDefault(player.getUuid(), 1);
    }

    /** Draw distinct recipients using fixed weights for this draft. */
    public static List<ServerPlayerEntity> selectPlayersByWeight(List<ServerPlayerEntity> candidates,
            int count, ToDoubleFunction<ServerPlayerEntity> weight, Random random) {
        List<ServerPlayerEntity> remaining = new ArrayList<>(candidates);
        List<ServerPlayerEntity> selected = new ArrayList<>();
        double[] weights = new double[remaining.size()];
        for (int i = 0; i < remaining.size(); i++) weights[i] = weight.applyAsDouble(remaining.get(i));
        for (int draw = 0; draw < count && draw < remaining.size(); draw++) {
            double total = 0;
            for (double value : weights) total += value;
            double threshold = random.nextDouble() * total;
            int chosen = -1;
            for (int i = 0; i < weights.length; i++) {
                if (weights[i] <= 0) continue;
                chosen = i;
                threshold -= weights[i];
                if (threshold <= 0) break;
            }
            if (chosen < 0) break;
            selected.add(remaining.get(chosen));
            weights[chosen] = 0;
        }
        return selected;
    }

    public static String getRoleName(Role role) {
        if (role == null) return "Unknown role";
        return role.identifier().getPath().toLowerCase();
    }

    public static String getRoleId(Role role) {
        if (role == null) return "Unknown role";
        return role.identifier().toString();
    }

    public static String getModifierId(Modifier modifier) {
        if (modifier == null) return "Unknown role";
        return modifier.identifier().toString();
    }

    public static String getModifierName(Modifier modifier) {
        if (modifier == null) return "Unknown modifier";
        return modifier.identifier().getPath().toLowerCase();
    }
    public static UUID selectWeightedPlayerId(Map<UUID, Double> playerWeights, double threshold) {
        double cumulativeWeight = 0.0;
        for (Map.Entry<UUID, Double> entry : playerWeights.entrySet()) {
            double weight = entry.getValue();
            cumulativeWeight += weight;
            if (cumulativeWeight >= threshold) {
                return entry.getKey();
            }
        }

        return playerWeights.entrySet().iterator().next().getKey();
    }

    public static boolean isEligibleKillerRole(Role role) {
        return roleMeetPlayerRequirement(role)
                && !Harpymodloader.VANNILA_ROLES.contains(role)
                && role.canUseKiller()
                && !HarpyModLoaderConfig.HANDLER.instance().disabled.contains(getRoleId(role));
    }

    /** Replaces promotion choices with Copycat when role picking is enabled. */
    public static boolean filterKillerPromotionRoles(List<Role> roles) {
        if (WyspiaExpress.ROLES_CONFIG.enableRolePicking()) {
            roles.clear();
            return roles.add(WyspiaExpressRoles.COPYCAT);
        }
        return roles.removeIf(role -> !isEligibleKillerRole(role));
    }

    /** Opt-outs re-enter the existing lottery only when the willing pool cannot fill the slots. */
    public static List<ServerPlayerEntity> preferRoleCandidates(List<ServerPlayerEntity> candidates, int slots,
                                                               Predicate<ServerPlayerEntity> willing) {
        List<ServerPlayerEntity> preferred = candidates.stream().filter(willing).toList();
        return new ArrayList<>(preferred.size() >= Math.max(0, slots) ? preferred : candidates);
    }
}
