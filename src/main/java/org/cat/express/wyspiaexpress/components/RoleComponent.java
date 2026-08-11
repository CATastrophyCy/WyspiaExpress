package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.HashSet;
import java.util.Set;

public class RoleComponent implements AutoSyncedComponent {
    public static final ComponentKey<RoleComponent> KEY =
            ComponentRegistry.getOrCreate(
                    Identifier.of(WyspiaExpress.MOD_ID, "role_component"),
                    RoleComponent.class
            );
    @NotNull
    private final World world;
    public final Set<String> disabledRoles = new HashSet<>();
    public final Set<String> disabledModifiers = new HashSet<>();

    public RoleComponent(@NotNull World world) {
        this.world = world;
    }
    public void init(){
        disabledRoles.addAll(HarpyModLoaderConfig.HANDLER.instance().disabled);
        disabledModifiers.addAll(HarpyModLoaderConfig.HANDLER.instance().disabledModifiers);
        sync();
    }
    public void startRound(){
        disabledRoles.clear();
        disabledModifiers.clear();

        disabledRoles.addAll(WatheRoles.ROLES.stream().filter(role -> !isValidRole(role)).map(WyspiaExpressRoles::getRoleId).toList());
        disabledModifiers.addAll(HarpyModLoaderConfig.HANDLER.instance().disabledModifiers);
        sync();
    }
    public void endRound(){
        disabledRoles.removeIf(roleId -> !HarpyModLoaderConfig.HANDLER.instance().disabled.contains(roleId));
        sync();
    }
    public void sync() {
        KEY.sync(this.world);
    }
    private static boolean isValidRole(Role role) {
        String roleId = WyspiaExpressRoles.getRoleId(role);

        return !HarpyModLoaderConfig.HANDLER.instance().disabled.contains(roleId)
                && WyspiaExpressRoles.roleMeetPlayerRequirement(role);
    }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        tag.put("DisabledRoles", toNbtList(this.disabledRoles));
        tag.put("DisabledModifiers", toNbtList(this.disabledModifiers));
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.disabledRoles.clear();
        this.disabledModifiers.clear();

        fromNbtList(tag, "DisabledRoles", this.disabledRoles);
        fromNbtList(tag, "DisabledModifiers", this.disabledModifiers);
    }

    private static NbtList toNbtList(Set<String> set) {
        NbtList list = new NbtList();
        for (String entry : set) {
            list.add(NbtString.of(entry));
        }
        return list;
    }

    private static void fromNbtList(NbtCompound tag, String key, Set<String> set) {
        if (tag.contains(key, NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList(key, NbtElement.STRING_TYPE);
            for (NbtElement element : list) {
                set.add(element.asString());
            }
        }
    }



}