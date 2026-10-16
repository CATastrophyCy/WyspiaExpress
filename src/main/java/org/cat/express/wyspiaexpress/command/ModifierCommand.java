package org.cat.express.wyspiaexpress.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.agmas.harpymodloader.commands.argument.ModifierArgumentType;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ModifierRemoved;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.WyspiaExpressCommands;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;

public final class ModifierCommand {
    private ModifierCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        register(dispatcher, "addModifier", true);
        register(dispatcher, "removeModifier", false);
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, String name, boolean add) {
        dispatcher.register(CommandManager.literal(name)
                .requires(WyspiaExpressCommands::isOperator)
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("modifier", ModifierArgumentType.create())
                                .executes(context -> execute(context, add)))));
    }

    private static int execute(CommandContext<ServerCommandSource> context, boolean add) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        if (!GameWorldComponent.KEY.get(player.getWorld()).isRunning()) {
            context.getSource().sendError(Text.translatable("commands.modifier.not_running"));
            return 0;
        }

        Modifier modifier = ModifierArgumentType.getModifier(context, "modifier");
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(player.getWorld());
        Text modifierText = modifier.getName().withColor(modifier.color()).styled(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(WyspiaExpressRoles.getModifierId(modifier)))));
        if (modifiers.isModifier(player, modifier) == add) {
            context.getSource().sendError(Text.translatable(add
                    ? "commands.modifier.already_assigned" : "commands.modifier.not_assigned",
                    player.getDisplayName(), modifierText));
            return 0;
        }

        if (add) {
            modifiers.addModifier(player.getUuid(), modifier);
            ModifierAssigned.EVENT.invoker().assignModifier(player, modifier);
        } else {
            // Remove every matching entry, including duplicates left by other assignment paths.
            modifiers.getModifiers(player).removeIf(modifier::equals);
            ModifierRemoved.EVENT.invoker().removeModifier(player, modifier);
        }
        modifiers.sync();
        context.getSource().sendFeedback(() -> Text.translatable(add
                ? "commands.modifier.added" : "commands.modifier.removed",
                player.getDisplayName(), modifierText), true);
        return 1;
    }
}
