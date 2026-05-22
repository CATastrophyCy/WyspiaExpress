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
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PlayerSenseDeadComponent implements AutoSyncedComponent, ServerTickingComponent{
    public static final ComponentKey<PlayerSenseDeadComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "sense_dead"),
            PlayerSenseDeadComponent.class);
    private final PlayerEntity player;
    public int duration = 0;
    public PlayerSenseDeadComponent(@NotNull PlayerEntity player) {
        this.player = player;
    }
    @Override
    public void serverTick() {
        if (this.duration > 0) {
            -- this.duration;
            this.sync();
        }
    }
    public void setDuration(int seconds) {
        this.duration = seconds * 20;
        this.sync();
    }
    public boolean isActive(){
        return this.duration > 0;
    }
    public void addDuration(int ticks) {
        this.duration += ticks;
        if (this.duration < 0) {
            this.duration = 0;
        }
        this.sync();
    }
    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        this.duration = 0;
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("duration", this.duration);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.duration = tag.contains("duration") ? tag.getInt("duration") : 0;
    }
}
