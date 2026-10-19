package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public final class PlayerConfigComponent implements AutoSyncedComponent {
    public static final ComponentKey<PlayerConfigComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "player_config"), PlayerConfigComponent.class);

    public enum Option { AVOID_KILLER, AVOID_NEUTRAL, AUTO_JOIN }

    private final PlayerEntity player;
    private boolean avoidKiller;
    private boolean avoidNeutral;
    private boolean autoJoin;

    public PlayerConfigComponent(PlayerEntity player) { this.player = player; }

    public boolean avoidKiller() { return avoidKiller; }
    public boolean avoidNeutral() { return avoidNeutral; }
    public boolean autoJoin() { return autoJoin; }

    public boolean isEnabled(Option option) {
        return switch (option) {
            case AVOID_KILLER -> avoidKiller;
            case AVOID_NEUTRAL -> avoidNeutral;
            case AUTO_JOIN -> autoJoin;
        };
    }

    public static boolean canEdit(PlayerEntity player) {
        return !GameWorldComponent.KEY.get(player.getWorld()).isRunning() || player.isSpectator();
    }

    public void set(Option option, boolean enabled) {
        switch (option) {
            case AVOID_KILLER -> avoidKiller = enabled;
            case AVOID_NEUTRAL -> avoidNeutral = enabled;
            case AUTO_JOIN -> autoJoin = enabled;
        }
        sync();
    }

    public void sync() { KEY.sync(player); }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putBoolean("avoidKiller", avoidKiller);
        tag.putBoolean("avoidNeutral", avoidNeutral);
        tag.putBoolean("autoJoin", autoJoin);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        avoidKiller = tag.getBoolean("avoidKiller");
        avoidNeutral = tag.getBoolean("avoidNeutral");
        autoJoin = tag.getBoolean("autoJoin");
    }
}
