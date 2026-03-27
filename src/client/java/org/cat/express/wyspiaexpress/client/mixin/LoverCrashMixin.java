package org.cat.express.wyspiaexpress.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(RoleNameRenderer.class)
public class LoverCrashMixin {

    // spectator hud lovers crash fix
    @WrapMethod(method = "renderHud")
    private static void wyspiaexpress$fixLoversNullPlayer(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, Operation<Void> original) {
        try {
            original.call(renderer, player, context, tickCounter);
        } catch (NullPointerException ignored) {
        }
    }
}