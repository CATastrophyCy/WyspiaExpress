package org.cat.express.wyspiaexpress.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressGameFunctions;

public final class RoundTimeCommand {
    private RoundTimeCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("roundTime").executes(context -> {
            if (WyspiaExpress.ROUND_TICKS < 0) {
                context.getSource().sendFeedback(() -> Text.translatable("commands.roundtime.not_started"), false);
                return 0;
            }
            context.getSource().sendFeedback(() -> Text.translatable(
                    GameWorldComponent.KEY.get(context.getSource().getServer().getOverworld()).getGameStatus()
                            == GameWorldComponent.GameStatus.ACTIVE
                            ? "commands.roundtime.current" : "commands.roundtime.previous",
                    WyspiaExpressGameFunctions.formatRoundTime(WyspiaExpress.ROUND_TICKS)), false);
            return 1;
        }));
    }
}
