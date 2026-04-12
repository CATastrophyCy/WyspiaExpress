package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
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

import static dev.doctor4t.wathe.Wathe.isSkyVisibleAdjacent;

public class PlayerFreezeComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<PlayerFreezeComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "player_freeze_component"),
            PlayerFreezeComponent.class);
    private int freezeTick = 0;
    private final PlayerEntity player;
    public PlayerFreezeComponent(@NotNull PlayerEntity player) {
        this.player = player;
    }
    @Override
    public void serverTick() {
        if(WyspiaExpress.SERVER_CONFIG.freeze() ) {
            if (isSkyVisibleAdjacent(this.player) && GameFunctions.isPlayerAliveAndSurvival(this.player)  ) {
                freezeTick += 2;
                this.sync();
                if (freezeTick >= 2 * GameConstants.getInTicks(0, WyspiaExpress.SERVER_CONFIG.freezeTimer())) {
                    GameFunctions.killPlayer(this.player,true,null, Identifier.of(WyspiaExpress.MOD_ID, "player_freeze"));
                }
            } else if (freezeTick > 0) {
                freezeTick--;
                this.sync();
            }
        }
    }
    public int getFreezeTick() {
        return freezeTick;
    }
    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        this.freezeTick = 0;
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("tick", this.freezeTick);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.freezeTick = tag.contains("tick") ? tag.getInt("tick") : 0;
    }
}
