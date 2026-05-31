package org.cat.express.wyspiaexpress.client.roles;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.cat.express.wyspiaexpress.client.WyspiaexpressClient;
import org.cat.express.wyspiaexpress.packets.CultLeaderReviveC2SPacket;
import org.cat.express.wyspiaexpress.packets.LichReviveC2SPacket;

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
}
