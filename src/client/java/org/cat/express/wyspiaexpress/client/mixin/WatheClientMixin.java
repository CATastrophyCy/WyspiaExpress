package org.cat.express.wyspiaexpress.client.mixin;

import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WatheClient.class)
public abstract class WatheClientMixin {
    @Inject(method = "lambda$onInitializeClient$13", at = @At("TAIL"))
    private static void forceDarkness(ClientWorld clientWorld, CallbackInfo ci) {
        float lightLevel = -0.04f;
        if (WatheClient.isInstinctEnabled()) {
            if (WatheClient.isPlayerSpectatingOrCreative()) {
                lightLevel = 0.5f;
            } else if (WatheClient.isPlayerAliveAndInSurvival() && WatheClient.isKiller()
                    && !GameUtil.isUnderSmokeEffect(MinecraftClient.getInstance().player)) {
                lightLevel = MathHelper.lerp((float) WyspiaExpress.RULES_CONFIG.killerInstinctBrightness(), -0.04f, 0.5f);
            }
        }
        WatheClient.prevInstinctLightLevel = lightLevel;
        WatheClient.instinctLightLevel = lightLevel;
    }
}
