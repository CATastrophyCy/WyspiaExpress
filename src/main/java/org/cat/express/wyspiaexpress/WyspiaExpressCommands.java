package org.cat.express.wyspiaexpress;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.cat.express.wyspiaexpress.command.ModifierCommand;
import org.cat.express.wyspiaexpress.command.ReviveCommand;
import org.cat.express.wyspiaexpress.command.RoundTimeCommand;
import org.cat.express.wyspiaexpress.command.SetMoodCommand;
import org.cat.express.wyspiaexpress.command.SetRoleCommand;
import org.cat.express.wyspiaexpress.command.SetTaskCommand;
import org.cat.express.wyspiaexpress.command.SpectatorVoiceCommand;

import java.util.List;

public final class WyspiaExpressCommands {
    private static final List<CommandRegistrationCallback> COMMANDS = List.of(
            SpectatorVoiceCommand::register,
            SetRoleCommand::register,
            ReviveCommand::register,
            SetMoodCommand::register,
            ModifierCommand::register,
            SetTaskCommand::register,
            RoundTimeCommand::register
    );

    private WyspiaExpressCommands() {}

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                COMMANDS.forEach(command -> command.register(dispatcher, registryAccess, environment)));
    }
}
