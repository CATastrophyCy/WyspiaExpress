package org.cat.express.wyspiaexpress.client.roles;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.client.WyspiaexpressClient;
import org.cat.express.wyspiaexpress.components.roles.PlayerCultistComponent;
import org.cat.express.wyspiaexpress.packets.CultLeaderReviveC2SPacket;
import org.cat.express.wyspiaexpress.packets.LichReviveC2SPacket;
import org.jetbrains.annotations.NotNull;

public class TargetAbilityUtil {
    public static void sendLichPacket(MinecraftClient client){
        client.execute(() -> {
            if (MinecraftClient.getInstance().player == null || WyspiaexpressClient.TARGET_BODY == null) return;
            ClientPlayNetworking.send(new LichReviveC2SPacket(WyspiaexpressClient.TARGET_BODY.getUuid()));
        });
    }
    public static void sendCultLeaderPacket(MinecraftClient client){
        client.execute(() -> {
            if (MinecraftClient.getInstance().player == null || WyspiaexpressClient.TARGET_BODY == null) return;
            ClientPlayNetworking.send(new CultLeaderReviveC2SPacket(WyspiaexpressClient.TARGET_BODY.getUuid()));
        });
    }

    public static boolean isBodyConverted(@NotNull World world, @NotNull PlayerBodyEntity body) {
        PlayerEntity other = world.getPlayerByUuid(body.getPlayerUuid());
        if(other == null) return false;
        return isPlayerConverted(other);
    }
    public static boolean isPlayerConverted(@NotNull PlayerEntity player) {
        PlayerCultistComponent playerCultistComponent = PlayerCultistComponent.KEY.get(player);
        return GameFunctions.isPlayerSpectatingOrCreative(player) && playerCultistComponent.isConverted();
    }
}
