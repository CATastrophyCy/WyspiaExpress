package org.cat.express.wyspiaexpress.client.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.client.MinecraftClient;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.agmas.noellesroles.Noellesroles;
import org.aussiebox.starexpress.client.gui.screen.GuidebookScreen;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.RoleComponent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;

@Mixin(GuidebookScreen.class)
public abstract class GuideBookScreenMixin {

    @ModifyExpressionValue(
            method = "getRoleInfo",
            at = @At(value = "FIELD",
                    target = "Ldev/doctor4t/wathe/api/WatheRoles;ROLES:Ljava/util/ArrayList;",
                    opcode = Opcodes.GETSTATIC),
            remap = false
    )
    private ArrayList<Role> wyspiaexpress$replaceRoles(ArrayList<Role> original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return original;
        RoleComponent comp = RoleComponent.KEY.get(client.world);
        return  original.stream()
                .filter(role ->
                        (!comp.disabledRoles.contains(WyspiaExpressRoles.getRoleId(role)) || role == WyspiaExpressRoles.COPYCAT )
                        && !comp.hiddenRoles.contains(WyspiaExpressRoles.getRoleId(role))
                )
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    @ModifyExpressionValue(
            method = "getRoleInfo",
            at = @At(value = "FIELD",
                    target = "Lorg/agmas/harpymodloader/modifiers/HMLModifiers;MODIFIERS:Ljava/util/ArrayList;",
                    opcode = Opcodes.GETSTATIC),
            remap = false
    )
    private ArrayList<Modifier> wyspiaexpress$replaceModifiers(ArrayList<Modifier> original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return original;

        RoleComponent comp = RoleComponent.KEY.get(client.world);
        return original.stream()
                .filter(mod ->
                        (!comp.disabledModifiers.contains(WyspiaExpressRoles.getModifierId(mod)) || mod == Noellesroles.GUESSER)
                        && !comp.hiddenModifiers.contains(WyspiaExpressRoles.getModifierId(mod))

                )
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}