package org.cat.express.wyspiaexpress;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.entity.GrenadeEntity;
import org.cat.express.wyspiaexpress.entity.SmokeBombEntity;
import org.jetbrains.annotations.NotNull;

public class WyspiaExpressEntities {

    public static final @NotNull EntityType<@NotNull GrenadeEntity> GRENADE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(WyspiaExpress.MOD_ID, "grenade"),
            EntityType.Builder.create(GrenadeEntity::new, SpawnGroup.MISC)
                    .dimensions(.25f, .25f)
                    .maxTrackingRange(128).build("grenade")
    );
    public static final @NotNull EntityType<@NotNull SmokeBombEntity> SMOKE_BOMB = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(WyspiaExpress.MOD_ID, "smoke_bomb"),
            EntityType.Builder.create(SmokeBombEntity::new, SpawnGroup.MISC)
                    .dimensions(.25f, .25f)
                    .maxTrackingRange(128).build("smoke_bomb")
    );
    public static void init() {}
}