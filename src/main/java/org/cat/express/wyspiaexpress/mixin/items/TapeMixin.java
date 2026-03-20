package org.cat.express.wyspiaexpress.mixin.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.aussiebox.starexpress.item.custom.TapeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TapeItem.class)
public abstract class TapeMixin {
    @Redirect(
            method = "useOnEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDD" +
                            "Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"
            )
    )
    private void starexpress$tapeOnlyUserHears(
            World world,
            PlayerEntity player,
            double x, double y, double z,
            SoundEvent sound,
            SoundCategory category,
            float volume, float pitch
    ) {
        player.playSoundToPlayer(sound,category ,volume, pitch);
    }
}
