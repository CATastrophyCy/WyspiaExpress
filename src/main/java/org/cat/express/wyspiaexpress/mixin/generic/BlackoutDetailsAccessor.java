package org.cat.express.wyspiaexpress.mixin.generic;

import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldBlackoutComponent.BlackoutDetails.class)
public interface BlackoutDetailsAccessor {
    @Accessor("time")
    int wyspiaexpress$getTime();
}
