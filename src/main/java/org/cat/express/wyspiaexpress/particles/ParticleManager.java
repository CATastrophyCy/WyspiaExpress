package org.cat.express.wyspiaexpress.particles;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ParticleManager {

    // Track active effects by id; UUID keeps it simple.
    private static final Map<UUID, TimedParticleEffect> ACTIVE = new ConcurrentHashMap<>();

    private ParticleManager() {}

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(ParticleManager::onWorldTick);
    }

    public static UUID addEffect(TimedParticleEffect effect) {
        UUID id = UUID.randomUUID();
        ACTIVE.put(id, effect);
        return id;
    }
    public static void removeEffect(UUID id) {
        ACTIVE.remove(id);
    }

    private static void onWorldTick(ServerWorld world) {
        // Tick only effects that belong to this world
        Iterator<Map.Entry<UUID, TimedParticleEffect>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, TimedParticleEffect> entry = it.next();
            TimedParticleEffect effect = entry.getValue();
            // Here you can store world in the effect; for simplicity assume it knows its world.
            if (effect.isIn(world) && !effect.tick()) {
                it.remove();
            }
        }
    }
}

