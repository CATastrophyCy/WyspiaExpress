package org.cat.express.wyspiaexpress.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.util.ItemUtil;

import java.util.List;

public record TrashC2SPacket(List<Selection> selections) implements CustomPayload {
    public record Selection(int slot, ItemStack stack) {
        private static final PacketCodec<RegistryByteBuf, Selection> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Selection::slot, ItemStack.PACKET_CODEC, Selection::stack, Selection::new);
    }

    public static final Id<TrashC2SPacket> ID = new Id<>(Identifier.of(WyspiaExpress.MOD_ID, "trash"));
    public static final PacketCodec<RegistryByteBuf, TrashC2SPacket> CODEC = Selection.CODEC
            .collect(PacketCodecs.toList(36)).xmap(TrashC2SPacket::new, TrashC2SPacket::selections);

    @Override public Id<? extends CustomPayload> getId() { return ID; }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) ->
                context.server().execute(() -> handle(packet, context.player())));
    }

    public static void handle(TrashC2SPacket packet, ServerPlayerEntity player) {
        if (!ItemUtil.canUseInventoryTrash(player)) return;
        var inventory = player.getInventory();
        for (Selection selection : packet.selections) {
            if (selection.slot < 0 || selection.slot >= inventory.main.size()
                    || !ItemUtil.canTrash(inventory.getStack(selection.slot))
                    || !ItemStack.areEqual(selection.stack, inventory.getStack(selection.slot))) {
                player.sendMessage(Text.translatable("gui.wyspiaexpress.trash.changed"), true);
                player.currentScreenHandler.syncState();
                return;
            }
        }
        for (Selection selection : packet.selections) inventory.setStack(selection.slot, ItemStack.EMPTY);
        inventory.markDirty();
        player.currentScreenHandler.sendContentUpdates();
    }
}
