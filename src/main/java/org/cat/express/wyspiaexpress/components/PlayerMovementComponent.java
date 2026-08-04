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

public class PlayerMovementComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<PlayerMovementComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "movement_component"),
            PlayerMovementComponent.class);

    @NotNull private final PlayerEntity player;
    public int ticks = 0;

    public PlayerMovementComponent(@NotNull PlayerEntity player) {this.player = player;}

    @Override
    public void serverTick() {
        if (this.ticks > 0) {
            -- this.ticks;
            if(ticks <= 0) {
                forceSync();
            }
            else sync();
        }
    }

    public boolean isRestricted(){
        return this.ticks > 0;
    }
    public void setTicks(int ticks) {
        this.ticks = Math.max(ticks, this.ticks);
        forceSync();
    }

    public void reset() {
        this.ticks = 0;
        forceSync();
    }

    public void sync() {
        // sync only takes effect every second or if its 0
        if(WyspiaExpress.TICK % 20 == 0) {
            KEY.sync(this.player);
        }
    }
    public void forceSync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("ticks", this.ticks);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.ticks = tag.contains("ticks") ? tag.getInt("ticks") : 0;
    }
}