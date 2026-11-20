package org.cat.express.wyspiaexpress.mixin.items;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.util.GunShootPayload;
import dev.doctor4t.wathe.util.KnifeStabPayload;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.packet.items.BlowgunC2SPacket;
import org.BsXinQin.kinswathe.packet.items.HuntingKnifeC2SPacket;
import org.BsXinQin.kinswathe.packet.items.PanC2SPacket;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({KnifeStabPayload.Receiver.class, HuntingKnifeC2SPacket.Receiver.class,
        PanC2SPacket.Receiver.class, BlowgunC2SPacket.Receiver.class, GunShootPayload.Receiver.class})
public class WeaponReachMixin {
    @WrapOperation(method = "receive", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;distanceTo(Lnet/minecraft/entity/Entity;)F"))
    private float useSleepingHitbox(PlayerEntity target, Entity attacker, Operation<Float> original) {
        return GameUtil.isSleepingOnBed(target)
                ? GameUtil.distanceToSleepingHitbox(attacker, target) : original.call(target, attacker);
    }
}
