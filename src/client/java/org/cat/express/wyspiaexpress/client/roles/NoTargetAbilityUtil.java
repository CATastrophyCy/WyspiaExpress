package org.cat.express.wyspiaexpress.client.roles;

import dev.doctor4t.wathe.api.Role;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.cat.express.wyspiaexpress.packets.NoTargetAbilityC2SPacket;

import java.util.Set;

public class NoTargetAbilityUtil{

    private static final Set<Role> VALID = Set.of(WyspiaExpressRoles.OUTLAW, WyspiaExpressRoles.EDDIE_WAFFLES, WyspiaExpressRoles.LOVER_1, WyspiaExpressRoles.LOVER_2);
    public static boolean isValid(Role role){
        return VALID.contains(role) || role == Noellesroles.MORPHLING
                && MinecraftClient.getInstance().player != null
                && MorphlingPlayerComponent.KEY.get(MinecraftClient.getInstance().player).getMorphTicks() > 0;
    }
    public static void sendPacket(MinecraftClient client){
        client.execute(() -> {
            if (MinecraftClient.getInstance().player == null) return;
            ClientPlayNetworking.send(new NoTargetAbilityC2SPacket());
        });
    }
}
