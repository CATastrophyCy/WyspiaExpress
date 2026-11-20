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
    protected Box calculateBoundingBox() {
        if (!isInPose(EntityPose.SLEEPING)) return super.calculateBoundingBox();
        Direction facing = getSleepingDirection();
        if (facing == null) return super.calculateBoundingBox();

        // Match the player renderer's offset from the bed's head and its 15/16 model scale.
        double scale = getScale() * 0.9375;
        Vec3d alongBed = Vec3d.of(facing.getVector());
        Vec3d feet = getPos().subtract(alongBed.multiply(getEyeHeight(EntityPose.STANDING) - 0.1));
        Vec3d head = feet.add(alongBed.multiply(2 * scale));
        double halfWidth = 0.53 * scale;
        return new Box(feet.subtract(0, 0.125, 0), head.add(0, 0.29 * scale, 0))
                .expand(facing.getAxis() == Direction.Axis.X ? 0 : halfWidth, 0,
                        facing.getAxis() == Direction.Axis.Z ? 0 : halfWidth);
    }
}
