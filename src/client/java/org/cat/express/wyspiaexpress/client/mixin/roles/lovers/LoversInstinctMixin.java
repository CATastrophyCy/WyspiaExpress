package org.cat.express.wyspiaexpress.client.mixin.roles.lovers;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.roles.LoversComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WatheClient.class, priority = 200)
public abstract class LoversInstinctMixin {

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void isInstinctEnabled(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || WatheClient.isPlayerSpectatingOrCreative()) return;
        if (LoversComponent.isLover(GameWorldComponent.KEY.get(player.getWorld()).getRole(player))
                && WatheClient.instinctKeybind.isPressed()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlight(Entity target, CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !(target instanceof PlayerEntity targetPlayer)
                || !GameFunctions.isPlayerAliveAndSurvival(player)
                || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) return;
        if (!LoversComponent.isLover(GameWorldComponent.KEY.get(player.getWorld()).getRole(player))) return;

        if (LoversComponent.KEY.get(player.getWorld()).arePartners(player.getUuid(), targetPlayer.getUuid())) {
            cir.setReturnValue(WyspiaExpressRoles.LOVER_1.color());
        } else if (WatheClient.isInstinctEnabled()) {
            cir.setReturnValue(MathHelper.hsvToRgb(0.0F, 0.0F, 0.6F));
        }
    }
}
