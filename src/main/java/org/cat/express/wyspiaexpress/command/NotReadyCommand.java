package org.cat.express.wyspiaexpress.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;

public final class NotReadyCommand {
    private NotReadyCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("notReady")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    var world = source.getWorld();
                    if (GameWorldComponent.KEY.get(world).isRunning()) {
                        source.sendError(Text.translatable("commands.notready.not_in_lobby"));
                        return 0;
                    }

                    var readyArea = MapVariablesWorldComponent.KEY.get(world).getReadyArea();
                    var players = world.getPlayers(player -> !PlayerConfigComponent.KEY.get(player).autoJoin()
                            && (readyArea == null || !readyArea.contains(player.getPos())));
                    if (players.isEmpty()) {
                        source.sendFeedback(() -> Text.translatable("commands.notready.everyone_ready"), false);
                        return 0;
                    }

                    MutableText message = Text.translatable("commands.notready.players", players.size());
                    for (var player : players) {
                        message.append("\n").append(player.getName().copy().styled(style -> style
                                .withColor(Formatting.AQUA).withUnderline(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/tp @s " + player.getUuidAsString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.translatable("commands.notready.teleport", player.getName())))));
                    }
                    source.sendFeedback(() -> message, false);
                    return players.size();
                }));
    }
}
