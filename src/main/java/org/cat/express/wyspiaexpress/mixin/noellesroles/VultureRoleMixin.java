package org.cat.express.wyspiaexpress.mixin.noellesroles;

import com.google.common.collect.Lists;
import dev.doctor4t.wathe.api.Role;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
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
}