package org.cat.express.wyspiaexpress.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.modifiers.GuesserAbility;
import java.util.UUID;

public record GuessC2SPacket(UUID target, String guess) implements CustomPayload {
    public static final Id<GuessC2SPacket> ID = new Id<>(Identifier.of(WyspiaExpress.MOD_ID, "guess"));
    public static final PacketCodec<RegistryByteBuf, GuessC2SPacket> CODEC = PacketCodec.of(
            (packet, buf) -> { buf.writeUuid(packet.target); buf.writeString(packet.guess, 128); },
            buf -> new GuessC2SPacket(buf.readUuid(), buf.readString(128)));
    @Override public Id<? extends CustomPayload> getId() { return ID; }
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) -> context.server().execute(() ->
                GuesserAbility.guess(context.player(), packet.target, packet.guess)));
    }
}
