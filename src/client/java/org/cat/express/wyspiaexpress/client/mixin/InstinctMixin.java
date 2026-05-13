package org.cat.express.wyspiaexpress.client.mixin;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.UUID;

@Mixin(WatheClient.class)
public abstract class InstinctMixin {
    // apparently they replaced fake poison's poisoner to this uuid, and in PlayerPoisonComponent they used a mixin to guarantee that delusion_maker won't kill
    @Unique private static final UUID DELUSION_MARKER = UUID.fromString("00000000-0000-0000-dead-c0de00000000"); // unique string used by Kinswathe

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (target instanceof PlayerEntity targetPlayer) {
            if(GameFunctions.isPlayerSpectatingOrCreative(player)
                    && (GameFunctions.isPlayerAliveAndSurvival(targetPlayer)  || targetPlayer.equals(player))
                    && WatheClient.isInstinctEnabled() ) {
                GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(targetPlayer.getWorld());
                PlayerPoisonComponent playerPoisonComponent = PlayerPoisonComponent.KEY.get(targetPlayer);
                BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(targetPlayer);
                PhysicianComponent physicianComponent = PhysicianComponent.KEY.get(targetPlayer);
                StarstruckComponent starstruckComponent = StarstruckComponent.KEY.get(targetPlayer);
                DreamerComponent dreamerComponent = DreamerComponent.KEY.get(targetPlayer);
                MorphlingPlayerComponent morphlingPlayerComponent = MorphlingPlayerComponent.KEY.get(targetPlayer);
                if( starstruckComponent.ticks > 0 && gameWorldComponent.isRole(targetPlayer, StarryExpressRoles.STARSTRUCK)) {
                    cir.setReturnValue(0x77C2F2);
                    cir.cancel();
                    return;
                }
                if( gameWorldComponent.isRole(targetPlayer, KinsWatheRoles.ROBOT) && targetPlayer.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    cir.setReturnValue(0xE0E0E0);
                    cir.cancel();
                    return;
                }
                if( gameWorldComponent.isRole(targetPlayer, Noellesroles.MORPHLING) && morphlingPlayerComponent.morphTicks > 0 ) {
                    cir.setReturnValue(0x2E0000);
                    cir.cancel();
                    return;
                }
                if (playerPoisonComponent.poisonTicks > 0) {
                    if(playerPoisonComponent.poisoner.equals(DELUSION_MARKER)) {
                        cir.setReturnValue(0xB099FF);
                        cir.cancel();
                        return;
                    }
                    cir.setReturnValue(Color.RED.getRGB());
                    cir.cancel();
                    return;
                }
                if( bartenderPlayerComponent.armor > 0 || physicianComponent.physicianArmor > 0) {
                    cir.setReturnValue(Color.BLUE.getRGB());
                    cir.cancel();
                    return;
                }
                if( dreamerComponent.dreamArmor > 0) {
                    cir.setReturnValue(0xFF6BFA);
                    cir.cancel();
                    return;
                }
            }
        }
    }
}