package org.cat.express.wyspiaexpress.client.mixin.roles.cult;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.roles.TargetAbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WatheClient.class)
public abstract class CultInstinctMixin {
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(target.getWorld());
        if(!(gameWorldComponent.isRole(player, WyspiaExpressRoles.CULT_LEADER) || gameWorldComponent.isRole(player, WyspiaExpressRoles.CULTIST))
                || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        // instinct to players
        if (target instanceof PlayerEntity targetPlayer
                && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
            if(gameWorldComponent.isRole(targetPlayer, WyspiaExpressRoles.CULTIST)) {
                cir.setReturnValue(WyspiaExpressRoles.CULTIST.color());
                cir.cancel();
                return;
            }
            if(gameWorldComponent.isRole(targetPlayer, WyspiaExpressRoles.CULT_LEADER)) {
                cir.setReturnValue(WyspiaExpressRoles.CULT_LEADER.color());
                cir.cancel();
                return;
            }
            if(TargetAbilityUtil.isPlayerConverted(targetPlayer)) {
                cir.setReturnValue(0xb59eb5);
                cir.cancel();
                return;
            }
        }
        // instinct to player bodies
        if(target instanceof PlayerBodyEntity targetBody){
            // only reveal if the body isn't already glowing
            if(!targetBody.hasStatusEffect(StatusEffects.GLOWING) && TargetAbilityUtil.isBodyConverted(player.getWorld(),targetBody)){
                cir.setReturnValue(0xb59eb5);
                cir.cancel();
                return;
            }
        }
    }
}
