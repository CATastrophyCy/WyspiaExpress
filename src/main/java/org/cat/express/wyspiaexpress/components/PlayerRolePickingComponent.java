package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.List;

public class PlayerRolePickingComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<PlayerRolePickingComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(WyspiaExpress.MOD_ID, "player_role_pick_component"),
            PlayerRolePickingComponent.class);

    private final PlayerEntity player;
    private final List<String> roles = new ArrayList<>();
    private int tick;
    public PlayerRolePickingComponent(@NotNull PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        if (this.tick > 0) {
            this.tick--;
            if(this.tick == 0) {
                GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
                if (gameWorldComponent.isRole(player, WyspiaExpressRoles.COPYCAT) && GameFunctions.isPlayerAliveAndSurvival(player)) {
                    String roleID = this.roles.get(this.player.getRandom().nextInt(this.roles.size()));
                    Role role = WyspiaExpressRoles.STRING_ROLES.get(roleID);
                    if (role == null) role = WatheRoles.KILLER;
                    gameWorldComponent.addRole(player, role);
                    ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, role);
                }
                this.tick = 0;
                this.roles.clear();
            }
            this.sync();
        }
    }
    public int getTick() {
        return tick;
    }
    public List<String> getRoles() {
        return this.roles;
    }
    public void set(List<String> roles, int tick){
        this.roles.addAll(roles);
        this.tick = tick;
        this.sync();
    }
    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        this.tick = 0;
        this.roles.clear();
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.putInt("tick", this.tick);

        NbtList nbtList = new NbtList();
        for (String s : this.roles) {
            nbtList.add(NbtString.of(s));
        }
        tag.put("roles", nbtList);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.tick = tag.contains("tick") ? tag.getInt("tick") : 0;
        this.roles.clear();
        if (tag.contains("roles", NbtElement.LIST_TYPE)) {
            NbtList nbtList = tag.getList("roles", NbtElement.STRING_TYPE);
            for (int i = 0; i < nbtList.size(); i++) {
                this.roles.add(nbtList.getString(i));
            }
        }
    }
}