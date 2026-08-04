package org.cat.express.wyspiaexpress.particles;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.cat.express.wyspiaexpress.WyspiaExpress;

public abstract class RadialTimedEffect implements TimedParticleEffect {

    protected final Vec3d center;
    protected final int durationTicks;
    protected int age = 0;
    protected final double radius;
    protected final ParticleEffect particle;
    protected final int particlesPerTick;
    protected ServerWorld world;
    protected RadialTimedEffect(ServerWorld world, Vec3d center,
                                double radius,
                                int durationTicks,
                                ParticleEffect particle,
                                int particlesPerTick) {
        this.world = world;
        this.center = center;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.particle = particle;
        this.particlesPerTick = particlesPerTick;
    }

    @Override
    public boolean tick() {
        age++;
        if (age > durationTicks) return false;
        spawnParticles();
        return true;
    }
    @Override
    public boolean isIn(ServerWorld world) {
        return this.world == world;
    }
    protected void spawnParticles() {
        // Default: spawn in a filled sphere around center
        world.spawnParticles(
                particle,
                center.x, center.y, center.z,
                particlesPerTick,              // count
                radius * 0.7, radius * 0.4, radius * 0.7, // spread
                0.01                           // extra speed
        );
    }
}