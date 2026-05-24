package org.cat.express.wyspiaexpress.mixin.roles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.voice.NoellesrolesVoiceChatPlugin;
import org.cat.express.wyspiaexpress.components.PlayerHearDeadComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoellesrolesVoiceChatPlugin.class)
public abstract class EddieHearDeadPeopleMixin {

    @WrapOperation(
            method = "lambda$paranoidEvent$0",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;isRole(Lnet/minecraft/entity/player/PlayerEntity;Ldev/doctor4t/wathe/api/Role;)Z",
                    remap = false
            ),
            remap = false
    )
    private static boolean interceptParanoidRoleCheck(
            GameWorldComponent instance,
            PlayerEntity player,
            Role role,
            Operation<Boolean> original
    ) {
        PlayerHearDeadComponent component = PlayerHearDeadComponent.KEY.get(player);
        if (component.isActive()) {
            return true;
        }
        return original.call(instance, player, role);
    }
}