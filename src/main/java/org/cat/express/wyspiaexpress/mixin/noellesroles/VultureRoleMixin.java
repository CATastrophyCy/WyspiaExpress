package org.cat.express.wyspiaexpress.mixin.noellesroles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.cat.express.wyspiaexpress.util.RoleUtil;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.packet.VultureEatC2SPacket;
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
    private static boolean modifyVultureRolesFilter(ArrayList<Role> instance, Predicate<? super Role> originalPredicate,
                                                    VultureEatC2SPacket payload, ServerPlayNetworking.Context context) {
        return RoleUtil.filterKillerPromotionRoles(instance, GameWorldComponent.KEY.get(context.player().getWorld()));
    }

    @WrapOperation(method = "lambda$registerPackets$12", at = @At(value = "INVOKE",
            target = "Lorg/agmas/harpymodloader/events/ModdedRoleAssigned;assignModdedRole(Lnet/minecraft/entity/player/PlayerEntity;Ldev/doctor4t/wathe/api/Role;)V"))
    private static void wyspiaexpress$announcePromotion(ModdedRoleAssigned event, PlayerEntity player, Role role,
                                                        Operation<Void> original) {
        original.call(event, player, role);
        GameUtil.sendRoleChangedMessage(((ServerPlayerEntity) player).getServerWorld(),
                player, Noellesroles.VULTURE, role);
    }
}
