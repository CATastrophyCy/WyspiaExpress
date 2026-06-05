package org.cat.express.wyspiaexpress.components;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.entities.CaptureDeviceEntity;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.UUID;

public class CaptureDeviceEntityComponent implements AutoSyncedComponent {
    public static final ComponentKey<CaptureDeviceEntityComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "capture_device_component"),
            CaptureDeviceEntityComponent.class);
    private final CaptureDeviceEntity device;
    private UUID owner;
    public CaptureDeviceEntityComponent(@NotNull CaptureDeviceEntity device) {
        this.device = device;
    }

    public void sync() {
        KEY.sync(this.device);
    }
    public void reset() {
        this.owner = null;
        this.sync();
    }
    public void setOwner(UUID owner) {
        this.owner = owner;
        this.sync();
    }
    public UUID getOwner() {
        return owner;
    }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putUuid("owner", this.owner);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.owner = tag.contains("owner")? tag.getUuid("reported") : null;
    }
}
