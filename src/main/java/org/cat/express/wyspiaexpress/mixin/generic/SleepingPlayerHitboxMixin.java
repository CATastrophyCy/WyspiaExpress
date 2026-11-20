package org.cat.express.wyspiaexpress.mixin.generic;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class SleepingPlayerHitboxMixin extends LivingEntity {
    protected SleepingPlayerHitboxMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void setSleepingPosition(BlockPos pos) {
        super.setSleepingPosition(pos);
        // Vanilla moves the player onto the bed before setting the tracked bed position.
        if (isInPose(EntityPose.SLEEPING)) calculateDimensions();
    }

    @Override
    public boolean isPushable() {
        return !isInPose(EntityPose.SLEEPING) && super.isPushable();
    }

    @Override
    protected void tickCramming() {
        if (!isInPose(EntityPose.SLEEPING)) super.tickCramming();
    }

    @Override
    protected Box calculateBoundingBox() {
        if (!isInPose(EntityPose.SLEEPING)) return super.calculateBoundingBox();
        BlockPos bed = getSleepingPosition().orElse(null);
        if (bed == null) return super.calculateBoundingBox();
        Direction facing = getSleepingDirection();
        if (facing == null) return super.calculateBoundingBox();

        // Match the renderer's offset and scale along the bed and keep the normal player width.
        double playerScale = getScale();
        double modelScale = playerScale * 0.9375;
        Vec3d alongBed = Vec3d.of(facing.getVector());
        Vec3d feet = getPos().subtract(alongBed.multiply(getEyeHeight(EntityPose.STANDING) - 0.1));
        Vec3d head = feet.add(alongBed.multiply(2 * modelScale));
        double halfWidth = getDimensions(EntityPose.STANDING).width() / 2;
        double height = 0.125 * playerScale + 0.29 * modelScale;
        double bedTop = bed.getY() + getWorld().getBlockState(bed)
                .getCollisionShape(getWorld(), bed).getMax(Direction.Axis.Y);
        double bottom = Math.max(feet.y - 0.125 * playerScale, bedTop);
        return new Box(feet.x, bottom, feet.z, head.x, bottom + height, head.z)
                .expand(facing.getAxis() == Direction.Axis.X ? 0 : halfWidth, 0,
                        facing.getAxis() == Direction.Axis.Z ? 0 : halfWidth);
    }
}
