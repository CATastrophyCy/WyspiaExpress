package org.cat.express.wyspiaexpress.client.mixin.items;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.client.items.ThrowablePreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class ThrowablePreviewMixin {
    @ModifyReturnValue(method = "getOverlay", at = @At("RETURN"))
    private static int wyspiaexpress$highlightBlastTargets(int original, LivingEntity entity, float whiteOverlayProgress) {
        if (entity instanceof PlayerEntity player && ThrowablePreview.shouldHighlight(player)) {
            // Vanilla's white overlay at U=5 gives a soft 25% tint. Preserve hurt/death and stronger overlays.
            return OverlayTexture.packUv(Math.max(original & 0xFFFF, 5), original >>> 16);
        }
        return original;
    }
}
