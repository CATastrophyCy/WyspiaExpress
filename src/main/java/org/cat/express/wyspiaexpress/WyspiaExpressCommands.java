package org.cat.express.wyspiaexpress;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WyspiaExpressCommands {


    public static final Map<Integer, UUID> GROUP_IDS = new HashMap<>();
    public static final Map<Integer, Group> GROUPS = new HashMap<>();

    public static void init() {
        buildGroupIdTable();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerSv(dispatcher);
            registerSetRole(dispatcher);
        });
    }

    private static void buildGroupIdTable() {
        GROUP_IDS.clear();
        GROUP_IDS.put(0, TrainVoicePlugin.GROUP_ID);
        for (int i = 1; i <= WyspiaExpress.SERVER_CONFIG.extraSpectatorsVoicechat(); i++) {
            GROUP_IDS.put(i, UUID.nameUUIDFromBytes(
                    ("wyspiaexpress:train_spectator_" + i).getBytes(StandardCharsets.UTF_8)
            ));
        }
    }

    private static void registerSv(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sv")
                .then(CommandManager.literal("join")
                        .then(CommandManager.argument("index", IntegerArgumentType.integer(0, WyspiaExpress.SERVER_CONFIG.extraSpectatorsVoicechat()))
                                .executes(WyspiaExpressCommands::executeSvJoin)))
                .then(CommandManager.literal("leave")
                        .executes(WyspiaExpressCommands::executeSvLeave)));
    }

    private static int executeSvJoin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        if(!player.isCreative() && !player.isSpectator()) {
            context.getSource().sendError(Text.literal("Command reserved for spectators!."));
            return 0;
        }
        int index = IntegerArgumentType.getInteger(context, "index");


        VoicechatServerApi api = TrainVoicePlugin.SERVER_API;
        if (api == null) {
            context.getSource().sendError(Text.literal("Simple Voice Chat is not available right now."));
            return 0;
        }

        VoicechatConnection connection = api.getConnectionOf(player.getUuid());
        if (connection == null) {
            context.getSource().sendError(Text.literal("You are not connected to voice chat."));
            return 0;
        }

        Group targetGroup = getOrCreateGroup(index);
        if (targetGroup == null) {
            context.getSource().sendError(Text.literal("Could not create or resolve spectator voice group " + index + "."));
            return 0;
        }

        connection.setGroup(targetGroup);
        context.getSource().sendFeedback(
                () -> Text.literal("Joined spectator voice group " + index + "."),
                true
        );
        return 1;
    }

    private static int executeSvLeave(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        VoicechatServerApi api = TrainVoicePlugin.SERVER_API;
        if (api == null) {
            context.getSource().sendError(Text.literal("Simple Voice Chat is not available right now."));
            return 0;
        }

        VoicechatConnection connection = api.getConnectionOf(player.getUuid());
        if (connection == null) {
            context.getSource().sendError(Text.literal("You are not connected to voice chat."));
            return 0;
        }

        connection.setGroup(null);
        return 1;
    }

    @Nullable
    private static Group getOrCreateGroup(int index) {
        VoicechatServerApi api = TrainVoicePlugin.SERVER_API;
        if (api == null) {
            return null;
        }

        if (index == 0) {
            if (TrainVoicePlugin.GROUP != null) {
                return TrainVoicePlugin.GROUP;
            }

            TrainVoicePlugin.GROUP = api.groupBuilder()
                    .setHidden(true)
                    .setId(TrainVoicePlugin.GROUP_ID)
                    .setName("Train Spectators")
                    .setPersistent(true)
                    .setType(Group.Type.OPEN)
                    .build();

            return TrainVoicePlugin.GROUP;
        }

        Group cached = GROUPS.get(index);
        if (cached != null) {
            return cached;
        }

        UUID id = GROUP_IDS.get(index);
        if (id == null) {
            return null;
        }

        Group created = api.groupBuilder()
                .setHidden(true)
                .setId(id)
                .setName("Train Spectators " + index)
                .setPersistent(true)
                .setType(Group.Type.OPEN)
                .build();

        GROUPS.put(index, created);
        return created;
    }


    private static void registerSetRole(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("setRole")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(CommandManager.argument("player", net.minecraft.command.argument.EntityArgumentType.player())
                        .then(CommandManager.argument("role", org.agmas.harpymodloader.commands.argument.RoleArgumentType.skipVanilla())
                                .executes(WyspiaExpressCommands::executeSetRole))));
    }

    private static int executeSetRole(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = net.minecraft.command.argument.EntityArgumentType.getPlayer(context, "player");
        dev.doctor4t.wathe.api.Role role = org.agmas.harpymodloader.commands.argument.RoleArgumentType.getRole(context, "role");
        dev.doctor4t.wathe.cca.GameWorldComponent gameWorld = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(targetPlayer.getWorld());
        final net.minecraft.text.MutableText roleText = org.agmas.harpymodloader.Harpymodloader.getRoleName(role).withColor(role.color()).styled(style ->
                style.withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, net.minecraft.text.Text.literal(role.identifier().toString()))));

        if (!gameWorld.isRunning()) {
            context.getSource().sendFeedback(() -> net.minecraft.text.Text.translatable("commands.setrole.fail"), true);
            return 0;
        }

        targetPlayer.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.NIGHT_VISION);

        gameWorld.addRole(targetPlayer, role);
        org.agmas.harpymodloader.events.ModdedRoleAssigned.EVENT.invoker().assignModdedRole(targetPlayer, role);

        context.getSource().sendFeedback(() -> net.minecraft.text.Text.translatable("commands.setrole.success", targetPlayer.getDisplayName(), roleText), true);

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                targetPlayer,
                new dev.doctor4t.wathe.util.AnnounceWelcomePayload(
                        dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(org.agmas.harpymodloader.Harpymodloader.autogeneratedAnnouncements.get(role)),
                        gameWorld.getAllKillerTeamPlayers().size(),
                        0
                )
        );

        return 1;
    }
}