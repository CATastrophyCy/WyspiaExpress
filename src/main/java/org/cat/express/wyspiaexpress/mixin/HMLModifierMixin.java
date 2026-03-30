package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import java.util.*;

@Mixin(ModdedMurderGameMode.class) // Replace with the actual class name
public abstract class HMLModifierMixin {
    @Redirect(
            method = "initializeGame",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/agmas/harpymodloader/modded_murder/ModdedMurderGameMode;" +
                            "assignModifiers(ILnet/minecraft/server/world/ServerWorld;" +
                            "Ldev/doctor4t/wathe/cca/GameWorldComponent;Ljava/util/List;)V"
            )
    )
    public void wyspiaexpress$onAssignModifiers(ModdedMurderGameMode instance, int desiredRoleCount, ServerWorld serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayerEntity> players) {
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(serverWorld);
        worldModifierComponent.getModifiers().clear();

        // Pre-calculate the maximum allowed distribution for each modifier
        Map<Modifier, Integer> maxAllowedPerMod = new HashMap<>();
        Map<Modifier, Integer> currentlyAssigned = new HashMap<>();
        int killerMods = (int) HMLModifiers.MODIFIERS.stream().filter(m -> m.killerOnly).count();
        for (Modifier mod : HMLModifiers.MODIFIERS) {
            currentlyAssigned.put(mod, 0);
            int target = desiredRoleCount;
            if (mod.killerOnly && killerMods > 0) {
                target = Math.max(1, (int) Math.floor(players.size() / (double) gameWorldComponent.getKillerDividend() / killerMods));
            }
            // Apply hard limits if they exist
            if (Harpymodloader.MODIFIER_MAX.containsKey(mod.identifier)) {
                target = Math.min(target, Harpymodloader.MODIFIER_MAX.get(mod.identifier));
            }
            maxAllowedPerMod.put(mod, target);
        }

        // Handle forced modifiers
        if (!Harpymodloader.FORCED_MODDED_MODIFIER.isEmpty()) {
            for (Modifier mod : HMLModifiers.MODIFIERS) {
                if (Harpymodloader.FORCED_MODDED_MODIFIER.containsKey(mod)) {
                    List<UUID> forcedUUIDs = Harpymodloader.FORCED_MODDED_MODIFIER.get(mod);
                    for (ServerPlayerEntity player : players) {
                        if (forcedUUIDs.contains(player.getUuid())) {
                            worldModifierComponent.addModifier(player.getUuid(), mod);
                            ModifierAssigned.EVENT.invoker().assignModifier(player, mod);
                            currentlyAssigned.put(mod, currentlyAssigned.get(mod) + 1);
                        }
                    }
                }
            }
        }

        // Random Assignment
        List<ServerPlayerEntity> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers); // Randomize who gets to "draw" first
        Random rand = new Random();

        List<Modifier> randomModsForPlayer = new ArrayList<>(HMLModifiers.MODIFIERS.stream()
                .filter(mod -> !HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(mod.identifier.toString()))
                .toList());

        for (ServerPlayerEntity player : shuffledPlayers) {
            // Check if player is already full from forced modifiers
            if (getPlayerModCount(player, worldModifierComponent) >= HarpyModLoaderConfig.HANDLER.instance().modifierMaximum) {
                continue;
            }
            // Shuffle ALL modifiers so this specific player gets a random draw order
            Collections.shuffle(randomModsForPlayer, rand);

            for (Modifier mod : randomModsForPlayer) {
                // Stop trying modifiers if the player hit their personal limit
                if (getPlayerModCount(player, worldModifierComponent) >= HarpyModLoaderConfig.HANDLER.instance().modifierMaximum) {
                    break;
                }
                // Skip disabled modifiers
                if (HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(mod.identifier.toString())) {
                    continue;
                }
                // Skip if this modifier has already been handed out the maximum amount of times globally
                if (currentlyAssigned.get(mod) >= maxAllowedPerMod.get(mod)) {
                    continue;
                }
                // Skip if the player already has THIS specific modifier
                if (worldModifierComponent.getModifiers(player) != null && worldModifierComponent.getModifiers(player).contains(mod)) {
                    continue;
                }
                // Check role, killer, and civilian eligibility. Skip if it cannot be applied.
                if (!isEligible(player, mod, gameWorldComponent)) {
                    continue;
                }
                // All checks passed! The player successfully rolled this modifier.
                worldModifierComponent.addModifier(player.getUuid(), mod);
                ModifierAssigned.EVENT.invoker().assignModifier(player, mod);
                currentlyAssigned.put(mod, currentlyAssigned.get(mod) + 1);
            }
        }
        // Give killer guesser
        if(WyspiaExpress.SERVER_CONFIG.killerAlwaysGuesser()) {
            int count = 0;
            for (ServerPlayerEntity player : shuffledPlayers) {
                if (!worldModifierComponent.isModifier(player, Noellesroles.GUESSER) && gameWorldComponent.canUseKillerFeatures(player)) {
                    worldModifierComponent.addModifier(player.getUuid(), Noellesroles.GUESSER);
                    count++;
                }
                if( count >= WyspiaExpress.SERVER_CONFIG.maximumGuessers())
                    break;

            }
        }
        // Announcements
        for (ServerPlayerEntity player : players) {
            if (worldModifierComponent.getModifiers(player) != null && !worldModifierComponent.getModifiers(player).isEmpty()) {
                MutableText modifiersText = Text.translatable("announcement.modifier").formatted(Formatting.GRAY)
                        .append(Texts.join(worldModifierComponent.getModifiers(player), Text.literal(", "), modifier -> modifier.getName(false).
                                withColor(modifier.color)));
                player.sendMessage(modifiersText, true);
            } else {
                if (!HMLModifiers.MODIFIERS.isEmpty()) {
                    player.sendMessage(Text.translatable("announcement.no_modifiers").formatted(Formatting.DARK_GRAY), true);
                }
            }
        }
    }

    @Unique
    private boolean isEligible(ServerPlayerEntity player, Modifier mod, GameWorldComponent gwc) {
        if (mod.canOnlyBeAppliedTo != null && gwc.getRole(player) != null && !mod.canOnlyBeAppliedTo.contains(gwc.getRole(player))) return false;
        if (mod.cannotBeAppliedTo != null && gwc.getRole(player) != null && mod.cannotBeAppliedTo.contains(gwc.getRole(player))) return false;
        if (mod.killerOnly && !gwc.canUseKillerFeatures(player)) return false;
        if (mod.civilianOnly && gwc.canUseKillerFeatures(player)) return false;
        return true;
    }
    @Unique
    private int getPlayerModCount(ServerPlayerEntity player, WorldModifierComponent wmc) {
        return wmc.getModifiers(player) == null ? 0 : wmc.getModifiers(player).size();
    }
}