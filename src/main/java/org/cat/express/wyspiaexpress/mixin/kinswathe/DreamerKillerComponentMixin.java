package org.cat.express.wyspiaexpress.mixin.kinswathe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.cat.express.wyspiaexpress.util.RoleUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.function.Predicate;

@Mixin(DreamerKillerComponent.class)
public abstract class DreamerKillerComponentMixin {

    @Shadow(remap = false)
    public int dreamerRequired;

    @Shadow(remap = false)
    public abstract void sync();

    @Inject(method = "setDreamerRequired", at = @At("HEAD"), cancellable = true, remap = false)
    private void wyspiaexpress$setDreamerRequired(CallbackInfo ci) {

        int baseRequired = WyspiaExpressRoles.ROUND_PLAYER_COUNT / 5;
        this.dreamerRequired = MathHelper.clamp(baseRequired,
                WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.dreamerConfig.minimumRequirement(),
                WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.dreamerConfig.maximumRequirement());

        this.sync();
        ci.cancel();
    }

    @Redirect(
            method = "triggerBecomeKiller",
            at = @At(
            value = "INVOKE",
            target = "Ljava/util/ArrayList;removeIf(Ljava/util/function/Predicate;)Z",
            remap = false)
    )
    private static boolean wyspiaexpress$replaceRoles(ArrayList<Role> instance, Predicate<? super Role> originalPredicate) {
        return RoleUtil.filterKillerPromotionRoles(instance);
    }

    @WrapOperation(method = "triggerBecomeKiller", at = @At(value = "INVOKE",
            target = "Lorg/agmas/harpymodloader/events/ModdedRoleAssigned;assignModdedRole(Lnet/minecraft/entity/player/PlayerEntity;Ldev/doctor4t/wathe/api/Role;)V"))
    private void wyspiaexpress$announcePromotion(ModdedRoleAssigned event, PlayerEntity player, Role role,
                                                 Operation<Void> original) {
        original.call(event, player, role);
        GameUtil.sendRoleChangedMessage(((ServerPlayerEntity) player).getServerWorld(),
                player, KinsWatheRoles.DREAMER, role);
    }
}
