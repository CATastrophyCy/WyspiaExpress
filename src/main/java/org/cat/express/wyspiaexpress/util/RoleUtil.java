package org.cat.express.wyspiaexpress.util;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modded_murder.ModdedWeights;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedGameMode;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.jetbrains.annotations.NotNull;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class RoleUtil {
    private RoleUtil() {}

    public static @NotNull ArrayList<Role> getKillerRoles() {
        ArrayList<Role> killerRoles = new ArrayList<>(WatheRoles.ROLES);

        killerRoles.removeIf(role -> !isEligibleKillerRole(role));
        return killerRoles;
    }

    public static @NotNull ArrayList<Role> getKillerRoles(GameWorldComponent game) {
        var roles = getKillerRoles();
        if (game.getGameMode() instanceof WyspiaExpressModdedGameMode mode) {
            roles.removeIf(role -> !mode.isKillerRoleAllowed(role));
        }
        return roles;
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

    public static List<ServerPlayerEntity> selectPlayersForRole(List<ServerPlayerEntity> candidates, Role role,
            int count, boolean useWeights, Random random) {
        if (role != SERoles.AMNESIAC
                && (WyspiaExpressRoles.TRUE_NEUTRALS.contains(role) || WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role))) {
            return selectPreferredPlayers(candidates, count, RoleUtil::wantsNeutral,
                    (players, slots) -> selectPlayersByRoleHistory(players, List.of(role), slots, useWeights, random));
        }
        return selectPlayersByRoleHistory(candidates, List.of(role), count, useWeights, random);
    }

    private static int getRoleHistoryCount(Role role, PlayerEntity player) {
        var counts = ModdedWeights.roleRounds.get(roleHistoryKey(role));
        return counts == null ? 1 : counts.getOrDefault(player.getUuid(), 1);
    }

    public static Role roleHistoryKey(Role role) {
        return role == WyspiaExpressRoles.LOVER_2 ? WyspiaExpressRoles.LOVER_1 : role;
    }

    public static void recordRoleAssignment(PlayerEntity player, Role role) {
        var history = ModdedWeights.roleRounds.computeIfAbsent(roleHistoryKey(role), ignored -> new HashMap<>());
        history.put(player.getUuid(), history.getOrDefault(player.getUuid(), 1) + 1);
    }

    /** Draw distinct recipients using fixed weights for this draft. */
    public static List<ServerPlayerEntity> selectPlayersByWeight(List<ServerPlayerEntity> candidates,
            int count, ToDoubleFunction<ServerPlayerEntity> weight, Random random) {
        if (count <= 0) return List.of();
        List<ServerPlayerEntity> selected = new ArrayList<>();
        double[] weights = new double[candidates.size()];
        for (int i = 0; i < weights.length; i++) weights[i] = weight.applyAsDouble(candidates.get(i));
        for (int draw = 0; draw < count && draw < weights.length; draw++) {
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
            selected.add(candidates.get(chosen));
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
    public static boolean isEligibleKillerRole(Role role) {
        return roleMeetPlayerRequirement(role)
                && !Harpymodloader.VANNILA_ROLES.contains(role)
                && role.canUseKiller()
                && !HarpyModLoaderConfig.HANDLER.instance().disabled.contains(getRoleId(role));
    }

    /** Replaces promotion choices with Copycat when role picking is enabled. */
    public static boolean filterKillerPromotionRoles(List<Role> roles, GameWorldComponent game) {
        if (WyspiaExpress.ROLES_CONFIG.enableRolePicking()) {
            roles.clear();
            return roles.add(WyspiaExpressRoles.COPYCAT);
        }
        return roles.retainAll(getKillerRoles(game));
    }

    public static boolean wantsKiller(ServerPlayerEntity player) {
        return !PlayerConfigComponent.KEY.get(player).avoidKiller();
    }

    public static boolean wantsNeutral(ServerPlayerEntity player) {
        return !PlayerConfigComponent.KEY.get(player).avoidNeutral();
    }

    public static List<ServerPlayerEntity> eligibleNeutralCandidates(GameWorldComponent game,
            List<ServerPlayerEntity> candidates) {
        return candidates.stream().filter(player -> !Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())
                && Harpymodloader.OVERWRITE_ROLES.contains(game.getRole(player))).toList();
    }

    public static List<ServerPlayerEntity> selectPreferredPlayers(List<ServerPlayerEntity> candidates, int slots,
            Predicate<ServerPlayerEntity> willing,
            BiFunction<List<ServerPlayerEntity>, Integer, List<ServerPlayerEntity>> draw) {
        if (slots <= 0 || candidates.isEmpty()) return List.of();
        List<ServerPlayerEntity> preferred = candidates.stream().filter(willing).toList();
        if (preferred.size() >= slots) return draw.apply(preferred, slots);
        List<ServerPlayerEntity> selected = new ArrayList<>(preferred);
        selected.addAll(draw.apply(candidates.stream().filter(willing.negate()).toList(), slots - preferred.size()));
        return selected;
    }

    public static List<ServerPlayerEntity> prepareKillerCandidates(List<ServerPlayerEntity> participants,
            ScoreboardRoleSelectorComponent selector) {
        var forcedKillers = new HashSet<>(selector.forcedKillers);
        selector.forcedKillers.clear();
        List<ServerPlayerEntity> candidates = new ArrayList<>();
        for (ServerPlayerEntity player : participants) {
            UUID uuid = player.getUuid();
            Role forcedRole = Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(uuid);
            if (forcedRole != null) {
                if (forcedRole.canUseKiller()) selector.forcedKillers.add(uuid);
            } else if (forcedKillers.contains(uuid)) {
                selector.forcedKillers.add(uuid);
            } else {
                candidates.add(player);
            }
        }
        return candidates;
    }

    public static void restrictForcedPlayers(List<UUID> forced, List<ServerPlayerEntity> candidates) {
        var eligible = new HashSet<UUID>();
        for (ServerPlayerEntity player : candidates) eligible.add(player.getUuid());
        var valid = new LinkedHashSet<>(forced);
        valid.retainAll(eligible);
        forced.clear();
        forced.addAll(valid);
    }

    public static int assignNativeKillers(ServerWorld world, GameWorldComponent game,
            ScoreboardRoleSelectorComponent selector, List<ServerPlayerEntity> candidates, int quota) {
        List<ServerPlayerEntity> willing = candidates.stream().filter(RoleUtil::wantsKiller).toList();
        if (willing.size() >= quota - selector.forcedKillers.size()) {
            return selector.assignKillers(world, game, willing, quota);
        }
        // Every willing candidate has a slot. Let Wathe draw the remaining slots once.
        for (ServerPlayerEntity player : willing) selector.forcedKillers.add(player.getUuid());
        return selector.assignKillers(world, game, candidates.stream().filter(player -> !wantsKiller(player)).toList(), quota);
    }

    public static int assignWeightedKillers(ServerWorld world, GameWorldComponent game,
            ScoreboardRoleSelectorComponent selector, List<ServerPlayerEntity> candidates, int quota,
            ToDoubleFunction<ServerPlayerEntity> weight) {
        List<UUID> killers = new ArrayList<>(selector.forcedKillers);
        selector.forcedKillers.clear();
        for (ServerPlayerEntity player : selectPreferredPlayers(candidates, quota - killers.size(), RoleUtil::wantsKiller,
                (players, slots) -> selectPlayersByWeight(players, slots, weight, world.getRandom()))) {
            killers.add(player.getUuid());
        }
        for (UUID uuid : killers) {
            selector.killerRounds.put(uuid, selector.killerRounds.getOrDefault(uuid, 1) + 1);
            game.addRole(uuid, WatheRoles.KILLER);
            PlayerEntity player = world.getPlayerByUuid(uuid);
            if (player != null) ShopUtil.setCoin(player, GameConstants.MONEY_START);
        }
        return killers.size();
    }

    public static void assignWeightedVigilantes(ServerWorld world, GameWorldComponent game,
            ScoreboardRoleSelectorComponent selector, List<ServerPlayerEntity> candidates, int quota,
            ToDoubleFunction<ServerPlayerEntity> weight) {
        List<ServerPlayerEntity> vigilantes = new ArrayList<>();
        List<ServerPlayerEntity> remaining = new ArrayList<>();
        for (ServerPlayerEntity player : candidates) {
            if (selector.forcedVigilantes.contains(player.getUuid())) vigilantes.add(player);
            else remaining.add(player);
        }
        selector.forcedVigilantes.clear();
        vigilantes.addAll(selectPlayersByWeight(remaining, quota - vigilantes.size(), weight, world.getRandom()));
        for (ServerPlayerEntity player : vigilantes) {
            UUID uuid = player.getUuid();
            selector.vigilanteRounds.put(uuid, selector.vigilanteRounds.getOrDefault(uuid, 1) + 1);
            player.giveItemStack(new ItemStack(WatheItems.REVOLVER)); // I'd like to change this to roleAssigned, but that would break the normal behavior
            game.addRole(player, WatheRoles.VIGILANTE);
        }
    }
}
