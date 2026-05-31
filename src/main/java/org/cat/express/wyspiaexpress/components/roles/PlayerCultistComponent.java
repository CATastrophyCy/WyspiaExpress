package org.cat.express.wyspiaexpress.components.roles;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Collection;

public class PlayerCultistComponent implements AutoSyncedComponent, ServerTickingComponent{
    public static final ComponentKey<PlayerCultistComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "player_cultist_component"),
            PlayerCultistComponent.class);
    private final PlayerEntity player;
    GameWorldComponent gameWorldComponent;
    private int conversionTick;
    public PlayerCultistComponent(@NotNull PlayerEntity player) {
        this.player = player;
        this.gameWorldComponent = GameWorldComponent.KEY.get(this.player.getWorld());
    }
    @Override
    public void serverTick() {

        if(gameWorldComponent.isRole(this.player, WyspiaExpressRoles.CULT_LEADER)
                && gameWorldComponent.isRunning()
                && GameFunctions.isPlayerAliveAndSurvival(this.player)
                && WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.enableConversion()
        ) {
            ServerPlayerEntity player = (ServerPlayerEntity) this.player;

            Collection<ServerPlayerEntity> nearby =
                    PlayerLookup.around(player.getServerWorld(), player.getPos(), 5.0);

            for (ServerPlayerEntity other : nearby) {
                if (other == player) continue;

                PlayerCultistComponent otherPlayer = PlayerCultistComponent.KEY.get(other);
                if(!otherPlayer.isConverted() && !gameWorldComponent.isRole(other, WyspiaExpressRoles.CULTIST)) {
                    otherPlayer.conversionTick++;
                    if(otherPlayer.isConverted()) otherPlayer.forceSync();
                    else otherPlayer.sync();
                }

            }
        }
    }
    public int getConversionTick() {
        return this.conversionTick;
    }

    public boolean isConverted(){
        return this.conversionTick > WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.conversionTime();
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
        this.conversionTick = 0;
        this.forceSync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("tick", this.conversionTick);
    }
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.conversionTick = tag.contains("tick") ? tag.getInt("tick") : 0;
    }
}