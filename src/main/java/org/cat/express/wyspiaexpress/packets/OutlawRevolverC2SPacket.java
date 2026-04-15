package org.cat.express.wyspiaexpress.packets;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.ShootMuzzleS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
public record OutlawRevolverC2SPacket(int target) implements CustomPayload {

    public static final Identifier OUTLAW_REVOLVER_PLAYLOAD_ID = Identifier.of(WyspiaExpress.MOD_ID, "outlaw_revolver");
    public static final Id<OutlawRevolverC2SPacket> ID = new Id<>(OUTLAW_REVOLVER_PLAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, OutlawRevolverC2SPacket> CODEC;
    public @NotNull Id<? extends @NotNull CustomPayload> getId() {return ID;}

    public void write(PacketByteBuf buf) {buf.writeInt(this.target);}

    public static OutlawRevolverC2SPacket read(PacketByteBuf buf) {return new OutlawRevolverC2SPacket(buf.readInt());}

    public int target() {return this.target;}

    static {
        CODEC = PacketCodec.of(OutlawRevolverC2SPacket::write, OutlawRevolverC2SPacket::read);
    }
    public static void register(){
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, OutlawRevolverC2SPacket::handle);
    }
    public static void handle(@NotNull OutlawRevolverC2SPacket payload, @NotNull ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        context.server().execute(() -> {

            ItemStack mainHandStack = player.getMainHandStack();
            if (!mainHandStack.isOf(WyspiaExpressItems.OUTLAW_REVOLVER)) return;
            if (player.getItemCooldownManager().isCoolingDown(mainHandStack.getItem())) return;

            player.getWorld().playSound(null, player.getX(), player.getEyeY(), player.getZ(), WatheSounds.ITEM_REVOLVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1f + player.getRandom().nextFloat() * .1f - .05f);


            if (!(player.getServerWorld().getEntityById(payload.target()) instanceof @NotNull PlayerEntity target) || target.distanceTo(player) > 15.0F) {
                setCooldown(player, WyspiaExpress.ITEMS_CONFIG.itemConfig.outlawRevolverConfig.missCooldown());;
            }
            else {
                GameFunctions.killPlayer(target, true, player, GameConstants.DeathReasons.GUN);
                setCooldown(player, WyspiaExpress.ITEMS_CONFIG.itemConfig.outlawRevolverConfig.cooldown());
            }

            player.getWorld().playSound(null, player.getX(), player.getEyeY(), player.getZ(), WatheSounds.ITEM_REVOLVER_SHOOT, SoundCategory.PLAYERS, 5f, 1f + player.getRandom().nextFloat() * .1f - .05f);
            for (ServerPlayerEntity tracking : PlayerLookup.tracking(player))
                ServerPlayNetworking.send(tracking, new ShootMuzzleS2CPayload(player.getUuidAsString()));
            ServerPlayNetworking.send(player, new ShootMuzzleS2CPayload(player.getUuidAsString()));

        });
    }
    public static void setCooldown(ServerPlayerEntity player, int cooldown) {
        if (!player.isCreative())
            WyspiaExpressItems.setItemCooldown(player, WyspiaExpressItems.OUTLAW_REVOLVER, null, cooldown);
    }
}