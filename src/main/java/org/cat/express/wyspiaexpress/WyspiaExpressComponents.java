package org.cat.express.wyspiaexpress;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.roles.ReanimatorReviveComponent;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class WyspiaExpressComponents implements EntityComponentInitializer, WorldComponentInitializer{
    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(PlayerEntity.class, AbilityCooldownComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(AbilityCooldownComponent::new);
    }
    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(ReanimatorReviveComponent.KEY, ReanimatorReviveComponent::new);
    }
}
