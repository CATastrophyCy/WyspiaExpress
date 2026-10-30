package org.cat.express.wyspiaexpress.client.items;

import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressEntities;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ThrowablePreview {
    private static final int RING_SEGMENTS = 32;
    private static boolean selfHitWarning;
    private static @Nullable BlastPreview blastPreview;

    private ThrowablePreview() {}

    public static void register() {
        WorldRenderEvents.BEFORE_ENTITIES.register(ThrowablePreview::update);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ThrowablePreview::renderArea);
        HudRenderCallback.EVENT.register(ThrowablePreview::renderWarning);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            blastPreview = null;
            selfHitWarning = false;
        });
    }

    @Nullable
    private static Item previewItem(MinecraftClient client) {
        var player = client.player;
        if (player == null || player.isSpectator() || client.options.hudHidden
                || client.currentScreen != null || client.gameRenderer.getCamera().getFocusedEntity() != player) return null;
        Item item = player.getMainHandStack().getItem();
        if (item != WatheItems.GRENADE && item != WyspiaExpressItems.SMOKE_BOMB) {
            item = player.getOffHandStack().getItem();
        }
        if (item == WatheItems.GRENADE && WyspiaExpress.ITEMS_CONFIG.itemConfig.grenadeConfig.showTrajectory()) return item;
        if (item == WyspiaExpressItems.SMOKE_BOMB && WyspiaExpress.ITEMS_CONFIG.itemConfig.smokeBombConfig.showTrajectory()) return item;
        return null;
    }

    private static void update(WorldRenderContext context) {
        selfHitWarning = false;
        var client = MinecraftClient.getInstance();
        Item item = previewItem(client);
        if (item == null) {
            blastPreview = null;
            return;
        }
        var player = client.player;
        boolean smoke = item == WyspiaExpressItems.SMOKE_BOMB;
        var items = WyspiaExpress.ITEMS_CONFIG.itemConfig;
        double radius = smoke ? items.smokeBombConfig.radius() : items.grenadeConfig.radius();
        double ownerRadius = smoke ? items.smokeBombConfig.ownerRadius() : items.grenadeConfig.ownerRadius();
        BlastPreview preview = preview(player, context.tickCounter().getTickDelta(false), smoke, radius, ownerRadius);
        selfHitWarning = preview.selfHit();
    }

    private static void renderArea(WorldRenderContext context) {
        if (blastPreview == null || blastPreview.impact() == null || previewItem(MinecraftClient.getInstance()) == null) return;
        var matrices = context.matrixStack();
        var consumers = context.consumers();
        if (matrices == null || consumers == null) return;
        var matrix = matrices.peek();
        Vec3d camera = context.camera().getPos();
        var lines = consumers.getBuffer(RenderLayer.getLines());
        ring(lines, matrix, camera, blastPreview.boundary()[0], 0xB0FFD080);
        ring(lines, matrix, camera, blastPreview.boundary()[1], selfHitWarning ? 0xF0FF4040 : 0xC0FF6060);
        // Snap the small cross to the hit face; the server's blast origin remains separate.
        Impact impact = blastPreview.impact();
        Vec3d first = impact.face().getAxis() == Direction.Axis.X ? new Vec3d(0, 0.08, 0) : new Vec3d(0.08, 0, 0);
        Vec3d second = impact.face().getAxis() == Direction.Axis.Z ? new Vec3d(0, 0.08, 0) : new Vec3d(0, 0, 0.08);
        line(lines, matrix, camera, impact.marker().subtract(first), impact.marker().add(first), 0xB0FFFFFF);
        line(lines, matrix, camera, impact.marker().subtract(second), impact.marker().add(second), 0xB0FFFFFF);
    }

    public static boolean shouldHighlight(PlayerEntity target) {
        var client = MinecraftClient.getInstance();
        return blastPreview != null && previewItem(client) != null
                && !target.isInvisibleTo(client.player) && blastPreview.targets().contains(target.getUuid());
    }

    private static boolean isAffected(PlayerEntity owner, PlayerEntity target, Vec3d impact,
                                      double radius, double ownerRadius) {
        double effectiveRadius = target.getUuid().equals(owner.getUuid()) ? Math.min(radius, ownerRadius) : radius;
        return GameFunctions.isPlayerAliveAndSurvival(target)
                && GameUtil.isWithinCylinder(target, impact, effectiveRadius, 3.5)
                && Explosion.getExposure(impact, target) > 0;
    }

    private static boolean isSelfHit(PlayerEntity player, Vec3d impact, double radius, double ownerRadius) {
        return GameUtil.isWithinCylinder(player, impact, Math.min(radius, ownerRadius), 3.5)
                && Explosion.getExposure(impact, player) > 0;
    }

    private static void renderWarning(DrawContext context, RenderTickCounter tickCounter) {
        var client = MinecraftClient.getInstance();
        Item item = previewItem(client);
        if (!selfHitWarning || item == null) return;
        context.drawCenteredTextWithShadow(client.textRenderer,
                Text.translatable(item == WyspiaExpressItems.SMOKE_BOMB
                    ? "hud.wyspiaexpress.smoke_bomb.self_hit" : "hud.wyspiaexpress.grenade.self_hit"),
                context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() / 2 + 18, 0xFFFF6060);
    }

    private static @Nullable Impact predict(PlayerEntity player, float tickDelta, boolean smoke) {
        var probe = new PreviewProjectile(player.getWorld(), smoke);
        probe.setOwner(player);
        probe.setPosition(player.getLerpedPos(tickDelta).add(0, player.getEyeHeight(player.getPose()) - 0.1, 0));
        // Match both items: deterministic launch, same speed and inherited movement.
        probe.setVelocity(player, player.getPitch(tickDelta), player.getYaw(tickDelta), 0, 0.5F, 0);
        for (int tick = 0; tick < 120; tick++) {
            if (!player.getWorld().isChunkLoaded(BlockPos.ofFloored(probe.getPos()))
                    || probe.getY() < player.getWorld().getBottomY() - 16) break;
            boolean water = probe.prepareStep();
            HitResult hit = ProjectileUtil.getCollision(probe, probe::canHitInPreview);
            if (hit.getType() != HitResult.Type.MISS) {
                // The actual explosion uses the current box center, before ThrownEntity advances its position.
                Vec3d origin = probe.getBoundingBox().getCenter();
                if (hit instanceof BlockHitResult block) {
                    Vec3d marker = block.getPos().add(Vec3d.of(block.getSide().getVector()).multiply(0.01));
                    return new Impact(origin, marker, block.getSide());
                }
                return new Impact(origin, origin, Direction.UP);
            }
            Vec3d velocity = probe.getVelocity();
            probe.setPosition(probe.getPos().add(velocity));
            probe.setVelocity(velocity.multiply(water ? (double) 0.8F : (double) 0.99F)
                    .subtract(0, probe.getFinalGravity(), 0));
        }
        return null;
    }

    private static BlastPreview preview(PlayerEntity player, float tickDelta, boolean smoke,
                                        double radius, double ownerRadius) {
        World world = player.getWorld();
        long tick = world.getTime();
        // Predict and test exposure only once per game tick, regardless of frame rate.
        if (blastPreview == null || blastPreview.world() != world || blastPreview.tick() != tick
                || blastPreview.smoke() != smoke || blastPreview.radius() != radius || blastPreview.ownerRadius() != ownerRadius) {
            Impact impact = predict(player, tickDelta, smoke);
            Set<UUID> targets = new HashSet<>();
            if (impact != null) {
                for (PlayerEntity target : world.getPlayers()) {
                    if (isAffected(player, target, impact.origin(), radius, ownerRadius)) targets.add(target.getUuid());
                }
            }
            Vec3d[][] boundary = impact == null ? new Vec3d[0][] : boundary(player, impact.origin(), radius, ownerRadius);
            blastPreview = new BlastPreview(world, tick, smoke, radius, ownerRadius, impact, boundary, Set.copyOf(targets),
                    impact != null && isSelfHit(player, impact.origin(), radius, ownerRadius));
        }
        return blastPreview;
    }

    private static Vec3d[][] boundary(PlayerEntity player, Vec3d impact, double radius, double ownerRadius) {
        Vec3d[][] samples = new Vec3d[2][RING_SEGMENTS];
        for (int i = 0; i < RING_SEGMENTS; i++) {
            double angle = i * Math.PI * 2 / RING_SEGMENTS;
            Vec3d direction = new Vec3d(Math.cos(angle), 0, Math.sin(angle));
            HitResult hit = player.getWorld().raycast(new RaycastContext(impact, impact.add(direction.multiply(radius)),
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
            double reach = hit.getType() == HitResult.Type.MISS ? radius : Math.max(0, impact.distanceTo(hit.getPos()) - 0.01);
            samples[0][i] = groundPoint(player, impact, impact.add(direction.multiply(reach)));
            samples[1][i] = groundPoint(player, impact, impact.add(direction.multiply(Math.min(reach, ownerRadius))));
        }
        // Store independent line pairs: never connect across missing ground, steps or drops.
        Vec3d[][] segments = new Vec3d[2][RING_SEGMENTS * 2];
        for (int ring = 0; ring < samples.length; ring++) {
            for (int i = 0; i < RING_SEGMENTS; i++) {
                Vec3d from = samples[ring][i];
                Vec3d to = samples[ring][(i + 1) % RING_SEGMENTS];
                if (from == null || to == null || Math.abs(from.y - to.y) > 0.01) continue;
                Vec3d middle = groundPoint(player, impact, from.lerp(to, 0.5));
                if (middle == null || Math.abs(middle.y - from.y) > 0.01) continue;
                segments[ring][i * 2] = from;
                segments[ring][i * 2 + 1] = to;
            }
        }
        return segments;
    }

    private static @Nullable Vec3d groundPoint(PlayerEntity player, Vec3d impact, Vec3d sample) {
        // Search below the blast plane only, avoiding ceilings and respecting the server's +/-3.5 Y limit.
        Vec3d start = new Vec3d(sample.x, impact.y + 0.125, sample.z);
        Vec3d end = new Vec3d(sample.x, impact.y - 3.501, sample.z);
        BlockHitResult hit = player.getWorld().raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK || hit.isInsideBlock() || hit.getSide() != Direction.UP
                || impact.y - hit.getPos().y > 3.5) return null;
        return hit.getPos().add(0, 0.015, 0);
    }

    private static void ring(VertexConsumer lines, MatrixStack.Entry matrix, Vec3d camera, Vec3d[] segments, int color) {
        for (int i = 0; i < segments.length; i += 2) {
            if (segments[i] != null && segments[i + 1] != null) {
                line(lines, matrix, camera, segments[i], segments[i + 1], color);
            }
        }
    }

    private static void line(VertexConsumer lines, MatrixStack.Entry matrix, Vec3d camera, Vec3d start, Vec3d end, int color) {
        Vec3d from = start.subtract(camera);
        Vec3d to = end.subtract(camera);
        Vec3d direction = to.subtract(from).normalize();
        if (direction.lengthSquared() == 0) return;
        lines.vertex(matrix, (float) from.x, (float) from.y, (float) from.z).color(color)
                .normal(matrix, (float) direction.x, (float) direction.y, (float) direction.z);
        lines.vertex(matrix, (float) to.x, (float) to.y, (float) to.z).color(color)
                .normal(matrix, (float) direction.x, (float) direction.y, (float) direction.z);
    }

    private record Impact(Vec3d origin, Vec3d marker, Direction face) {}

    private record BlastPreview(World world, long tick, boolean smoke, double radius, double ownerRadius,
                                @Nullable Impact impact, Vec3d[][] boundary, Set<UUID> targets, boolean selfHit) {}

    private static final class PreviewProjectile extends ThrownItemEntity {
        private boolean leftOwner;

        private PreviewProjectile(World world, boolean smoke) {
            super(smoke ? WyspiaExpressEntities.SMOKE_BOMB : WyspiaExpressEntities.GRENADE, world);
        }

        @Override
        protected Item getDefaultItem() { return WatheItems.THROWN_GRENADE; }

        private boolean prepareStep() {
            Entity owner = getOwner();
            if (!leftOwner) {
                leftOwner = owner == null || getWorld().getOtherEntities(this,
                        getBoundingBox().stretch(getVelocity()).expand(1),
                        entity -> !entity.isSpectator() && entity.canHit()).stream()
                        .noneMatch(entity -> entity.getRootVehicle() == owner.getRootVehicle());
            }
            // Read water drag/current without Entity.tick's sounds, particles or block interactions.
            return updateMovementInFluid(FluidTags.WATER, 0.014);
        }

        private boolean canHitInPreview(Entity entity) {
            return entity.canBeHitByProjectile()
                    && (getOwner() == null || leftOwner || !getOwner().isConnectedThroughVehicle(entity));
        }
    }
}
