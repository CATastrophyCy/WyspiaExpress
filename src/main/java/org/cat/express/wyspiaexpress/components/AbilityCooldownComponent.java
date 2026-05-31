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


public class AbilityCooldownComponent implements AutoSyncedComponent, ServerTickingComponent{
    public static final ComponentKey<AbilityCooldownComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "ability_cooldown"),
            AbilityCooldownComponent.class);
    private final PlayerEntity player;
    public int cooldown = 0;
    public AbilityCooldownComponent(@NotNull PlayerEntity player) {
        this.player = player;
    }
    @Override
    public void serverTick() {
        if (this.cooldown > 0) {
            -- this.cooldown;
            if(this.cooldown <= 0) this.forceSync();
            else this.sync();
        }
    }
    public void setAbilityCooldown(int ticks) {
        this.cooldown = ticks * 20;
        this.forceSync();
    }
    public boolean isCooldown(){
        return this.cooldown > 0;
    }
    public void addCooldown(int ticks) {
        this.cooldown += ticks;
        if (this.cooldown < 0) {
            this.cooldown = 0;
        }
        this.forceSync();
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
    public void reset() {
        this.cooldown = 0;
        this.forceSync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("cooldown", this.cooldown);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
    }
}
