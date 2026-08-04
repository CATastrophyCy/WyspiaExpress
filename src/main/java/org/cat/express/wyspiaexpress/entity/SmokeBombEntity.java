package org.cat.express.wyspiaexpress.entity;


import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheParticles;
import dev.doctor4t.wathe.index.WatheSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressEntities;
import org.cat.express.wyspiaexpress.components.PlayerMovementComponent;
import org.cat.express.wyspiaexpress.config.WyspiaExpressItemsConfig;

public class SmokeBombEntity extends ThrownItemEntity {
    public SmokeBombEntity(EntityType<?> ignored, World world) {
        super(WyspiaExpressEntities.SMOKE_BOMB, world);
    }

    @Override
    protected Item getDefaultItem() {
        return WatheItems.THROWN_GRENADE;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.getWorld() instanceof ServerWorld world) {
            world.playSound(null, this.getBlockPos(), WatheSounds.ITEM_GRENADE_EXPLODE, SoundCategory.PLAYERS, 5f, 1f + this.getRandom().nextFloat() * .1f - .05f);
            world.spawnParticles(WatheParticles.BIG_EXPLOSION, this.getX(), this.getY() + .1f, this.getZ(), 1, 0, 0, 0, 0);
            world.spawnParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + .1f, this.getZ(), 100, 0, 0, 0, .2f);
            world.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, this.getDefaultItem().getDefaultStack()), this.getX(), this.getY() + .1f, this.getZ(), 100, 0, 0, 0, 1f);

            Vec3d origin = this.getBoundingBox().getCenter(); // center of grenade
            PlayerEntity owner = (PlayerEntity) this.getOwner();
            WyspiaExpressItemsConfig.SmokeBombConfig config = WyspiaExpress.ITEMS_CONFIG.itemConfig.smokeBombConfig;
            for (ServerPlayerEntity player : PlayerLookup.around(world, origin, config.radius())) {
                if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                    continue;
                }
                // more mercy towards the owner
                if(owner != null && player.getUuid().equals(owner.getUuid())
                        && origin.squaredDistanceTo(player.getPos()) > config.ownerRadius() * config.ownerRadius())
                    continue;

                float exposure = Explosion.getExposure(origin, player); // vanilla explosion ray cast
                // exposure <= 0 means fully occluded by blocks (no line of sight)
                if (exposure <= 0.0f) {
                    continue;
                }
                PlayerMovementComponent.KEY.get(player).setTicks(config.duration());

            }
            this.discard();
        }
    }
}