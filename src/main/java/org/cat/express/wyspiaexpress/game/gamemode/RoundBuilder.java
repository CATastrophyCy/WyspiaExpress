package org.cat.express.wyspiaexpress.game.gamemode;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.agmas.harpymodloader.modded_murder.ModdedWeights;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.RoleCategoryStatisticsManager;
import org.cat.express.wyspiaexpress.RoleDraftStatisticsManager;
import org.cat.express.wyspiaexpress.RoleDraftStatisticsManager.Pool;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.RoleComponent;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.cat.express.wyspiaexpress.config.GameConfigs;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.cat.express.wyspiaexpress.util.RoleUtil;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;

/**
 * One round's assignment context. Gamemodes choose the order and role pools; this builder
 * applies those choices to the live role map. Counts and draft results interrupt fluent steps
 * where the caller needs a result. Discard the builder after completing assignments.
 */
final class RoundBuilder {
    private final ServerWorld world;
    private final GameWorldComponent gameWorld;
    private final List<ServerPlayerEntity> participants;
    private final ScoreboardRoleSelectorComponent nativeRoleSelector;
    private final ToDoubleBiFunction<Pool, ServerPlayerEntity> selectionWeights;
    private final WorldModifierComponent worldModifiers;
    private boolean nativeTeamSelection;

    RoundBuilder(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> participants) {
        this(world, gameWorld, participants, (pool, player) -> gameWorld.areWeightsEnabled()
                ? RoleDraftStatisticsManager.getInstance().computeWeight(player.getUuid(), pool) : 1);
    }

    RoundBuilder(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> participants,
                 ToDoubleBiFunction<Pool, ServerPlayerEntity> selectionWeights) {
        this.world = world;
        this.gameWorld = gameWorld;
        this.participants = participants;
        this.nativeRoleSelector = ScoreboardRoleSelectorComponent.KEY.get(world.getScoreboard());
        this.selectionWeights = selectionWeights;
        this.worldModifiers = WorldModifierComponent.KEY.get(world);
    }

    /** Legacy rounds can opt into Wathe's native team selector; pool draws keep their configured weights. */
    RoundBuilder useNativeTeamSelection() {
        nativeTeamSelection = true;
        return this;
    }

    RoundBuilder prepareRound() {
        RoundResultComponent.KEY.get(world).startRound(participants);
        GameConfigs.refreshDerivedValues();
        Harpymodloader.refreshRoles();
        HarpyModLoaderConfig.HANDLER.load();
        gameWorld.clearRoleMap();
        WyspiaExpressRoles.ROUND_PLAYER_COUNT = participants.size();
        for (ServerPlayerEntity player : participants) {
            ResetPlayerEvent.EVENT.invoker().resetPlayer(player);
            gameWorld.addRole(player, WatheRoles.CIVILIAN);
        }
        return this;
    }

    RoundBuilder discardInvalidForcedRoles(Predicate<Role> allowed) {
        Harpymodloader.FORCED_MODDED_ROLE_FLIP.entrySet().removeIf(entry -> {
            Role role = entry.getValue();
            return !allowed.test(role)
                    || (WyspiaExpressRoles.HIDDEN_ROLES.contains(role) && role != WyspiaExpressRoles.COPYCAT)
                    || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(RoleUtil.getRoleId(role))
                    || !RoleUtil.roleMeetPlayerRequirement(role);
        });
        return this;
    }

    List<ServerPlayerEntity> unassignedCivilians() {
        return new ArrayList<>(participants.stream().filter(player ->
                Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player))
                        && !gameWorld.canUseKillerFeatures(player)).toList());
    }

    List<ServerPlayerEntity> unassignedKillers() {
        return new ArrayList<>(participants.stream().filter(player ->
                Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player))
                        && gameWorld.canUseKillerFeatures(player)).toList());
    }

    int assignBaseKillers(int killerQuota) {
        List<ServerPlayerEntity> candidates = new ArrayList<>(participants);
        candidates.removeIf(player -> {
            Role forced = Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(player.getUuid());
            if (forced == null) return false;
            if (forced.canUseKiller()) nativeRoleSelector.forcedKillers.add(player.getUuid());
            return true;
        });
        if (candidates.stream().anyMatch(player -> PlayerConfigComponent.KEY.get(player).avoidKiller())) {
            candidates.removeIf(player -> nativeRoleSelector.forcedKillers.contains(player.getUuid()));
            candidates = RoleUtil.preferRoleCandidates(candidates, killerQuota - nativeRoleSelector.forcedKillers.size(),
                    player -> !PlayerConfigComponent.KEY.get(player).avoidKiller());
        }
        return nativeTeamSelection ? nativeRoleSelector.assignKillers(world, gameWorld, candidates, killerQuota)
                : assignWeightedBaseKillers(candidates, killerQuota);
    }

    RoundBuilder assignVigilantes(int vigilanteQuota) {
        List<ServerPlayerEntity> candidates = unassignedCivilians();
        candidates.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid()));
        if (nativeTeamSelection) nativeRoleSelector.assignVigilantes(world, gameWorld, candidates, vigilanteQuota);
        else assignWeightedVigilantes(candidates, vigilanteQuota);
        return this;
    }

    RoundBuilder assignCopycatRoles() {
        WyspiaExpressRoles.COPYCAT_ROLES.clear();
        for (ServerPlayerEntity player : unassignedKillers()) gameWorld.addRole(player, WyspiaExpressRoles.COPYCAT);
        return this;
    }

    List<Role> eligibleRoles(Predicate<Role> allowed) {
        return WatheRoles.ROLES.stream().filter(role ->
                !Harpymodloader.NON_MURDER_ROLES.contains(role)
                        && !Harpymodloader.VANNILA_ROLES.contains(role)
                        && allowed.test(role)
                        && !HarpyModLoaderConfig.HANDLER.instance().disabled.contains(RoleUtil.getRoleId(role))
                        && RoleUtil.roleMeetPlayerRequirement(role)).toList();
    }

    /** Assigned count excludes fallback; availability includes forced picks and pre-draw role capacity. */
    record DraftResult(int assignedCount, boolean hadEligibleCapacity) {}

    DraftResult assignNeutralPool(List<Role> rolePool, int quota, Pool historyPool) {
        boolean hadEligibleCapacity = quota > 0 && rolePool.stream()
                .anyMatch(role -> RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld) > 0);
        List<ServerPlayerEntity> candidates = unassignedCivilians();
        int forcedCount = assignForcedRoles(candidates, rolePool);
        int remainingQuota = Math.max(0, quota - forcedCount);
        List<ServerPlayerEntity> selectedPlayers = selectPlayersForNeutralPool(candidates, remainingQuota, historyPool);
        int assignedCount = assignNeutralRoleCopies(selectedPlayers, rolePool, remainingQuota);
        assignAmnesiacFallback(selectedPlayers);
        return new DraftResult(forcedCount + assignedCount, forcedCount > 0 || hadEligibleCapacity);
    }

    List<ServerPlayerEntity> selectPlayersForNeutralPool(List<ServerPlayerEntity> candidates, int quota, Pool historyPool) {
        if (quota <= 0 || candidates.isEmpty()) return List.of();
        return selectPlayersByPoolWeight(eligibleNeutralCandidates(candidates, quota), quota, historyPool);
    }

    private List<ServerPlayerEntity> eligibleNeutralCandidates(List<ServerPlayerEntity> candidates, int quota) {
        List<ServerPlayerEntity> eligibleCandidates = new ArrayList<>(candidates);
        eligibleCandidates.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())
                || !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
        return RoleUtil.preferRoleCandidates(eligibleCandidates, quota,
                player -> !PlayerConfigComponent.KEY.get(player).avoidNeutral());
    }

    private List<ServerPlayerEntity> selectPlayersByPoolWeight(List<ServerPlayerEntity> candidates, int quota, Pool historyPool) {
        return RoleUtil.selectPlayersByWeight(candidates, quota,
                player -> selectionWeights.applyAsDouble(historyPool, player), world.getRandom());
    }

    RoundBuilder assignAmnesiacFallback() {
        return assignAmnesiacFallback(unassignedCivilians());
    }

    RoundBuilder assignAmnesiacFallback(List<ServerPlayerEntity> candidates) {
        for (ServerPlayerEntity player : candidates) {
            if (gameWorld.isRole(player, WatheRoles.CIVILIAN)) gameWorld.addRole(player, SERoles.AMNESIAC);
        }
        return this;
    }

    RoundBuilder configureLichRevives(int targetKillerCount) {
        if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(RoleUtil.getRoleId(WyspiaExpressRoles.LICH))) {
            LichReviveComponent component = LichReviveComponent.KEY.get(world);
            component.setAvailableRevives(WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.additionalRevive());
            component.setMaxRevives(WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.additionalRevive() + targetKillerCount);
        }
        return this;
    }

    RoundBuilder assignKillerReplacements(Predicate<Role> allowed) {
        List<Role> killerRoles = new ArrayList<>();
        for (Role role : RoleUtil.getKillerRoles()) {
            if (Harpymodloader.NON_MURDER_ROLES.contains(role)) continue;
            if (!allowed.test(role)) continue;
            killerRoles.add(role);
        }
        Collections.shuffle(killerRoles);
        List<ServerPlayerEntity> unassignedCandidates = unassignedKillers();
        for (Role role : killerRoles) {
            int copyCount = Math.min(RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld),
                    unassignedCandidates.size());
            if (copyCount > 0) {
                copyCount = world.getRandom().nextInt(copyCount) + 1;
            }

            assignRoleCopies(copyCount, role, unassignedCandidates);
            removeAssignedPlayers(unassignedCandidates);
        }
        return this;
    }

    private int assignWeightedBaseKillers(List<ServerPlayerEntity> candidates, int remainingKillerQuota) {
        ArrayList<UUID> selectedKillerIds = new ArrayList<>();
        for (UUID uuid : nativeRoleSelector.forcedKillers) {
            selectedKillerIds.add(uuid);
            remainingKillerQuota--;
            nativeRoleSelector.killerRounds.put(uuid, nativeRoleSelector.killerRounds.getOrDefault(uuid, 1) + 1);
        }
        nativeRoleSelector.forcedKillers.clear();

        if (remainingKillerQuota > 0) {
            List<ServerPlayerEntity> eligibleCandidates = new ArrayList<>(candidates);
            eligibleCandidates.removeIf(player -> selectedKillerIds.contains(player.getUuid())
                    || Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid()));
            eligibleCandidates = RoleUtil.preferRoleCandidates(eligibleCandidates, remainingKillerQuota,
                    player -> !PlayerConfigComponent.KEY.get(player).avoidKiller());
            for (ServerPlayerEntity player : selectPlayersByPoolWeight(eligibleCandidates, remainingKillerQuota, Pool.KILLER)) {
                selectedKillerIds.add(player.getUuid());
                nativeRoleSelector.killerRounds.put(player.getUuid(), nativeRoleSelector.killerRounds.getOrDefault(player.getUuid(), 1) + 1);
            }
        }
        for (UUID killerId : selectedKillerIds) {
            gameWorld.addRole(killerId, WatheRoles.KILLER);
            PlayerEntity killer = world.getPlayerByUuid(killerId);
            if (killer != null) {
                ShopUtil.setCoin(killer, GameConstants.MONEY_START);
            }
        }
        return selectedKillerIds.size();
    }

    private void assignWeightedVigilantes(List<ServerPlayerEntity> candidates, int remainingVigilanteQuota) {
        ArrayList<UUID> selectedVigilanteIds = new ArrayList<>();
        for (UUID uuid : nativeRoleSelector.forcedVigilantes) {
            selectedVigilanteIds.add(uuid);
            remainingVigilanteQuota--;
            nativeRoleSelector.vigilanteRounds.put(uuid, nativeRoleSelector.vigilanteRounds.getOrDefault(uuid, 1) + 1);

        }
        nativeRoleSelector.forcedVigilantes.clear();

        if (remainingVigilanteQuota > 0) {
            List<ServerPlayerEntity> eligibleCandidates = new ArrayList<>(candidates);
            eligibleCandidates.removeIf(player -> selectedVigilanteIds.contains(player.getUuid())
                    || Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid()));
            for (ServerPlayerEntity player : selectPlayersByPoolWeight(eligibleCandidates, remainingVigilanteQuota, Pool.VIGILANTE)) {
                selectedVigilanteIds.add(player.getUuid());
                nativeRoleSelector.vigilanteRounds.put(player.getUuid(), nativeRoleSelector.vigilanteRounds.getOrDefault(player.getUuid(), 1) + 1);
            }
        }

        for (UUID uuid : selectedVigilanteIds) {
            PlayerEntity player = world.getPlayerByUuid(uuid);
            if (player instanceof ServerPlayerEntity serverPlayer && candidates.contains(serverPlayer) && !gameWorld.canUseKillerFeatures(player)) {
                // Preserve Wathe's base Vigilante equipment before role-assignment callbacks.
                player.giveItemStack(new ItemStack(WatheItems.REVOLVER));
                gameWorld.addRole(player, WatheRoles.VIGILANTE);
            }
        }
    }

    List<ServerPlayerEntity> selectNeutralPlayersByRoleHistory(List<ServerPlayerEntity> candidates,
            List<Role> rolePool, int quota) {
        if (quota <= 0 || candidates.isEmpty()) return new ArrayList<>();
        return RoleUtil.selectPlayersByRoleHistory(eligibleNeutralCandidates(candidates, quota), rolePool, quota,
                gameWorld.areWeightsEnabled(), world.getRandom());
    }

    int assignForcedRoles(List<ServerPlayerEntity> candidates, List<Role> eligibleRoles) {
        List<ServerPlayerEntity> unassignedCandidates = new ArrayList<>(candidates);
        int assignedCount = 0;
        for (Role role : eligibleRoles) {
            assignedCount += assignRoleCopies(0, role, unassignedCandidates);
            removeAssignedPlayers(unassignedCandidates);
        }
        return assignedCount;
    }

    int assignNeutralRoleCopies(List<ServerPlayerEntity> candidates, List<Role> roles, int poolQuota) {
        if (poolQuota <= 0 || candidates.isEmpty() || roles.isEmpty()) return 0;

        List<Role> shuffledRoles = new ArrayList<>(roles);
        Collections.shuffle(shuffledRoles);
        List<ServerPlayerEntity> unassignedCandidates = new ArrayList<>(candidates);
        poolQuota = Math.min(poolQuota, candidates.size());
        int assignedCount = 0;
        for (Role role : shuffledRoles) {
            if (assignedCount >= poolQuota) break;

            int copyCount = Math.min(RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld),
                    unassignedCandidates.size());
            if (!Harpymodloader.ROLE_MAX.containsKey(role.identifier())
                    && !WyspiaExpressRoles.ROLES_BASIC_CONFIG.containsKey(role)) {
                copyCount = Math.min(Math.round((float) candidates.size() / shuffledRoles.size()), copyCount);
            }
            copyCount = Math.min(copyCount, poolQuota - assignedCount);

            int newAssignments = assignRoleCopies(copyCount, role, unassignedCandidates);

            removeAssignedPlayers(unassignedCandidates);
            assignedCount += newAssignments;

        }
        return assignedCount;
    }

    RoundBuilder assignCivilianReplacements(List<Role> civilianRoles) {
        List<ServerPlayerEntity> candidates = unassignedCivilians();
        if (candidates.isEmpty() || civilianRoles.isEmpty()) return this;
        List<Role> shuffledCivilianRoles = new ArrayList<>(civilianRoles);
        Collections.shuffle(shuffledCivilianRoles);

        List<ServerPlayerEntity> unassignedCandidates = new ArrayList<>(candidates);
        for (Role role : shuffledCivilianRoles) {
            int copyCount = Math.min(RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld),
                    unassignedCandidates.size());
            if (!Harpymodloader.ROLE_MAX.containsKey(role.identifier())
                    && !WyspiaExpressRoles.ROLES_BASIC_CONFIG.containsKey(role)) {
                copyCount = Math.min(Math.round((float) candidates.size() / shuffledCivilianRoles.size()), copyCount);
            }
            if (copyCount > 0) {
                copyCount = world.random.nextInt(copyCount) + 1;
            }

            assignRoleCopies(copyCount, role, unassignedCandidates);
            removeAssignedPlayers(unassignedCandidates);

        }
        return this;
    }

    private void removeAssignedPlayers(List<ServerPlayerEntity> candidates) {
        candidates.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
    }

    private int assignRoleCopies(int requestedCount, Role role,
                                   List<ServerPlayerEntity> candidates) {
        List<ServerPlayerEntity> assignedPlayers = new ArrayList<>();

        if (Harpymodloader.FORCED_MODDED_ROLE.containsKey(role)) {
            for (UUID uuid : Harpymodloader.FORCED_MODDED_ROLE.get(role)) {
                PlayerEntity player = world.getPlayerByUuid(uuid);
                if (player instanceof ServerPlayerEntity serverPlayer && candidates.contains(serverPlayer)
                        && role.equals(Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(uuid))
                        && !assignedPlayers.contains(serverPlayer)) {
                    assignedPlayers.add(serverPlayer);
                    requestedCount--;
                    recordRoleAssignment(serverPlayer, role);
                }
            }
        }
        // Forced picks retain their override; normal picks use only the remaining scaled slots.
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if (config != null && config.playersPerAdditionalSpawn() > 0) {
            int remainingRoleSlots = Math.max(0,
                    RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld) - assignedPlayers.size());
            requestedCount = Math.min(requestedCount, remainingRoleSlots);
        }
        List<ServerPlayerEntity> eligibleCandidates = new ArrayList<>(candidates);
        eligibleCandidates.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid()));
        for (ServerPlayerEntity player : RoleUtil.selectPlayersByRoleHistory(eligibleCandidates, List.of(role),
                requestedCount, gameWorld.areWeightsEnabled(), world.getRandom())) {
            assignedPlayers.add(player);
            recordRoleAssignment(player, role);
        }

        int assignedCount = assignedPlayers.size();
        for (ServerPlayerEntity player : assignedPlayers) {
            gameWorld.addRole(player, role);
        }
        return assignedCount;
    }

    private void recordRoleAssignment(ServerPlayerEntity player, Role role) {
        Map<UUID, Integer> roleRounds = ModdedWeights.roleRounds.computeIfAbsent(role, ignored -> new HashMap<>());
        roleRounds.put(player.getUuid(), roleRounds.getOrDefault(player.getUuid(), 1) + 1);
    }

    private void distributeModifiers(int modifierBudget, Predicate<Role> additionalGuesserRoles) {
        worldModifiers.getModifiers().clear();
        Map<Modifier, Integer> modifierCaps = createModifierCaps(modifierBudget);
        Map<Modifier, Integer> modifierCounts = new HashMap<>();
        for (Modifier modifier : HMLModifiers.MODIFIERS) modifierCounts.put(modifier, 0);

        assignForcedModifiers(modifierCounts);
        List<ServerPlayerEntity> shuffledPlayers = new ArrayList<>(participants);
        Collections.shuffle(shuffledPlayers);
        assignRandomModifiers(shuffledPlayers, modifierCaps, modifierCounts);
        grantAutomaticGuessers(shuffledPlayers, additionalGuesserRoles);
        assignRandomBomber(shuffledPlayers);
        announceModifiers();
    }

    private Map<Modifier, Integer> createModifierCaps(int modifierBudget) {
        Map<Modifier, Integer> modifierCaps = new HashMap<>();
        int killerModifierTypeCount = (int) HMLModifiers.MODIFIERS.stream().filter(m -> m.killerOnly).count();
        for (Modifier modifier : HMLModifiers.MODIFIERS) {
            int modifierCap = modifierBudget;
            if (modifier.killerOnly && killerModifierTypeCount > 0) {
                modifierCap = Math.max(1,
                        (int) Math.floor(
                                (participants.size() / (double) GameConfigs.killerDividend(gameWorld)) / killerModifierTypeCount
                        )
                );
            }
            // Apply hard limits if they exist
            if (Harpymodloader.MODIFIER_MAX.containsKey(modifier.identifier)) {
                modifierCap = Math.min(modifierCap, Harpymodloader.MODIFIER_MAX.get(modifier.identifier));
            }
            modifierCaps.put(modifier, modifierCap);
        }
        // BOMBER is special: handled manually later
        modifierCaps.put(WyspiaExpressRoles.BOMBER, 0);

        return modifierCaps;
    }

    private void assignForcedModifiers(Map<Modifier, Integer> modifierCounts) {
        if (Harpymodloader.FORCED_MODDED_MODIFIER.isEmpty()) return;
        for (Modifier modifier : HMLModifiers.MODIFIERS) {
            List<UUID> forcedPlayerIds = Harpymodloader.FORCED_MODDED_MODIFIER.get(modifier);
            if (forcedPlayerIds == null) continue;
            for (ServerPlayerEntity player : participants) {
                if (!forcedPlayerIds.contains(player.getUuid())) continue;
                worldModifiers.addModifier(player.getUuid(), modifier);
                ModifierAssigned.EVENT.invoker().assignModifier(player, modifier);
                modifierCounts.put(modifier, modifierCounts.get(modifier) + 1);
            }
        }
    }

    private void assignRandomModifiers(List<ServerPlayerEntity> shuffledPlayers,
            Map<Modifier, Integer> modifierCaps, Map<Modifier, Integer> modifierCounts) {
        List<Modifier> randomModifiers = new ArrayList<>(
                HMLModifiers.MODIFIERS.stream()
                        .filter(modifier -> !HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(RoleUtil.getModifierId(modifier)))
                        .toList()
        );

        for (ServerPlayerEntity player : shuffledPlayers) {
            if (countPlayerModifiers(player) >= HarpyModLoaderConfig.HANDLER.instance().modifierMaximum) {
                continue;
            }
            Collections.shuffle(randomModifiers);

            for (Modifier modifier : randomModifiers) {
                if (countPlayerModifiers(player) >= HarpyModLoaderConfig.HANDLER.instance().modifierMaximum) {
                    break;
                }
                if (modifierCounts.get(modifier) >= modifierCaps.get(modifier)) {
                    continue;
                }
                if (worldModifiers.getModifiers(player) != null && worldModifiers.getModifiers(player).contains(modifier)) {
                    continue;
                }
                if (!canReceiveModifier(player, modifier)) {
                    continue;
                }

                worldModifiers.addModifier(player.getUuid(), modifier);
                ModifierAssigned.EVENT.invoker().assignModifier(player, modifier);
                modifierCounts.put(modifier, modifierCounts.get(modifier) + 1);
            }
        }
    }

    private void grantAutomaticGuessers(List<ServerPlayerEntity> shuffledPlayers,
                                        Predicate<Role> additionalGuesserRoles) {
        if (WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.killerAlwaysGuesser()) {
            int grantedGuessers = 0;
            for (ServerPlayerEntity player : shuffledPlayers) {
                if (!worldModifiers.isModifier(player, WyspiaExpressRoles.GUESSER)
                        && (gameWorld.canUseKillerFeatures(player) || additionalGuesserRoles.test(gameWorld.getRole(player)))) {
                    worldModifiers.addModifier(player.getUuid(), WyspiaExpressRoles.GUESSER);
                    ModifierAssigned.EVENT.invoker().assignModifier(player, WyspiaExpressRoles.GUESSER);
                    grantedGuessers++;
                }
                if (grantedGuessers >= WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.maximumGuessers()) {
                    break;
                }
            }
        }
    }

    private void assignRandomBomber(List<ServerPlayerEntity> shuffledPlayers) {
        if (WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.enabled()) {
            if (world.random.nextInt(WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.chance()) == 0) {
                List<ServerPlayerEntity> civilianCandidates = shuffledPlayers.stream()
                        .filter(gameWorld::isInnocent)
                        .toList();
                if (!civilianCandidates.isEmpty()) {
                    int chosenIndex = world.random.nextInt(civilianCandidates.size());
                    ServerPlayerEntity chosen = civilianCandidates.get(chosenIndex);
                    WyspiaExpress.LOGGER.info("[BOMBER] Player {} received BOMBER!", chosen.getName().getString());
                    worldModifiers.addModifier(chosen.getUuid(), WyspiaExpressRoles.BOMBER);
                    ModifierAssigned.EVENT.invoker().assignModifier(chosen, WyspiaExpressRoles.BOMBER);
                }
            }
        }
    }

    private void announceModifiers() {
        for (ServerPlayerEntity player : participants) {
            if (worldModifiers.getModifiers(player) != null && !worldModifiers.getModifiers(player).isEmpty()) {
                MutableText modifiersText = Text.translatable("announcement.modifier").formatted(Formatting.GRAY)
                        .append(Texts.join(
                                worldModifiers.getModifiers(player),
                                Text.literal(", "),
                                modifier -> modifier.getName(false).withColor(modifier.color)
                        ));
                player.sendMessage(modifiersText, true);
            } else if (!HMLModifiers.MODIFIERS.isEmpty()) {
                player.sendMessage(Text.translatable("announcement.no_modifiers").formatted(Formatting.DARK_GRAY), true);
            }
        }
    }

    private boolean canReceiveModifier(ServerPlayerEntity player, Modifier modifier) {
        if (modifier.canOnlyBeAppliedTo != null && gameWorld.getRole(player) != null &&
                !modifier.canOnlyBeAppliedTo.contains(gameWorld.getRole(player))) {
            return false;
        }
        if (modifier.cannotBeAppliedTo != null && gameWorld.getRole(player) != null &&
                modifier.cannotBeAppliedTo.contains(gameWorld.getRole(player))) {
            return false;
        }

        if (modifier.killerOnly && modifier.civilianOnly) {
            return gameWorld.canUseKillerFeatures(player) || gameWorld.isInnocent(player);
        }
        if (modifier.killerOnly && !gameWorld.canUseKillerFeatures(player)) return false;
        if (modifier.civilianOnly && !gameWorld.isInnocent(player)) return false;
        return true;
    }

    private int countPlayerModifiers(ServerPlayerEntity player) {
        return worldModifiers.getModifiers(player) == null ? 0 : worldModifiers.getModifiers(player).size();
    }

    RoundBuilder completeAssignments(int mainThreatCount) {
        return completeAssignments(mainThreatCount, role -> false);
    }

    RoundBuilder completeAssignments(int mainThreatCount, Predicate<Role> additionalGuesserRoles) {
        notifyRoleAssignments();
        int modifierBudget = mainThreatCount * HarpyModLoaderConfig.HANDLER.instance().modifierMultiplier;
        distributeModifiers(modifierBudget, additionalGuesserRoles);
        announceRoles(mainThreatCount);
        Harpymodloader.FORCED_MODDED_ROLE.clear();
        Harpymodloader.FORCED_MODDED_ROLE_FLIP.clear();
        Harpymodloader.FORCED_MODDED_MODIFIER.clear();
        WyspiaExpressRoles.GAME_START_TIME = GameTimeComponent.KEY.get(world).time;
        RoleComponent.KEY.get(world).startRound();
        return this;
    }

    private void notifyRoleAssignments() {
        // Notify role listeners only after the complete role map is populated.
        for (ServerPlayerEntity player : participants) {
            Role role = gameWorld.getRole(player);
            if (role == null) continue;

            ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, role);

            if (gameWorld.canUseKillerFeatures(player)) {
                RoleCategoryStatisticsManager.getInstance()
                        .recordRoleCategory(player.getUuid(), player.getName().getString(), RoleCategoryStatisticsManager.RoleCategory.KILLER);
            } else if (gameWorld.isInnocent(player)) {
                if (gameWorld.isRole(player, WatheRoles.VIGILANTE)) {
                    RoleCategoryStatisticsManager.getInstance()
                            .recordRoleCategory(player.getUuid(), player.getName().getString(), RoleCategoryStatisticsManager.RoleCategory.VIGILANTE);
                } else {
                    RoleCategoryStatisticsManager.getInstance()
                            .recordRoleCategory(player.getUuid(), player.getName().getString(), RoleCategoryStatisticsManager.RoleCategory.CIVILIAN);
                }
            } else {
                RoleCategoryStatisticsManager.getInstance()
                        .recordRoleCategory(player.getUuid(), player.getName().getString(), RoleCategoryStatisticsManager.RoleCategory.NEUTRAL);
            }
        }
    }

    private void announceRoles(int mainThreatCount) {
        for (ServerPlayerEntity player : participants) {
            Role role = gameWorld.getRole(player);
            int announcementIndex;
            if (Harpymodloader.VANNILA_ROLES.contains(role)) {
                if (gameWorld.isRole(player, WatheRoles.KILLER)) {
                    announcementIndex = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.KILLER);
                } else if (gameWorld.isRole(player, WatheRoles.VIGILANTE)) {
                    announcementIndex = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.VIGILANTE);
                } else {
                    announcementIndex = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.CIVILIAN);
                }
            } else {
                announcementIndex = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(
                        Harpymodloader.autogeneratedAnnouncements.get(role)
                );
            }
            ServerPlayNetworking.send(
                    player,
                    new AnnounceWelcomePayload(
                            announcementIndex,
                            mainThreatCount,
                            participants.size() - mainThreatCount
                    )
            );
        }
    }

}
