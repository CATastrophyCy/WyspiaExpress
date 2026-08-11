package org.cat.express.wyspiaexpress.mixin.kinswathe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

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

    @ModifyExpressionValue(
            method = "triggerBecomeKiller",
            at = @At(value = "FIELD",
                    target = "Ldev/doctor4t/wathe/api/WatheRoles;ROLES:Ljava/util/ArrayList;",
                    opcode = Opcodes.GETSTATIC),
            remap = false
    )
    private ArrayList<Role> wyspiaexpress$replaceRoles(ArrayList<Role> original) {

        return original.stream()
                .filter(WyspiaExpressRoles::roleMeetPlayerRequirement)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}