package org.cat.express.wyspiaexpress.mixin.noellesroles;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = PlayerPoisonComponent.class, priority = 500)
public class DefenseVialEffectMixin {
    @Shadow @Final private PlayerEntity player;

    @Inject(method = "setPoisonTicks", at = @At("HEAD"))
    private void defenseVialApply(int ticks, UUID poisoner, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(poisoner, Noellesroles.BARTENDER)) {
            if (player.getWorld().getPlayerByUuid(poisoner) == null) return;
            Text message = Text.literal("You are now protected by a mysterious protection!");
            player.sendMessage(message, true);
        }
    }
}