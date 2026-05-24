package org.cat.express.wyspiaexpress;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.cat.express.wyspiaexpress.components.*;
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
        registry.beginRegistration(PlayerEntity.class, PlayerDepressedComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerDepressedComponent::new);
        registry.beginRegistration(PlayerEntity.class, PlayerFreezeComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerFreezeComponent::new);
        registry.beginRegistration(PlayerEntity.class, PlayerRolePickingComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerRolePickingComponent::new);
        registry.beginRegistration(PlayerEntity.class, PlayerSenseDeadComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerSenseDeadComponent::new);
        registry.beginRegistration(PlayerEntity.class, PlayerHearDeadComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerHearDeadComponent::new);
        registry.beginRegistration(PlayerBodyEntity.class, PlayerBodyEntityComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerBodyEntityComponent::new);
    }
    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(LichReviveComponent.KEY, LichReviveComponent::new);
        registry.register(WorldComponent.KEY, WorldComponent::new);
    }
}
