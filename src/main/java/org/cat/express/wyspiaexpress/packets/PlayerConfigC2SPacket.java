package org.cat.express.wyspiaexpress.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;

public record PlayerConfigC2SPacket(int option) implements CustomPayload {
    public static final Id<PlayerConfigC2SPacket> ID = new Id<>(Identifier.of(WyspiaExpress.MOD_ID, "player_config"));
    public static final PacketCodec<RegistryByteBuf, PlayerConfigC2SPacket> CODEC = PacketCodec.of(
            (packet, buf) -> buf.writeVarInt(packet.option),
            buf -> new PlayerConfigC2SPacket(buf.readVarInt()));

    @Override public Id<? extends CustomPayload> getId() { return ID; }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) -> context.server().execute(() -> {
            var player = context.player();
            var config = PlayerConfigComponent.KEY.get(player);
            var options = PlayerConfigComponent.Option.values();
            if (packet.option >= 0 && packet.option < options.length && PlayerConfigComponent.canEdit(player)) {
                // Toggle authoritative state so consecutive clicks cannot reuse a stale client value.
                var option = options[packet.option];
                config.set(option, !config.isEnabled(option));
            } else {
                config.sync();
            }
        }));
    }
}
