package org.cat.express.wyspiaexpress.mixin;

import com.mojang.brigadier.context.CommandContext;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.commands.ListRolesCommand;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ListRolesCommand.class)
public abstract class ListRolesCommandMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void wyspiaexpress$execute(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) {
        HarpyModLoaderConfig.HANDLER.save();
        final MutableText message = Text.empty();
        message.append(Text.translatable("commands.listroles.role.title")).append("\n");
        message.append(Texts.join(WatheRoles.ROLES.stream().filter(ListRolesCommandMixin::wyspiaexpress$shouldShowRole).toList(),
                Text.literal("\n"), role -> {
            final boolean disabled = HarpyModLoaderConfig.HANDLER.instance().disabled.contains(WyspiaExpressRoles.getRoleId(role));
            final MutableText status = wyspiaexpress$createStatus(context.getSource(), disabled, "/setEnabledRole " + role.identifier() + " " + disabled);
            return wyspiaexpress$buildElementText(Harpymodloader.getRoleName(role).withColor(role.color()), role.identifier(), status);
        }));
        message.append("\n\n");
        message.append(Text.translatable("commands.listroles.modifier.title")).append("\n");
        message.append(Texts.join(HMLModifiers.MODIFIERS.stream().filter(ListRolesCommandMixin::wyspiaexpress$shouldShowModifier).toList(), Text.literal("\n"), modifier -> {
            final boolean disabled = HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(WyspiaExpressRoles.getModifierId(modifier));
            final MutableText status = wyspiaexpress$createStatus(context.getSource(), disabled, "/setEnabledModifier " + modifier.identifier() + " " + disabled);
            return wyspiaexpress$buildElementText(modifier.getName().withColor(modifier.color), modifier.identifier(), status);
        }));

        context.getSource().sendMessage(message);
        cir.setReturnValue(1); // cancels the original method and returns 1
    }

    @Unique
    private static MutableText wyspiaexpress$buildElementText(Text name, Identifier identifier, Text status) {
        return Text.empty().append(name.copy()).append(" ").append(Text.literal("(" + identifier + ")")).append(" ").append(status);
    }

    @Unique
    private static MutableText wyspiaexpress$createStatus(ServerCommandSource source, boolean disabled, String cmd) {
        String key = disabled ? "disabled" : "enabled";
        return Text.translatable("commands.listroles.status." + key + ".text").styled(style -> {
            if (source.hasPermissionLevel(2)) {
                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("commands.listroles.status." + key + ".hover", cmd))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
            } else {
                return style;
            }
        });
    }
    @Unique
    private static boolean wyspiaexpress$shouldShowRole(Role role){
        return !HarpyModLoaderConfig.HANDLER.instance().disabled.contains(WyspiaExpressRoles.getRoleId(role))
                || !WyspiaExpressRoles.HIDDEN_ROLES.contains(role);
    }
    @Unique
    private static boolean wyspiaexpress$shouldShowModifier(Modifier modifier){
        return !HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(WyspiaExpressRoles.getModifierId(modifier))
                || !WyspiaExpressRoles.HIDDEN_MODIFIERS.contains(modifier);
    }
}