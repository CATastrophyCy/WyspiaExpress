package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.components.PlayerTaskComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerMoodComponent.class)
public abstract class PlayerMoodMixin implements PlayerTaskComponent.TaskCompletionAccess {
    @Shadow @Final private PlayerEntity player;
    @Shadow public abstract float getMood();
    @Shadow public abstract void setMood(float mood);
    @Unique private Float wyspiaexpress$completionMood;

    @WrapMethod(method = "serverTick")
    private void wyspiaexpress$tickTasks(Operation<Void> original) {
        PlayerTaskComponent.KEY.get(player).tickTasks((PlayerMoodComponent) (Object) this);
    }

    @WrapMethod(method = "clientTick")
    private void wyspiaexpress$tickClientMood(Operation<Void> original) {
        PlayerTaskComponent.KEY.get(player).drainMood((PlayerMoodComponent) (Object) this);
        original.call();
    }

    @WrapOperation(method = "clientTick", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/PlayerMoodComponent;setMood(F)V"))
    private void wyspiaexpress$skipNativeClientDrain(PlayerMoodComponent instance, float mood, Operation<Void> original) {
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void wyspiaexpress$resetTasks(CallbackInfo ci) {
        PlayerTaskComponent.KEY.get(player).reset();
    }

    @Override public void wyspiaexpress$restoreTaskMood(float restoration) {
        float before = getMood();
        Float previous = wyspiaexpress$completionMood;
        wyspiaexpress$completionMood = Math.clamp(before + restoration, 0, 1);
        try {
            // Other mods detect completion from this positive argument, including at zero restoration.
            setMood(before + GameConstants.MOOD_GAIN);
        } finally { wyspiaexpress$completionMood = previous; }
    }

    @ModifyExpressionValue(method = "setMood", at = @At(value = "INVOKE", target = "Ljava/lang/Math;clamp(FFF)F"))
    private float wyspiaexpress$storeConfiguredRestoration(float clamped) {
        return wyspiaexpress$completionMood == null ? clamped : wyspiaexpress$completionMood;
    }
}
