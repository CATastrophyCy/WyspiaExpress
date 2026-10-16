package org.cat.express.wyspiaexpress.mixin.noellesroles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressGameFunctions;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.function.Predicate;

@Mixin(Noellesroles.class)
public abstract class VultureRoleMixin {
    @Redirect(
            method = "lambda$registerPackets$12",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/ArrayList;removeIf(Ljava/util/function/Predicate;)Z",
                    remap = false
            )
    )
    private static boolean modifyVultureRolesFilter(ArrayList<Role> instance, Predicate<? super Role> originalPredicate) {
        if(WyspiaExpress.ROLES_CONFIG.enableRolePicking()) {
            instance.clear();
            return instance.add(WyspiaExpressRoles.COPYCAT);
        }
        return instance.removeIf(role ->
                Harpymodloader.VANNILA_ROLES.contains(role) ||
                        !role.canUseKiller() ||
                        HarpyModLoaderConfig.HANDLER.instance().disabled.contains(WyspiaExpressRoles.getRoleId(role))
                        || !WyspiaExpressRoles.roleMeetPlayerRequirement(role)
        );
    }

    @WrapOperation(method = "lambda$registerPackets$12", at = @At(value = "INVOKE",
            target = "Lorg/agmas/harpymodloader/events/ModdedRoleAssigned;assignModdedRole(Lnet/minecraft/entity/player/PlayerEntity;Ldev/doctor4t/wathe/api/Role;)V"))
    private static void wyspiaexpress$announcePromotion(ModdedRoleAssigned event, PlayerEntity player, Role role,
                                                        Operation<Void> original) {
        original.call(event, player, role);
        WyspiaExpressGameFunctions.sendRoleChangedMessage(((ServerPlayerEntity) player).getServerWorld(),
                player, Noellesroles.VULTURE, role);
    }
}
