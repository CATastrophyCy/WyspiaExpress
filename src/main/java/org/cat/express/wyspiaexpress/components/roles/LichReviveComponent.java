package org.cat.express.wyspiaexpress.components.roles;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class LichReviveComponent implements AutoSyncedComponent{
    public static final ComponentKey<LichReviveComponent> KEY =
            ComponentRegistry.getOrCreate(
                    Identifier.of(WyspiaExpress.MOD_ID, "lich_revive_component"),
                    LichReviveComponent.class
            );
    @NotNull private final World world;
    private int availableRevives;
    private int maxRevives;
    public LichReviveComponent(@NotNull World world) {
        this.world = world;
    }
    public void sync() {
        KEY.sync(this.world);
    }

    public void reset() {
        this.availableRevives = 0;
        this.maxRevives = 0;
        sync();
    }

    public void setAvailableRevives(int availableRevives) {
        this.availableRevives = availableRevives;
        this.sync();
    }
    public int getAvailableRevives() {
        return this.availableRevives;
    }
    public int getMaxRevives() {
        return this.maxRevives;
    }
    public void setMaxRevives(int maxRevives) {
        this.maxRevives = maxRevives;
        this.sync();
    }
    public void incrementAvailableRevives() {
        this.availableRevives++;
        this.sync();
    }
    public void decrementAvailableRevives() {
        this.availableRevives--;
        this.sync();
    }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("available_revivals", this.availableRevives);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.availableRevives = tag.contains("available_revivals") ? tag.getInt("available_revivals") : 0;
    }


}
