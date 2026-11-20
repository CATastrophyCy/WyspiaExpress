package org.cat.express.wyspiaexpress.mixin.items;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.util.KnifeStabPayload;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KnifeStabPayload.Receiver.class)
public abstract class KnifeReachMixin {
    @WrapOperation(method = "receive(Ldev/doctor4t/wathe/util/KnifeStabPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;distanceTo(Lnet/minecraft/entity/Entity;)F"))
    private float useSleepingHitbox(PlayerEntity target, Entity attacker, Operation<Float> original) {
        return GameUtil.isSleepingOnBed(target)
                ? GameUtil.distanceToSleepingHitbox(attacker, target) : original.call(target, attacker);
    }
}
