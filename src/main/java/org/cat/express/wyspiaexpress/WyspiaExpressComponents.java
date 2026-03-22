package org.cat.express.wyspiaexpress;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.PlayerBodyEntityComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
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

        registry.beginRegistration(PlayerBodyEntity.class, PlayerBodyEntityComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerBodyEntityComponent::new);
    }
    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(LichReviveComponent.KEY, LichReviveComponent::new);
        registry.register(WorldComponent.KEY, WorldComponent::new);
    }
}
