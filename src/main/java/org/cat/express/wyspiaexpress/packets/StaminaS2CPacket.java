package org.cat.express.wyspiaexpress.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;

/** One correction to the affected player's local sprint counter when it is refilled. */
public record StaminaS2CPacket(float ticks) implements CustomPayload {
    public static final Id<StaminaS2CPacket> ID = new Id<>(Identifier.of(WyspiaExpress.MOD_ID, "stamina"));
    public static final PacketCodec<RegistryByteBuf, StaminaS2CPacket> CODEC = PacketCodec.of(
            (packet, buf) -> buf.writeFloat(packet.ticks), buf -> new StaminaS2CPacket(buf.readFloat()));

    @Override public Id<? extends CustomPayload> getId() { return ID; }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void send(PlayerEntity player, float ticks) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new StaminaS2CPacket(ticks));
        }
    }
}
