package org.cat.express.wyspiaexpress.packets;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressGameFunctions;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.PlayerBodyEntityComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.components.roles.PlayerCultistComponent;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record CultLeaderReviveC2SPacket(UUID playerBody) implements CustomPayload {

    public static final Identifier REVIVE_PAYLOAD_ID = Identifier.of(WyspiaExpress.MOD_ID, "cult_revive");
    public static final Id<CultLeaderReviveC2SPacket> ID = new Id<>(REVIVE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, CultLeaderReviveC2SPacket> CODEC;

    public CultLeaderReviveC2SPacket(UUID playerBody) {this.playerBody = playerBody;}
    public @NotNull Id<? extends @NotNull CustomPayload> getId() {return ID;}

    public void write(PacketByteBuf buf) {buf.writeUuid(this.playerBody);}

    public static CultLeaderReviveC2SPacket read(PacketByteBuf buf) {return new CultLeaderReviveC2SPacket(buf.readUuid());}

    public UUID playerBody() {return this.playerBody;}

    static {
        CODEC = PacketCodec.of(CultLeaderReviveC2SPacket::write, CultLeaderReviveC2SPacket::read);
    }
    public static void register(){
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, CultLeaderReviveC2SPacket::handle);
    }

    public static void handle(@NotNull CultLeaderReviveC2SPacket payload, @NotNull ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        context.server().execute(() -> {
            ServerWorld world = player.getServerWorld();
            if(world == null) return;
            
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(world);
            AbilityCooldownComponent abilityPlayerComponent = AbilityCooldownComponent.KEY.get(player);
            if (gameWorldComponent.isRole(player, WyspiaExpressRoles.CULT_LEADER) && GameFunctions.isPlayerAliveAndSurvival(player)
                && !abilityPlayerComponent.isCooldown()) {

                List<PlayerBodyEntity> playerBodyEntities = world.getEntitiesByType(TypeFilter.equals(PlayerBodyEntity.class),
                        player.getBoundingBox().expand(10), (playerBodyEntity -> playerBodyEntity.getUuid().equals(payload.playerBody())));
                if (!playerBodyEntities.isEmpty()) {
                    // check if the selected body can be revived
                    PlayerBodyEntity body = playerBodyEntities.getFirst();
                    if(!PlayerBodyEntityComponent.KEY.get(body).isConverted())return;

                    var revived = (ServerPlayerEntity) world.getPlayerByUuid(body.getPlayerUuid());
                    var world_component = WorldComponent.KEY.get(world);
                    if(revived == null
                            || gameWorldComponent.getRole(revived) == WyspiaExpressRoles.CULTIST
                            || !GameFunctions.isPlayerSpectatingOrCreative(revived)
                            || !world_component.isPlayerDead(revived.getUuid())) return;
                    PlayerCultistComponent playerCultistComponent = PlayerCultistComponent.KEY.get(revived);
                    if(!playerCultistComponent.isConverted()) return;

                    // activate cooldown
                    abilityPlayerComponent.setAbilityCooldown( Math.max(0, WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.cooldown()));

                    // teleport revived to player and discard the body
                    TeleportTarget target = new TeleportTarget(world, player.getPos(), Vec3d.ZERO, body.getYaw(), body.getPitch(), TeleportTarget.NO_OP);
                    revived.teleportTo(target);
                    body.remove(Entity.RemovalReason.DISCARDED);

                    WyspiaExpressGameFunctions.revivedPlayer(gameWorldComponent, world_component, revived, WyspiaExpressRoles.CULTIST,
                            List.of(WatheItems.KEY));
                    ShopUtil.setCoin(revived, WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.startingCoin());

                    WyspiaExpressGameFunctions.sendRevivedMessage(world, gameWorldComponent, player, revived);
                }
            }
        });
    }
}
