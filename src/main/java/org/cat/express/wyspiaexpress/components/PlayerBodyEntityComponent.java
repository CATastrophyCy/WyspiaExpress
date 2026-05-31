package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class PlayerBodyEntityComponent implements AutoSyncedComponent {
    public static final ComponentKey<PlayerBodyEntityComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "player_body"),
            PlayerBodyEntityComponent.class);
    private final PlayerBodyEntity playerBody;
    private boolean isReported = false;
    public PlayerBodyEntityComponent(@NotNull PlayerBodyEntity playerBody) {
        this.playerBody = playerBody;
    }

    public void sync() {
        KEY.sync(this.playerBody);
    }

    public void reset() {
        isReported = false;
        this.sync();
    }
    public void setReported(boolean reported) {
        isReported = reported;
        this.sync();
    }
    public boolean isReported() {
        return isReported;
    }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putBoolean("reported", this.isReported);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.isReported = tag.contains("reported") && tag.getBoolean("reported");
    }
}
