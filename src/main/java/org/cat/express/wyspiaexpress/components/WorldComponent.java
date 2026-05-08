package org.cat.express.wyspiaexpress.components;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldComponent implements AutoSyncedComponent {
    public static final ComponentKey<WorldComponent> KEY =
            ComponentRegistry.getOrCreate(
                    Identifier.of(WyspiaExpress.MOD_ID, "world_component"),
                    WorldComponent.class
            );
    @NotNull
    private final World world;
    private final Set<UUID> playerDead = new HashSet<UUID>();

    public WorldComponent(@NotNull World world) {
        this.world = world;
    }
    public void sync() {
        KEY.sync(this.world);
    }

    public void reset() {
        playerDead.clear();
        sync();
    }

   public void addPlayerDead(UUID uuid) {
        this.playerDead.add(uuid);
        this.sync();
   }
   public void removePlayerDead(UUID uuid) {
        this.playerDead.remove(uuid);
        this.sync();
   }
   public boolean isPlayerDead(UUID uuid) {
        return this.playerDead.contains(uuid);
   }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (UUID uuid : this.playerDead) {
            list.add(NbtHelper.fromUuid(uuid));
        }
        tag.put("DeadPlayers", list);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.playerDead.clear();
        if (tag.contains("DeadPlayers", NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList("DeadPlayers", NbtElement.INT_ARRAY_TYPE);
            for (net.minecraft.nbt.NbtElement nbtElement : list) {
                this.playerDead.add(NbtHelper.toUuid(nbtElement));
            }
        }
    }


}
