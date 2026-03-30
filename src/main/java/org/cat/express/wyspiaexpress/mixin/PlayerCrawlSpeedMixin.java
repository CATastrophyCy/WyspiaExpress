package org.cat.express.wyspiaexpress.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerEntity.class, priority = 2000)
public abstract class PlayerCrawlSpeedMixin extends LivingEntity {

    protected PlayerCrawlSpeedMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @ModifyReturnValue(method = "getMovementSpeed", at = @At("RETURN"))
    public float wyspiaexpress$overrideMovementSpeed(float original) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (isCrawling(self)) {
            return getMovementSpeed(self, original);
        }
        return original;
    }
    @Unique
    private float getMovementSpeed(PlayerEntity player, float original) {
        WorldModifierComponent wmc = WorldModifierComponent.KEY.get(player.getWorld());
        float speed = (float) (original *  WyspiaExpress.SERVER_CONFIG.crawlSpeedMultiplier() );
        if(wmc.isModifier(player, WyspiaExpressRoles.VENT_CRAWLER)){
            speed = (float) (speed * WyspiaExpress.MODIFIERS_CONFIG.ventCrawlerConfig.crawlSpeedModifier());
        }
        return speed;
    }
    @Unique
    private boolean isCrawling(PlayerEntity player) {
        if (player.isCrawling()) {
            return true;
        }
        if (player.getPose().name().contains("CRAWL") || player.getPose().name().contains("PRON")) {
            return true;
        }
        float currentHeight = player.getDimensions(player.getPose()).height();
        if (currentHeight < 1.0F && !player.isSwimming() && !player.isFallFlying()) {
            return true;
        }
        return false;
    }
}
