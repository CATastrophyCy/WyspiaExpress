package org.cat.express.wyspiaexpress.mixin.kinswathe;

import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
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
}