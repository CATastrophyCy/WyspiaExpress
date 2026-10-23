package org.cat.express.wyspiaexpress.client;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.config.*;
import java.util.concurrent.CompletableFuture;

public final class ClientConfigSync {
    private static String lastMode;
    private ClientConfigSync() {}

    public static void initialize() {
        GameConfigs.initializeClientFiles();
        // The play disconnect can precede integrated-server shutdown. Restore client tables after it stops.
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server ->
                MinecraftClient.getInstance().execute(ClientConfigSync::refresh));
        GameConfigs.setClientThread(() -> MinecraftClient.getInstance().isOnThread());
        GameConfigs.setClientMode(ClientConfigSync::mode);
        ClientLoginConnectionEvents.INIT.register((handler, client) -> {
            GameConfigs.clearClient(); lastMode = null;
        });
        ClientLoginNetworking.registerGlobalReceiver(ConfigSync.CHANNEL, (client, handler, buffer, listenerAdder) -> {
            var reply = PacketByteBufs.create();
            try {
                if (buffer.readVarInt() != ConfigBundle.PROTOCOL) throw new IllegalArgumentException("Configuration protocol mismatch");
                String digest = buffer.readString(64);
                byte[] compressed = buffer.readByteArray(ConfigBundle.MAX_COMPRESSED_BYTES);
                if (buffer.isReadable()) throw new IllegalArgumentException("Unexpected trailing configuration data");
                var bundle = ConfigBundle.decode(compressed, digest, GameConfigs.schema());
                GameConfigs.installClient(bundle);
                reply.writeVarInt(ConfigBundle.PROTOCOL);
                reply.writeBoolean(true);
                reply.writeString(bundle.digest(), 64);
            } catch (RuntimeException e) {
                GameConfigs.clearClient();
                WyspiaExpress.LOGGER.error("Could not apply server configuration", e);
                reply.writeVarInt(ConfigBundle.PROTOCOL);
                reply.writeBoolean(false);
                String reason = String.valueOf(e.getMessage());
                reply.writeString(reason.substring(0, Math.min(reason.length(), 256)), 256);
            }
            return CompletableFuture.completedFuture(reply);
        });
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> clear());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!GameConfigs.hasClientSnapshot()) {
                handler.getConnection().disconnect(Text.literal("This server did not synchronize WyspiaExpress configuration. Install matching client and server builds."));
                return;
            }
            refresh();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (GameConfigs.hasClientSnapshot() && !mode().equals(lastMode)) refresh();
        });
    }

    private static String mode() {
        var client = MinecraftClient.getInstance();
        return client.world == null ? "shared" : GameWorldComponent.KEY.get(client.world).getGameMode().identifier.toString();
    }
    private static void refresh() { lastMode = mode(); GameConfigs.refreshDerivedValues(); }
    private static void clear() { GameConfigs.clearClient(); lastMode = null; GameConfigs.refreshDerivedValues(); }
}
