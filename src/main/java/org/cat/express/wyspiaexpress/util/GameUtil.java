package org.cat.express.wyspiaexpress.util;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheEntities;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.PlayerBodyEntityComponent;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;
import org.cat.express.wyspiaexpress.components.PlayerDepressedComponent;
import org.cat.express.wyspiaexpress.components.PlayerFreezeComponent;
import org.cat.express.wyspiaexpress.components.PlayerHearDeadComponent;
import org.cat.express.wyspiaexpress.components.PlayerMovementComponent;
import org.cat.express.wyspiaexpress.components.PlayerSenseDeadComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.cat.express.wyspiaexpress.components.roles.LoversComponent;
import org.cat.express.wyspiaexpress.components.roles.PlayerCultistComponent;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import pro.fazeclan.river.stupid_express.role.thief.ThiefItemTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class GameUtil {
    private GameUtil() {}

    public static boolean canAddCivilianDeathTime(GameTimeComponent timer) {
        int limitMinutes = WyspiaExpress.RULES_CONFIG.maxTimeForDeathBonus();
        return limitMinutes < 0 || timer.getTime() <= GameConstants.getInTicks(limitMinutes, 0);
    }

    // kills a player with no killers, and bypasses protection
    public static void forceDeath(ServerPlayerEntity victim, boolean spawnBody, Identifier deathReason) {
        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) return;
        ServerWorld world = victim.getServerWorld();
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(victim);
        if (psycho.getPsychoTicks() > 0) psycho.stopPsycho();
        victim.changeGameMode(GameMode.SPECTATOR);
        PlayerMoodComponent.KEY.get(victim).reset();

        if (spawnBody) {
            PlayerBodyEntity body = WatheEntities.PLAYER_BODY.create(world);
            if (body != null) {
                body.setPlayerUuid(victim.getUuid());
                Vec3d pos = victim.getPos().add(victim.getRotationVector().normalize());
                body.refreshPositionAndAngles(pos.x, victim.getY(), pos.z, victim.getHeadYaw(), 0);
                body.setYaw(victim.getHeadYaw());
                body.setHeadYaw(victim.getHeadYaw());
                // Preserve the corpse data normally supplied by killPlayer mixins.
                Role role = game.getRole(victim);
                if (role != null) {
                    var coroner = org.agmas.noellesroles.coroner.BodyDeathReasonComponent.KEY.get(body);
                    coroner.deathReason = deathReason;
                    coroner.playerRole = role.identifier();
                    org.BsXinQin.kinswathe.component.BodyDeathReasonComponent.KEY.get(body).deathReason = deathReason;
                }
                if (PlayerCultistComponent.KEY.get(victim).isConverted()) {
                    PlayerBodyEntityComponent.KEY.get(body).setConverted(true);
                }
                world.spawnEntity(body);
            }
        }
        PlayerInventory inventory = victim.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (GameFunctions.shouldDropOnDeath(stack, victim)) {
                victim.dropItem(stack, true, false);
                inventory.setStack(slot, ItemStack.EMPTY);
            }
        }
        GameTimeComponent timer = GameTimeComponent.KEY.get(world);
        if (game.isInnocent(victim) && canAddCivilianDeathTime(timer)) {
            timer.addTime(GameConstants.TIME_ON_CIVILIAN_KILL);
        }
        TrainVoicePlugin.addPlayer(victim.getUuid());
        PlayerPoisonComponent.KEY.get(victim).reset();
        SilenceComponent.KEY.get(victim).reset();
        victim.removeStatusEffect(StatusEffects.NIGHT_VISION);
        victim.removeStatusEffect(StatusEffects.SLOWNESS);
        victim.removeStatusEffect(StatusEffects.BLINDNESS);
        PlayerHearDeadComponent.KEY.get(victim).reset();
        PlayerSenseDeadComponent.KEY.get(victim).reset();
        PlayerMovementComponent.KEY.get(victim).reset();
        PlayerEffectComponent.KEY.get(victim).reset();
        confirmExecutionerTargetDeath(victim, null);
        onPlayerDeath(victim, null, deathReason);
        ThiefItemTracker.onKillPlayer(victim);
    }

    public static void onPlayerDeath(PlayerEntity victim, @Nullable PlayerEntity killer, Identifier deathReason) {
        var world = victim.getWorld();

        if(world instanceof ServerWorld serverWorld) {
            var component = GameWorldComponent.KEY.get(serverWorld);
            if (component.canUseKillerFeatures(victim)) {
                var reviveComponent = LichReviveComponent.KEY.get(serverWorld);
                // count killers that are still alive
                if (serverWorld.getPlayers().stream().filter(component::canUseKillerFeatures).filter(GameFunctions::isPlayerAliveAndSurvival).count()
                        + reviveComponent.getAvailableRevives() < reviveComponent.getMaxRevives())
                    reviveComponent.incrementAvailableRevives();
            }
            var world_component = WorldComponent.KEY.get(serverWorld);
            world_component.addPlayerDead(victim.getUuid());

            GameUtil.sendPlayerDeathMessage(serverWorld, component, victim, killer, deathReason);

            LoversComponent.KEY.get(serverWorld).onDeath(victim);

            if (killer == null) return;
            // remove phantom invisibility depending on the config
            if (component.isRole(killer, Noellesroles.PHANTOM) && WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.phantomConfig.loseInvisibilityWhenKill()) {
                if (deathReason != null) {
                    if (!deathReason.equals(GameConstants.DeathReasons.POISON)) {
                        killer.removeStatusEffect(StatusEffects.INVISIBILITY);
                    }
                }

            }
         /*
            FIX: fix neutral roles get punished for killing players with KinsWathe, this happens because kinswathe only checks if the player is not innocent to give them coin
                , but with IncreaseMoneyWhenKill <= 100 it ends up punishing them. Either way the code doesn't give them correct reward
         */
            if (!component.isInnocent(killer) && !component.canUseKillerFeatures(killer) && KinsWatheConfig.HANDLER.instance().EnableWatheModify) {
                ShopUtil.addCoin(killer, -(KinsWatheConfig.HANDLER.instance().IncreaseMoneyWhenKill - 100));
            }
            victim.removeStatusEffect(StatusEffects.NIGHT_VISION);
            victim.removeStatusEffect(StatusEffects.SLOWNESS);
            victim.removeStatusEffect(StatusEffects.BLINDNESS);

            PlayerHearDeadComponent.KEY.get(victim).reset();
            PlayerSenseDeadComponent.KEY.get(victim).reset();
            PlayerMovementComponent.KEY.get(victim).reset();
            PlayerEffectComponent.KEY.get(victim).reset();

        }
    }

    public static void confirmExecutionerTargetDeath(PlayerEntity victim, @Nullable PlayerEntity killer) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.getWorld());
        for (UUID uuid : gameWorldComponent.getAllWithRole(Noellesroles.EXECUTIONER)) {
            PlayerEntity executioner = victim.getWorld().getPlayerByUuid(uuid);
            if (executioner == null) continue;
            boolean invalidKill = false;
            if (killer != null) {
                if (gameWorldComponent.getRole(killer).canUseKiller()) invalidKill = true;
            }
            ExecutionerPlayerComponent executionerPlayerComponent = ExecutionerPlayerComponent.KEY.get(executioner);
            if (executionerPlayerComponent.target.equals(victim.getUuid()) && !invalidKill) {
                executionerPlayerComponent.won = true;
                ArrayList<Role> shuffledKillerRoles;
                if(WyspiaExpress.ROLES_CONFIG.enableRolePicking()) {
                    shuffledKillerRoles = new ArrayList<>(List.of(WyspiaExpressRoles.COPYCAT));
                }else {
                    shuffledKillerRoles = RoleUtil.getKillerRoles(gameWorldComponent);
                    if (shuffledKillerRoles.isEmpty()) shuffledKillerRoles.add(WatheRoles.KILLER);
                    Collections.shuffle(shuffledKillerRoles);
                }
                gameWorldComponent.addRole(executioner,shuffledKillerRoles.getFirst());
                ModdedRoleAssigned.EVENT.invoker().assignModdedRole(executioner,shuffledKillerRoles.getFirst());
                GameUtil.sendRoleChangedMessage(((ServerPlayerEntity) executioner).getServerWorld(),
                        executioner, Noellesroles.EXECUTIONER, shuffledKillerRoles.getFirst());
                ShopUtil.addCoin(executioner, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.executionerConfig.reward());
                PlayerPoisonComponent.KEY.get(executioner).reset();
                if (Harpymodloader.VANNILA_ROLES.contains(gameWorldComponent.getRole(executioner))) {
                    ServerPlayNetworking.send((ServerPlayerEntity) executioner, new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(WatheRoles.KILLER), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));
                } else {
                    GameUtil.sendRoleAnnouncement((ServerPlayerEntity) executioner, gameWorldComponent.getRole(executioner), gameWorldComponent);
                }

            }
        }
    }

    public static void revivePlayer(@NotNull GameWorldComponent gameWorldComponent, @NotNull WorldComponent worldComponent,
                                    @NotNull ServerPlayerEntity player, @NotNull Role role, @NotNull List<Item> keptItems) {

        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
            Item item = inv.getStack(i).getItem();
            if (!keptItems.contains(item))
                player.getInventory().setStack(i, ItemStack.EMPTY);
        }

        player.changeGameMode(GameMode.ADVENTURE);
        worldComponent.removePlayerDead(player.getUuid());
        // sound effect to revived player
        player.playSoundToPlayer(SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.7f, 1.0f);
        // stun the revived player
        PlayerEffectComponent.KEY.get(player).setStunTicks(WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.stun());
        // reset components
        PlayerCultistComponent.KEY.get(player).reset();
        PlayerFreezeComponent.KEY.get(player).reset();
        PlayerMoodComponent.KEY.get(player).reset();
        PlayerDepressedComponent.KEY.get(player).reset();
        WorldModifierComponent.KEY.get(player.getWorld()).getModifiers(player).remove(WyspiaExpressRoles.GUESSER);
        gameWorldComponent.addRole(player, role);
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, role);
        sendRoleAnnouncement(player, role, gameWorldComponent);
        TrainVoicePlugin.resetPlayer(player.getUuid());
    }

    public static void convertToCultist(@Nullable ServerPlayerEntity converter, ServerPlayerEntity target) {
        ServerWorld world = target.getServerWorld();
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        LoversComponent lovers = LoversComponent.KEY.get(world);
        UUID partnerId = lovers.endBond(target.getUuid());

        if (game.isInnocent(target) && canAddCivilianDeathTime(GameTimeComponent.KEY.get(world))) {
            GameTimeComponent.KEY.get(world).addTime(GameConstants.TIME_ON_CIVILIAN_KILL);
        }
        if (game.canUseKillerFeatures(target)) {
            var revives = LichReviveComponent.KEY.get(world);
            if (world.getPlayers().stream().filter(game::canUseKillerFeatures)
                    .filter(GameFunctions::isPlayerAliveAndSurvival).count()
                    + revives.getAvailableRevives() - 1 < revives.getMaxRevives()) {
                revives.incrementAvailableRevives();
            }
        }
        WorldModifierComponent.KEY.get(world).getModifiers(target).remove(WyspiaExpressRoles.GUESSER);
        TrainVoicePlugin.resetPlayer(target.getUuid());
        if (converter != null) sendConvertedMessage(world, game, converter, target);
        game.addRole(target, WyspiaExpressRoles.CULTIST);
        PlayerCultistComponent.KEY.get(target).reset();
        ShopUtil.setCoin(target, WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.startingCoin());
        sendRoleAnnouncement(target, WyspiaExpressRoles.CULTIST, game);
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(target, WyspiaExpressRoles.CULTIST);

        // Removing the bond first prevents the partner's conversion from coming back to this player.
        if (partnerId != null) {
            ServerPlayerEntity partner = world.getServer().getPlayerManager().getPlayer(partnerId);
            if (partner == null) lovers.queueConversion(partnerId);
            else convertToCultist(converter, partner);
        }
    }

    public static void sendRoleAnnouncement(ServerPlayerEntity player, Role role, GameWorldComponent gameWorld) {
        ServerPlayNetworking.send(player, new AnnounceWelcomePayload(
                RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(Harpymodloader.autogeneratedAnnouncements.get(role)),
                gameWorld.getAllKillerTeamPlayers().size(), 0));
    }

    public static Text getRoleAnnouncement(Role role) {
        if (role == null) return Text.literal("Unknown role");
        return Text.translatable(role.identifier().toTranslationKey("announcement.role")).setStyle(Style.EMPTY.withItalic(true).withColor(role.color()));
    }

    public static String formatRoundTime(long ticks) {
        long seconds = Math.max(0, ticks) / 20;
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    public static MutableText withRoundTime(Text message) {
        if (WyspiaExpress.ROUND_TICKS < 0) return message.copy();
        return Text.empty()
                .append(Text.literal("[" + formatRoundTime(WyspiaExpress.ROUND_TICKS) + "] ").formatted(Formatting.GRAY))
                .append(message);
    }

    public static void sendPlayerDeathMessage(@NotNull ServerWorld world, @NotNull GameWorldComponent gameWorldComponent,
                                              @NotNull PlayerEntity victim, @Nullable PlayerEntity killer, @Nullable Identifier deathReason) {

        Text victimName = ((MutableText) Objects.requireNonNull(victim.getDisplayName())).setStyle(Style.EMPTY.withItalic(true));
        Text killerName = killer != null ? ((MutableText) Objects.requireNonNull(killer.getDisplayName())).setStyle(Style.EMPTY.withItalic(true)) : null;
        Text victimRole = getRoleAnnouncement(gameWorldComponent.getRole(victim));
        Text reason = deathReason != null ? getDeathReason(deathReason) : Text.literal("Unknown reason");
        // Text.literal(RoleUtil.getRoleName(gameWorldComponent.getRole(victim))).setStyle(Style.EMPTY.withItalic(true));
        Text killerRole = killer != null ? getRoleAnnouncement(gameWorldComponent.getRole(killer)) : null;
        MutableText message = Text.literal("You have fallen to ").append(reason);
        if (killerName != null)
            message.append(" by ").append(killerName).append(" [").append(killerRole).append("]");
        message.setStyle(Style.EMPTY.withColor(Formatting.RED)).append("\n");

        if (gameHasAliveRole(world, gameWorldComponent, WyspiaExpressRoles.EDDIE_WAFFLES)) {
            message.append(Text.literal("Eddie Waffles awaits your guidance").setStyle(Style.EMPTY.withColor(Formatting.WHITE))).
                    append(
                            Text.literal("[click here to join him]\n")
                                    .setStyle(Style.EMPTY
                                            .withColor(Formatting.AQUA)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sv join 5"))
                                            .withItalic(true)
                                    )
                    );
        }
        if (gameHasAliveRole(world, gameWorldComponent, WyspiaExpressRoles.LICH) && !gameWorldComponent.isRole(victim, WyspiaExpressRoles.LICH_GHOUL)) {
            message.append(Text.literal("There is still a Lich that could revive you\n").setStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN)));
        }
        if (gameHasAliveRole(world, gameWorldComponent, WyspiaExpressRoles.CULT_LEADER) && PlayerCultistComponent.KEY.get(victim).isConverted()) {
            message.append(Text.literal("You are fortunate enough to be converted by a Cult Leader\n").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)));
        }
        victim.sendMessage(withRoundTime(message), false);

        MutableText deathMessage = Text.literal("").append(victimName)
            .append(" [").append(victimRole).append("] has died to ").append(reason);
        if (killerName != null)
            deathMessage.append(" by ").append(killerName).append(" [").append(killerRole).append("]");
        deathMessage.append("\n").setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        sendMessageDeadPlayers(world, deathMessage, victim);
    }

    public static void sendRevivedMessage(@NotNull ServerWorld world, @NotNull GameWorldComponent gameWorldComponent,
                                          @NotNull PlayerEntity player, @NotNull PlayerEntity revived) {
        Text revivedName = ((MutableText) Objects.requireNonNull(revived.getDisplayName())).setStyle(Style.EMPTY.withItalic(true));
        Text playerName = ((MutableText) Objects.requireNonNull(player.getDisplayName())).setStyle(Style.EMPTY.withItalic(true));
        Text playerRole = getRoleAnnouncement(gameWorldComponent.getRole(player));

        Text revivedMessage = Text.literal("").append(revivedName).append(" has been revived by ")
                .append(playerName).append(" [").append(playerRole).append("]");
        sendMessageDeadPlayers(world, revivedMessage);
    }

    public static void sendConvertedMessage(@NotNull ServerWorld world, @NotNull GameWorldComponent gameWorldComponent,
                                     @NotNull PlayerEntity player, @NotNull PlayerEntity converted) {
        Text convertedName = ((MutableText) Objects.requireNonNull(converted.getDisplayName())).setStyle(Style.EMPTY.withItalic(true));
        Text convertedRole = getRoleAnnouncement(gameWorldComponent.getRole(converted));
        Text playerName = ((MutableText) Objects.requireNonNull(player.getDisplayName())).setStyle(Style.EMPTY.withItalic(true));

        Text convertedMessage = Text.literal("").append(convertedName).append(" [").append(convertedRole).append("]")
                .append(" has been forcefully converted by ")
                .append(playerName).append("");
        sendMessageDeadPlayers(world, convertedMessage);
    }

    public static void sendRoleChangedMessage(@NotNull ServerWorld world, @NotNull PlayerEntity player,
                                             @NotNull Role previousRole, @NotNull Role role) {
        Text message = Text.empty().append(player.getDisplayName().copy().formatted(Formatting.ITALIC))
                .append(" [").append(getRoleAnnouncement(previousRole)).append("]")
                .append(previousRole == WyspiaExpressRoles.COPYCAT ? " selected " : " has become ")
                .append(getRoleAnnouncement(role));
        sendMessageDeadPlayers(world, message);
    }

    public static boolean gameHasAliveRole(@NotNull ServerWorld world,@NotNull GameWorldComponent component,@NotNull Role role) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (GameFunctions.isPlayerSpectatingOrCreative(player))
                continue;
            if (component.isRole(player, role))
                return true;
        }
        return false;
    }

    public static MutableText getDeathReason(@NotNull Identifier reason) {
        return Text.translatable(reason.toTranslationKey("death_reason")).setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.RED));
    }

    public static boolean gameHasAliveRole(@NotNull ServerWorld world,@NotNull GameWorldComponent component, @NotNull List<Role> roles) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (GameFunctions.isPlayerSpectatingOrCreative(player))
                continue;
            Role playerRole = component.getRole(player);
            if (roles.contains(playerRole))
                return true;
        }
        return false;
    }

    public static void sendMessageDeadPlayers(@NotNull ServerWorld world, @NotNull Text message) {
        sendMessageDeadPlayers(world, message, (PlayerEntity) null);
    }

    public static void sendMessageDeadPlayers(@NotNull ServerWorld world, @NotNull Text message, @Nullable PlayerEntity excluded) {
        Text timedMessage = withRoundTime(message);
        for (ServerPlayerEntity player: world.getPlayers()) {
            if (player == excluded || GameFunctions.isPlayerAliveAndSurvival(player)) {
                continue;
            }
            player.sendMessage(timedMessage, false);
        }
    }

    public static void sendMessageDeadPlayers(@NotNull ServerWorld world, @NotNull Text message, @NotNull List<PlayerEntity> excluded) {
        Text timedMessage = withRoundTime(message);
        for (ServerPlayerEntity player: world.getPlayers()) {
            if (excluded.contains(player) || GameFunctions.isPlayerAliveAndSurvival(player)) {
                continue;
            }
            player.sendMessage(timedMessage, false);
        }
    }

    public static boolean isUnderSmokeEffect(@Nullable Entity entity) {
        return entity instanceof PlayerEntity player && PlayerMovementComponent.KEY.get(player).isRestricted();
    }

    public static boolean isObscuredBySmoke(@Nullable PlayerEntity viewer, @Nullable Entity target) {
        return isUnderSmokeEffect(viewer) || isUnderSmokeEffect(target);
    }

    public static boolean isWithinCylinder(Entity entity, Vec3d origin, double radius, double verticalRange) {
        double dx = entity.getX() - origin.x;
        double dz = entity.getZ() - origin.z;
        return Math.abs(entity.getY() - origin.y) <= verticalRange
                && dx * dx + dz * dz <= radius * radius;
    }

    public static long countAutoJoinPlayersOutsideReadyArea(World world) {
        Box readyArea = MapVariablesWorldComponent.KEY.get(world).getReadyArea();
        return world.getPlayers().stream().filter(player -> PlayerConfigComponent.KEY.get(player).autoJoin()
                && (readyArea == null || !readyArea.contains(player.getPos()))).count();
    }

    public static void teleportToReadyArea(ServerPlayerEntity player) {
        var target = WyspiaExpress.SERVER_CONFIG.readyTrainTp;
        player.teleportTo(new TeleportTarget(player.getServerWorld(),
                new Vec3d(target.x(), target.y(), target.z()), Vec3d.ZERO,
                target.yaw(), target.pitch(), TeleportTarget.NO_OP));
    }
}
