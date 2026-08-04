package org.cat.express.wyspiaexpress.client.mixin.roles.villain_licensed;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WatheClient.class)
public class VillainLicensedInstinctMixin {

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void isInstinctEnabled(@NotNull CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance().player == null) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        if (gameWorld.isRole(MinecraftClient.getInstance().player, WyspiaExpressRoles.VILLAIN_LICENSED) && WatheClient.instinctKeybind.isPressed() && GameFunctions.isPlayerAliveAndSurvival(MinecraftClient.getInstance().player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlight(@NotNull Entity target, @NotNull CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(target.getWorld());
        if (target instanceof PlayerEntity targetPlayer && GameFunctions.isPlayerAliveAndSurvival(targetPlayer) &&
                GameFunctions.isPlayerAliveAndSurvival(player) && gameWorld.isRole(player, WyspiaExpressRoles.VILLAIN_LICENSED) && WatheClient.isInstinctEnabled()) {
            cir.setReturnValue(WyspiaExpressRoles.VILLAIN_LICENSED.color());
        }
    }
}