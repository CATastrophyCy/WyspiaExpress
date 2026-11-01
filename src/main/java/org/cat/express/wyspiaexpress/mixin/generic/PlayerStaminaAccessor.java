package org.cat.express.wyspiaexpress.mixin.generic;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PlayerEntity.class, priority = 500)
public interface PlayerStaminaAccessor {
    @Accessor(value = "sprintingTicks", remap = false)
    float wyspiaexpress$getStamina();

    @Accessor(value = "sprintingTicks", remap = false)
    void wyspiaexpress$setStamina(float ticks);
}
