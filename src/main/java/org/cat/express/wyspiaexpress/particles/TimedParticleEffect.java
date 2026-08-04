package org.cat.express.wyspiaexpress.particles;


import net.minecraft.server.world.ServerWorld;

public interface TimedParticleEffect {
    /** Called each tick while the effect is active. Return false to remove the effect. */
    boolean tick();
    boolean isIn(ServerWorld world);
    /** Optional: per-effect id for debugging / management. */
    default String getId() {
        return this.getClass().getSimpleName();
    }
}