package org.cat.express.wyspiaexpress.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerEntity.class, priority = 2000)
public abstract class PlayerCrawlSpeedMixin extends LivingEntity {

    protected PlayerCrawlSpeedMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @ModifyReturnValue(method = "getMovementSpeed", at = @At("RETURN"))
    public float overrideMovementSpeed(float original) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (GameFunctions.isPlayerAliveAndSurvival(self) && isCrawling(self)) {
            return (float) (original * ( 1 + WyspiaExpress.SERVER_CONFIG.crawlSpeedMultiplier()));
        }
        return original;
    }
    @Unique
    private boolean isCrawling(PlayerEntity player) {
        EntityPose pose = player.getPose();
        if(WyspiaExpress.CRAWL_MOD_LOADED && pose.name().equals("CRAWLING")) {
            return true;
        }
        return pose == EntityPose.SWIMMING
                && !player.isTouchingWater()
                && !player.isInLava()
                && !player.isSleeping();
    }
}
