package org.cat.express.wyspiaexpress.packets;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.index.WatheSounds;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.cat.express.wyspiaexpress.util.ItemUtil;
import org.jetbrains.annotations.NotNull;

public  record RitualDaggerC2SPacket (int target) implements CustomPayload {

    public static final Identifier RITUAL_DAGGER_PLAYLOAD_ID = Identifier.of(WyspiaExpress.MOD_ID, "ritual_dagger");
    public static final CustomPayload.Id<RitualDaggerC2SPacket> ID = new CustomPayload.Id<>(RITUAL_DAGGER_PLAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, RitualDaggerC2SPacket> CODEC;
    public @NotNull CustomPayload.Id<? extends @NotNull CustomPayload> getId() {return ID;}

    public void write(PacketByteBuf buf) {buf.writeInt(this.target);}

    public static RitualDaggerC2SPacket read(PacketByteBuf buf) {return new RitualDaggerC2SPacket(buf.readInt());}

    public int target() {return this.target;}

    static {
        CODEC = PacketCodec.of(RitualDaggerC2SPacket::write, RitualDaggerC2SPacket::read);
    }
    public static void register(){
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, RitualDaggerC2SPacket::handle);
    }
    public static void handle(@NotNull RitualDaggerC2SPacket payload, @NotNull ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        context.server().execute(() -> {
            ServerWorld world = player.getServerWorld();
            if(world == null) return;

            ItemStack mainHandStack = player.getMainHandStack();
            if (!mainHandStack.isOf(WyspiaExpressItems.RITUAL_DAGGER)) return;
            if (player.getItemCooldownManager().isCoolingDown(mainHandStack.getItem())) return;
            if (!(world.getEntityById(payload.target()) instanceof @NotNull ServerPlayerEntity target) || target.distanceTo(player) > 3.0F) return;

            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(target.getWorld());

            PlayerEffectComponent playerEffectComponent = PlayerEffectComponent.KEY.get(target);

            target.playSound(WatheSounds.ITEM_KNIFE_STAB, 1.0f, 1.0f);

            ItemUtil.setItemCooldown(player, WyspiaExpressItems.RITUAL_DAGGER, null);
            playerEffectComponent.setStunTicks(WyspiaExpress.ITEMS_CONFIG.itemConfig.ritualDaggerConfig.duration());

            // Psycho blocks conversion; the dagger still applies its normal stun and cooldown.
            if (gameWorldComponent.isRunning() && !gameWorldComponent.isRole(target, WyspiaExpressRoles.CULTIST)
                    && PlayerPsychoComponent.KEY.get(target).getPsychoTicks() <= 0) {
                GameUtil.convertToCultist(player, target);
            }

            player.swingHand(Hand.MAIN_HAND);

        });
    }
}
