package org.cat.express.wyspiaexpress.gameplay;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerMoodService {
    private static final ThreadLocal<Boolean> ADMIN_CHANGE = ThreadLocal.withInitial(() -> false);

    private PlayerMoodService() {}

    public static boolean getAdminChange() {
        return ADMIN_CHANGE.get();
    }

    public static void setMoodAdmin(ServerPlayerEntity player, float amount) {
        boolean previous = ADMIN_CHANGE.get();
        ADMIN_CHANGE.set(true);
        try {
            PlayerMoodComponent.KEY.get(player).setMood(amount);
        } finally {
            ADMIN_CHANGE.set(previous);
        }
    }
}
