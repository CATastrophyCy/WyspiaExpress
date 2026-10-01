package org.cat.express.wyspiaexpress.client.mixin.ui;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.aussiebox.starexpress.client.gui.widget.GuidebookButtonWidget;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class LegacyGuidebookButtonMixin {
    @Inject(method = "addDrawableChild", at = @At("HEAD"), cancellable = true)
    private void wyspiaexpress$suppressLegacyGuide(Element element, CallbackInfoReturnable<Element> cir) {
        if (element instanceof GuidebookButtonWidget button && WyspiaExpress.SERVER_CONFIG.suppressOldGuidebook()) {
            button.visible = false;
            button.active = false;
            // Do not register a drawable, click target, focus target or narrated element.
            cir.setReturnValue(element);
        }
    }
}
