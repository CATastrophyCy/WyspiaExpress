package org.cat.express.wyspiaexpress.client.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.roles.TargetAbilityUtil;
import org.cat.express.wyspiaexpress.components.PlayerHearDeadComponent;
import org.cat.express.wyspiaexpress.components.PlayerSenseDeadComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.UUID;

@Mixin(value = WatheClient.class, priority =  100)
public abstract class InstinctMixin {
    // apparently they replaced fake poison's poisoner to this uuid, and in PlayerPoisonComponent they used a mixin to guarantee that delusion_maker won't kill
    @Unique private static final UUID DELUSION_MARKER = UUID.fromString("00000000-0000-0000-dead-c0de00000000"); // unique string used by Kinswathe

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void isInstinctEnabled(@NotNull CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance().player == null) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());

        if(WatheClient.isPlayerSpectatingOrCreative()) return;

        if (gameWorld.isRole(MinecraftClient.getInstance().player, KinsWatheRoles.DREAMER)
                && !WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.dreamerConfig.enableInstinct()) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }
        if (gameWorld.isRole(MinecraftClient.getInstance().player, KinsWatheRoles.HACKER)
                && !WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.hackerConfig.enableInstinct()) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }
        if (gameWorld.isRole(MinecraftClient.getInstance().player, WyspiaExpressRoles.VILLAIN_LICENSED) && WatheClient.instinctKeybind.isPressed()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(target.getWorld());
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(target.getWorld());

        // instinct to players
        if (target instanceof PlayerEntity targetPlayer) {
            // handle spectator instinct
            if (GameFunctions.isPlayerSpectatingOrCreative(player)
                    && (GameFunctions.isPlayerAliveAndSurvival(targetPlayer) || targetPlayer.equals(player))
                    && WatheClient.isInstinctEnabled()
                    && WyspiaExpress.SERVER_CONFIG.spectatorSpecialInstinct()) {
                int color = handleSpectatorInstinct(targetPlayer, gameWorldComponent);
                if (color != -1) {
                    cir.setReturnValue(color);
                    cir.cancel();
                    return;
                }
            }

            // target has to be alive
            if(!GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) return;

            if(GameFunctions.isPlayerAliveAndSurvival(player)) {

                // handle invisible players
                if (targetPlayer.isInvisible()) {
                    // Phantom allow killer buddies to see them
                    if (!gameWorldComponent.isRole(targetPlayer, Noellesroles.PHANTOM) || !WatheClient.isKiller()) {
                        cir.setReturnValue(-1);
                        cir.cancel();
                        return;
                    }
                }
                // if the player is elusive then hide them
                if (worldModifierComponent.isModifier(targetPlayer, WyspiaExpressRoles.ELUSIVE)) {
                    double distance = player.squaredDistanceTo(targetPlayer);
                    if (distance >= WyspiaExpress.MODIFIERS_CONFIG.elusiveConfig.minimumDistance() * WyspiaExpress.MODIFIERS_CONFIG.elusiveConfig.minimumDistance()
                            && distance <= WyspiaExpress.MODIFIERS_CONFIG.elusiveConfig.maximumDistance() * WyspiaExpress.MODIFIERS_CONFIG.elusiveConfig.maximumDistance()) {
                        cir.setReturnValue(-1);
                        cir.cancel();
                        return;
                    }
                }
                if (gameWorldComponent.isRole(player, Noellesroles.BARTENDER)) {
                    cir.setReturnValue(handleBartender(player, targetPlayer));
                    cir.cancel();
                    return;
                }
                // handle player instinct with instinct pressed, doesn't handle passive instinct
                if (WatheClient.isInstinctEnabled()) {

                    Role role = gameWorldComponent.getRole(targetPlayer);
                    if (role != null) {
                        // If the current player is killer and is alive
                        if (WatheClient.isKiller()) {
                            if (WyspiaExpressRoles.TRUE_NEUTRALS.contains(role)) {
                                cir.setReturnValue(0x4EDD35);
                                cir.cancel();
                                return;
                            } else if (WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) {
                                if (WyspiaExpress.SERVER_CONFIG.killerSpecialInstinct()) {
                                    // role specific color instinct
                                    cir.setReturnValue(role.color());
                                    cir.cancel();
                                    return;
                                }
                                // generic killer red
                                cir.setReturnValue(MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F));
                                cir.cancel();
                                return;
                            }
                        }
                        if(gameWorldComponent.isRole(player, KinsWatheRoles.HACKER)){
                            if (WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) {
                                if (WyspiaExpress.SERVER_CONFIG.killerSpecialInstinct()) {
                                    // role specific color instinct
                                    cir.setReturnValue(role.color());
                                    cir.cancel();
                                    return;
                                }
                                // generic killer red
                                cir.setReturnValue(MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F));
                                cir.cancel();
                                return;
                            }
                        }
                    }
                }
            }
        }
        // instinct to player bodies
        if(target instanceof PlayerBodyEntity targetBody){
            PlayerSenseDeadComponent senseDeadComponent = PlayerSenseDeadComponent.KEY.get(player);
            // only reveal if the body isn't already glowing
            if(GameFunctions.isPlayerAliveAndSurvival(player)
                    && senseDeadComponent.isActive()
                && !targetBody.hasStatusEffect(StatusEffects.GLOWING)){
                cir.setReturnValue(0x606060);
                cir.cancel();
                return;
            }
            // spectators see converted bodies
            if(GameFunctions.isPlayerSpectatingOrCreative(player) && WatheClient.isInstinctEnabled() && TargetAbilityUtil.isBodyConverted(targetBody)){
                cir.setReturnValue(0xADAAAA);
                cir.cancel();
                return;
            }
        }
    }
    @Unique
    private static int handleSpectatorInstinct(PlayerEntity targetPlayer, GameWorldComponent gameWorldComponent) {
        PlayerPoisonComponent playerPoisonComponent = PlayerPoisonComponent.KEY.get(targetPlayer);
        StarstruckComponent starstruckComponent = StarstruckComponent.KEY.get(targetPlayer);
        PlayerHearDeadComponent playerHearDeadComponent = PlayerHearDeadComponent.KEY.get(targetPlayer);
        if (playerPoisonComponent.poisonTicks > 0) {
            return Color.RED.getRGB();
        }
        if( starstruckComponent.ticks > 0 && gameWorldComponent.isRole(targetPlayer, StarryExpressRoles.STARSTRUCK)) {
            return 0x77C2F2;
        }
        if(playerHearDeadComponent.isActive()){
            return 0x0D0A0A;
        }
        return -1;
    }
    @Unique
    private static int handleBartender(PlayerEntity bartender, PlayerEntity targetPlayer) {
        BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(targetPlayer);
        PlayerPoisonComponent playerPoisonComponent =  PlayerPoisonComponent.KEY.get(targetPlayer);
        if (playerPoisonComponent.poisonTicks > 0) {
            return Color.RED.getRGB();
        }
        if (bartenderPlayerComponent.armor > 0) {
            if(targetPlayer == bartender)
                return Color.BLUE.getRGB();
        }
        if (bartenderPlayerComponent.glowTicks > 0) return Color.GREEN.getRGB();

        return -1;
    }
}