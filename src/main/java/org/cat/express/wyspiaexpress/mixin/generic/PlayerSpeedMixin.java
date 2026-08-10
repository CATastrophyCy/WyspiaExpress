package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.PlayerMovementComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerEntity.class, priority = 2000)
public abstract class PlayerSpeedMixin extends LivingEntity {

    protected PlayerSpeedMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @ModifyReturnValue(method = "getMovementSpeed", at = @At("RETURN"))
    public float wyspiaexpress$overrideMovementSpeed(float original) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        float multiplier = 1.0f;
        if(PlayerMovementComponent.KEY.get(self).isRestricted() && GameFunctions.isPlayerAliveAndSurvival(self))
            multiplier -= (float) WyspiaExpress.ITEMS_CONFIG.itemConfig.smokeBombConfig.slowness();
        if (isCrawling(self)) {
            multiplier *= getCrawlingSpeedMultiplier(self);
        }
        return original * multiplier;
    }
    @Unique
    private float getCrawlingSpeedMultiplier(PlayerEntity player) {
        WorldModifierComponent wmc = WorldModifierComponent.KEY.get(player.getWorld());
        float multiplier = (float) WyspiaExpress.SERVER_CONFIG.crawlSpeedMultiplier();
        if(wmc.isModifier(player, WyspiaExpressRoles.VENT_CRAWLER)){
            multiplier *= (float) WyspiaExpress.MODIFIERS_CONFIG.ventCrawlerConfig.crawlSpeedModifier();
        }
        return multiplier;
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
        return currentHeight < 1.0F && !player.isSwimming() && !player.isFallFlying();
    }
}
