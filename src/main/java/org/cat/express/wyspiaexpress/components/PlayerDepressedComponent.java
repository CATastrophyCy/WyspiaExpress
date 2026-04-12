package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
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

public class PlayerDepressedComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<PlayerDepressedComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "player_depressed_component"),
            PlayerDepressedComponent.class);
    private final PlayerEntity player;
    private int depressionTick;
    public PlayerDepressedComponent(@NotNull PlayerEntity player) {
        this.player = player;
    }
    @Override
    public void serverTick() {
        if(WyspiaExpress.SERVER_CONFIG.depressionKilling() ) {
            PlayerMoodComponent component = PlayerMoodComponent.KEY.get(this.player);
            if (component.getMood() <= 0.01f && GameFunctions.isPlayerAliveAndSurvival(this.player)  ) {
                depressionTick += 2;
                this.sync();
                if (depressionTick >= 2 * GameConstants.getInTicks(0, WyspiaExpress.SERVER_CONFIG.depressedTimer()) ) {
                    GameFunctions.killPlayer(this.player,true,null, Identifier.of(WyspiaExpress.MOD_ID, "player_depressed"));
                }
            } else if (depressionTick > 0) {
                depressionTick--;
                this.sync();
            }
        }
    }
    public int getDepressionTick() {
        return this.depressionTick;
    }
    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        this.depressionTick = 0;
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("tick", this.depressionTick);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.depressionTick = tag.contains("tick") ? tag.getInt("tick") : 0;
    }
}
