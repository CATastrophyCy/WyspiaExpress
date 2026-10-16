package org.cat.express.wyspiaexpress.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.components.PlayerTaskComponent;

import java.util.Arrays;
import java.util.Locale;

public final class SetTaskCommand {
    private SetTaskCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("setTask")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("task")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("task", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(Arrays.stream(PlayerMoodComponent.Task.values())
                                                .map(type -> type.name().toLowerCase(Locale.ROOT)), builder))
                                        .executes(SetTaskCommand::setTask))))
                .then(CommandManager.literal("timer")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0, Integer.MAX_VALUE / 20))
                                        .executes(SetTaskCommand::setTimer)))));
    }

    private static boolean isRunning(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        if (GameWorldComponent.KEY.get(player.getWorld()).isRunning()) return true;
        context.getSource().sendError(Text.translatable("commands.settask.not_running"));
        return false;
    }

    private static int setTask(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        if (!isRunning(context, player)) return 0;
        PlayerMoodComponent.Task type;
        try { type = PlayerMoodComponent.Task.valueOf(StringArgumentType.getString(context, "task").toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException invalid) {
            context.getSource().sendError(Text.translatable("commands.settask.invalid_task"));
            return 0;
        }
        PlayerTaskComponent.KEY.get(player).forceTask(type);
        context.getSource().sendFeedback(() -> Text.translatable("commands.settask.task_set", player.getDisplayName(),
                PlayerTaskComponent.taskName(type)), true);
        return 1;
    }

    private static int setTimer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        if (!isRunning(context, player)) return 0;
        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        if (!PlayerTaskComponent.KEY.get(player).setNextTaskSeconds(seconds)) {
            context.getSource().sendError(Text.translatable("commands.settask.active_task", player.getDisplayName()));
            return 0;
        }
        context.getSource().sendFeedback(() -> Text.translatable("commands.settask.timer_set", player.getDisplayName(), seconds), true);
        return 1;
    }
}
