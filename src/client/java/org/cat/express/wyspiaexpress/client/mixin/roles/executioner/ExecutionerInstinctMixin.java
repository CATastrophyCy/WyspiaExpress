package org.cat.express.wyspiaexpress.client.mixin.roles.executioner;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WatheClient.class, priority = 200)
public abstract class ExecutionerInstinctMixin {

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void isInstinctEnabled(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || WatheClient.isPlayerSpectatingOrCreative()) return;
        if (GameWorldComponent.KEY.get(player.getWorld()).isRole(player, Noellesroles.EXECUTIONER)) {
            cir.setReturnValue(WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.executionerConfig.enableInstinct()
                    && GameFunctions.isPlayerAliveAndSurvival(player) && WatheClient.instinctKeybind.isPressed());
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlight(Entity target, CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !(target instanceof PlayerEntity targetPlayer)
                || !GameFunctions.isPlayerAliveAndSurvival(player)
                || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) return;
        if (GameWorldComponent.KEY.get(player.getWorld()).isRole(player, Noellesroles.EXECUTIONER)
                && WatheClient.isInstinctEnabled()) {
            cir.setReturnValue(0x999999);
        }
    }
}
