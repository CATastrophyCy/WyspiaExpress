package org.cat.express.wyspiaexpress.client.mixin;

import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(WatheClient.class)
public abstract class InstinctMixin {

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (target instanceof PlayerEntity targetPlayer) {
            if(GameFunctions.isPlayerSpectatingOrCreative((player)) && GameFunctions.isPlayerAliveAndSurvival(targetPlayer) && WatheClient.isInstinctEnabled()) {
                    PlayerPoisonComponent playerPoisonComponent = PlayerPoisonComponent.KEY.get(targetPlayer);
                    BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(target);
                    PhysicianComponent physicianComponent = PhysicianComponent.KEY.get(player);
                    DreamerComponent dreamerComponent = DreamerComponent.KEY.get(target);
                    if (playerPoisonComponent.poisonTicks > 0) {
                        cir.setReturnValue(Color.RED.getRGB());
                        return;
                    }
                    if( bartenderPlayerComponent.armor > 0 || physicianComponent.physicianArmor > 0) {
                        cir.setReturnValue(Color.BLUE.getRGB());
                        return;
                    }
                    if( dreamerComponent.dreamArmor > 0) {
                        cir.setReturnValue(KinsWatheRoles.DREAMER.color());
                        return;
                    }
            }
        }
    }
}