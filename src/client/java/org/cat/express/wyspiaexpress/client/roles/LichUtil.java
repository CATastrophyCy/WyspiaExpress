package org.cat.express.wyspiaexpress.client.roles;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.cat.express.wyspiaexpress.packets.LichReviveC2SPacket;

public class LichUtil {
    public static PlayerBodyEntity BODY = null;

    public static void sendLichPacket(MinecraftClient client){
        client.execute(() -> {
            if (MinecraftClient.getInstance().player == null || BODY == null) return;
            ClientPlayNetworking.send(new LichReviveC2SPacket(BODY.getUuid()));
        });
    }
}
