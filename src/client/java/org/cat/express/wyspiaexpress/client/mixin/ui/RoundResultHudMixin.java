package org.cat.express.wyspiaexpress.client.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import org.cat.express.wyspiaexpress.client.ui.RoundResultRenderer;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = RoundTextRenderer.class, priority = 100)
public abstract class RoundResultHudMixin {
    @Shadow private static int endTime;
    @Shadow private static int welcomeTime;
    @Shadow @Final private static int END_DURATION;

    @WrapMethod(method = "renderHud")
    private static void wyspiaexpress$renderResult(TextRenderer font, ClientPlayerEntity player, DrawContext context,
                                                  Operation<Void> original) {
        RoundResultComponent result = RoundResultComponent.KEY.get(player.getWorld());
        if (!result.ownsResult() || endTime <= 0 || welcomeTime > 0
                || GameWorldComponent.KEY.get(player.getWorld()).isRunning()) {
            original.call(font, player, context);
            return;
        }
        if (endTime < END_DURATION - GameConstants.FADE_TIME * 2) {
            RoundResultRenderer.render(context, font, result);
        }
    }
}
