package org.cat.express.wyspiaexpress.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpressCommands;
import org.cat.express.wyspiaexpress.gameplay.PlayerMoodService;

public final class SetMoodCommand {
    private SetMoodCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("setMood")
                .requires(WyspiaExpressCommands::isOperator)
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("amount", FloatArgumentType.floatArg(0.0F, 1.0F))
                                .executes(SetMoodCommand::execute))));
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (role == null || role.getMoodType() != Role.MoodType.REAL) {
            context.getSource().sendError(Text.literal("").append(player.getDisplayName())
                    .append(" does not have a role with adjustable mood."));
            return 0;
        }
        float amount = FloatArgumentType.getFloat(context, "amount");
        PlayerMoodService.setMoodAdmin(player, amount);
        context.getSource().sendFeedback(() -> Text.literal("Set ").append(player.getDisplayName())
                .append("'s mood to " + amount + "."), true);
        return 1;
    }
}
