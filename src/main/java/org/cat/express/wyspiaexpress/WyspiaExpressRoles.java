package org.cat.express.wyspiaexpress;

import java.util.HashMap;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.config.WyspiaExpressServerConfig;

import static org.BsXinQin.kinswathe.KinsWatheRoles.*;
import static org.agmas.noellesroles.Noellesroles.*;
import static org.aussiebox.starexpress.StarryExpressRoles.MUZZLER;
import static pro.fazeclan.river.stupid_express.constants.SERoles.AVARICIOUS;
import static pro.fazeclan.river.stupid_express.constants.SERoles.NECROMANCER;

public class WyspiaExpressRoles {

    public static void init() {
        registerRoleConfigs();
        limitRoleSpawn();
    }

    private static final HashMap<String, Role> ROLES = new HashMap<>();
    public static final HashMap<Role, WyspiaExpressServerConfig.RoleBasicConfig> ROLES_BASIC_CONFIG = new HashMap<>();

    // this map here is necessary right now because of how the config is structured, I will change this in the future
    public static final HashMap<Role, WyspiaExpressServerConfig.ShopConfig> ROLES_SHOP_CONFIG = new HashMap<>();

    public static HashMap<String, Role> getRoles() {return ROLES;}
    private static final HashMap<String, Modifier> MODIFIERS = new HashMap<>();
    public static HashMap<String, Modifier> getModifiers() {return MODIFIERS;}

    // This bandit is an attempt at making a custom Role, right now it seems to work
    public static Role BANDIT = registerRole(new Role(
            Identifier.of(Wyspiaexpress.MOD_ID, "bandit"),
            0xCC0066,
            false,
            true,
            Role.MoodType.FAKE,
            Wyspiaexpress.CONFIG.roleConfig.banditConfig.basic.maxSprintTime(),
            Wyspiaexpress.CONFIG.roleConfig.banditConfig.basic.seeTimer()
    ));

    private static void registerRoleBasicConfig(Role role, WyspiaExpressServerConfig.RoleBasicConfig config) {
        ROLES_BASIC_CONFIG.put(role, config);
    }
    public static Role registerRole(Role role) {
        WatheRoles.registerRole(role);
        ROLES.put(role.identifier().getPath(), role);
        return role;
    }

    public static Modifier registerModifier(Modifier modifier) {
        HMLModifiers.registerModifier(modifier);
        MODIFIERS.put(modifier.identifier().getPath(), modifier);
        return modifier;
    }

    public static void limitRoleSpawn(){
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            for( Role role : ROLES_BASIC_CONFIG.keySet()){
                WyspiaExpressServerConfig.RoleBasicConfig config = ROLES_BASIC_CONFIG.get(role);
                if (server.getPlayerManager().getCurrentPlayerCount() >= config.minimumPlayerSpawn())
                {
                    Harpymodloader.setRoleMaximum(role,config.maximumSpawn());
                }
                else {
                    Harpymodloader.setRoleMaximum(role, 0);
                }
            }
        }));

    }
    public static void registerRoleConfigs(){

        registerRoleBasicConfig(BANDIT, Wyspiaexpress.CONFIG.roleConfig.banditConfig.basic);
        ROLES_SHOP_CONFIG.put(BANDIT,Wyspiaexpress.CONFIG.roleConfig.banditConfig.basic.shopConfig);
        // Starry Express roles
        registerRoleBasicConfig(MUZZLER, Wyspiaexpress.CONFIG.roleConfig.muzzlerConfig.basic);
        ROLES_SHOP_CONFIG.put(MUZZLER,Wyspiaexpress.CONFIG.roleConfig.muzzlerConfig.basic.shopConfig);
        // Stupid Express roles
        registerRoleBasicConfig(AVARICIOUS, Wyspiaexpress.CONFIG.roleConfig.avariciousConfig.basic);
        ROLES_SHOP_CONFIG.put(AVARICIOUS,Wyspiaexpress.CONFIG.roleConfig.necromancerConfig.basic.shopConfig);
        registerRoleBasicConfig(NECROMANCER, Wyspiaexpress.CONFIG.roleConfig.necromancerConfig.basic);
        ROLES_SHOP_CONFIG.put(NECROMANCER,Wyspiaexpress.CONFIG.roleConfig.necromancerConfig.basic.shopConfig);
        // Noelles role
        registerRoleBasicConfig(MORPHLING, Wyspiaexpress.CONFIG.roleConfig.morphlingConfig.basic);
        ROLES_SHOP_CONFIG.put(MORPHLING,Wyspiaexpress.CONFIG.roleConfig.morphlingConfig.basic.shopConfig);
        registerRoleBasicConfig(PHANTOM, Wyspiaexpress.CONFIG.roleConfig.phantomConfig.basic);
        ROLES_SHOP_CONFIG.put(PHANTOM,Wyspiaexpress.CONFIG.roleConfig.phantomConfig.basic.shopConfig);
        registerRoleBasicConfig(SWAPPER, Wyspiaexpress.CONFIG.roleConfig.swapperConfig.basic);
        ROLES_SHOP_CONFIG.put(SWAPPER,Wyspiaexpress.CONFIG.roleConfig.swapperConfig.basic.shopConfig);
        // Kin's wathe roles
        registerRoleBasicConfig(BODYMAKER, Wyspiaexpress.CONFIG.roleConfig.bodymakerConfig.basic);
        ROLES_SHOP_CONFIG.put(BODYMAKER,Wyspiaexpress.CONFIG.roleConfig.bodymakerConfig.basic.shopConfig);
        registerRoleBasicConfig(CLEANER, Wyspiaexpress.CONFIG.roleConfig.cleanerConfig.basic);
        ROLES_SHOP_CONFIG.put(CLEANER,Wyspiaexpress.CONFIG.roleConfig.cleanerConfig.basic.shopConfig);
        registerRoleBasicConfig(HUNTER, Wyspiaexpress.CONFIG.roleConfig.hunterConfig.basic);
        ROLES_SHOP_CONFIG.put(HUNTER,Wyspiaexpress.CONFIG.roleConfig.hunterConfig.basic.shopConfig);
        registerRoleBasicConfig(KIDNAPPER, Wyspiaexpress.CONFIG.roleConfig.kidnapperConfig.basic);
        ROLES_SHOP_CONFIG.put(KIDNAPPER,Wyspiaexpress.CONFIG.roleConfig.kidnapperConfig.basic.shopConfig);
        registerRoleBasicConfig(DRUGMAKER, Wyspiaexpress.CONFIG.roleConfig.drugmakerConfig.basic);
        ROLES_SHOP_CONFIG.put(DRUGMAKER,Wyspiaexpress.CONFIG.roleConfig.drugmakerConfig.basic.shopConfig);
    }
}
