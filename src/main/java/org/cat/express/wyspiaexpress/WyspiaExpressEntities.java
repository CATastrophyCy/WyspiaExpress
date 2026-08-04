package org.cat.express.wyspiaexpress;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.entity.GrenadeEntity;
import org.jetbrains.annotations.NotNull;

public class WyspiaExpressEntities {

    public static final @NotNull EntityType<@NotNull GrenadeEntity> GRENADE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(WyspiaExpress.MOD_ID, "grenade"),
            EntityType.Builder.create(GrenadeEntity::new, SpawnGroup.MISC)
                    .dimensions(.45f, .45f)
                    .maxTrackingRange(128).build("grenade")
    );

    public static void init() {}
}