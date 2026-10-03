package org.cat.express.wyspiaexpress.modifiers;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheParticles;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.GuesserComponent;

import java.util.List;
import java.util.UUID;

public final class GuesserAbility {

    private static final List<Role> EXCLUDED_TARGET_ROLES = List.of(
            WyspiaExpressRoles.LICH_GHOUL,
            WyspiaExpressRoles.CULTIST
    );

    public enum Result { IGNORED, FORBIDDEN, INVALID, CORRECT, WRONG }
    public record Guess(Role role, boolean forbidden) {}
    private GuesserAbility() {}

    public static boolean canTarget(Role role) {
        return role != null && !EXCLUDED_TARGET_ROLES.contains(role);
    }

    public static boolean civilian(Role role) {
        return role != null && role.isInnocent() && !role.canUseKiller()
                && !WyspiaExpressRoles.TRUE_NEUTRALS.contains(role)
                && !WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)
                && !Harpymodloader.SPECIAL_ROLES.contains(role)
                && !WyspiaExpressRoles.HIDDEN_ROLES.contains(role);
    }

    public static Guess resolve(String input) {
        String text = input == null ? "" : input.strip();
        List<Role> matches = WatheRoles.ROLES.stream().filter(role ->
                WyspiaExpressRoles.getRoleId(role).equalsIgnoreCase(text) || WyspiaExpressRoles.getRoleName(role).equalsIgnoreCase(text)).toList();
        if (matches.size() != 1) return new Guess(null, false);
        Role role = matches.getFirst();
        return new Guess(role, !civilian(role));
    }

    public static Result guess(ServerPlayerEntity player, UUID targetId, String input) {
        var world = player.getServerWorld();
        var game = GameWorldComponent.KEY.get(world);
        var cooldown = GuesserComponent.KEY.get(player);
        if (!game.isRunning() || !GameFunctions.isPlayerAliveAndSurvival(player)
                || game.getRole(player) == null || cooldown.cooldown() > 0
                || !WorldModifierComponent.KEY.get(world).isModifier(player, WyspiaExpressRoles.GUESSER)) return Result.IGNORED;
        var target = targetId == null ? null : world.getPlayerByUuid(targetId);
        if (target == player || !(target instanceof ServerPlayerEntity) || !GameFunctions.isPlayerAliveAndSurvival(target)
                || !canTarget(game.getRole(target))) return Result.IGNORED;
        long civilians = world.getPlayers().stream().filter(GameFunctions::isPlayerAliveAndSurvival)
                .filter(p -> civilian(game.getRole(p))).count();
        if (civilians < WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.minPlayer()) return Result.IGNORED;
        Guess guess = resolve(input);
        if (guess.forbidden() || guess.role() == null) {
            cooldown.setCooldown(WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.invalidGuessCooldown() * 20);
            player.sendMessage(Text.translatable("gui.wyspiaexpress.guesser.civilian_only"), true);
            return guess.forbidden() ? Result.FORBIDDEN : Result.INVALID;
        }
        cooldown.setCooldown(WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.cooldown() * 20);
        if (guess.role().equals(game.getRole(target))) {
            player.playSoundToPlayer(SoundEvents.ENTITY_PIG_DEATH, SoundCategory.PLAYERS, 1, 1);
            GameFunctions.killPlayer(target, true, player, Noellesroles.VOODOO_MAGIC_DEATH_REASON);
            return Result.CORRECT;
        }
        player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1, 1);
        String punishment = NoellesRolesConfig.HANDLER.instance().guesserDiesAfterIncorrectGuess;
        if (punishment.equalsIgnoreCase("death")) {
            GameFunctions.killPlayer(player, true, null, Noellesroles.VOODOO_MAGIC_DEATH_REASON);
        } else if (punishment.equalsIgnoreCase("explode")) {
            world.playSound(null, player.getBlockPos(), WatheSounds.ITEM_GRENADE_EXPLODE, SoundCategory.BLOCKS, 5, 1);
            world.spawnParticles(WatheParticles.BIG_EXPLOSION, player.getX(), player.getY() + .1, player.getZ(), 1, 0, 0, 0, 0);
            world.spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + .1, player.getZ(), 100, 0, 0, 0, .2);
            for (var victim : world.getPlayers(p -> p.getBoundingBox().intersects(player.getBoundingBox().expand(2)))) {
                GameFunctions.killPlayer(victim, true, player, GameConstants.DeathReasons.GRENADE);
            }
        }
        return Result.WRONG;
    }
}
