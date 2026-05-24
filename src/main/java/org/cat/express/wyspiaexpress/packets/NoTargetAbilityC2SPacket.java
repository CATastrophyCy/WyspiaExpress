package org.cat.express.wyspiaexpress.packets;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.PlayerHearDeadComponent;
import org.jetbrains.annotations.NotNull;

public record NoTargetAbilityC2SPacket() implements CustomPayload {

    public static final Identifier ABILITY_PAYLOAD_ID = Identifier.of(KinsWathe.MOD_ID, "ability_no_target");
    public static final Id<NoTargetAbilityC2SPacket> ID = new Id<>(ABILITY_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, NoTargetAbilityC2SPacket> CODEC;
    static {CODEC = PacketCodec.of(NoTargetAbilityC2SPacket::write, NoTargetAbilityC2SPacket::read);}
    public void write(PacketByteBuf buf) {}
    public static NoTargetAbilityC2SPacket read(PacketByteBuf buf) {
        return new NoTargetAbilityC2SPacket();
    }
    public @NotNull Id<? extends @NotNull CustomPayload> getId() {return ID;}
    public static void register(){
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, NoTargetAbilityC2SPacket::handle);
    }
    public static void handle(@NotNull NoTargetAbilityC2SPacket payload, @NotNull ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        context.server().execute(() -> {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
            AbilityCooldownComponent abilityPlayerComponent = AbilityCooldownComponent.KEY.get(player);
            if (GameFunctions.isPlayerAliveAndSurvival(player) && !abilityPlayerComponent.isCooldown()){

                if(gameWorldComponent.isRole(player, WyspiaExpressRoles.OUTLAW))
                    handleOutlaw(player, gameWorldComponent, abilityPlayerComponent);
                if(gameWorldComponent.isRole(player, WyspiaExpressRoles.EDDIE_WAFFLES))
                    handleEddieWaffles(player, gameWorldComponent, abilityPlayerComponent);
            }
        });
    }
    public static void handleOutlaw(@NotNull ServerPlayerEntity player, GameWorldComponent gameWorldComponent, AbilityCooldownComponent abilityPlayerComponent) {
        PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
        if (playerShop.balance < WyspiaExpress.ROLES_CONFIG.roleConfig.outlawConfig.cost()) return;
        playerShop.addToBalance(-WyspiaExpress.ROLES_CONFIG.roleConfig.outlawConfig.cost());
        PlayerEffectComponent.KEY.get(player).setStunTicks(WyspiaExpress.ROLES_CONFIG.roleConfig.outlawConfig.selfStunDuration());
        player.getItemCooldownManager().set(WyspiaExpressItems.OUTLAW_REVOLVER, 0);
        player.getItemCooldownManager().set(WyspiaExpressItems.FAKE_REVOLVER, 0);
        player.getItemCooldownManager().set(WatheItems.REVOLVER, 0);
        player.getItemCooldownManager().set(WatheItems.DERRINGER, 0);
        player.playSoundToPlayer(SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        abilityPlayerComponent.setAbilityCooldown(WyspiaExpress.ROLES_CONFIG.roleConfig.outlawConfig.cooldown());

    }
    public static void handleEddieWaffles(@NotNull ServerPlayerEntity player, GameWorldComponent gameWorldComponent, AbilityCooldownComponent abilityPlayerComponent){
        PlayerHearDeadComponent component = PlayerHearDeadComponent.KEY.get(player);
        component.toggle();
        abilityPlayerComponent.setAbilityCooldown(WyspiaExpress.ROLES_CONFIG.roleConfig.eddieWafflesConfig.cooldown());
    }
}