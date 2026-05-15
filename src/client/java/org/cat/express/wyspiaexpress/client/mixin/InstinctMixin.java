package org.cat.express.wyspiaexpress.client.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
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

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(target.getWorld());
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
            // handle alive player instinct with instinct pressed, doesn't handle passive instinct
            if (GameFunctions.isPlayerAliveAndSurvival(targetPlayer) && WatheClient.isInstinctEnabled()) {
                Role role = gameWorldComponent.getRole(targetPlayer);
                if (role != null) {
                    // If the player is killer and the player is alive
                    if (WatheClient.isKiller() && WatheClient.isPlayerAliveAndInSurvival()) {
                        if (KinsWatheRoles.NEUTRAL_ROLES.contains(role)) {
                            cir.setReturnValue(0x4EDD35);
                            cir.cancel();
                            return;
                        } else if (KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role)) {
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
                    // if the current player is a dreamer
                    if (gameWorldComponent.isRole(player, KinsWatheRoles.DREAMER)) {
                        cir.setReturnValue(handleDreamerInstinct(targetPlayer, gameWorldComponent));
                        cir.cancel();
                        return;
                    }
                    // if the current player is a hacker
                    if (gameWorldComponent.isRole(player, KinsWatheRoles.HACKER)) {
                        cir.setReturnValue(handleHackerInstinct(targetPlayer, gameWorldComponent, role));
                        cir.cancel();
                        return;
                    }
                }
            }
        }
    }

    @Unique
    private static int handleHackerInstinct(PlayerEntity targetPlayer, GameWorldComponent gameWorldComponent, Role role) {
            HackerComponent targetHack = HackerComponent.KEY.get(targetPlayer);
            if(WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.hackerConfig.enableInstinct()) {
                if (gameWorldComponent.canUseKillerFeatures(targetPlayer) || gameWorldComponent.isRole(targetPlayer, Noellesroles.MIMIC)) {
                    return MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F);
                } else if (KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role)) {
                    if (WyspiaExpress.SERVER_CONFIG.killerSpecialInstinct()) {
                        return role.color();
                    }
                    return MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F);
                } else {
                    return KinsWatheRoles.HACKER.color();
                }
            }
            else if (targetHack.hackingTime >= ConfigWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld()).HackerHackingTime * 20) {
                return Color.GREEN.getRGB();
            }
            // disable instinct
            return -1;
    }
    @Unique
    private static int handleDreamerInstinct(PlayerEntity targetPlayer, GameWorldComponent gameWorldComponent) {
        DreamerComponent targetDream = DreamerComponent.KEY.get(targetPlayer);
        // if the target player is protected by this dreamer or that the instinct is enabled
        if (WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.dreamerConfig.enableInstinct() || targetDream.dreamerUUID != null) {
            return KinsWatheRoles.DREAMER.color();
        }
        return -1;
    }
    @Unique
    private static int handleSpectatorInstinct(PlayerEntity targetPlayer, GameWorldComponent gameWorldComponent) {
        PlayerPoisonComponent playerPoisonComponent = PlayerPoisonComponent.KEY.get(targetPlayer);
        BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(targetPlayer);
        PhysicianComponent physicianComponent = PhysicianComponent.KEY.get(targetPlayer);
        StarstruckComponent starstruckComponent = StarstruckComponent.KEY.get(targetPlayer);
        DreamerComponent dreamerComponent = DreamerComponent.KEY.get(targetPlayer);
        MorphlingPlayerComponent morphlingPlayerComponent = MorphlingPlayerComponent.KEY.get(targetPlayer);
        SilenceComponent silenceComponent = SilenceComponent.KEY.get(targetPlayer);
        if( starstruckComponent.ticks > 0 && gameWorldComponent.isRole(targetPlayer, StarryExpressRoles.STARSTRUCK)) {
            return 0x77C2F2;
        }
        if( gameWorldComponent.isRole(targetPlayer, KinsWatheRoles.ROBOT) && targetPlayer.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            return 0x0D0B0B;
        }
        if( gameWorldComponent.isRole(targetPlayer, Noellesroles.MORPHLING) && morphlingPlayerComponent.morphTicks > 0 ) {
            return 0x2E0000;
        }
        if (playerPoisonComponent.poisonTicks > 0) {
            if(playerPoisonComponent.poisoner != null && playerPoisonComponent.poisoner.equals(DELUSION_MARKER)) {
                return 0xB099FF;
            }
            return Color.RED.getRGB();
        }
        if(silenceComponent.isSilenced()){
            return 0x4A3A54;
        }
        if( bartenderPlayerComponent.armor > 0 || physicianComponent.physicianArmor > 0) {
            return Color.BLUE.getRGB();
        }
        if( dreamerComponent.dreamArmor > 0) {
            return 0xFF6BFA;
        }
        return -1;
    }
}