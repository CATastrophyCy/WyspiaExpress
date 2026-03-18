package org.cat.express.wyspiaexpress.packets;
import de.maxhenkel.voicechat.api.ServerPlayer;
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
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.roles.ReanimatorReviveComponent;
import org.jetbrains.annotations.NotNull;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
public record ReanimatorReviveC2SPacket (UUID playerBody) implements CustomPayload {

    public static final Identifier REVIVE_PAYLOAD_ID = Identifier.of(WyspiaExpress.MOD_ID, "reanimator_revive");
    public static final Id<ReanimatorReviveC2SPacket> ID = new Id<>(REVIVE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, ReanimatorReviveC2SPacket> CODEC;

    public ReanimatorReviveC2SPacket(UUID playerBody) {this.playerBody = playerBody;}
    public @NotNull Id<? extends @NotNull CustomPayload> getId() {return ID;}

    public void write(PacketByteBuf buf) {buf.writeUuid(this.playerBody);}

    public static ReanimatorReviveC2SPacket read(PacketByteBuf buf) {return new ReanimatorReviveC2SPacket(buf.readUuid());}

    public UUID playerBody() {return this.playerBody;}

    static {
        CODEC = PacketCodec.of(ReanimatorReviveC2SPacket::write, ReanimatorReviveC2SPacket::read);
    }
    public static void register(){
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, ReanimatorReviveC2SPacket::handle);
    }

    public static void handle(@NotNull ReanimatorReviveC2SPacket payload,@NotNull ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        context.server().execute(() -> {

            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
            AbilityCooldownComponent abilityPlayerComponent = (AbilityCooldownComponent) AbilityCooldownComponent.KEY.get(player);
            ReanimatorReviveComponent reviveComponent = ReanimatorReviveComponent.KEY.get(player.getWorld());
            if (gameWorldComponent.isRole(player, WyspiaExpressRoles.REANIMATOR) && GameFunctions.isPlayerAliveAndSurvival(player)
                && reviveComponent.getAvailableRevives() > 0 && !abilityPlayerComponent.isCooldown()) {

                List<PlayerBodyEntity> playerBodyEntities = player.getWorld().getEntitiesByType(TypeFilter.equals(PlayerBodyEntity.class),
                        player.getBoundingBox().expand(10), (playerBodyEntity -> {
                    return playerBodyEntity.getUuid().equals(payload.playerBody());
                }));

                if (!playerBodyEntities.isEmpty()) {
                    // check if the selected body can be revived
                    PlayerBodyEntity body = playerBodyEntities.getFirst();
                    var revived = (ServerPlayerEntity) player.getServerWorld().getPlayerByUuid(body.getPlayerUuid());
                    if(revived == null || gameWorldComponent.getRole(revived) == WyspiaExpressRoles.REANIMATOR_GHOUL) return;

                    // activate cooldown
                    abilityPlayerComponent.setAbilityCooldown( Math.max(0, WyspiaExpress.ROLES_CONFIG.roleConfig.reanimatorConfig.cooldown()));
                    reviveComponent.decrementAvailableRevives();
                    // get random killer role
                    var roles = new ArrayList<>(List.of(WyspiaExpressRoles.REANIMATOR_GHOUL));
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
                    playerShopComponent.setBalance(WyspiaExpress.ROLES_CONFIG.roleConfig.reanimatorConfig.startingCoin());
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
