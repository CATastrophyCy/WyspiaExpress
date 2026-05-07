package org.cat.express.wyspiaexpress.mixin.kinswathe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DreamerKillerComponent.class)
public abstract class DreamerKillerComponentMixin {

    @Final @Shadow(remap = false)
    private PlayerEntity player;

    @Shadow(remap = false)
    public int dreamerRequired;

    @Shadow(remap = false)
    public abstract void sync();

    @Inject(method = "setDreamerRequired", at = @At("HEAD"), cancellable = true, remap = false)
    private void wyspiaexpress$setDreamerRequired(CallbackInfo ci) {

        int baseRequired = WyspiaExpressRoles.PLAYER_COUNT / 5;
        this.dreamerRequired = MathHelper.clamp(baseRequired,
                WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.dreamerConfig.minimumRequirement(),
                WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.dreamerConfig.maximumRequirement());

        this.sync();
        ci.cancel();
    }
}