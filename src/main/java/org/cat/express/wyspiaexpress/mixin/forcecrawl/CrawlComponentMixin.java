package org.cat.express.wyspiaexpress.mixin.forcecrawl;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.fexl.forcecrawl.crawl.CrawlComponent", remap = false)
public abstract class CrawlComponentMixin {
    @Shadow @Final private PlayerEntity owner;
    @Shadow public abstract void reset();

    @Inject(method = "getCrawlPhase", at = @At("RETURN"), cancellable = true)
    private void stopCrawlingInBed(CallbackInfoReturnable<Enum<?>> cir) {
        Enum<?> phase = cir.getReturnValue();
        if (!owner.isSleeping() || phase.ordinal() == 0) return;
        if (!owner.getWorld().isClient) reset();
        cir.setReturnValue(phase.getDeclaringClass().getEnumConstants()[0]); // IDLE
    }
}
