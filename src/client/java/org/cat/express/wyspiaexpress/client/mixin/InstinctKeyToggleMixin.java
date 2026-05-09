package org.cat.express.wyspiaexpress.client.mixin;

import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public abstract class InstinctKeyToggleMixin {

    @Shadow private boolean pressed;

    @Unique
    private static boolean toggle = false;

    @Unique
    private boolean wasDown = false;

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    private void wyspiaexpress$onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == WatheClient.instinctKeybind) {
            boolean isDown = this.pressed;

            if (isDown && !wasDown) {
                toggle = !toggle;
            }
            wasDown = isDown;
            cir.setReturnValue(toggle);
        }
    }
}