package org.cat.express.wyspiaexpress.particles;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.Nullable;

public class SmokeBombEffect extends RadialTimedEffect {

    private final @Nullable Runnable tickAction;

    public SmokeBombEffect(ServerWorld world, Vec3d center, double radius, int durationTicks) {
        this(world, center, radius, durationTicks, null);
    }

    public SmokeBombEffect(ServerWorld world, Vec3d center, double radius, int durationTicks, @Nullable Runnable tickAction) {
        super(world, center, radius, durationTicks, ParticleTypes.LARGE_SMOKE,
                WyspiaExpress.ITEMS_CONFIG.itemConfig.smokeBombConfig.particles());
        this.tickAction = tickAction;
    }


    @Override
    protected Vec3d randomOffsetInSphere(double radius) {
        return super.randomOffsetInSphere(radius).multiply(1.15, 1.0, 1.15);
    }

    @Override
    public boolean tick() {
        if (!super.tick()) return false;
        if (tickAction != null) tickAction.run();
        return true;
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