package org.cat.express.wyspiaexpress.client.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.components.PlayerTaskComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MoodRenderer.class)
public abstract class MoodRendererMixin {
    @Unique private static float wyspiaexpress$timerAlpha = 0.075f;
    @Unique private static float wyspiaexpress$timerOffset = -1;

    @ModifyExpressionValue(method = "renderHud", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    private static boolean wyspiaexpress$keepDrainingMoodVisible(boolean empty,
                                                               @Local(argsOnly = true) PlayerEntity player) {
        return empty && !(WyspiaExpress.SERVER_CONFIG.moodTasks.passiveMoodDrain()
                && PlayerMoodComponent.KEY.get(player).getMood() < 1);
    }

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
    private static int wyspiaexpress$hideCompletedTask(DrawContext context, TextRenderer renderer, Text text,
                                                     int x, int y, int color, Operation<Integer> original,
                                                     PlayerEntity player) {
        // Finished task rows normally fade out here; the countdown occupies the same position.
        if (PlayerTaskComponent.KEY.get(player).isWaiting() && PlayerMoodComponent.KEY.get(player).tasks.isEmpty()) return 0;
        return original.call(context, renderer, text, x, y, color);
    }

    @Inject(method = "renderHud", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;getRole(Lnet/minecraft/entity/player/PlayerEntity;)Ldev/doctor4t/wathe/api/Role;"))
    private static void wyspiaexpress$renderNextTask(PlayerEntity player, TextRenderer renderer,
                                                    DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!PlayerTaskComponent.KEY.get(player).isWaiting() || !PlayerMoodComponent.KEY.get(player).tasks.isEmpty()) {
            wyspiaexpress$timerAlpha = 0.075f;
            wyspiaexpress$timerOffset = -1;
            return;
        }
        if (WyspiaExpress.SERVER_CONFIG.moodTasks.passiveMoodDrain()
                && PlayerMoodComponent.KEY.get(player).getMood() < 1) {
            // An idle client may never have rendered a task from which to obtain a bar width.
            MoodRenderer.moodTextWidth = Math.max(MoodRenderer.moodTextWidth, 80);
        }
        // Match Wathe's TaskRenderer fade, slide, and text position.
        float delta = tickCounter.getTickDelta(true);
        wyspiaexpress$timerAlpha = MathHelper.lerp(delta / 16, wyspiaexpress$timerAlpha, 1);
        wyspiaexpress$timerOffset = MathHelper.lerp(delta / 32, wyspiaexpress$timerOffset, 0);
        context.getMatrices().push();
        context.getMatrices().translate(0, 10 * wyspiaexpress$timerOffset, 0);
        context.drawTextWithShadow(renderer, PlayerTaskComponent.KEY.get(player).hudText(), 22, 6,
                0xFFFFFF | (int) (wyspiaexpress$timerAlpha * 255) << 24);
        context.getMatrices().pop();
    }
}
