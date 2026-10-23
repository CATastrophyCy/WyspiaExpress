package org.cat.express.wyspiaexpress.game.gamemode;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import dev.doctor4t.wathe.cca.TrainWorldComponent;
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
import net.minecraft.world.World;
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
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.RoleComponent;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.cat.express.wyspiaexpress.util.RoleUtil;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.*;

/** Assignment owned exclusively by the WyspiaExpress mode; HML keeps its existing mixin. */
final class WyspiaExpressRoleAssignment {
    /** Populate the new round before notifying role and modifier listeners. */
    public void initializeRound(
            ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> participants
    ) {
        org.cat.express.wyspiaexpress.config.GameConfigs.refreshDerivedValues();
        // Reset participants before selecting base teams.
        Harpymodloader.refreshRoles();
        HarpyModLoaderConfig.HANDLER.load();
        TrainWorldComponent.KEY.get(world).setTimeOfDay(TrainWorldComponent.TimeOfDay.NIGHT);
        gameWorld.clearRoleMap();

        WyspiaExpressRoles.ROUND_PLAYER_COUNT = participants.size();
        for (ServerPlayerEntity player : participants) {
            ResetPlayerEvent.EVENT.invoker().resetPlayer(player);
            gameWorld.addRole(player, WatheRoles.CIVILIAN);
        }

        // Category budgets and the actual number of selected base killers are distinct.
        int targetKillerCount = participants.size() / org.cat.express.wyspiaexpress.config.GameConfigs.killerDividend(gameWorld);
        int targetNeutralCount = participants.size() /  WyspiaExpress.GAMEMODES_CONFIG.neutralDividend();
        int assignedKillerCount = assignBaseTeams(world, gameWorld, participants);

        List<ServerPlayerEntity> killerPlayers = new ArrayList<>(participants);
        killerPlayers.removeIf(player -> {
            Role role = gameWorld.getRole(player);
            return !Harpymodloader.OVERWRITE_ROLES.contains(role) || !role.canUseKiller();
        });
        // Replace the selected base killers, or let them choose through Copycat.
        if (WyspiaExpress.ROLES_CONFIG.enableRolePicking()) {
            WyspiaExpressRoles.COPYCAT_ROLES.clear();
            for (ServerPlayerEntity player : killerPlayers) {
                gameWorld.addRole(player, WyspiaExpressRoles.COPYCAT);
            }
        }
        else{
            assignKillerRoles(world, gameWorld, killerPlayers);
        }

        List<ServerPlayerEntity> civilianCandidates = new ArrayList<>(participants);
        civilianCandidates.removeIf(player -> {
            Role role = gameWorld.getRole(player);
            return !Harpymodloader.OVERWRITE_ROLES.contains(role) || role.canUseKiller(); // Base civilians still awaiting a replacement role.
        });

        assignCivilianAndNeutralRoles(world, gameWorld, civilianCandidates, targetNeutralCount);

        // turn any left over civilian into amnesiacs
        for(ServerPlayerEntity player : civilianCandidates){
            if(gameWorld.isRole(player, WatheRoles.CIVILIAN)){
                gameWorld.addRole(player, SERoles.AMNESIAC);
            }
        }
        // lich initialize
        if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(RoleUtil.getRoleId(WyspiaExpressRoles.LICH))) {
            int maxRevives = WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.additionalRevive() + targetKillerCount;
            LichReviveComponent component = LichReviveComponent.KEY.get(world);
            component.setAvailableRevives(WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.additionalRevive());
            component.setMaxRevives(maxRevives);
        }

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

        // modifier assign
        int modifierAssignmentBudget = assignedKillerCount * HarpyModLoaderConfig.HANDLER.instance().modifierMultiplier;
        assignAndAnnounceModifiers(world, gameWorld, participants, modifierAssignmentBudget);

        // role announcements
        for (ServerPlayerEntity player : participants) {
            Role role = gameWorld.getRole(player);
            int index;
            if (Harpymodloader.VANNILA_ROLES.contains(role)) {
                if (gameWorld.isRole(player, WatheRoles.KILLER)) {
                    index = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.KILLER);
                } else if (gameWorld.isRole(player, WatheRoles.VIGILANTE)) {
                    index = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.VIGILANTE);
                } else {
                    index = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.CIVILIAN);
                }
            } else {
                index = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(
                        Harpymodloader.autogeneratedAnnouncements.get(role)
                );
            }
            ServerPlayNetworking.send(
                    player,
                    new AnnounceWelcomePayload(
                            index,
                            assignedKillerCount,
                            participants.size() - assignedKillerCount
                    )
            );
        }
        // clean up
        Harpymodloader.FORCED_MODDED_ROLE.clear();
        Harpymodloader.FORCED_MODDED_ROLE_FLIP.clear();
        Harpymodloader.FORCED_MODDED_MODIFIER.clear();
        // end initialization
        GameTimeComponent gameTimeComponent = GameTimeComponent.KEY.get(world);
        WyspiaExpressRoles.GAME_START_TIME = gameTimeComponent.time;

        RoleComponent.KEY.get(world).startRound();

    }

    private int assignBaseTeams(ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> participants) {
        ScoreboardRoleSelectorComponent roleSelector =
                ScoreboardRoleSelectorComponent.KEY.get(world.getScoreboard());

        int killerSlots =  participants.size() /  org.cat.express.wyspiaexpress.config.GameConfigs.killerDividend(gameWorld);
        int vigilanteSlots = participants.size() /  org.cat.express.wyspiaexpress.config.GameConfigs.vigilanteDividend(gameWorld);

        List<ServerPlayerEntity> vigilanteCandidates = new ArrayList<>(participants);
        vigilanteCandidates.removeIf(player ->
                Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())
        );

        List<ServerPlayerEntity> killerCandidates = new ArrayList<>(participants);
        killerCandidates.removeIf(player -> {
            if (!Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())) {
                return false;
            }
            Role forced = Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(player.getUuid());
            if (forced.canUseKiller()) {
                roleSelector.forcedKillers.add(player.getUuid());
            }
            return true;
        });
        if (killerCandidates.stream().anyMatch(player -> PlayerConfigComponent.KEY.get(player).avoidKiller())) {
            killerCandidates.removeIf(player -> roleSelector.forcedKillers.contains(player.getUuid()));
            killerCandidates = RoleUtil.preferRoleCandidates(killerCandidates,
                    killerSlots - roleSelector.forcedKillers.size(),
                    player -> !PlayerConfigComponent.KEY.get(player).avoidKiller());
        }
        int assignedKillerCount;
        if(WyspiaExpress.GAMEMODES_CONFIG.useCustomWeightedAssignment()){
            assignedKillerCount = assignKillersUsingCategoryHistory(world, roleSelector, gameWorld, killerCandidates, killerSlots);
            vigilanteCandidates.removeIf(gameWorld::canUseKillerFeatures);
            assignVigilantesUsingCategoryHistory(world, roleSelector, gameWorld, vigilanteCandidates, vigilanteSlots);
        }
        else {
            assignedKillerCount = roleSelector.assignKillers(world, gameWorld, killerCandidates, killerSlots);
            vigilanteCandidates.removeIf(gameWorld::canUseKillerFeatures);
            roleSelector.assignVigilantes(world, gameWorld, vigilanteCandidates, vigilanteSlots);
        }
        return assignedKillerCount;
    }
    private int assignKillersUsingCategoryHistory(ServerWorld world, ScoreboardRoleSelectorComponent roleSelector, GameWorldComponent gameWorld,
            List<ServerPlayerEntity> candidatePlayers, int remainingKillerSlots) {
        ArrayList<UUID> selectedKillerIds = new ArrayList<>();
        for (UUID uuid : roleSelector.forcedKillers) {
            selectedKillerIds.add(uuid);
            remainingKillerSlots--;
            roleSelector.killerRounds.put(uuid, roleSelector.killerRounds.getOrDefault(uuid, 1) + 1);
        }
        roleSelector.forcedKillers.clear();

        if(remainingKillerSlots > 0) {
            List<ServerPlayerEntity> eligiblePlayers = new ArrayList<>(candidatePlayers);
            eligiblePlayers.removeIf(player -> selectedKillerIds.contains(player.getUuid())
                    || Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid()));
            eligiblePlayers = RoleUtil.preferRoleCandidates(eligiblePlayers, remainingKillerSlots,
                    player -> !PlayerConfigComponent.KEY.get(player).avoidKiller());
            double totalWeight = 0;
            HashMap<UUID, Double> playerWeights = new HashMap<>();
            for (ServerPlayerEntity player : eligiblePlayers) {
                if (selectedKillerIds.contains(player.getUuid())) continue;
                if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())) continue;
                double weight = RoleCategoryStatisticsManager.getInstance().computeCategoryWeight(org.cat.express.wyspiaexpress.config.GameConfigs.killerDividend(gameWorld), player.getUuid(),
                        RoleCategoryStatisticsManager.RoleCategory.KILLER);
                playerWeights.put(player.getUuid(), weight);
                totalWeight += weight;
            }

            for (int i = 0; i < remainingKillerSlots && !playerWeights.isEmpty(); i++) {
                double random = world.getRandom().nextDouble() * totalWeight;

                UUID selectedPlayerId = RoleUtil.selectWeightedPlayerId(playerWeights, random);
                double weight = playerWeights.get(selectedPlayerId);

                selectedKillerIds.add(selectedPlayerId);
                playerWeights.remove(selectedPlayerId);
                totalWeight -= weight;

                roleSelector.killerRounds.put(selectedPlayerId, roleSelector.killerRounds.getOrDefault(selectedPlayerId, 1) + 1);
            }
        }
        for (UUID killerUUID : selectedKillerIds) {
            gameWorld.addRole(killerUUID, WatheRoles.KILLER);
            PlayerEntity killer = world.getPlayerByUuid(killerUUID);
            if (killer != null) {
                ShopUtil.setCoin(killer, GameConstants.MONEY_START);
            }
        }
        return selectedKillerIds.size();
    }

    private void assignVigilantesUsingCategoryHistory(ServerWorld world, ScoreboardRoleSelectorComponent roleSelector, GameWorldComponent gameWorld,
            List<ServerPlayerEntity> candidatePlayers, int remainingVigilanteSlots) {
        ArrayList<UUID> selectedVigilanteIds = new ArrayList<>();
        for (UUID uuid : roleSelector.forcedVigilantes) {
            selectedVigilanteIds.add(uuid);
            remainingVigilanteSlots--;
            roleSelector.vigilanteRounds.put(uuid, roleSelector.vigilanteRounds.getOrDefault(uuid, 1) + 1);

        }
        roleSelector.forcedVigilantes.clear();

        if(remainingVigilanteSlots > 0) {
            double totalWeight = 0;
            HashMap<UUID, Double> playerWeights = new HashMap<>();
            for (ServerPlayerEntity player : candidatePlayers) {
                if (selectedVigilanteIds.contains(player.getUuid())) continue;
                if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())) continue;
                double weight = RoleCategoryStatisticsManager.getInstance().computeCategoryWeight(org.cat.express.wyspiaexpress.config.GameConfigs.vigilanteDividend(gameWorld), player.getUuid(),
                        RoleCategoryStatisticsManager.RoleCategory.VIGILANTE);
                playerWeights.put(player.getUuid(), weight);
                totalWeight += weight;
            }

            for (int i = 0; i < remainingVigilanteSlots && !playerWeights.isEmpty(); i++) {
                double random = world.getRandom().nextDouble() * totalWeight;

                UUID selectedPlayerId = RoleUtil.selectWeightedPlayerId(playerWeights, random);
                double weight = playerWeights.get(selectedPlayerId);

                selectedVigilanteIds.add(selectedPlayerId);
                playerWeights.remove(selectedPlayerId);
                totalWeight -= weight;

                roleSelector.vigilanteRounds.put(selectedPlayerId, roleSelector.vigilanteRounds.getOrDefault(selectedPlayerId, 1) + 1);
            }
        }

        for (UUID uuid : selectedVigilanteIds) {
            PlayerEntity player = world.getPlayerByUuid(uuid);
            if (player instanceof ServerPlayerEntity serverPlayer && candidatePlayers.contains(serverPlayer) && !gameWorld.canUseKillerFeatures(player)) {
                player.giveItemStack(new ItemStack(WatheItems.REVOLVER)); // I'd like to change this to roleAssigned, but that would break the normal behavior
                gameWorld.addRole(player, WatheRoles.VIGILANTE);
            }
        }
    }
    private List<ServerPlayerEntity> selectNeutralsUsingCategoryHistory(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> candidatePlayers,
            int remainingSlots) {
        List<ServerPlayerEntity> selectedPlayers = new ArrayList<>();
        if (remainingSlots <= 0 || candidatePlayers.isEmpty()) {
            return selectedPlayers;
        }
        List<ServerPlayerEntity> eligiblePlayers = new ArrayList<>(candidatePlayers);
        eligiblePlayers.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())
                || !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
        eligiblePlayers = RoleUtil.preferRoleCandidates(eligiblePlayers, remainingSlots,
                player -> !PlayerConfigComponent.KEY.get(player).avoidNeutral());
        double totalWeight = 0;
        HashMap<UUID, Double> playerWeights = new HashMap<>();
        for (ServerPlayerEntity player : eligiblePlayers) {
            if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())) continue;
            if (!Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player))) continue;
            double weight = RoleCategoryStatisticsManager.getInstance().computeCategoryWeight(WyspiaExpress.GAMEMODES_CONFIG.neutralDividend(), player.getUuid(),
                    RoleCategoryStatisticsManager.RoleCategory.NEUTRAL);
            playerWeights.put(player.getUuid(), weight);
            totalWeight += weight;
        }

        for (int i = 0; i < remainingSlots && !playerWeights.isEmpty(); i++) {
            double random = world.getRandom().nextDouble() * totalWeight;

            UUID selectedPlayerId = RoleUtil.selectWeightedPlayerId(playerWeights, random);
            double weight = playerWeights.get(selectedPlayerId);
            PlayerEntity player = world.getPlayerByUuid(selectedPlayerId);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                selectedPlayers.add(serverPlayer);
                playerWeights.remove(selectedPlayerId);
                totalWeight -= weight;
            }
        }
        return selectedPlayers;
    }
    private void assignKillerRoles(ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> killerPlayers) {
        List<Role> killerRoles = new ArrayList<>();
        for (Role role : RoleUtil.getKillerRoles()) {
            if (Harpymodloader.NON_MURDER_ROLES.contains(role)) continue;
            killerRoles.add(role);
        }
        Collections.shuffle(killerRoles);
        List<ServerPlayerEntity> remainingCandidates = new ArrayList<>(killerPlayers);
        for (Role role : killerRoles) {
            int roleAssignmentCount = Math.min(RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld),
                    remainingCandidates.size());
            if (roleAssignmentCount > 0) {
                roleAssignmentCount = world.getRandom().nextInt(roleAssignmentCount) + 1;
            }

            assignPlayersToRole(
                    roleAssignmentCount, role, remainingCandidates, gameWorld, world
            );
            remainingCandidates.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
        }
    }
    private void assignCivilianAndNeutralRoles(ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> civilianCandidates,
            int targetNeutralCount) {
        List<Role> civilianRoles = new ArrayList<>();
        List<Role> trueNeutralRoles = new ArrayList<>();
        List<Role> killerAlignedNeutralRoles = new ArrayList<>();
        for (Role role : WatheRoles.ROLES) {
            if (Harpymodloader.NON_MURDER_ROLES.contains(role)) continue;
            if (Harpymodloader.VANNILA_ROLES.contains(role)) continue;
            if (HarpyModLoaderConfig.HANDLER.instance().disabled.contains(RoleUtil.getRoleId(role))) continue;
            if(!RoleUtil.roleMeetPlayerRequirement(role)) continue;
            if (WyspiaExpressRoles.TRUE_NEUTRALS.contains(role)) {
                trueNeutralRoles.add(role);
            } else if (WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) {
                killerAlignedNeutralRoles.add(role);
            } else if (role.isInnocent() && !role.canUseKiller()) {
                civilianRoles.add(role);
            }
        }
        List<Role> eligibleNeutralRoles = new ArrayList<>(trueNeutralRoles);
        eligibleNeutralRoles.addAll(killerAlignedNeutralRoles);
        int forcedNeutralCount = assignForcedNeutralRoles(world, gameWorld, civilianCandidates, eligibleNeutralRoles);
        int remainingNeutralSlots = Math.max(0, targetNeutralCount - forcedNeutralCount);

        int maxKillerAlignedNeutralSlots = remainingNeutralSlots / 2 + (remainingNeutralSlots % 2 );
        int killerAlignedNeutralSlots = maxKillerAlignedNeutralSlots <= 0 ? 0 : world.getRandom().nextInt(maxKillerAlignedNeutralSlots + 1);
        int trueNeutralSlots = Math.max(0, remainingNeutralSlots - killerAlignedNeutralSlots);

        List<ServerPlayerEntity> neutralCandidates = WyspiaExpress.GAMEMODES_CONFIG.useCustomWeightedAssignment()?
                selectNeutralsUsingCategoryHistory(world, gameWorld, civilianCandidates, remainingNeutralSlots)
                :
                selectNeutralsUsingRoleHistory(
                world, gameWorld, civilianCandidates, trueNeutralRoles, killerAlignedNeutralRoles, remainingNeutralSlots
        );

        assignNeutralRoles(world, gameWorld, neutralCandidates, trueNeutralRoles, trueNeutralSlots);

        List<ServerPlayerEntity> killerAlignedCandidates = new ArrayList<>(neutralCandidates);
        killerAlignedCandidates.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
        assignNeutralRoles(world, gameWorld, killerAlignedCandidates, killerAlignedNeutralRoles, killerAlignedNeutralSlots);

        for (ServerPlayerEntity player : neutralCandidates) {
            if(gameWorld.isRole(player, WatheRoles.CIVILIAN))
                gameWorld.addRole(player, SERoles.AMNESIAC);
        }
        // Assign civilian replacements to the remaining base civilians.
        List<ServerPlayerEntity> civilianRoleCandidates = new ArrayList<>(civilianCandidates);
        civilianRoleCandidates.removeIf(player -> !gameWorld.getRole(player).isInnocent());

        assignCivilianRoles(world, gameWorld, civilianRoleCandidates, civilianRoles);
    }

    private int assignForcedNeutralRoles(ServerWorld world, GameWorldComponent gameWorld,
            List<ServerPlayerEntity> candidatePlayers, List<Role> eligibleNeutralRoles) {
        List<ServerPlayerEntity> remainingCandidates = new ArrayList<>(candidatePlayers);
        int assignedCount = 0;
        for (Role role : eligibleNeutralRoles) {
            assignedCount += assignPlayersToRole(0, role, remainingCandidates, gameWorld, world);
            remainingCandidates.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
        }
        return assignedCount;
    }

    private List<ServerPlayerEntity> selectNeutralsUsingRoleHistory(ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> candidatePlayers,
            List<Role> trueNeutralRoles,
            List<Role> killerAlignedNeutralRoles,
            int remainingSlots) {
        List<ServerPlayerEntity> selectedPlayers = new ArrayList<>();
        if (remainingSlots <= 0 || candidatePlayers.isEmpty()) {
            return selectedPlayers;
        }
        List<ServerPlayerEntity> eligiblePlayers = new ArrayList<>(candidatePlayers);
        eligiblePlayers.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())
                || !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
        eligiblePlayers = RoleUtil.preferRoleCandidates(eligiblePlayers, remainingSlots,
                player -> !PlayerConfigComponent.KEY.get(player).avoidNeutral());
        List<Role> eligibleRoles = new ArrayList<>(trueNeutralRoles);
        eligibleRoles.addAll(killerAlignedNeutralRoles);
        return RoleUtil.selectPlayersByRoleHistory(eligiblePlayers, eligibleRoles, remainingSlots,
                gameWorld.areWeightsEnabled(), world.getRandom());
    }
    private void assignNeutralRoles(ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> candidatePlayers,
            List<Role> roles,
            int groupAssignmentLimit) {
        if (groupAssignmentLimit <= 0 || candidatePlayers.isEmpty() || roles.isEmpty()) return;

        List<Role> shuffledRoles = new ArrayList<>(roles);
        Collections.shuffle(shuffledRoles);
        List<ServerPlayerEntity> remainingCandidates = new ArrayList<>(candidatePlayers);
        groupAssignmentLimit = Math.min(groupAssignmentLimit, candidatePlayers.size());
        int assignedCount = 0;
        for (Role role : shuffledRoles) {
            if (assignedCount >= groupAssignmentLimit) break;

            int roleAssignmentCount = Math.min(RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld),
                    remainingCandidates.size());
            if (!Harpymodloader.ROLE_MAX.containsKey(role.identifier())
                    && !WyspiaExpressRoles.ROLES_BASIC_CONFIG.containsKey(role)) {
                roleAssignmentCount = Math.min(Math.round((float) candidatePlayers.size() / shuffledRoles.size()), roleAssignmentCount);
            }
            roleAssignmentCount = Math.min(roleAssignmentCount, groupAssignmentLimit - assignedCount);

            int newAssignments = assignPlayersToRole(
                    roleAssignmentCount, role, remainingCandidates, gameWorld, world
            );

            remainingCandidates.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
            assignedCount += newAssignments;

        }
    }
    private void assignCivilianRoles(ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> civilianRoleCandidates,
            List<Role> civilianRoles) {
        if (civilianRoleCandidates.isEmpty() || civilianRoles.isEmpty()) return;
        List<Role> shuffledCivilianRoles = new ArrayList<>(civilianRoles);
        Collections.shuffle(shuffledCivilianRoles);

        List<ServerPlayerEntity> remainingCandidates = new ArrayList<>(civilianRoleCandidates);
        for (Role role : shuffledCivilianRoles) {
            int roleAssignmentCount = Math.min(RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld),
                    remainingCandidates.size());
            if (!Harpymodloader.ROLE_MAX.containsKey(role.identifier())
                    && !WyspiaExpressRoles.ROLES_BASIC_CONFIG.containsKey(role)) {
                roleAssignmentCount = Math.min(Math.round((float) civilianRoleCandidates.size() / shuffledCivilianRoles.size()), roleAssignmentCount);
            }
            if(roleAssignmentCount > 0)
                roleAssignmentCount = world.random.nextInt(roleAssignmentCount) + 1;

            assignPlayersToRole(
                    roleAssignmentCount, role, remainingCandidates, gameWorld, world
            );
            remainingCandidates.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));

        }
    }

    private int assignPlayersToRole(int requestedAssignments,
            Role role,
            List<ServerPlayerEntity> candidatePlayers,
            GameWorldComponent gameWorld,
            World world) {
        List<ServerPlayerEntity> assignedPlayers = new ArrayList<>();

        if (Harpymodloader.FORCED_MODDED_ROLE.containsKey(role)) {
            for (UUID uuid : Harpymodloader.FORCED_MODDED_ROLE.get(role)) {
                PlayerEntity player = world.getPlayerByUuid(uuid);
                if (player instanceof ServerPlayerEntity serverPlayer && candidatePlayers.contains(serverPlayer)
                        && role.equals(Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(uuid))
                        && !assignedPlayers.contains(serverPlayer)) {
                    assignedPlayers.add(serverPlayer);
                    requestedAssignments--;
                    ModdedWeights.roleRounds
                            .computeIfAbsent(role, r -> new HashMap<>())
                            .put(player.getUuid(),
                                    ModdedWeights.roleRounds.get(role).getOrDefault(player.getUuid(), 1) + 1);
                }
            }
        }
        // Forced picks retain their override; normal picks use only the remaining scaled slots.
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if (config != null && config.playersPerAdditionalSpawn() > 0) {
            int remainingRoleSlots = Math.max(0,
                    RoleUtil.getRemainingRoleSpawnLimit(role, gameWorld) - assignedPlayers.size());
            requestedAssignments = Math.min(requestedAssignments, remainingRoleSlots);
        }
        List<ServerPlayerEntity> eligiblePlayers = new ArrayList<>(candidatePlayers);
        eligiblePlayers.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid()));
        for (ServerPlayerEntity player : RoleUtil.selectPlayersByRoleHistory(eligiblePlayers, List.of(role),
                requestedAssignments, gameWorld.areWeightsEnabled(), world.getRandom())) {
            assignedPlayers.add(player);
            ModdedWeights.roleRounds
                    .computeIfAbsent(role, r -> new HashMap<>())
                    .put(player.getUuid(), ModdedWeights.roleRounds.get(role).getOrDefault(player.getUuid(), 1) + 1);
        }

        int assignedCount = assignedPlayers.size();
        for (ServerPlayerEntity player : assignedPlayers) {
            gameWorld.addRole(player, role);
        }
        return assignedCount;
    }
    private void assignAndAnnounceModifiers(ServerWorld world,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> participants,
            int modifierAssignmentBudget) {

        WorldModifierComponent worldModifiers = WorldModifierComponent.KEY.get(world);
        worldModifiers.getModifiers().clear();

        // Pre-calculate the maximum allowed distribution for each modifier
        Map<Modifier, Integer> modifierCaps = new HashMap<>();
        Map<Modifier, Integer> modifierCounts = new HashMap<>();

        int killerModifierTypes = (int) HMLModifiers.MODIFIERS.stream().filter(m -> m.killerOnly).count();
        for (Modifier modifier : HMLModifiers.MODIFIERS) {
            modifierCounts.put(modifier, 0);
            int modifierCap = modifierAssignmentBudget;
            if (modifier.killerOnly && killerModifierTypes > 0) {
                modifierCap = Math.max(1,
                        (int) Math.floor(
                                (participants.size() / (double) org.cat.express.wyspiaexpress.config.GameConfigs.killerDividend(gameWorld)) / killerModifierTypes
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

        // Forced modifiers
        if (!Harpymodloader.FORCED_MODDED_MODIFIER.isEmpty()) {
            for (Modifier modifier : HMLModifiers.MODIFIERS) {
                if (Harpymodloader.FORCED_MODDED_MODIFIER.containsKey(modifier)) {
                    List<UUID> forcedUUIDs = Harpymodloader.FORCED_MODDED_MODIFIER.get(modifier);
                    for (ServerPlayerEntity player : participants) {
                        if (forcedUUIDs.contains(player.getUuid())) {
                            worldModifiers.addModifier(player.getUuid(), modifier);
                            ModifierAssigned.EVENT.invoker().assignModifier(player, modifier);
                            modifierCounts.put(modifier, modifierCounts.get(modifier) + 1);
                        }
                    }
                }
            }
        }

        // Random assignment
        List<ServerPlayerEntity> shuffledPlayers = new ArrayList<>(participants);
        Collections.shuffle(shuffledPlayers);

        List<Modifier> randomModifiers = new ArrayList<>(
                HMLModifiers.MODIFIERS.stream()
                        .filter(modifier -> !HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(RoleUtil.getModifierId(modifier)))
                        .toList()
        );

        for (ServerPlayerEntity player : shuffledPlayers) {
            if (countPlayerModifiers(player, worldModifiers) >= HarpyModLoaderConfig.HANDLER.instance().modifierMaximum) {
                continue;
            }
            Collections.shuffle(randomModifiers);

            for (Modifier modifier : randomModifiers) {
                if (countPlayerModifiers(player, worldModifiers) >= HarpyModLoaderConfig.HANDLER.instance().modifierMaximum) {
                    break;
                }
                if (modifierCounts.get(modifier) >= modifierCaps.get(modifier)) {
                    continue;
                }
                if (worldModifiers.getModifiers(player) != null && worldModifiers.getModifiers(player).contains(modifier)) {
                    continue;
                }
                if (!canReceiveModifier(player, modifier, gameWorld)) {
                    continue;
                }

                worldModifiers.addModifier(player.getUuid(), modifier);
                ModifierAssigned.EVENT.invoker().assignModifier(player, modifier);
                modifierCounts.put(modifier, modifierCounts.get(modifier) + 1);
            }
        }

        // Killer guessers
        if (WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.killerAlwaysGuesser()) {
            int count = 0;
            for (ServerPlayerEntity player : shuffledPlayers) {
                if (!worldModifiers.isModifier(player, WyspiaExpressRoles.GUESSER) && gameWorld.canUseKillerFeatures(player)) {
                    worldModifiers.addModifier(player.getUuid(), WyspiaExpressRoles.GUESSER);
                    ModifierAssigned.EVENT.invoker().assignModifier(player, WyspiaExpressRoles.GUESSER);
                    count++;
                }
                if (count >= WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.maximumGuessers()) {
                    break;
                }
            }
        }

        // Random civilian bomber
        if (WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.enabled()) {
            if (world.random.nextInt(WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.chance()) == 0) {
                List<ServerPlayerEntity> civilianCandidates = shuffledPlayers.stream()
                        .filter(gameWorld::isInnocent)
                        .toList();
                if (!civilianCandidates.isEmpty()) {
                    int i = world.random.nextInt(civilianCandidates.size());
                    ServerPlayerEntity chosen = civilianCandidates.get(i);
                    WyspiaExpress.LOGGER.info("[BOMBER] Player {} received BOMBER!", chosen.getName().getString());
                    worldModifiers.addModifier(chosen.getUuid(), WyspiaExpressRoles.BOMBER);
                    ModifierAssigned.EVENT.invoker().assignModifier(chosen, WyspiaExpressRoles.BOMBER);
                }
            }
        }

        // Modifier announcements
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

    private boolean canReceiveModifier(ServerPlayerEntity player,
            Modifier modifier,
            GameWorldComponent gameWorld) {
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

    private int countPlayerModifiers(ServerPlayerEntity player,
            WorldModifierComponent worldModifiers) {
        return worldModifiers.getModifiers(player) == null ? 0 : worldModifiers.getModifiers(player).size();
    }
}
