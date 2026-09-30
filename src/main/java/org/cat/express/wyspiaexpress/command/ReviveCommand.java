package org.cat.express.wyspiaexpress.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpressCommands;
import org.cat.express.wyspiaexpress.components.PlayerDepressedComponent;
import org.cat.express.wyspiaexpress.components.PlayerFreezeComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.voicechat.SpectatorVoiceService;
import org.jetbrains.annotations.Nullable;

public final class ReviveCommand {
    private ReviveCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("revive")
                .requires(WyspiaExpressCommands::isOperator)
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> execute(context, null))
                        .then(CommandManager.argument("coordinates", Vec3ArgumentType.vec3())
                                .executes(context -> execute(context,
                                        Vec3ArgumentType.getVec3(context, "coordinates"))))));
    }

    private static int execute(CommandContext<ServerCommandSource> context, @Nullable Vec3d coordinates)
            throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        if (coordinates != null && !World.isValid(BlockPos.ofFloored(coordinates))) {
            context.getSource().sendError(Text.translatable("commands.teleport.invalidPosition"));
            return 0;
        }

        // Clear the source world's death record before an optional cross-dimension teleport.
        ServerWorld previousWorld = player.getServerWorld();
        WorldComponent.KEY.get(previousWorld).removePlayerDead(player.getUuid());
        player.setCameraEntity(player);
        if (coordinates != null) {
            player.teleportTo(new TeleportTarget(context.getSource().getWorld(), coordinates, Vec3d.ZERO,
                    player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
        }
        player.changeGameMode(GameMode.ADVENTURE);
        if (player.getServerWorld() != previousWorld) {
            WorldComponent.KEY.get(player.getWorld()).removePlayerDead(player.getUuid());
        }
        // Expired survival timers must not immediately kill a freshly revived player.
        PlayerFreezeComponent.KEY.get(player).reset();
        PlayerDepressedComponent.KEY.get(player).reset();
        PlayerMoodComponent.KEY.get(player).reset();
        SpectatorVoiceService.leave(player.getUuid());

        context.getSource().sendFeedback(() -> Text.literal("Revived ").append(player.getDisplayName())
                .append(coordinates == null ? "." : " at " + coordinates.x + ", " + coordinates.y + ", " + coordinates.z + "."), true);
        return 1;
    }
}
