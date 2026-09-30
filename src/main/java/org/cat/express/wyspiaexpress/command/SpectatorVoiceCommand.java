package org.cat.express.wyspiaexpress.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressCommands;
import org.cat.express.wyspiaexpress.voicechat.SpectatorVoiceService;

public final class SpectatorVoiceCommand {
    private SpectatorVoiceCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("sv")
                .then(CommandManager.literal("join")
                        .then(CommandManager.argument("index", IntegerArgumentType.integer(0))
                                .executes(SpectatorVoiceCommand::join)))
                .then(CommandManager.literal("leave")
                        .executes(SpectatorVoiceCommand::leave))
                .then(CommandManager.literal("drag")
                        .requires(WyspiaExpressCommands::isOperator)
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> drag(context, null))
                                .then(CommandManager.argument("index", IntegerArgumentType.integer(0))
                                        .executes(context -> drag(context,
                                                IntegerArgumentType.getInteger(context, "index")))))));
    }

    private static int join(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        if (!player.isCreative() && !player.isSpectator()) {
            context.getSource().sendError(Text.literal("Command reserved for spectators!"));
            return 0;
        }
        int index = IntegerArgumentType.getInteger(context, "index");
        if (!reportResult(context.getSource(), SpectatorVoiceService.join(player.getUuid(), index), player)) return 0;
        context.getSource().sendFeedback(() -> Text.literal("Joined spectator voice group " + index + "."), true);
        return 1;
    }

    private static int leave(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        if (!reportResult(context.getSource(), SpectatorVoiceService.leave(player.getUuid()), player)) return 0;
        context.getSource().sendFeedback(() -> Text.literal("Left spectator voice chat."), false);
        return 1;
    }

    private static int drag(CommandContext<ServerCommandSource> context, Integer index) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        SpectatorVoiceService.Result result = index == null ? SpectatorVoiceService.leave(player.getUuid())
                : SpectatorVoiceService.join(player.getUuid(), index);
        if (!reportResult(context.getSource(), result, player)) return 0;
        context.getSource().sendFeedback(() -> index == null
                ? Text.literal("Removed ").append(player.getDisplayName()).append(" from spectator voice chat.")
                : Text.literal("Moved ").append(player.getDisplayName()).append(" to spectator voice group " + index + "."), true);
        return 1;
    }

    private static boolean reportResult(ServerCommandSource source, SpectatorVoiceService.Result result,
                                        ServerPlayerEntity player) {
        if (result == SpectatorVoiceService.Result.SUCCESS) return true;
        source.sendError(switch (result) {
            case UNAVAILABLE -> Text.literal("Simple Voice Chat is not available right now.");
            case NOT_CONNECTED -> Text.literal("").append(player.getDisplayName()).append(" is not connected to voice chat.");
            case INVALID_INDEX -> Text.literal("Spectator voice group index must be between 0 and "
                    + WyspiaExpress.SERVER_CONFIG.extraSpectatorsVoicechat() + ".");
            case GROUP_UNAVAILABLE -> Text.literal("Could not create or resolve the spectator voice group.");
            case SUCCESS -> throw new IllegalStateException("Successful voice operation has no error");
        });
        return false;
    }
}
