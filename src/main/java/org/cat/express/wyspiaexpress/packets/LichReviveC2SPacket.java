package org.cat.express.wyspiaexpress.packets;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import org.agmas.harpymodloader.Harpymodloader;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
public record LichReviveC2SPacket(UUID playerBody) implements CustomPayload {

    public static final Identifier REVIVE_PAYLOAD_ID = Identifier.of(WyspiaExpress.MOD_ID, "lich_revive");
    public static final Id<LichReviveC2SPacket> ID = new Id<>(REVIVE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, LichReviveC2SPacket> CODEC;

    public LichReviveC2SPacket(UUID playerBody) {this.playerBody = playerBody;}
    public @NotNull Id<? extends @NotNull CustomPayload> getId() {return ID;}

    public void write(PacketByteBuf buf) {buf.writeUuid(this.playerBody);}

    public static LichReviveC2SPacket read(PacketByteBuf buf) {return new LichReviveC2SPacket(buf.readUuid());}

    public UUID playerBody() {return this.playerBody;}

    static {
        CODEC = PacketCodec.of(LichReviveC2SPacket::write, LichReviveC2SPacket::read);
    }
    public static void register(){
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, LichReviveC2SPacket::handle);
    }

    public static void handle(@NotNull LichReviveC2SPacket payload, @NotNull ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        context.server().execute(() -> {

            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
            AbilityCooldownComponent abilityPlayerComponent = (AbilityCooldownComponent) AbilityCooldownComponent.KEY.get(player);
            LichReviveComponent reviveComponent = LichReviveComponent.KEY.get(player.getWorld());
            if (gameWorldComponent.isRole(player, WyspiaExpressRoles.LICH) && GameFunctions.isPlayerAliveAndSurvival(player)
                && reviveComponent.getAvailableRevives() > 0 && !abilityPlayerComponent.isCooldown()) {

                List<PlayerBodyEntity> playerBodyEntities = player.getWorld().getEntitiesByType(TypeFilter.equals(PlayerBodyEntity.class),
                        player.getBoundingBox().expand(10), (playerBodyEntity -> {
                    return playerBodyEntity.getUuid().equals(payload.playerBody());
                }));

                if (!playerBodyEntities.isEmpty()) {
                    // check if the selected body can be revived
                    PlayerBodyEntity body = playerBodyEntities.getFirst();
                    var revived = (ServerPlayerEntity) player.getServerWorld().getPlayerByUuid(body.getPlayerUuid());
                    if(revived == null || gameWorldComponent.getRole(revived) == WyspiaExpressRoles.LICH_GHOUL) return;

                    // activate cooldown
                    abilityPlayerComponent.setAbilityCooldown( Math.max(0, WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.cooldown()));
                    reviveComponent.decrementAvailableRevives();
                    // get random killer role
                    var roles = new ArrayList<>(List.of(WyspiaExpressRoles.LICH_GHOUL));
                    if (roles.isEmpty()) roles.add(WatheRoles.KILLER);
                    Collections.shuffle(roles);

                    // revive player and give them the role
                    var selectedRole = roles.getFirst();
                    TeleportTarget target = new TeleportTarget(player.getServerWorld(), body.getPos(), Vec3d.ZERO, body.getYaw(), body.getPitch(), TeleportTarget.NO_OP);
                    revived.teleportTo(target);
                    revived.changeGameMode(GameMode.ADVENTURE);
                    body.remove(Entity.RemovalReason.DISCARDED); // like it never existed

                    gameWorldComponent.addRole(revived, selectedRole);
                    PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(revived);
                    playerShopComponent.setBalance(WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.startingCoin());
                    ServerPlayNetworking.send(
                                revived,
                                new AnnounceWelcomePayload(
                                        RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(Harpymodloader.autogeneratedAnnouncements.get(selectedRole)),
                                        gameWorldComponent.getAllKillerTeamPlayers().size(),
                                        0
                                )
                        );
                    TrainVoicePlugin.resetPlayer(revived.getUuid());
                }
            }
        });
    }
}
