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

public record ResetAttackCooldownS2CPacket() implements CustomPayload {
    public static final Id<ResetAttackCooldownS2CPacket> ID = new Id<>(Identifier.of(WyspiaExpress.MOD_ID, "reset_attack_cooldown"));
    public static final PacketCodec<RegistryByteBuf, ResetAttackCooldownS2CPacket> CODEC =
            PacketCodec.unit(new ResetAttackCooldownS2CPacket());

    @Override public Id<? extends CustomPayload> getId() { return ID; }

    public static void register() { PayloadTypeRegistry.playS2C().register(ID, CODEC); }

    public static void send(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new ResetAttackCooldownS2CPacket());
        }
    }
}
