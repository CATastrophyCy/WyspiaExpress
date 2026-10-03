package org.cat.express.wyspiaexpress.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class GuesserComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<GuesserComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "guesser"), GuesserComponent.class);
    private final PlayerEntity player;
    private int cooldown;

    public GuesserComponent(PlayerEntity player) { this.player = player; }
    public int cooldown() { return cooldown; }
    public void setCooldown(int ticks) {
        cooldown = Math.max(0, ticks);
        KEY.sync(player);
    }
    public void reset() { setCooldown(0); }
    @Override public void serverTick() {
        if (cooldown > 0 && (--cooldown == 0 || cooldown % 20 == 0)) KEY.sync(player);
    }
    @Override public void clientTick() { if (cooldown > 0) --cooldown; }
    @Override public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) { tag.putInt("cooldown", cooldown); }
    @Override public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) { cooldown = Math.max(0, tag.getInt("cooldown")); }
}
