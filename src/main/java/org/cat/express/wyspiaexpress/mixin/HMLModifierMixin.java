package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.*;

@Mixin(ModdedMurderGameMode.class) // Replace with the actual class name
public abstract class HMLModifierMixin {
    @Inject(
            method = "assignModifiers",
            at = @At("HEAD"),
            cancellable = true
    )
    public void wyspiaexpress$onAssignModifiers(int desiredRoleCount, ServerWorld serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayerEntity> players, CallbackInfo ci){
        WyspiaExpressRoles.COPYCAT_ROLES.clear();
        for (ServerPlayerEntity player : players) {
            // before assigning modifiers, check if theres any civilians, give them amnesiac instead
            if (gameWorldComponent.isRole(player, WatheRoles.CIVILIAN)) {
                gameWorldComponent.addRole(player, SERoles.AMNESIAC);
                ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, SERoles.AMNESIAC);
            }
            else if(gameWorldComponent.canUseKillerFeatures(player) && WyspiaExpress.ROLES_CONFIG.enableRolePicking()) {
                for (int i = 1; i < PlayerInventory.getHotbarSize(); i++) {
                    if(!player.getInventory().getStack(i).isOf(KinsWatheItems.PHONE))
                        player.getInventory().setStack(i, ItemStack.EMPTY);
                }
                gameWorldComponent.addRole(player, WyspiaExpressRoles.COPYCAT);
                ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, WyspiaExpressRoles.COPYCAT);
            }
        }
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
        maxAllowedPerMod.put(WyspiaExpressRoles.BOMBER, 0);
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
        if(WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.killerAlwaysGuesser()) {
            int count = 0;
            for (ServerPlayerEntity player : shuffledPlayers) {
                if (!worldModifierComponent.isModifier(player, Noellesroles.GUESSER) && gameWorldComponent.canUseKillerFeatures(player)) {
                    worldModifierComponent.addModifier(player.getUuid(), Noellesroles.GUESSER);
                    ModifierAssigned.EVENT.invoker().assignModifier(player, Noellesroles.GUESSER);
                    count++;
                }
                if( count >= WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.maximumGuessers())
                    break;

            }
        }
        // Give a random civilian bomber
        if(WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.enabled()) {
            if(serverWorld.random.nextInt( WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.chance()) == 0 ){
                List<ServerPlayerEntity> civilians = shuffledPlayers.stream().filter(gameWorldComponent::isInnocent).toList();
                int i = serverWorld.random.nextInt(civilians.size());
                if(civilians.size() > i) {
                    WyspiaExpress.LOGGER.info("[BOMBER] Player {} received BOMBER!", civilians.get(i).getName().getString());
                    worldModifierComponent.addModifier(civilians.get(i).getUuid(), WyspiaExpressRoles.BOMBER);
                    ModifierAssigned.EVENT.invoker().assignModifier(civilians.get(i), WyspiaExpressRoles.BOMBER);
                }
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




        ci.cancel();
    }

    @Unique
    private boolean isEligible(ServerPlayerEntity player, Modifier mod, GameWorldComponent gwc) {
        if (mod.canOnlyBeAppliedTo != null && gwc.getRole(player) != null && !mod.canOnlyBeAppliedTo.contains(gwc.getRole(player))) return false;
        if (mod.cannotBeAppliedTo != null && gwc.getRole(player) != null && mod.cannotBeAppliedTo.contains(gwc.getRole(player))) return false;

        if(mod.killerOnly && mod.civilianOnly) return gwc.canUseKillerFeatures(player) || gwc.isInnocent(player);
        if (mod.killerOnly && !gwc.canUseKillerFeatures(player)) return false;
        if (mod.civilianOnly && !gwc.isInnocent(player)) return false;
        return true;
    }
    @Unique
    private int getPlayerModCount(ServerPlayerEntity player, WorldModifierComponent wmc) {
        return wmc.getModifiers(player) == null ? 0 : wmc.getModifiers(player).size();
    }
}