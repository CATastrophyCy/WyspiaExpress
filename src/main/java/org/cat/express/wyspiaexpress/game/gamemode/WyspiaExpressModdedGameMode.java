package org.cat.express.wyspiaexpress.game.gamemode;

import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;
import org.cat.express.wyspiaexpress.components.RoundResultComponent.Outcome;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.RoleCategoryStatisticsManager;
import org.cat.express.wyspiaexpress.RoleDraftStatisticsManager.Pool;
import org.cat.express.wyspiaexpress.config.GameConfigs;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.role.avaricious.AvariciousGoldHandler;
import pro.fazeclan.river.stupid_express.role.thief.ThiefItemTracker;

import java.util.List;
import java.util.ArrayList;

/** HML-style modded Murder with the pack's assignment, economy and victory patches owned here. */
public class WyspiaExpressModdedGameMode extends GameMode {
    public WyspiaExpressModdedGameMode(Identifier identifier) {
        this(identifier, 6);
    }

    protected WyspiaExpressModdedGameMode(Identifier identifier, int minimumPlayers) {
        super(identifier, 10, minimumPlayers);
    }

    public boolean isRoleAllowed(Role role) { return true; }

    @Override
    public void initializeGame(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> players) {
        RoundBuilder round = createLegacyRoundBuilder(world, gameWorld, players).prepareRound();
        int killerQuota = players.size() / GameConfigs.killerDividend(gameWorld);
        if (!WyspiaExpress.GAMEMODES_CONFIG.useCustomWeightedAssignment()) round.useNativeTeamSelection();
        int assignedKillerCount = round.assignBaseKillers(killerQuota);
        round.assignVigilantes(players.size() / GameConfigs.vigilanteDividend(gameWorld));
        if (WyspiaExpress.ROLES_CONFIG.enableRolePicking()) round.assignCopycatRoles();
        else round.assignKillerReplacements(role -> true);

        assignLegacyNeutrals(world, gameWorld, round, players.size() / WyspiaExpress.GAMEMODES_CONFIG.neutralDividend());
        round.assignCivilianReplacements(round.eligibleRoles(role -> role.isInnocent() && !role.canUseKiller()
                && !WyspiaExpressRoles.TRUE_NEUTRALS.contains(role) && !WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)))
                .assignAmnesiacFallback()
                .configureLichRevives(killerQuota);
        round.completeAssignments(assignedKillerCount);
    }

    // Legacy Wyspia keeps its category-frequency weighting and randomized neutral split.
    private RoundBuilder createLegacyRoundBuilder(ServerWorld world, GameWorldComponent gameWorld,
                                                List<ServerPlayerEntity> players) {
        return new RoundBuilder(world, gameWorld, players, (pool, player) -> {
            int dividend = switch (pool) {
                case KILLER -> GameConfigs.killerDividend(gameWorld);
                case VIGILANTE -> GameConfigs.vigilanteDividend(gameWorld);
                default -> WyspiaExpress.GAMEMODES_CONFIG.neutralDividend();
            };
            var category = switch (pool) {
                case KILLER -> RoleCategoryStatisticsManager.RoleCategory.KILLER;
                case VIGILANTE -> RoleCategoryStatisticsManager.RoleCategory.VIGILANTE;
                default -> RoleCategoryStatisticsManager.RoleCategory.NEUTRAL;
            };
            return RoleCategoryStatisticsManager.getInstance().computeCategoryWeight(dividend, player.getUuid(), category);
        });
    }

    private void assignLegacyNeutrals(ServerWorld world, GameWorldComponent gameWorld,
                                      RoundBuilder round, int neutralQuota) {
        List<Role> eligibleRoles = round.eligibleRoles(role -> true);
        List<Role> trueNeutralRoles = eligibleRoles.stream().filter(WyspiaExpressRoles.TRUE_NEUTRALS::contains).toList();
        List<Role> killerAlignedNeutralRoles = eligibleRoles.stream().filter(WyspiaExpressRoles.KILLER_SIDED_NEUTRALS::contains).toList();
        List<Role> neutralRolePool = new ArrayList<>(trueNeutralRoles);
        neutralRolePool.addAll(killerAlignedNeutralRoles);
        List<ServerPlayerEntity> candidates = round.unassignedCivilians();
        int forcedNeutralCount = round.assignForcedRoles(candidates, neutralRolePool);
        int remainingNeutralQuota = Math.max(0, neutralQuota - forcedNeutralCount);
        int alignedQuotaLimit = (remainingNeutralQuota + 1) / 2;
        int alignedQuota = alignedQuotaLimit == 0 ? 0 : world.getRandom().nextInt(alignedQuotaLimit + 1);
        List<ServerPlayerEntity> selectedNeutralPlayers = WyspiaExpress.GAMEMODES_CONFIG.useCustomWeightedAssignment()
                ? round.selectPlayersForNeutralPool(candidates, remainingNeutralQuota, Pool.KILLER_ALIGNED_NEUTRAL)
                : round.selectNeutralPlayersByRoleHistory(candidates, neutralRolePool, remainingNeutralQuota);
        round.assignNeutralRoleCopies(selectedNeutralPlayers, trueNeutralRoles, remainingNeutralQuota - alignedQuota);
        List<ServerPlayerEntity> alignedCandidates = new ArrayList<>(selectedNeutralPlayers);
        alignedCandidates.removeIf(player -> !org.agmas.harpymodloader.Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
        round.assignNeutralRoleCopies(alignedCandidates, killerAlignedNeutralRoles, alignedQuota);
        round.assignAmnesiacFallback(selectedNeutralPlayers);
    }

    @Override
    public void finalizeGame(ServerWorld world, GameWorldComponent gameWorld) {
        WorldModifierComponent.KEY.get(world).getModifiers().clear();
    }

    @Override
    public void tickServerGameLoop(ServerWorld world, GameWorldComponent gameWorld) {
        if (!gameWorld.isRunning()) return;
        int passiveIncome = GameConstants.PASSIVE_MONEY_TICKER.apply(world.getTime());
        payPassiveIncome(world, gameWorld, passiveIncome);

        // Timeout always awards passengers the win, before any team or neutral ending checks.
        GameFunctions.WinStatus result = GameTimeComponent.KEY.get(world).hasTime()
                ? GameFunctions.WinStatus.NONE : GameFunctions.WinStatus.TIME;
        if (result == GameFunctions.WinStatus.NONE) {
            boolean civilianAlive = world.getPlayers().stream()
                    .anyMatch(player -> gameWorld.isInnocent(player) && !GameFunctions.isPlayerEliminated(player));
            if (!civilianAlive) result = GameFunctions.WinStatus.KILLERS;
            if (result == GameFunctions.WinStatus.NONE && gameWorld.getAllKillerTeamPlayers().stream()
                    .noneMatch(uuid -> !GameFunctions.isPlayerEliminated(world.getPlayerByUuid(uuid)))) {
                result = GameFunctions.WinStatus.PASSENGERS;
            }

            List<ServerPlayerEntity> alive = world.getPlayers().stream()
                    .filter(GameFunctions::isPlayerAliveAndSurvival).toList();
            if (applyNeutralConditions(world, gameWorld, alive, result)) return;
        }
        if (result != GameFunctions.WinStatus.NONE && gameWorld.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
            GameRoundEndComponent.KEY.get(world).setRoundEndData(world.getPlayers(), result);
            GameFunctions.stopGame(world);
        }
        // These were TAIL patches. They still run through the stopping fade unless a neutral blocks the tick.
        payAdditionalIncome(world, gameWorld, passiveIncome);
    }

    private void payPassiveIncome(ServerWorld world, GameWorldComponent gameWorld, int amount) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            Role role = gameWorld.getRole(player);
            if (role == null) continue;
            // Noelle/Kin's outer income wrappers return early, bypassing Wyspia's alive/tape checks.
            boolean upstreamIncome = receivesUpstreamIncome(role);
            if (!upstreamIncome && !GameFunctions.isPlayerAliveAndSurvival(player)) continue;
            boolean silenced = SilenceComponent.KEY.get(player).isSilenced();
            if (!upstreamIncome && world.getTime() % GameConstants.getInTicks(0, 10) == 0 && silenced
                    && role.getMoodType() == Role.MoodType.REAL
                    && WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.enableMoodLost()) {
                PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
                mood.setMood(mood.getMood() - WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.moodLostAmount());
            }
            if (amount <= 0 || !receivesPassiveIncome(gameWorld, player, role)) continue;
            int bonus = 0;
            if ((role == StarryExpressRoles.MUZZLER || role == Noellesroles.MORPHLING)
                    && WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.muzzlerConfig.enablePayout()) {
                for (ServerPlayerEntity other : world.getServer().getPlayerManager().getPlayerList()) {
                    if (GameFunctions.isPlayerAliveAndSurvival(other) && gameWorld.isInnocent(other)
                            && SilenceComponent.KEY.get(other).isSilenced()) {
                        bonus += WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.muzzlerConfig.muzzledPayout();
                    }
                }
            }
            ShopUtil.addCoin(player, amount + bonus);
        }
    }

    private boolean receivesPassiveIncome(GameWorldComponent gameWorld, ServerPlayerEntity player, Role role) {
        if (role == SERoles.AVARICIOUS) return false;
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        return gameWorld.canUseKillerFeatures(player) || (config != null && config.passiveIncome())
                || receivesUpstreamIncome(role);
    }

    private boolean receivesUpstreamIncome(Role role) {
        return role == Noellesroles.NOISEMAKER || role == Noellesroles.MIMIC
                || role == Noellesroles.JESTER || role == Noellesroles.EXECUTIONER
                || role == KinsWatheRoles.COOK || role == KinsWatheRoles.DREAMER || role == KinsWatheRoles.JUDGE
                || (role == KinsWatheRoles.HACKER && KinsWatheConfig.HANDLER.instance().HackerHasShop);
    }

    private void payAdditionalIncome(ServerWorld world, GameWorldComponent gameWorld, int passiveIncome) {
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(world);
        for (ServerPlayerEntity player : world.getPlayers()) {
            // The original Magnate patch also pays eliminated holders.
            if (passiveIncome > 0 && modifiers.isModifier(player, KinsWatheRoles.MAGNATE)) {
                PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
                shop.balance += passiveIncome;
                shop.sync();
            }
        }
        long time = GameTimeComponent.KEY.get(world).time;
        if (AvariciousGoldHandler.gameStartTime == -1) {
            AvariciousGoldHandler.gameStartTime = time;
        } else if ((time - AvariciousGoldHandler.gameStartTime) % AvariciousGoldHandler.TIMER_TICKS == 0) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!gameWorld.isRole(player, SERoles.AVARICIOUS)) continue;
                int nearby = 0;
                for (ServerPlayerEntity other : world.getPlayers()) {
                    if (other != player && !GameFunctions.isPlayerEliminated(other)
                            && other.distanceTo(player) <= AvariciousGoldHandler.MAX_DISTANCE) nearby++;
                }
                if (nearby > 0) {
                    ShopUtil.addCoin(player, nearby * AvariciousGoldHandler.PAYOUT_PER_PLAYER);
                    player.playSoundToPlayer(WatheSounds.UI_SHOP_BUY, SoundCategory.PLAYERS, 10, 0.5f);
                }
            }
        }
        if (gameWorld.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE && time % 20 == 0) {
            int ticksRemaining = AvariciousGoldHandler.TIMER_TICKS - (int) (time % AvariciousGoldHandler.TIMER_TICKS);
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (gameWorld.isRole(player, SERoles.AVARICIOUS)) {
                    player.sendMessageToClient(Text.translatable("hud.stupid_express.avaricious.payout_timer", 60 - ticksRemaining / 20)
                            .formatted(Formatting.GOLD, Formatting.BOLD), true);
                }
            }
        }
    }

    private boolean applyNeutralConditions(ServerWorld world, GameWorldComponent gameWorld,
                                           List<ServerPlayerEntity> alive, GameFunctions.WinStatus result) {
        boolean kinVillain = false;
        boolean eddie = false;
        boolean wyspiaVillain = false;
        boolean cult = false;
        boolean onlyCult = !alive.isEmpty();
        boolean arsonist = false;
        boolean thief = false;
        for (ServerPlayerEntity player : alive) {
            Role role = gameWorld.getRole(player);
            kinVillain |= role == KinsWatheRoles.LICENSED_VILLAIN;
            eddie |= role == WyspiaExpressRoles.EDDIE_WAFFLES;
            wyspiaVillain |= role == WyspiaExpressRoles.VILLAIN_LICENSED;
            boolean cultMember = role == WyspiaExpressRoles.CULTIST || role == WyspiaExpressRoles.CULT_LEADER;
            cult |= cultMember;
            onlyCult &= cultMember;
            arsonist |= role == SERoles.ARSONIST && StupidExpress.CONFIG.rolesSection.arsonistSection.arsonistKeepsGameGoing;
            thief |= role == SERoles.THIEF;
        }
        // All custom claims use the same positions as the old keep-alive patches, before ordinary endings.
        if (alive.size() == 1) {
            Role role = gameWorld.getRole(alive.getFirst());
            if (kinVillain) finishCustom(world, gameWorld, alive, role, Outcome.LICENSED_VILLAIN);
            else if (eddie) finishCustom(world, gameWorld, alive, role, Outcome.EDDIE_WAFFLES);
            else if (wyspiaVillain) finishCustom(world, gameWorld, alive, role, Outcome.LICENSED_VILLAIN);
            else if (arsonist) finishCustom(world, gameWorld, alive, role, Outcome.ARSONIST);
            else if (thief) finishCustom(world, gameWorld, alive, role, Outcome.THIEF);
        }
        if (cult && onlyCult) finishCustom(world, gameWorld, alive, WyspiaExpressRoles.CULT_LEADER, Outcome.CULT);
        boolean teamEnding = result == GameFunctions.WinStatus.KILLERS || result == GameFunctions.WinStatus.PASSENGERS;
        return teamEnding && (kinVillain || eddie || wyspiaVillain || cult || arsonist
                || (thief && ThiefItemTracker.isWeaponAvailable()));
    }

    private void finishCustom(ServerWorld world, GameWorldComponent gameWorld, List<ServerPlayerEntity> winners,
                              Role role, Outcome outcome) {
        // An item or another rule may already have ended this round; keep its result stable during STOPPING.
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        org.BsXinQin.kinswathe.component.CustomWinnerComponent.KEY.get(world).reset();
        pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent.KEY.get(world).reset();
        RoundResultComponent.KEY.get(world).setVictory(outcome, role.color(),
                winners.stream().map(ServerPlayerEntity::getUuid).toList());
        GameRoundEndComponent.KEY.get(world).setRoundEndData(world.getPlayers(), GameFunctions.WinStatus.KILLERS);
        GameFunctions.stopGame(world);
    }
}
