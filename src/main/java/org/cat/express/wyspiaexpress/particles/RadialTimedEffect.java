package org.cat.express.wyspiaexpress.particles;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.cat.express.wyspiaexpress.WyspiaExpress;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class RadialTimedEffect implements TimedParticleEffect {

    protected final Vec3d center;
    protected final int durationTicks;
    protected int age = 0;
    protected final double radius;
    protected final ParticleEffect particle;
    protected final int particlesPerTick;
    protected ServerWorld world;
    protected final List<Vec3d> cachedOffsets = new ArrayList<>();
    protected final Random random = new Random();
    protected RadialTimedEffect(ServerWorld world, Vec3d center,
                                double radius,
                                int durationTicks,
                                ParticleEffect particle,
                                int particlesPerTick) {
        this(world, center, radius, durationTicks, particle, particlesPerTick, 0);
    }
    protected RadialTimedEffect(ServerWorld world, Vec3d center,
                                double radius,
                                int durationTicks,
                                ParticleEffect particle,
                                int particlesPerTick, int preCompute) {
        this.world = world;
        this.center = center;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.particle = particle;
        this.particlesPerTick = particlesPerTick;
        precomputeOffsets(preCompute);
    }

    @Override
    public boolean tick() {
        if (age > durationTicks) return false;
        spawnParticles();
        age++;
        return true;
    }
    @Override
    public boolean isIn(ServerWorld world) {
        return this.world == world;
    }
    public double progress(){
        return (double)age / durationTicks;
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

    protected void precomputeOffsets(int samples) {
        cachedOffsets.clear();
        for (int i = 0; i < samples; i++) {
            Vec3d offset = randomOffsetInSphere(radius);
            Vec3d target = center.add(offset);
            if (isVisibleFromCenter(target)) {
                cachedOffsets.add(offset);
            }
        }
    }

    protected Vec3d randomOffsetInSphere(double radius) {
        double x, y, z;
        do {
            x = (random.nextDouble() * 2.0 - 1.0) * radius;
            y = (random.nextDouble() * 2.0 - 1.0) * radius;
            z = (random.nextDouble() * 2.0 - 1.0) * radius;
        } while (x * x + y * y + z * z > radius * radius);
        return new Vec3d(x, y, z);
    }
    protected boolean isVisibleFromCenter(Vec3d target) {
        RaycastContext context = new RaycastContext(
                center,
                target,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                ShapeContext.absent()
        );
        BlockHitResult hit = world.raycast(context);
        return hit.getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }
    protected Vec3d pickVisiblePosition() {
        if (!cachedOffsets.isEmpty()) {
            Vec3d offset = cachedOffsets.get(random.nextInt(cachedOffsets.size()));
            return center.add(offset);
        }

        for (int i = 0; i < 16; i++) {
            Vec3d offset = randomOffsetInSphere(radius);
            Vec3d target = center.add(offset);
            if (isVisibleFromCenter(target)) {
                return target;
            }
        }

        return center;
    }

}