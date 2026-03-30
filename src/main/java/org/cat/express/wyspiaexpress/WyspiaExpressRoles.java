package org.cat.express.wyspiaexpress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.config.WyspiaExpressRolesConfig;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;
import org.cat.express.wyspiaexpress.shop.ShopUtil;

import static org.BsXinQin.kinswathe.KinsWatheRoles.*;
import static org.agmas.noellesroles.Noellesroles.*;
import static org.aussiebox.starexpress.StarryExpressRoles.MUZZLER;
import static org.aussiebox.starexpress.StarryExpressRoles.STARSTRUCK;
import static pro.fazeclan.river.stupid_express.constants.SERoles.*;

public class WyspiaExpressRoles {

    public static void init() {
        registerRoleConfigs();

        registerRoleAssigned();
        registerModifierAssigned();
        limitRoleSpawn();
    }

    private static final HashMap<String, Role> ROLES = new HashMap<>();
    public static final HashMap<Role, WyspiaExpressRolesConfig.RoleBasicConfig> ROLES_BASIC_CONFIG = new HashMap<>();


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
            -1,
            true
    ));

    public static Role NOTE_TAKER = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "note_taker"),
            0x9BFFA8,
            true,
            false,
            Role.MoodType.REAL,
            200,
            false
    ));
    public static Role EDGE_LORD = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "edge_lord"),
            0x4A4A4A,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));
    public static Role GAMBLER = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "gambler"),
            0x118C4F,
            true,
            false,
            Role.MoodType.REAL,
            200,
            false
    ));
    public static Role LICH = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "lich"),
            0x37947b,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));
    public static Role LICH_GHOUL = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "lich_ghoul"),
            0x5d3954,
            false,
            true,
            Role.MoodType.FAKE,
            600,
            true
    ));

    public static Modifier EMPLOYEE = registerModifier(new Modifier(
            Identifier.of(WyspiaExpress.MOD_ID, "employee"),
            0x0D3B66,
            new ArrayList<>(List.of(CONDUCTOR)),null,
            false,false));

    public static Modifier VENT_CRAWLER = registerModifier(new Modifier(
            Identifier.of(WyspiaExpress.MOD_ID, "vent_crawler"),
            0x5A6B7A,
            new ArrayList<>(List.of(STARSTRUCK)),null,
            false,false));

    private static void registerRoleBasicConfig(Role role, WyspiaExpressRolesConfig.RoleBasicConfig config) {
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
            // this only takes into account the overworld
            Box readyArea = MapVariablesWorldComponent.KEY.get(server.getOverworld()).getReadyArea();
            Box playArea = MapVariablesWorldComponent.KEY.get(server.getOverworld()).getPlayArea();
            int count = 0;
            for(PlayerEntity player : server.getPlayerManager().getPlayerList()){
                if(readyArea.contains(player.getPos()) || playArea.contains(player.getPos())){
                    count++;
                }
            }
            for( Role role : ROLES_BASIC_CONFIG.keySet()){
                WyspiaExpressRolesConfig.RoleBasicConfig config = ROLES_BASIC_CONFIG.get(role);
                if (count >= config.minimumPlayerSpawn())
                {
                    Harpymodloader.setRoleMaximum(role,config.maximumSpawn());
                }
                else {
                    Harpymodloader.setRoleMaximum(role, 0);
                }
            }
        }));

    }
    public static void registerRoleAssigned(){
        registerStartingItems();
        registerRoleEffect();
    }
    public static void registerModifierAssigned(){
        ModifierAssigned.EVENT.register( (player, modifier) -> {
            if(modifier.equals(EMPLOYEE)){
                ItemStack itemStack = new ItemStack(WatheItems.KEY);
                itemStack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component ->
                        new LoreComponent(Text.literal("Employee Key").getWithStyle(Style.EMPTY.withItalic(false).withColor(0xFF8C00))));
                player.giveItemStack(itemStack);
            }
        });
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
    public static void registerRoleEffect(){
        ModdedRoleAssigned.EVENT.register((player, role)->{
            if(role.equals(EDGE_LORD)){
                // give edge lord night vision
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1 , 0, true, false, false));
            }
        });
        ResetPlayerEvent.EVENT.register(((playerEntity) -> {
            // remove night_vision
            playerEntity.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }));
    }

    public static void registerRoleConfigs(){
        // mine
        registerRoleBasicConfig(BANDIT, WyspiaExpress.ROLES_CONFIG.roleConfig.banditConfig.basic);
        registerRoleBasicConfig(NOTE_TAKER, WyspiaExpress.ROLES_CONFIG.roleConfig.noteTakerConfig.basic);
        registerRoleBasicConfig(EDGE_LORD,WyspiaExpress.ROLES_CONFIG.roleConfig.edgeLordConfig.basic);
        registerRoleBasicConfig(GAMBLER, WyspiaExpress.ROLES_CONFIG.roleConfig.gamblerConfig.basic);
        registerRoleBasicConfig(LICH, WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.basic);
        registerRoleBasicConfig(LICH_GHOUL, WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.ghoulConfig.basic);
        // Starry Express roles
        registerRoleBasicConfig(MUZZLER, WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.muzzlerConfig.basic);
        // Stupid Express roles
        registerRoleBasicConfig(AVARICIOUS, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.avariciousConfig.basic);
        registerRoleBasicConfig(NECROMANCER, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.necromancerConfig.basic);
        registerRoleBasicConfig(AMNESIAC, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.amnesiacConfig.basic);
        registerRoleBasicConfig(THIEF, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.thiefConfig.basic);
        // Noelles role
        registerRoleBasicConfig(MORPHLING, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.morphlingConfig.basic);
        registerRoleBasicConfig(PHANTOM, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.phantomConfig.basic);
        registerRoleBasicConfig(SWAPPER, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.swapperConfig.basic);
        registerRoleBasicConfig(NOISEMAKER,WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.noiseMakerConfig.basic);
        registerRoleBasicConfig(CORONER,WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.coronerConfig.basic);
        registerRoleBasicConfig(CONDUCTOR,WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.conductorConfig.basic);
        registerRoleBasicConfig(TRAPPER, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.trapperConfig.basic);
        registerRoleBasicConfig(MIMIC, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.mimicConfig.basic);
        registerRoleBasicConfig(VULTURE, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.vultureConfig.basic);
        // Kin's wathe roles
        registerRoleBasicConfig(BODYMAKER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.bodymakerConfig.basic);
        registerRoleBasicConfig(CLEANER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.cleanerConfig.basic);
        registerRoleBasicConfig(HUNTER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.hunterConfig.basic);
        registerRoleBasicConfig(KIDNAPPER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.kidnapperConfig.basic);
        registerRoleBasicConfig(DRUGMAKER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.drugmakerConfig.basic);
        registerRoleBasicConfig(DETECTIVE, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.detectiveConfig.basic);
        registerRoleBasicConfig(PHYSICIAN, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.physicianConfig.basic);

    }
}
