package org.cat.express.wyspiaexpress.mixin.roles;

import dev.doctor4t.wathe.game.GameConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import pro.fazeclan.river.stupid_express.role.thief.packet.ThiefTakeItemC2SPacket;

@Mixin(ThiefTakeItemC2SPacket.class)
public abstract class ThiefPacketC2SMixin {
    @ModifyConstant(method = "handleThiefTakeItem", constant = @Constant(intValue = 900))
    private static int modifyFailedCooldown(int original) {
        return GameConstants.getInTicks(0, 30); // e.g., 400
    }

    @ModifyConstant(method = "handleThiefTakeItem", constant = @Constant(intValue = 1800))
    private static int modifySuccessCooldown(int original) {
        return GameConstants.getInTicks(0, 60); // e.g., 1200
    }

    @ModifyConstant(method = "validateDistance", constant = @Constant(doubleValue = 1.0F))
    private static double modifyClientDistance(double original) {
        return 1.5;
    }

    @ModifyConstant(method = "validateDistance", constant = @Constant(doubleValue = 1.2))
    private static double modifyServerDistance(double original) {
        return 1.5; // e.g., 3.0
    }
}

