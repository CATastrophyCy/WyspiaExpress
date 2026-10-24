package org.cat.express.wyspiaexpress.client.mixin.ui;

import dev.doctor4t.wathe.client.gui.LobbyPlayersRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LobbyPlayersRenderer.class)
public abstract class LobbyPlayersRendererMixin {
    // The second matrix push starts only the original multiline credits block.
    @Inject(method = "renderHud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", ordinal = 1), cancellable = true)
    private static void wyspiaexpress$compactCredit(TextRenderer font, ClientPlayerEntity player,
                                                   DrawContext context, CallbackInfo ci) {
        var matrices = context.getMatrices();
        matrices.push();
        // Keep the single line above the hotbar, including at larger GUI scales.
        matrices.translate(6, context.getScaledWindowHeight() - font.fontHeight * .75f - 37, 0);
        matrices.scale(.75f, .75f, 1);
        context.drawTextWithShadow(font, Text.translatable("gui.wyspiaexpress.lobby.credit"), 0, 0, 0xAAAAAA);
        matrices.pop();
        ci.cancel();
    }
}
