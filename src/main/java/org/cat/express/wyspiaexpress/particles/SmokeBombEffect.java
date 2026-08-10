package org.cat.express.wyspiaexpress.particles;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.cat.express.wyspiaexpress.WyspiaExpress;

public class SmokeBombEffect extends RadialTimedEffect {

    public SmokeBombEffect(ServerWorld world, Vec3d center, double radius, int durationTicks) {
        super(  world,
                center,
                radius,
                durationTicks,
                ParticleTypes.LARGE_SMOKE,
                WyspiaExpress.ITEMS_CONFIG.itemConfig.smokeBombConfig.particles()
        );
    }

    @Override
    protected Vec3d randomOffsetInSphere(double radius) {
        return super.randomOffsetInSphere(radius).multiply(1.15, 1.0, 1.15);
    }

    @Override
    protected void spawnParticles() {
        if(age % 10 != 0) return;

        var progress = progress();
        if(progress <= 0.62)
            for (int i = 0; i < particlesPerTick; i++) {
                Vec3d pos = pickVisiblePosition();
                if (pos.equals(center)) continue;

                world.spawnParticles(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        pos.x, pos.y, pos.z,
                        1,
                        0.015, 0.03, 0.015,
                        0.0005
                );
            }

    }
}