package org.cat.express.wyspiaexpress.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MurderGameMode.class)
public abstract class PassiveIncomeMixin {
    // passive income
    @WrapOperation(method = "tickServerGameLoop", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    public boolean wyspiaexpress$setPassiveIncome(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player, @NotNull Operation<Boolean> original) {
        Role role = gameWorld.getRole(player);
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if (config != null && config.passiveIncome()) return true;
        return original.call(gameWorld,player);
    }
}
