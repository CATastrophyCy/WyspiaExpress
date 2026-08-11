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
import org.spongepowered.asm.mixin.Unique;
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
        original.removeIf(role -> !shouldShowRole(comp, role));
        return original;
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
        original.removeIf(mod -> !shouldShowModifier(comp, mod));
        return original;
    }
    @Unique
    private static boolean shouldShowRole(RoleComponent comp, Role role){
        String roleId = WyspiaExpressRoles.getRoleId(role);
        return role == WyspiaExpressRoles.COPYCAT || (!comp.disabledRoles.contains(roleId) && !WyspiaExpressRoles.HIDDEN_ROLES.contains(role));
    }
    @Unique
    private static boolean shouldShowModifier(RoleComponent comp, Modifier modifier){
        String modifierId = WyspiaExpressRoles.getModifierId(modifier);
        return modifier == Noellesroles.GUESSER || (!comp.disabledModifiers.contains(modifierId) && !WyspiaExpressRoles.HIDDEN_MODIFIERS.contains(modifier));
    }
}