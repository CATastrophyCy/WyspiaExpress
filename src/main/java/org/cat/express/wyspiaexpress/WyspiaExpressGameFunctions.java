package org.cat.express.wyspiaexpress;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.index.WatheEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.config.ServerConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WyspiaExpressGameFunctions {

    private static final Map<RegistryKey<World>, Boolean> prevStarting = new ConcurrentHashMap<>();
    public static void init(){
        registerEndWorldTick();
        registerOnJoin();
    }

    private static void registerOnJoin(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joining = handler.player;
            server.execute(() -> {
                ServerWorld overworld = server.getOverworld();
                GameWorldComponent gwc = GameWorldComponent.KEY.get(overworld);
                WorldComponent wc = WorldComponent.KEY.get(overworld);
                if (gwc.isRunning() && wc.isPlayerDead(joining.getUuid())) {
                    joining.changeGameMode(GameMode.SPECTATOR);
                }
                else if(joining.hasPermissionLevel(2) && !gwc.isRunning()){ // op join in creative mode
                    joining.changeGameMode(GameMode.CREATIVE);
                }
                else{
                    joining.changeGameMode(GameMode.ADVENTURE);
                }
            });
        });
    }
    private static void registerEndWorldTick(){
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!(world instanceof ServerWorld serverWorld)) return;
            GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
            GameWorldComponent.GameStatus status = gwc.getGameStatus();
            boolean gameRunning = status == GameWorldComponent.GameStatus.ACTIVE
                    || status == GameWorldComponent.GameStatus.STARTING
                    || status == GameWorldComponent.GameStatus.STOPPING;
            boolean isStarting = status == GameWorldComponent.GameStatus.STARTING;
            boolean wasStarting = prevStarting.getOrDefault(world.getRegistryKey(), false);
            if (isStarting && !wasStarting) {

                performRtp(serverWorld);
            }
            prevStarting.put(world.getRegistryKey(), isStarting);

            Box readyArea = MapVariablesWorldComponent.KEY.get(world).getReadyArea();
            for (net.minecraft.entity.player.PlayerEntity player : world.getPlayers()) {
                if (!(player instanceof ServerPlayerEntity serverPlayer)) continue;
                if (gameRunning || (readyArea != null && readyArea.contains(serverPlayer.getPos()) ) ) {
                    removeTeleportItem(serverPlayer);
                } else {
                    giveTeleportItem(serverPlayer);

                }
            }
            if (status == GameWorldComponent.GameStatus.ACTIVE && WyspiaExpress.SERVER_CONFIG.enableItemBoundChecking())
                itemBoundsCheck(serverWorld);

        });
    }

    private static void performRtp(ServerWorld world) {
        if(!WyspiaExpress.SERVER_CONFIG.enableRandomStartTp())
            return;
        List<ServerConfig.TpTargetPosition> slots = new ArrayList<>();
        for (String entry : WyspiaExpress.SERVER_CONFIG.randomStartTp()) {
            String[] parts = entry.split(",");
            if (parts.length >= 5) {
                try {
                    slots.add(new ServerConfig.TpTargetPosition(
                            Double.parseDouble(parts[0].trim()),
                            Double.parseDouble(parts[1].trim()),
                            Double.parseDouble(parts[2].trim()),
                            Float.parseFloat(parts[3].trim()),
                            Float.parseFloat(parts[4].trim())
                    ));
                } catch (NumberFormatException e) {
                }
            }
        }
        if (slots.isEmpty()) return;

        List<ServerPlayerEntity> eligiblePlayers = new ArrayList<>();
        MapVariablesWorldComponent mvc = MapVariablesWorldComponent.KEY.get(world);

        Box readyArea = mvc.getReadyArea();
        for (net.minecraft.entity.player.PlayerEntity player : world.getPlayers()) {
            if (!(player instanceof ServerPlayerEntity sp) || ! readyArea.contains(sp.getPos())) continue;
            eligiblePlayers.add(sp);
        }
        if (eligiblePlayers.isEmpty()) return;
        Collections.shuffle(eligiblePlayers);
        Collections.shuffle(slots);
        int count = Math.min(eligiblePlayers.size(), slots.size());
        for (int i = 0; i < count; i++) {
            ServerPlayerEntity player = eligiblePlayers.get(i);
            ServerConfig.TpTargetPosition slot = slots.get(i);
            TeleportTarget target = new TeleportTarget(world, new Vec3d(slot.x, slot.y, slot.z), Vec3d.ZERO, slot.yaw, slot.pitch, TeleportTarget.NO_OP);
            player.teleportTo(target);
        }
    }

    private static void giveTeleportItem(ServerPlayerEntity player) {
        boolean haveItem = false;
        for (int item = 0; item < player.getInventory().size(); item++) {
            if (player.getInventory().getStack(item).isOf(WyspiaExpressItems.TP_READY)) {
                haveItem = true;
                break;
            }
        }
        if (!haveItem) {
            player.getInventory().insertStack(WyspiaExpressItems.TP_READY.getDefaultStack());
        }
    }
    private static void removeTeleportItem(ServerPlayerEntity player) {
        for (int item = 0; item < player.getInventory().size(); item++) {
            if (player.getInventory().getStack(item).isOf(WyspiaExpressItems.TP_READY)) {
                player.getInventory().setStack(item, ItemStack.EMPTY);
            }
        }
    }
    private static void itemBoundsCheck(ServerWorld world) {
        Box playArea = MapVariablesWorldComponent.KEY.get(world).getPlayArea();
        if (playArea == null) return;
        List<Entity> targets = new ArrayList<>();
        for (PlayerEntity p : world.getPlayers()) {
            if (p instanceof ServerPlayerEntity player
                    && player.isAlive()
                    && !player.isSpectator()
                    && !player.isCreative()) {
                targets.add(player);
            }
        }
        targets.addAll(world.getEntitiesByType(WatheEntities.PLAYER_BODY, body -> true));

        if (targets.isEmpty()) return;

        for (ItemEntity item : world.getEntitiesByType(net.minecraft.entity.EntityType.ITEM, item -> !playArea.contains(item.getPos()))) {
            Entity closest = findClosestEntity(item.getPos(), targets);
            if (closest == null) continue;
            Vec3d dest = closest.getPos();
            item.requestTeleport(dest.x, dest.y, dest.z);
        }

    }
    private static Entity findClosestEntity(Vec3d from, List<Entity> candidates) {
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity candidate : candidates) {
            double dist = from.squaredDistanceTo(candidate.getPos());
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }
}
