package org.cat.express.wyspiaexpress;

import java.util.HashMap;
import java.util.List;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.config.WyspiaExpressServerConfig;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;
import org.cat.express.wyspiaexpress.shop.ShopUtil;

import static org.BsXinQin.kinswathe.KinsWatheRoles.*;
import static org.agmas.noellesroles.Noellesroles.*;
import static org.aussiebox.starexpress.StarryExpressRoles.MUZZLER;
import static pro.fazeclan.river.stupid_express.constants.SERoles.AVARICIOUS;
import static pro.fazeclan.river.stupid_express.constants.SERoles.NECROMANCER;

public class WyspiaExpressRoles {

    public static void init() {
        registerRoleConfigs();
        registerStartingItems();
        limitRoleSpawn();
    }

    private static final HashMap<String, Role> ROLES = new HashMap<>();
    public static final HashMap<Role, WyspiaExpressServerConfig.RoleBasicConfig> ROLES_BASIC_CONFIG = new HashMap<>();


    public static HashMap<String, Role> getRoles() {return ROLES;}
    private static final HashMap<String, Modifier> MODIFIERS = new HashMap<>();
    public static HashMap<String, Modifier> getModifiers() {return MODIFIERS;}

    // This bandit is an attempt at making a custom Role, right now it seems to work
    public static Role BANDIT = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "bandit"),
            0xCC0066,
            false,
            true,
            Role.MoodType.FAKE,
            WyspiaExpress.CONFIG.roleConfig.banditConfig.basic.maxSprintTime(),
            WyspiaExpress.CONFIG.roleConfig.banditConfig.basic.seeTimer()
    ));

    public static Role NOTE_TAKER = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "note_taker"),
            0x9BFFA8,
            true,
            false,
            Role.MoodType.REAL,
            WyspiaExpress.CONFIG.roleConfig.noteTakerConfig.basic.maxSprintTime(),
            WyspiaExpress.CONFIG.roleConfig.noteTakerConfig.basic.seeTimer()
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
    public static void registerStartingItems(){
        ModdedRoleAssigned.EVENT.register((player, role)->{
            var basicConfig = ROLES_BASIC_CONFIG.get(role);
            if(basicConfig != null){
                List<EnumShopEntry> startingItems = basicConfig.items();
                List<Integer> startingItemAmount = basicConfig.itemAmount();
                for(int i = 0; i < startingItems.size(); i++) {
                    EnumShopEntry entry = startingItems.get(i);
                    int amount = 1;
                    if( i < startingItemAmount.size()){
                        amount = startingItemAmount.get(i);
                    }
                    //
                    ItemStack item = ShopUtil.fromEnumShopEntry(entry);
                    item.setCount(amount);
                    player.giveItemStack(item);
                }
            }
        });
    }
    public static void registerRoleConfigs(){
        // mine
        registerRoleBasicConfig(BANDIT, WyspiaExpress.CONFIG.roleConfig.banditConfig.basic);
        registerRoleBasicConfig(NOTE_TAKER, WyspiaExpress.CONFIG.roleConfig.noteTakerConfig.basic);
        // Starry Express roles
        registerRoleBasicConfig(MUZZLER, WyspiaExpress.CONFIG.roleConfig.muzzlerConfig.basic);

        // Stupid Express roles
        registerRoleBasicConfig(AVARICIOUS, WyspiaExpress.CONFIG.roleConfig.avariciousConfig.basic);

        registerRoleBasicConfig(NECROMANCER, WyspiaExpress.CONFIG.roleConfig.necromancerConfig.basic);

        // Noelles role
        registerRoleBasicConfig(MORPHLING, WyspiaExpress.CONFIG.roleConfig.morphlingConfig.basic);

        registerRoleBasicConfig(PHANTOM, WyspiaExpress.CONFIG.roleConfig.phantomConfig.basic);

        registerRoleBasicConfig(SWAPPER, WyspiaExpress.CONFIG.roleConfig.swapperConfig.basic);

        registerRoleBasicConfig(NOISEMAKER,WyspiaExpress.CONFIG.roleConfig.noiseMakerConfig.basic);
        // Kin's wathe roles
        registerRoleBasicConfig(BODYMAKER, WyspiaExpress.CONFIG.roleConfig.bodymakerConfig.basic);

        registerRoleBasicConfig(CLEANER, WyspiaExpress.CONFIG.roleConfig.cleanerConfig.basic);

        registerRoleBasicConfig(HUNTER, WyspiaExpress.CONFIG.roleConfig.hunterConfig.basic);

        registerRoleBasicConfig(KIDNAPPER, WyspiaExpress.CONFIG.roleConfig.kidnapperConfig.basic);

        registerRoleBasicConfig(DRUGMAKER, WyspiaExpress.CONFIG.roleConfig.drugmakerConfig.basic);

    }
}
