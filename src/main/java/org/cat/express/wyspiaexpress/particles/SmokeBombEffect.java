package org.cat.express.wyspiaexpress.particles;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class SmokeBombEffect extends RadialTimedEffect {

    public SmokeBombEffect(ServerWorld world, Vec3d center, double radius, int durationTicks) {
        super(  world,
                center,
                radius,
                durationTicks,
                ParticleTypes.LARGE_SMOKE,
                2000
        );
    }

    @Override
    protected void spawnParticles() {
        // Layer 1: large smoke
        if(age % 4 == 0) {
            world.spawnParticles(
                    ParticleTypes.LARGE_SMOKE,
                    center.x, center.y, center.z,
                    particlesPerTick,
                    radius * 0.7, 1.8, radius * 0.7,
                    0.001
            );

            // Layer 2: campfire smoke for thicker, taller cloud
            world.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    center.x, center.y, center.z,
                    particlesPerTick / 3,
                    radius * 0.4, 1.0, radius * 0.4,
                    0.003
            );

        }
    }
}