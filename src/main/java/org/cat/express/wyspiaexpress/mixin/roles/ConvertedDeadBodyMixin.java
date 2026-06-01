package org.cat.express.wyspiaexpress.mixin.roles;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.components.PlayerBodyEntityComponent;
import org.cat.express.wyspiaexpress.components.roles.PlayerCultistComponent;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameFunctions.class)
public abstract class ConvertedDeadBodyMixin {

    @WrapOperation(
            method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private static boolean wrapSpawnBody(
            World world,
            Entity entity,
            Operation<Boolean> original,
            PlayerEntity victim,
            boolean spawnBody,
            PlayerEntity killer,
            Identifier deathReason
    ) {
        PlayerBodyEntity body = (PlayerBodyEntity) entity;
        if(body != null){
            PlayerEntity player = body.getWorld().getPlayerByUuid(body.getPlayerUuid());

            if( player != null && PlayerCultistComponent.KEY.get(player).isConverted())
                PlayerBodyEntityComponent.KEY.get(body).setConverted(true);
        }
        return original.call(world, entity);
    }
}