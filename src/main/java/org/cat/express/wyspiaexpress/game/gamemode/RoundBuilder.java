package org.cat.express.wyspiaexpress.game.gamemode;

import org.cat.express.wyspiaexpress.components.roles.LoversComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
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
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.cat.express.wyspiaexpress.config.GameConfigs;
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
    private final Map<Pool, List<ServerPlayerEntity>> draftCandidates = new EnumMap<>(Pool.class);
    private boolean nativeTeamSelection;

    RoundBuilder(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> participants) {
        this(world, gameWorld, participants,
                (pool, player) -> RoleDraftStatisticsManager.getInstance().computeWeight(player.getUuid(), pool));
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
        LoversComponent.KEY.get(world).reset();
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
        List<ServerPlayerEntity> candidates = RoleUtil.prepareKillerCandidates(participants, nativeRoleSelector);
        draftCandidates.put(Pool.KILLER, candidates);
        return nativeTeamSelection ? RoleUtil.assignNativeKillers(world, gameWorld, nativeRoleSelector, candidates, killerQuota)
                : RoleUtil.assignWeightedKillers(world, gameWorld, nativeRoleSelector, candidates, killerQuota,
                        player -> getWeight(Pool.KILLER, player));
    }

    RoundBuilder assignVigilantes(int vigilanteQuota) {
        List<ServerPlayerEntity> candidates = unassignedCivilians();
        candidates.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid()));
        RoleUtil.restrictForcedPlayers(nativeRoleSelector.forcedVigilantes, candidates);
        draftCandidates.put(Pool.VIGILANTE, candidates.stream()
                .filter(player -> !nativeRoleSelector.forcedVigilantes.contains(player.getUuid())).toList());
        if (nativeTeamSelection) nativeRoleSelector.assignVigilantes(world, gameWorld, candidates, vigilanteQuota);
        else RoleUtil.assignWeightedVigilantes(world, gameWorld, nativeRoleSelector, candidates, vigilanteQuota,
                player -> getWeight(Pool.VIGILANTE, player));
        return this;
    }

    RoundBuilder assignCopycatRoles() {
        WyspiaExpressRoles.COPYCAT_ROLES.clear();
        for (ServerPlayerEntity player : unassignedKillers()) gameWorld.addRole(player, WyspiaExpressRoles.COPYCAT);
        return this;
    }

    List<Role> eligibleRoles(Predicate<Role> allowed) {
        return WatheRoles.ROLES.stream().filter(role ->
                role != WyspiaExpressRoles.LOVER_2
                        && !Harpymodloader.NON_MURDER_ROLES.contains(role)
                        && !Harpymodloader.VANNILA_ROLES.contains(role)
                        && allowed.test(role)
                        && !HarpyModLoaderConfig.HANDLER.instance().disabled.contains(RoleUtil.getRoleId(role))
                        && RoleUtil.roleMeetPlayerRequirement(role)).toList();
    }

    int assignNeutralPool(List<Role> rolePool, int quota, Pool historyPool) {
        List<ServerPlayerEntity> candidates = unassignedCivilians();
        int forcedCount = assignForcedRoles(candidates, rolePool);
        int remainingQuota = Math.max(0, quota - forcedCount);
        draftCandidates.put(historyPool, RoleUtil.eligibleNeutralCandidates(gameWorld, candidates));
        List<ServerPlayerEntity> selectedPlayers = new ArrayList<>(
                selectPlayersForNeutralPool(candidates, remainingQuota, historyPool));
        int assignedCount = assignNeutralRoleCopies(selectedPlayers, rolePool, remainingQuota);
        // A partner can come from the shortlist, but Lover 2 does not fill a neutral slot.
        List<ServerPlayerEntity> picks = selectedPlayers;
        while (assignedCount < remainingQuota
                && picks.stream().anyMatch(player -> gameWorld.isRole(player, WyspiaExpressRoles.LOVER_2))) {
            int slots = remainingQuota - assignedCount;
            picks = selectPlayersForNeutralPool(candidates, slots, historyPool);
            int added = assignNeutralRoleCopies(picks, rolePool, slots);
            if (added == 0) break;
            assignedCount += added;
            selectedPlayers.addAll(picks);
        }
        assignAmnesiacFallback(selectedPlayers);
        return forcedCount + assignedCount;
    }

    void recordDraftHistory() {
        RoleDraftStatisticsManager.getInstance().recordRound(participants, gameWorld, draftCandidates);
    }

    List<ServerPlayerEntity> selectPlayersForNeutralPool(List<ServerPlayerEntity> candidates, int quota, Pool historyPool) {
        return RoleUtil.selectPreferredPlayers(RoleUtil.eligibleNeutralCandidates(gameWorld, candidates), quota, RoleUtil::wantsNeutral,
                (players, slots) -> RoleUtil.selectPlayersByWeight(players, slots,
                        player -> getWeight(historyPool, player), world.getRandom()));
    }

    private double getWeight(Pool pool, ServerPlayerEntity player) {
        return gameWorld.areWeightsEnabled() ? selectionWeights.applyAsDouble(pool, player) : 1;
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

    List<ServerPlayerEntity> selectNeutralPlayersByRoleHistory(List<ServerPlayerEntity> candidates,
            List<Role> rolePool, int quota) {
        if (rolePool.isEmpty()) return List.of();
        return RoleUtil.selectPreferredPlayers(RoleUtil.eligibleNeutralCandidates(gameWorld, candidates), quota, RoleUtil::wantsNeutral,
                (players, slots) -> RoleUtil.selectPlayersByRoleHistory(players, rolePool, slots,
                        gameWorld.areWeightsEnabled(), world.getRandom()));
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

    RoundBuilder assignCivilianReplacements(List<Role> eligibleRoles) {
        List<Role> civilianRoles = new ArrayList<>(eligibleRoles);
        civilianRoles.removeIf(role -> !role.isInnocent() || role.canUseKiller());
        List<ServerPlayerEntity> candidates = unassignedCivilians();
        if (candidates.isEmpty() || civilianRoles.isEmpty()) return this;
        Collections.shuffle(civilianRoles);

        List<ServerPlayerEntity> unassignedCandidates = new ArrayList<>(candidates);
        for (Role role : civilianRoles) {
            int copyCount = Math.min(RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld),
                    unassignedCandidates.size());
            if (!Harpymodloader.ROLE_MAX.containsKey(role.identifier())
                    && !WyspiaExpressRoles.ROLES_BASIC_CONFIG.containsKey(role)) {
                copyCount = Math.min(Math.round((float) candidates.size() / civilianRoles.size()), copyCount);
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
        if (role == WyspiaExpressRoles.LOVER_1) {
            return LoversComponent.KEY.get(world).assignPairs(requestedCount, candidates);
        }
        List<ServerPlayerEntity> assignedPlayers = new ArrayList<>();

        if (Harpymodloader.FORCED_MODDED_ROLE.containsKey(role)) {
            for (UUID uuid : Harpymodloader.FORCED_MODDED_ROLE.get(role)) {
                PlayerEntity player = world.getPlayerByUuid(uuid);
                if (player instanceof ServerPlayerEntity serverPlayer && candidates.contains(serverPlayer)
                        && role.equals(Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(uuid))
                        && !assignedPlayers.contains(serverPlayer)) {
                    assignedPlayers.add(serverPlayer);
                    requestedCount--;
                    RoleUtil.recordRoleAssignment(serverPlayer, role);
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
        for (ServerPlayerEntity player : RoleUtil.selectPlayersForRole(eligibleCandidates, role,
                requestedCount, gameWorld.areWeightsEnabled(), world.getRandom())) {
            assignedPlayers.add(player);
            RoleUtil.recordRoleAssignment(player, role);
        }

        int assignedCount = assignedPlayers.size();
        for (ServerPlayerEntity player : assignedPlayers) {
            gameWorld.addRole(player, role);
        }
        return assignedCount;
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
            RoleCategoryStatisticsManager.getInstance().recordRoundRole(player, role);
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
