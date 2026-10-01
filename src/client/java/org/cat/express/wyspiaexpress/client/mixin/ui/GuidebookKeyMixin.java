package org.cat.express.wyspiaexpress.client.mixin.ui;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.cat.express.wyspiaexpress.client.WyspiaexpressClient;
import org.cat.express.wyspiaexpress.client.guidebook.WyspiaGuidebookScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class GuidebookKeyMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void wyspiaexpress$guidebookKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        var binding = WyspiaexpressClient.guidebookBind;
        // L is also vanilla's Advancements key. Wathe suppresses keys equal to that binding
        // (including matchesKey/wasPressed). Compare the configured key itself instead;
        // leave screens/text entry and F3 shortcuts alone.
        if (window == client.getWindow().getHandle() && action == GLFW.GLFW_PRESS
                && binding != null && KeyBindingHelper.getBoundKeyOf(binding).equals(InputUtil.fromKeyCode(key, scanCode))
                && client.world != null && client.player != null && client.currentScreen == null
                && !InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3)) {
            client.setScreen(new WyspiaGuidebookScreen());
            ci.cancel();
        }
    }
}
