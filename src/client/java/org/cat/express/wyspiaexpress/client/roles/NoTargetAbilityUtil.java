package org.cat.express.wyspiaexpress.client.roles;

import dev.doctor4t.wathe.api.Role;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.packets.NoTargetAbilityC2SPacket;

import java.util.Set;

public class NoTargetAbilityUtil{

    private static final Set<Role> VALID = Set.of(WyspiaExpressRoles.OUTLAW);
    public static boolean isValid(Role role){
        return VALID.contains(role);
    }
    public static void sendPacket(MinecraftClient client){
        client.execute(() -> {
            if (MinecraftClient.getInstance().player == null) return;
            ClientPlayNetworking.send(new NoTargetAbilityC2SPacket());
        });
    }
}
