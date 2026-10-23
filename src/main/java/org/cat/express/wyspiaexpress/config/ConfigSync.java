package org.cat.express.wyspiaexpress.config;

import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;

/** Login cannot finish until the client acknowledges the validated, exact server snapshot. */
public final class ConfigSync {
    public static final Identifier CHANNEL = Identifier.of(WyspiaExpress.MOD_ID, "configuration");
    private ConfigSync() {}

    public static void initializeServer() {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            var bundle = GameConfigs.serverBundle();
            var buffer = PacketByteBufs.create();
            buffer.writeVarInt(ConfigBundle.PROTOCOL);
            buffer.writeString(bundle.digest(), 64);
            buffer.writeByteArray(bundle.compressed());
            sender.sendPacket(CHANNEL, buffer);
        });
        ServerLoginNetworking.registerGlobalReceiver(CHANNEL, (server, handler, understood, buffer, synchronizer, sender) -> {
            try {
                if (!understood) throw new IllegalArgumentException("Your client does not support WyspiaExpress configuration synchronization. Install the matching build.");
                if (buffer.readVarInt() != ConfigBundle.PROTOCOL) throw new IllegalArgumentException("Configuration protocol mismatch.");
                if (!buffer.readBoolean()) throw new IllegalArgumentException("Client rejected server settings: " + buffer.readString(256));
                String digest = buffer.readString(64);
                if (buffer.isReadable() || !digest.equals(GameConfigs.serverBundle().digest())) throw new IllegalArgumentException("Client did not acknowledge the exact server configuration.");
            } catch (RuntimeException e) {
                WyspiaExpress.LOGGER.warn("Rejected configuration handshake: {}", e.getMessage());
                handler.disconnect(Text.literal("WyspiaExpress configuration synchronization failed: " + e.getMessage()));
            }
        });
    }
}
