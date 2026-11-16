package org.cat.express.wyspiaexpress.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.harpymodloader.modded_murder.ModdedWeights;
import org.cat.express.wyspiaexpress.RoleDraftStatisticsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ScoreboardRoleSelectorComponent.class)
public class WeightsResetMixin {

    @Inject(method = "reset", at = @At("TAIL"), cancellable = false)
    public void wyspiaexpress$reset(CallbackInfoReturnable<Integer> cir) {
        ModdedWeights.roleRounds.values().forEach(Map::clear);
        ModdedWeights.roleWeights.values().forEach(Map::clear);
        if (RoleDraftStatisticsManager.getInstance() != null) RoleDraftStatisticsManager.getInstance().reset();
    }

    @WrapOperation(method = "assignVigilantes", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;isRole(Lnet/minecraft/entity/player/PlayerEntity;Ldev/doctor4t/wathe/api/Role;)Z"))
    private boolean wyspiaexpress$skipForcedVigilantes(GameWorldComponent game, PlayerEntity player, Role role,
            Operation<Boolean> original) {
        return original.call(game, player, role) || game.isRole(player, WatheRoles.VIGILANTE);
    }
}
