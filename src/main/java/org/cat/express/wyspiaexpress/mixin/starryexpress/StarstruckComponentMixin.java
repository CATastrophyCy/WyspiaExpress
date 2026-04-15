package org.cat.express.wyspiaexpress.mixin.starryexpress;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StarstruckComponent.class)
public abstract class StarstruckComponentMixin {

    @Final @Shadow private PlayerEntity player;;
    @Shadow public int ticks;
    @Shadow public abstract void sync();

    /**
     * @author CAT
     * @reason Made particle effects configurable
     */
    @Overwrite
    public void serverTick() {
        if (this.ticks > 0) {
            --this.ticks;

            if (WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.starstruckConfig.enablePersistentParticleEffects()
                    && this.player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.getServerWorld().spawnParticles(StarryExpress.STARSTRUCK_SPARKLE, serverPlayer.getX(), serverPlayer.getY() + 0.2, serverPlayer.getZ(),
                        this.player.getRandom().nextBetween(1, 2), 0.2, (double)0.0F, 0.2, (double)0.0F);
            }
            this.sync();
        }
    }
}