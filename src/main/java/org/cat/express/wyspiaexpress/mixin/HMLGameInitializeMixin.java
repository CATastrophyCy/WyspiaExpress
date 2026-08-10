package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.*;
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
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.agmas.harpymodloader.modded_murder.ModdedWeights;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.RoleCategoryStatisticsManager;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.RoleComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.*;

@Mixin(ModdedMurderGameMode.class)
public abstract class HMLGameInitializeMixin {

    @Unique
    private boolean HAS_HACKER;
    /**
     * Complete replacement for ModdedMurderGameMode.initializeGame.
     */
    @Inject(method = "initializeGame", at = @At("HEAD"), cancellable = true)
    private void wyspiaexpress$initializeGame(
            ServerWorld serverWorld,
            GameWorldComponent gameWorld,
            List<ServerPlayerEntity> players,
            CallbackInfo ci
    ) {
        // init
        Harpymodloader.refreshRoles();
        HarpyModLoaderConfig.HANDLER.load();
        TrainWorldComponent.KEY.get(serverWorld).setTimeOfDay(TrainWorldComponent.TimeOfDay.NIGHT);
        gameWorld.clearRoleMap();

        for (ServerPlayerEntity player : players) {
            ResetPlayerEvent.EVENT.invoker().resetPlayer(player);
            gameWorld.addRole(player, WatheRoles.CIVILIAN);
        }

        // assign roles
        int killerCount = players.size() / gameWorld.getKillerDividend();
        int neutralCount = players.size() /  WyspiaExpress.SERVER_CONFIG.neutralDividend();
        int roleCount = wyspiaexpress$assignBaseVanillaRoles(serverWorld, gameWorld, players);

        HAS_HACKER = false;

        List<ServerPlayerEntity> killers = new ArrayList<>(players);
        killers.removeIf(player -> {
            Role role = gameWorld.getRole(player);
            return !Harpymodloader.OVERWRITE_ROLES.contains(role) || !role.canUseKiller();
        });
        // copy cat or normal killer assignment
        if (WyspiaExpress.ROLES_CONFIG.enableRolePicking()) {
            WyspiaExpressRoles.COPYCAT_ROLES.clear();
            for (ServerPlayerEntity player : killers) {
                gameWorld.addRole(player, WyspiaExpressRoles.COPYCAT);
            }
        }
        else{
            wyspiaexpress$assignKillerReplacements(serverWorld, gameWorld, killers, roleCount);
        }

        List<ServerPlayerEntity> civilians = new ArrayList<>(players);
        civilians.removeIf(player -> {
            Role role = gameWorld.getRole(player);
            return !Harpymodloader.OVERWRITE_ROLES.contains(role) || role.canUseKiller(); // civilians are all that are not vigilante or killer
        });

        wyspiaexpress$assignCivilianNeutrals(serverWorld, gameWorld, civilians, roleCount, neutralCount);

        // turn any left over civilian into amnesiacs
        for(ServerPlayerEntity player : civilians){
            if(gameWorld.isRole(player, WatheRoles.CIVILIAN)){
                gameWorld.addRole(player, SERoles.AMNESIAC);
            }
        }
        // lich initialize
        if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(WyspiaExpressRoles.LICH.identifier().toString())) {
            int maxRevives = WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.additionalRevive() + killerCount;
            LichReviveComponent component = LichReviveComponent.KEY.get(serverWorld);
            component.setAvailableRevives(WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.additionalRevive());
            component.setMaxRevives(maxRevives);
        }

        // moddedRoleAssignedEvents and stats recording
        for (ServerPlayerEntity player : players) {
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
        int modifierRoleCount = roleCount * HarpyModLoaderConfig.HANDLER.instance().modifierMultiplier;
        wyspiaexpress$assignModifiersAndAnnounce(serverWorld, gameWorld, players, modifierRoleCount, killerCount);

        // role announcements
        for (ServerPlayerEntity player : players) {
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
                            roleCount,
                            players.size() - roleCount
                    )
            );
            //WyspiaExpress.LOGGER.info("Announcement sent!, player {}, role {}", player.getName().getString(), role.identifier().toString());
        }
        // clean up
        Harpymodloader.FORCED_MODDED_ROLE.clear();
        Harpymodloader.FORCED_MODDED_ROLE_FLIP.clear();
        Harpymodloader.FORCED_MODDED_MODIFIER.clear();
        // end initialization
        GameTimeComponent gameTimeComponent = GameTimeComponent.KEY.get(serverWorld);
        WyspiaExpressRoles.GAME_START_TIME = gameTimeComponent.time;

        RoleComponent.KEY.get(serverWorld).sync();

        ci.cancel();
    }

    @Unique
    private int wyspiaexpress$assignBaseVanillaRoles(ServerWorld serverWorld,
                                                     GameWorldComponent gameWorld,
                                                     List<ServerPlayerEntity> players) {
        ScoreboardRoleSelectorComponent roleSelector =
                ScoreboardRoleSelectorComponent.KEY.get(serverWorld.getScoreboard());

        int killerCount =  players.size() /  gameWorld.getKillerDividend();
        int vigilanteCount = players.size() /  gameWorld.getVigilanteDividend();

        List<ServerPlayerEntity> playersForVigilante = new ArrayList<>(players);
        playersForVigilante.removeIf(player ->
                Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())
        );

        List<ServerPlayerEntity> playersForKiller = new ArrayList<>(players);
        playersForKiller.removeIf(player -> {
            if (!Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())) {
                return false;
            }
            Role forced = Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(player.getUuid());
            if (forced.canUseKiller()) {
                roleSelector.forcedKillers.add(player.getUuid());
            }
            return true;
        });
        int total;
        if(WyspiaExpress.SERVER_CONFIG.useCustomWeightedAssignment()){
            total = customKillerAssigment(serverWorld, roleSelector, gameWorld, playersForKiller, killerCount);
            playersForVigilante.removeIf(gameWorld::canUseKillerFeatures);
            customVigilanteAssigment(serverWorld, roleSelector, gameWorld, playersForVigilante, vigilanteCount);
        }
        else {
            total = roleSelector.assignKillers(serverWorld, gameWorld, playersForKiller, killerCount);
            playersForVigilante.removeIf(gameWorld::canUseKillerFeatures);
            roleSelector.assignVigilantes(serverWorld, gameWorld, playersForVigilante, vigilanteCount);
        }
        return total;
    }
    @Unique
    private int customKillerAssigment(ServerWorld world, ScoreboardRoleSelectorComponent roleSelector, GameWorldComponent gameWorld,
                                      List<ServerPlayerEntity> players, int killerCount) {
        ArrayList<UUID> killers = new ArrayList<>();
        for (UUID uuid : roleSelector.forcedKillers) {
            killers.add(uuid);
            killerCount--;
            roleSelector.killerRounds.put(uuid, roleSelector.killerRounds.getOrDefault(uuid, 1) + 1);
        }
        roleSelector.forcedKillers.clear();

        if(killerCount > 0) {
            double totalWeight = 0;
            HashMap<UUID, Double> weighMap = new HashMap<>();
            for (ServerPlayerEntity player : players) {
                double weight = RoleCategoryStatisticsManager.getInstance().computeCategoryWeight(gameWorld.getKillerDividend(), player.getUuid(),
                        RoleCategoryStatisticsManager.RoleCategory.KILLER);
                weighMap.put(player.getUuid(), weight);
                totalWeight += weight;
            }

            for (int i = 0; i < killerCount; i++) {
                double random = world.getRandom().nextDouble() * totalWeight;

                UUID selected = weightedPick(weighMap, random);
                double weight = weighMap.get(selected);

                killers.add(selected);
                weighMap.remove(selected);
                totalWeight -= weight;

                roleSelector.killerRounds.put(selected, roleSelector.killerRounds.getOrDefault(selected, 1) + 1);
            }
        }
        for (UUID killerUUID : killers) {
            gameWorld.addRole(killerUUID, WatheRoles.KILLER);
            PlayerEntity killer = world.getPlayerByUuid(killerUUID);
            if (killer != null) {
                PlayerShopComponent.KEY.get(killer).setBalance(GameConstants.MONEY_START);
            }
        }
        return killers.size();
    }
    @Unique
    private UUID weightedPick(Map<UUID, Double> weighMap, double random) {
        double cumulativeWeight = 0.0;
        for (Map.Entry<UUID, Double> entry : weighMap.entrySet()) {
            double weight = entry.getValue();
            cumulativeWeight += weight;
            if(cumulativeWeight >= random) {
                return entry.getKey();
            }
        }

        return weighMap.entrySet().iterator().next().getKey();
    }
    @Unique
    private void customVigilanteAssigment(ServerWorld world, ScoreboardRoleSelectorComponent roleSelector, GameWorldComponent gameWorld,
                                          List<ServerPlayerEntity> players, int vigilanteCount) {
        ArrayList<UUID> vigilantes = new ArrayList<>();
        for (UUID uuid : roleSelector.forcedVigilantes) {
            vigilantes.add(uuid);
            vigilanteCount--;
            roleSelector.vigilanteRounds.put(uuid, roleSelector.vigilanteRounds.getOrDefault(uuid, 1) + 1);

        }
        roleSelector.forcedVigilantes.clear();

        if(vigilanteCount > 0) {
            double totalWeight = 0;
            HashMap<UUID, Double> weighMap = new HashMap<>();
            for (ServerPlayerEntity player : players) {
                double weight = RoleCategoryStatisticsManager.getInstance().computeCategoryWeight(gameWorld.getKillerDividend(), player.getUuid(),
                        RoleCategoryStatisticsManager.RoleCategory.VIGILANTE);
                weighMap.put(player.getUuid(), weight);
                totalWeight += weight;
            }

            for (int i = 0; i < vigilanteCount; i++) {
                double random = world.getRandom().nextDouble() * totalWeight;

                UUID selected = weightedPick(weighMap, random);
                double weight = weighMap.get(selected);

                vigilantes.add(selected);
                weighMap.remove(selected);
                totalWeight -= weight;

                roleSelector.vigilanteRounds.put(selected, roleSelector.vigilanteRounds.getOrDefault(selected, 1) + 1);
            }
        }

        for (UUID uuid : vigilantes) {
            PlayerEntity player = world.getPlayerByUuid(uuid);
            if (player instanceof ServerPlayerEntity serverPlayer && players.contains(serverPlayer) && !gameWorld.canUseKillerFeatures(player)) {
                player.giveItemStack(new ItemStack(WatheItems.REVOLVER)); // I'd like to change this to roleAssigned, but that would break the normal behavior
                gameWorld.addRole(player, WatheRoles.VIGILANTE);
            }
        }
    }
    @Unique
    private List<ServerPlayerEntity> customNeutralSelection(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> players,
                                                            List<Role> trueNeutralRoles, List<Role> killerSidedNeutralRoles, int neutralCount ) {
        List<ServerPlayerEntity> picked = new ArrayList<>();
        if (neutralCount <= 0 || players.isEmpty()) {
            return picked;
        }
        Set<UUID> pickedUuids = new HashSet<>();
        Set<Role> allNeutralRoles = new HashSet<>();
        allNeutralRoles.addAll(trueNeutralRoles);
        allNeutralRoles.addAll(killerSidedNeutralRoles);

        for (Role role : allNeutralRoles) {
            List<UUID> forced = Harpymodloader.FORCED_MODDED_ROLE.get(role);
            if (forced == null) continue;

            for (UUID uuid : forced) {
                PlayerEntity player = world.getPlayerByUuid(uuid);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    if (!players.contains(serverPlayer)) continue;
                    if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(uuid)) continue;
                    if (!Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(serverPlayer))) continue;
                    if (pickedUuids.add(uuid)) {
                        picked.add(serverPlayer);
                        neutralCount--;
                    }
                }
            }
        }

        if(neutralCount > 0) {
            double totalWeight = 0;
            HashMap<UUID, Double> weighMap = new HashMap<>();
            for (ServerPlayerEntity player : players) {
                double weight = RoleCategoryStatisticsManager.getInstance().computeCategoryWeight(gameWorld.getKillerDividend(), player.getUuid(),
                        RoleCategoryStatisticsManager.RoleCategory.NEUTRAL);
                weighMap.put(player.getUuid(), weight);
                totalWeight += weight;
            }

            for (int i = 0; i < neutralCount; i++) {
                double random = world.getRandom().nextDouble() * totalWeight;

                UUID selected = weightedPick(weighMap, random);
                double weight = weighMap.get(selected);
                PlayerEntity player = world.getPlayerByUuid(selected);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    picked.add(serverPlayer);
                    weighMap.remove(selected);
                    totalWeight -= weight;
                }
            }
        }
        return picked;
    }
    @Unique
    private void wyspiaexpress$assignKillerReplacements(ServerWorld world,
                                                        GameWorldComponent gwc,
                                                        List<ServerPlayerEntity> killers,
                                                        int desiredRoleCount) {
        List<Role> killerRoles = new ArrayList<>();
        for (Role role : WatheRoles.ROLES) {
            if (Harpymodloader.NON_MURDER_ROLES.contains(role)) continue;
            if (Harpymodloader.VANNILA_ROLES.contains(role)) continue;
            if (HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().toString())) continue;

            if (role.canUseKiller()) {
                killerRoles.add(role);
            }
        }
        Collections.shuffle(killerRoles);
        List<ServerPlayerEntity> assigning = new ArrayList<>(killers);
        for (Role role : killerRoles) {
            int roleSpecificDesiredCount = desiredRoleCount;

            if (Harpymodloader.ROLE_MAX.containsKey(role.identifier())) {
                roleSpecificDesiredCount = Math.min(Harpymodloader.ROLE_MAX.get(role.identifier()), roleSpecificDesiredCount);
            }
            if (roleSpecificDesiredCount > 0) {
                roleSpecificDesiredCount = world.getRandom().nextInt(roleSpecificDesiredCount) + 1;
            }

            wyspiaexpress$findAndAssignPlayers(
                    roleSpecificDesiredCount, role, assigning, gwc, world
            );
            assigning.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gwc.getRole(player)));
        }
    }
    @Unique
    private void wyspiaexpress$assignCivilianNeutrals(ServerWorld world,
                                                      GameWorldComponent gwc,
                                                      List<ServerPlayerEntity> civilians,
                                                      int desiredRoleCount,
                                                      int neutralCount) {
        List<Role> civilianRoles = new ArrayList<>();
        List<Role> trueNeutralRoles = new ArrayList<>();
        List<Role> killerSidedNeutralRoles = new ArrayList<>();
        for (Role role : WatheRoles.ROLES) {
            if (Harpymodloader.NON_MURDER_ROLES.contains(role)) continue;
            if (Harpymodloader.VANNILA_ROLES.contains(role)) continue;
            if (HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().toString())) continue;

            if (WyspiaExpressRoles.TRUE_NEUTRALS.contains(role)) {
                trueNeutralRoles.add(role);
            } else if (WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) {
                killerSidedNeutralRoles.add(role);
            } else if (role.isInnocent() && !role.canUseKiller()) {
                civilianRoles.add(role);
            }
        }
        // neutrals
        int killerSidedNeutralMax = neutralCount / 2 + (neutralCount % 2 );
        int killerSidedNeutralCount = killerSidedNeutralMax <= 0 ? 0 : world.getRandom().nextInt(killerSidedNeutralMax + 1);
        int trueNeutralCount = Math.max(0, neutralCount - killerSidedNeutralCount);

        List<ServerPlayerEntity> neutralPlayers = WyspiaExpress.SERVER_CONFIG.useCustomWeightedAssignment()?
                customNeutralSelection(world, gwc, civilians, trueNeutralRoles, killerSidedNeutralRoles, neutralCount)
                :
                wyspiaexpress$pickPlayersForNeutral(
                world, gwc, civilians, trueNeutralRoles, killerSidedNeutralRoles, neutralCount
        );

        wyspiaexpress$assignRoleGroupToPlayers(world, gwc, neutralPlayers, trueNeutralRoles, trueNeutralCount, desiredRoleCount);

        List<ServerPlayerEntity> playersForKillerNeutral = new ArrayList<>(neutralPlayers);
        playersForKillerNeutral.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gwc.getRole(player)));
        wyspiaexpress$assignRoleGroupToPlayers(world, gwc, playersForKillerNeutral, killerSidedNeutralRoles, killerSidedNeutralCount, desiredRoleCount);

        for (ServerPlayerEntity player : neutralPlayers) {
            if(gwc.isRole(player, WatheRoles.CIVILIAN))
                gwc.addRole(player, SERoles.AMNESIAC);
        }
        // civilians
        List<ServerPlayerEntity> playersForCivilians = new ArrayList<>(civilians);
        playersForCivilians.removeIf(player -> !gwc.getRole(player).isInnocent());

        wyspiaexpress$assignCivilianRoles(world, gwc, playersForCivilians, civilianRoles, desiredRoleCount);
    }

    @Unique
    private List<ServerPlayerEntity> wyspiaexpress$pickPlayersForNeutral(ServerWorld world,
                                                                         GameWorldComponent gwc,
                                                                         List<ServerPlayerEntity> source,
                                                                         List<Role> trueNeutralRoles,
                                                                         List<Role> killerSidedNeutralRoles,
                                                                         int amount) {
        List<ServerPlayerEntity> picked = new ArrayList<>();
        if (amount <= 0 || source.isEmpty()) {
            return picked;
        }
        Set<UUID> pickedUuids = new HashSet<>();
        Set<Role> allNeutralRoles = new HashSet<>();
        allNeutralRoles.addAll(trueNeutralRoles);
        allNeutralRoles.addAll(killerSidedNeutralRoles);

        // 1) Forced neutral picks first
        for (Role role : allNeutralRoles) {
            List<UUID> forced = Harpymodloader.FORCED_MODDED_ROLE.get(role);
            if (forced == null) continue;

            for (UUID uuid : forced) {
                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                if (player == null) continue;
                if (!source.contains(player)) continue;
                if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())) continue;
                if (!Harpymodloader.OVERWRITE_ROLES.contains(gwc.getRole(player))) continue;

                if (pickedUuids.add(player.getUuid())) {
                    picked.add(player);
                    amount--;

                }
            }
        }
        if(amount > 0) {
            // 2) Weighted fill for remaining neutral slots
            Map<ServerPlayerEntity, Float> weights = new HashMap<>();
            float total = 0.0F;

            for (ServerPlayerEntity player : source) {
                if (pickedUuids.contains(player.getUuid())) continue;
                if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())) continue;
                if (!Harpymodloader.OVERWRITE_ROLES.contains(gwc.getRole(player))) continue;

                float weight = 0.0F;
                for (Role role : trueNeutralRoles) {
                    weight += wyspiaexpress$getPlayerWeightForRole(gwc, player, role);
                }
                for (Role role : killerSidedNeutralRoles) {
                    weight += wyspiaexpress$getPlayerWeightForRole(gwc, player, role);
                }

                if (weight > 0.0F) {
                    weights.put(player, weight);
                    total += weight;
                }
            }

            int rolls = Math.min(amount, weights.size());
            for (int i = 0; i < rolls && total > 0.0F; i++) {
                float random = world.getRandom().nextFloat() * total;

                Iterator<Map.Entry<ServerPlayerEntity, Float>> it = weights.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<ServerPlayerEntity, Float> entry = it.next();
                    random -= entry.getValue();
                    if (random <= 0.0F) {
                        ServerPlayerEntity player = entry.getKey();
                        picked.add(player);
                        pickedUuids.add(player.getUuid());
                        total -= entry.getValue();
                        it.remove();
                        break;
                    }
                }
            }
        }
        return picked;
    }
    @Unique
    private void wyspiaexpress$assignRoleGroupToPlayers(ServerWorld world,
                                                        GameWorldComponent gwc,
                                                        List<ServerPlayerEntity> players,
                                                        List<Role> roles,
                                                        int totalWanted,
                                                        int desiredRoleCount) {
        if (totalWanted <= 0 || players.isEmpty() || roles.isEmpty()) return;

        List<Role> shuffledRoles = new ArrayList<>(roles);
        Collections.shuffle(shuffledRoles);
        List<ServerPlayerEntity> assigning = new ArrayList<>(players);
        totalWanted = Math.min(totalWanted, players.size());
        int assigned = 0;
        for (Role role : shuffledRoles) {
            if (assigned >= totalWanted) break;

            int roleSpecificDesiredCount = desiredRoleCount;

            if (Harpymodloader.ROLE_MAX.containsKey(role.identifier())) {
                roleSpecificDesiredCount = Math.min(Harpymodloader.ROLE_MAX.get(role.identifier()), roleSpecificDesiredCount);
            }
            else{
                roleSpecificDesiredCount = Math.min(Math.round((float) players.size() / shuffledRoles.size()), roleSpecificDesiredCount);
            }
            roleSpecificDesiredCount = Math.min(roleSpecificDesiredCount, totalWanted - assigned);

            // inititate fix, always assign 2 of this role
            if(role.equals(SERoles.INITIATE)) {
                if(assigning.size() >= 2  && roleSpecificDesiredCount > 0)
                    roleSpecificDesiredCount = 2;
                else
                    continue;
            }

            int justAssigned = wyspiaexpress$findAndAssignPlayers(
                    roleSpecificDesiredCount, role, assigning, gwc, world
            );

            assigning.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gwc.getRole(player)));
            assigned += justAssigned;

        }
    }
    @Unique
    private void wyspiaexpress$assignCivilianRoles(ServerWorld world,
                                                   GameWorldComponent gwc,
                                                   List<ServerPlayerEntity> playersForCivilianRoles,
                                                   List<Role> civilianRoles,
                                                   int desiredRoleCount) {
        if (playersForCivilianRoles.isEmpty() || civilianRoles.isEmpty()) return;
        List<Role> shuffledCivilianRoles = new ArrayList<>(civilianRoles);
        Collections.shuffle(shuffledCivilianRoles);

        List<ServerPlayerEntity> assigning = new ArrayList<>(playersForCivilianRoles);
        for (Role role : shuffledCivilianRoles) {
            int roleSpecificDesiredCount = desiredRoleCount;

            if (Harpymodloader.ROLE_MAX.containsKey(role.identifier())) {
                roleSpecificDesiredCount = Math.min(Harpymodloader.ROLE_MAX.get(role.identifier()), desiredRoleCount);
            }
            else{
                roleSpecificDesiredCount = Math.min(Math.round((float) playersForCivilianRoles.size() / shuffledCivilianRoles.size()), roleSpecificDesiredCount);
            }
            if(roleSpecificDesiredCount > 0)
                roleSpecificDesiredCount = world.random.nextInt(roleSpecificDesiredCount) + 1;

            wyspiaexpress$findAndAssignPlayers(
                    roleSpecificDesiredCount, role, assigning, gwc, world
            );
            assigning.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gwc.getRole(player)));

        }
    }

    @Unique
    private int wyspiaexpress$findAndAssignPlayers(int desiredRoleCount,
                                                   Role role,
                                                   List<ServerPlayerEntity> players,
                                                   GameWorldComponent gameWorldComponent,
                                                   World world) {
        if(HAS_HACKER && !KinsWatheConfig.HANDLER.instance().HackerGenerateWithMimic && role.equals(Noellesroles.MIMIC)){
            return 0;
        }
        List<ServerPlayerEntity> assignedPlayers = new ArrayList<>();

        if (Harpymodloader.FORCED_MODDED_ROLE.containsKey(role)) {
            for (UUID uuid : Harpymodloader.FORCED_MODDED_ROLE.get(role)) {
                PlayerEntity player = world.getPlayerByUuid(uuid);
                if (player instanceof ServerPlayerEntity serverPlayer && players.contains(serverPlayer)) {
                    assignedPlayers.add(serverPlayer);
                    desiredRoleCount--;
                    ModdedWeights.roleRounds
                            .computeIfAbsent(role, r -> new HashMap<>())
                            .put(player.getUuid(),
                                    ModdedWeights.roleRounds.get(role).getOrDefault(player.getUuid(), 1) + 1);
                }
            }
        }
        Map<ServerPlayerEntity, Float> weightMap = new HashMap<>();
        float total = 0.0F;

        for (ServerPlayerEntity player : players) {
            if (!Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUuid())) {
                float weight = wyspiaexpress$getPlayerWeightForRole(gameWorldComponent, player, role);
                weightMap.put(player, weight);
                total += weight;
            }
        }

        int rolls = Math.min(desiredRoleCount, weightMap.size());
        for (int i = 0; i < rolls; i++) {
            float random = world.getRandom().nextFloat() * total;

            Iterator<Map.Entry<ServerPlayerEntity, Float>> it = weightMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ServerPlayerEntity, Float> entry = it.next();
                random -= entry.getValue();
                if (random <= 0.0F) {
                    ServerPlayerEntity picked = entry.getKey();
                    assignedPlayers.add(picked);
                    total -= entry.getValue();
                    it.remove();

                    ModdedWeights.roleRounds
                            .computeIfAbsent(role, r -> new HashMap<>())
                            .put(picked.getUuid(),
                                    ModdedWeights.roleRounds.get(role).getOrDefault(picked.getUuid(), 1) + 1);
                    break;
                }
            }
        }

        int assigned = assignedPlayers.size();
        for (ServerPlayerEntity player : assignedPlayers) {
            gameWorldComponent.addRole(player, role);
        }
        if(assigned > 0 && role.equals(KinsWatheRoles.HACKER)){
            HAS_HACKER = true;
        }
        return assigned;
    }
    @Unique
    private float wyspiaexpress$getPlayerWeightForRole(GameWorldComponent gwc,
                                                       ServerPlayerEntity player,
                                                       Role role) {
        if (!gwc.areWeightsEnabled()) {
            return 1.0F;
        }

        int rounds = ModdedWeights.roleRounds
                .computeIfAbsent(role, r -> new HashMap<>())
                .getOrDefault(player.getUuid(), 1);

        return (float) Math.exp(-(rounds * 4.0));
    }

    @Unique
    private void wyspiaexpress$assignModifiersAndAnnounce(ServerWorld serverWorld,
                                                          GameWorldComponent gwc,
                                                          List<ServerPlayerEntity> players,
                                                          int desiredRoleCount,
                                                          int killerCount) {

        WorldModifierComponent wmc = WorldModifierComponent.KEY.get(serverWorld);
        wmc.getModifiers().clear();

        // Pre-calculate the maximum allowed distribution for each modifier
        Map<Modifier, Integer> maxAllowedPerMod = new HashMap<>();
        Map<Modifier, Integer> currentlyAssigned = new HashMap<>();

        int killerMods = (int) HMLModifiers.MODIFIERS.stream().filter(m -> m.killerOnly).count();
        for (Modifier mod : HMLModifiers.MODIFIERS) {
            currentlyAssigned.put(mod, 0);
            int target = desiredRoleCount;
            if (mod.killerOnly && killerMods > 0) {
                target = Math.max(1,
                        (int) Math.floor(
                                (players.size() / (double) gwc.getKillerDividend()) / killerMods
                        )
                );
            }
            // Apply hard limits if they exist
            if (Harpymodloader.MODIFIER_MAX.containsKey(mod.identifier)) {
                target = Math.min(target, Harpymodloader.MODIFIER_MAX.get(mod.identifier));
            }
            maxAllowedPerMod.put(mod, target);
        }
        // BOMBER is special: handled manually later
        maxAllowedPerMod.put(WyspiaExpressRoles.BOMBER, 0);

        // Forced modifiers
        if (!Harpymodloader.FORCED_MODDED_MODIFIER.isEmpty()) {
            for (Modifier mod : HMLModifiers.MODIFIERS) {
                if (Harpymodloader.FORCED_MODDED_MODIFIER.containsKey(mod)) {
                    List<UUID> forcedUUIDs = Harpymodloader.FORCED_MODDED_MODIFIER.get(mod);
                    for (ServerPlayerEntity player : players) {
                        if (forcedUUIDs.contains(player.getUuid())) {
                            wmc.addModifier(player.getUuid(), mod);
                            ModifierAssigned.EVENT.invoker().assignModifier(player, mod);
                            currentlyAssigned.put(mod, currentlyAssigned.get(mod) + 1);
                        }
                    }
                }
            }
        }

        // Random assignment
        List<ServerPlayerEntity> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        List<Modifier> randomModsForPlayer = new ArrayList<>(
                HMLModifiers.MODIFIERS.stream()
                        .filter(mod -> !HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(mod.identifier.toString()))
                        .toList()
        );

        for (ServerPlayerEntity player : shuffledPlayers) {
            if (wyspiaexpress$getPlayerModCount(player, wmc) >= HarpyModLoaderConfig.HANDLER.instance().modifierMaximum) {
                continue;
            }
            Collections.shuffle(randomModsForPlayer);

            for (Modifier mod : randomModsForPlayer) {
                if (wyspiaexpress$getPlayerModCount(player, wmc) >= HarpyModLoaderConfig.HANDLER.instance().modifierMaximum) {
                    break;
                }
                if (currentlyAssigned.get(mod) >= maxAllowedPerMod.get(mod)) {
                    continue;
                }
                if (wmc.getModifiers(player) != null && wmc.getModifiers(player).contains(mod)) {
                    continue;
                }
                if (!wyspiaexpress$isModifierEligible(player, mod, gwc)) {
                    continue;
                }

                wmc.addModifier(player.getUuid(), mod);
                ModifierAssigned.EVENT.invoker().assignModifier(player, mod);
                currentlyAssigned.put(mod, currentlyAssigned.get(mod) + 1);
            }
        }

        // Killer guessers
        if (WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.killerAlwaysGuesser()) {
            int count = 0;
            for (ServerPlayerEntity player : shuffledPlayers) {
                if (!wmc.isModifier(player, Noellesroles.GUESSER) && gwc.canUseKillerFeatures(player)) {
                    wmc.addModifier(player.getUuid(), Noellesroles.GUESSER);
                    ModifierAssigned.EVENT.invoker().assignModifier(player, Noellesroles.GUESSER);
                    count++;
                }
                if (count >= WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.maximumGuessers()) {
                    break;
                }
            }
        }

        // Random civilian bomber
        if (WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.enabled()) {
            if (serverWorld.random.nextInt(WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.chance()) == 0) {
                List<ServerPlayerEntity> civilians = shuffledPlayers.stream()
                        .filter(gwc::isInnocent)
                        .toList();
                if (!civilians.isEmpty()) {
                    int i = serverWorld.random.nextInt(civilians.size());
                    ServerPlayerEntity chosen = civilians.get(i);
                    WyspiaExpress.LOGGER.info("[BOMBER] Player {} received BOMBER!", chosen.getName().getString());
                    wmc.addModifier(chosen.getUuid(), WyspiaExpressRoles.BOMBER);
                    ModifierAssigned.EVENT.invoker().assignModifier(chosen, WyspiaExpressRoles.BOMBER);
                }
            }
        }

        // Modifier announcements
        for (ServerPlayerEntity player : players) {
            if (wmc.getModifiers(player) != null && !wmc.getModifiers(player).isEmpty()) {
                MutableText modifiersText = Text.translatable("announcement.modifier").formatted(Formatting.GRAY)
                        .append(Texts.join(
                                wmc.getModifiers(player),
                                Text.literal(", "),
                                modifier -> modifier.getName(false).withColor(modifier.color)
                        ));
                player.sendMessage(modifiersText, true);
            } else if (!HMLModifiers.MODIFIERS.isEmpty()) {
                player.sendMessage(Text.translatable("announcement.no_modifiers").formatted(Formatting.DARK_GRAY), true);
            }
        }
    }

    @Unique
    private boolean wyspiaexpress$isModifierEligible(ServerPlayerEntity player,
                                                     Modifier mod,
                                                     GameWorldComponent gwc) {
        if (mod.canOnlyBeAppliedTo != null && gwc.getRole(player) != null &&
                !mod.canOnlyBeAppliedTo.contains(gwc.getRole(player))) {
            return false;
        }
        if (mod.cannotBeAppliedTo != null && gwc.getRole(player) != null &&
                mod.cannotBeAppliedTo.contains(gwc.getRole(player))) {
            return false;
        }

        if (mod.killerOnly && mod.civilianOnly) {
            return gwc.canUseKillerFeatures(player) || gwc.isInnocent(player);
        }
        if (mod.killerOnly && !gwc.canUseKillerFeatures(player)) return false;
        if (mod.civilianOnly && !gwc.isInnocent(player)) return false;
        return true;
    }

    @Unique
    private int wyspiaexpress$getPlayerModCount(ServerPlayerEntity player,
                                                WorldModifierComponent wmc) {
        return wmc.getModifiers(player) == null ? 0 : wmc.getModifiers(player).size();
    }

}
