package org.cat.express.wyspiaexpress.client.mixin;


import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WatheClient.class)
public abstract class WatheClientMixin {



    @Inject(method = "lambda$onInitializeClient$13", at = @At("TAIL"))
    private static void forceDarkness(ClientWorld clientWorld, CallbackInfo ci) {
        WatheClient.prevInstinctLightLevel = -0.04f;
        WatheClient.instinctLightLevel = -0.04f;
        if(WatheClient.isPlayerSpectatingOrCreative() && WatheClient.isInstinctEnabled()){
            WatheClient.prevInstinctLightLevel = 0.5f;
            WatheClient.instinctLightLevel = 0.5f;
        }
    }

}
