package org.cat.express.wyspiaexpress.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
public class PlayerHearDeadComponent implements AutoSyncedComponent {
    public static final ComponentKey<PlayerHearDeadComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "hear_dead"),
            PlayerHearDeadComponent.class);
    private final PlayerEntity player;
    private boolean active;

    public PlayerHearDeadComponent(@NotNull PlayerEntity player) {
        this.player = player;
    }

    public void setActive(boolean active) {
        this.active = active;
        sync(); }
    public void toggle(){
        active = !active;
        sync();
    }
    public boolean isActive(){
        return this.active;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        this.active = false;
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putBoolean("active", this.active);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.active = tag.contains("active") && tag.getBoolean("active");
    }
}