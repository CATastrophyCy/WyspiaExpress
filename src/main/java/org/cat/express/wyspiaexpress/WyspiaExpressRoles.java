package org.cat.express.wyspiaexpress;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.api.event.CanSeePoison;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
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
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.cat.express.wyspiaexpress.components.*;
import org.cat.express.wyspiaexpress.config.WyspiaExpressRolesConfig;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.jetbrains.annotations.NotNull;
import pro.fazeclan.river.stupid_express.role.avaricious.AvariciousGoldHandler;

import java.util.*;

import static org.BsXinQin.kinswathe.KinsWatheRoles.*;
import static org.agmas.noellesroles.Noellesroles.*;
import static org.aussiebox.starexpress.StarryExpressRoles.MUZZLER;
import static org.aussiebox.starexpress.StarryExpressRoles.STARSTRUCK;
import static pro.fazeclan.river.stupid_express.constants.SERoles.*;

public class WyspiaExpressRoles {

    public static void init() {
        registerRoleConfigs();
        registerAnnouncements();
        registerRoleAssigned();
        registerModifierAssigned();
        registerStringRoleMap();
        limitRoleSpawn();
        initNeutralList();
        // allow spectator and creative mod to see poison
        CanSeePoison.EVENT.register((player)->{
            if (GameFunctions.isPlayerSpectatingOrCreative(player)) {
                return true;
            }
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
            Role role = gameWorldComponent.getRole(player);
            if(role == null) return false;
            var config = ROLES_BASIC_CONFIG.get(role);
            if(config == null) return false;
            return config.seePoison();
        });
    }
    public static int GAME_START_TIME = -1;
    public static int PLAYER_COUNT = 0;
    private static final HashMap<String, Role> ROLES = new HashMap<>();
    private static final HashMap<String, Role> NON_MURDER_ROLES = new HashMap<>();

    public static Set<Role> TRUE_NEUTRALS = new HashSet<>();
    public static Set<Role> KILLER_SIDED_NEUTRALS = new HashSet<>();
    public static final HashMap<Role, WyspiaExpressRolesConfig.RoleBasicConfig> ROLES_BASIC_CONFIG = new HashMap<>();

    public static final List<Role> COPYCAT_ROLES = new ArrayList<>();
    public static final HashMap<String, Role> STRING_ROLES = new HashMap<>();// a map for role picking widget

    public static HashMap<String, Role> getRoles() {return ROLES;}
    private static final HashMap<String, Modifier> MODIFIERS = new HashMap<>();
    public static HashMap<String, Modifier> getModifiers() {return MODIFIERS;}


    public static Role COPYCAT = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "copycat"),
            0x20B2AA,
            false,
            true,
            Role.MoodType.FAKE,
            -1,
            true
    ));
    public static Role OUTLAW = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "outlaw"),
            0xa3671d,
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
    public static Role EDDIE_WAFFLES = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "eddie_waffles"),
            0x8A2727,
            false,
            false,
            Role.MoodType.FAKE,
            600,
            true
    ));
    public static Role CULT_LEADER = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "cult_leader"),
            0x9E75FF,
            false,
            false,
            Role.MoodType.FAKE,
            600,
            true
    ));
    public static Role CULTIST = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "cultist"),
            0xD5A3FF,
            false,
            false,
            Role.MoodType.FAKE,
            600,
            true
    ));
    public static Modifier EMPLOYEE = registerModifier(new Modifier(
            Identifier.of(WyspiaExpress.MOD_ID, "employee"),
            0x0D3B66,
            new ArrayList<>(List.of(CONDUCTOR, LICENSED_VILLAIN)),null,
            false,false));

    public static Modifier VENT_CRAWLER = registerModifier(new Modifier(
            Identifier.of(WyspiaExpress.MOD_ID, "vent_crawler"),
            0x5A6B7A,
            new ArrayList<>(List.of(STARSTRUCK, HUNTER)),null,
            false,false));

    public static Modifier ELUSIVE = registerModifier(new Modifier(
            Identifier.of(WyspiaExpress.MOD_ID, "elusive"),
            0x96FFA9,
            new ArrayList<>(List.of(THIEF,NOISEMAKER, LICENSED_VILLAIN)),null,
            false,false));
    public static Modifier BOMBER = registerModifier(new Modifier(
            Identifier.of(WyspiaExpress.MOD_ID, "bomber"),
            0xB51010,
            null,null,
            false,true));
    private static void registerRoleBasicConfig(Role role, WyspiaExpressRolesConfig.RoleBasicConfig config) {
        ROLES_BASIC_CONFIG.put(role, config);
    }
    private static void registerAnnouncements(){

    }
    private static Role registerRole(Role role) {
        WatheRoles.registerRole(role);
        ROLES.put(role.identifier().getPath(), role);
        return role;
    }
    private static Role registerNonMurderRole(Role role){
        Harpymodloader.NON_MURDER_ROLES.add(role);
        NON_MURDER_ROLES.put(role.identifier().getPath(), role);
        return role;
    }
    private static Modifier registerModifier(Modifier modifier) {
        HMLModifiers.registerModifier(modifier);
        MODIFIERS.put(modifier.identifier().getPath(), modifier);
        return modifier;
    }
    private static void initNeutralList(){
        TRUE_NEUTRALS.add(EDDIE_WAFFLES);
        TRUE_NEUTRALS.add(AMNESIAC);
        TRUE_NEUTRALS.add(ARSONIST);
        TRUE_NEUTRALS.add(LICENSED_VILLAIN);
        TRUE_NEUTRALS.add(THIEF);
        TRUE_NEUTRALS.add(INITIATE);
        TRUE_NEUTRALS.add(CULT_LEADER);
        TRUE_NEUTRALS.add(CULTIST);

        KILLER_SIDED_NEUTRALS.add(JESTER);
        KILLER_SIDED_NEUTRALS.add(VULTURE);
        KILLER_SIDED_NEUTRALS.add(EXECUTIONER);
        KILLER_SIDED_NEUTRALS.add(DREAMER);
        KILLER_SIDED_NEUTRALS.add(HACKER);
    }
    private static void limitRoleSpawn(){
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            // this only takes into account the overworld
            Box readyArea = MapVariablesWorldComponent.KEY.get(server.getOverworld()).getReadyArea();
            Box playArea = MapVariablesWorldComponent.KEY.get(server.getOverworld()).getPlayArea();
            PLAYER_COUNT = 0;
            for(PlayerEntity player : server.getPlayerManager().getPlayerList()){
                if(readyArea.contains(player.getPos()) || playArea.contains(player.getPos())
                    && !player.isSpectator()
                )
                {
                    PLAYER_COUNT++;
                }
            }
            for( Role role : ROLES_BASIC_CONFIG.keySet()){
                WyspiaExpressRolesConfig.RoleBasicConfig config = ROLES_BASIC_CONFIG.get(role);
                if (PLAYER_COUNT >= config.minimumPlayerSpawn() && PLAYER_COUNT <= config.maximumPlayerSpawn())
                {
                    Harpymodloader.setRoleMaximum(role,config.maximumSpawn());
                }
                else {
                    Harpymodloader.setRoleMaximum(role, 0);
                }
            }
        }));

    }
    private static void registerRoleAssigned(){
        registerStartingItems();
        registerRoleEffect();
        registerCopyCat();
    }
    private static void registerModifierAssigned(){
        ModifierAssigned.EVENT.register( (player, modifier) -> {
            if(modifier.equals(EMPLOYEE)){
                ItemStack itemStack = new ItemStack(WatheItems.KEY);
                itemStack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component ->
                        new LoreComponent(Text.literal("Employee Key").getWithStyle(Style.EMPTY.withItalic(false).withColor(0xFF8C00))));
                player.giveItemStack(itemStack);
            }
            if(modifier.equals(BOMBER)){
                for(int i = 0 ; i < WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.grenadeAmount(); i++){
                    player.giveItemStack(WatheItems.GRENADE.getDefaultStack());
                }
            }
        });
    }
    private static void registerStartingItems(){
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
                    ItemStack item = ShopUtil.fromEnumShopEntry(entry).getDefaultStack();
                    item.setCount(amount);
                    player.giveItemStack(item);
                }
            }
        });
    }
    private static void registerCopyCat(){
        ModdedRoleAssigned.EVENT.register((player, role)->{
            if(!role.equals(COPYCAT))return;

            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
            if (KinsWatheConfig.HANDLER.instance().EnableWatheModify) {
                playerShop.addToBalance(- (KinsWatheConfig.HANDLER.instance().InitialKillerIncome - 100) * 2);
            }
            Set<String> roleIDs = new HashSet<>();
            while(roleIDs.size() < WyspiaExpress.ROLES_CONFIG.pickRoles()){
                String roleID;
                if(COPYCAT_ROLES.isEmpty()){
                    ArrayList<Role> killerRoles = getKillerRoles();
                    if (killerRoles.isEmpty()) {
                        roleIDs.add(getRoleString(WatheRoles.KILLER));
                        break;
                    }
                    COPYCAT_ROLES.addAll(killerRoles);
                    Collections.shuffle(COPYCAT_ROLES);
                }
                roleID = getRoleString(COPYCAT_ROLES.getFirst());
                COPYCAT_ROLES.removeFirst();
                roleIDs.add(roleID);
            }
            PlayerRolePickingComponent component = PlayerRolePickingComponent.KEY.get(player);
            component.set(new ArrayList<>(roleIDs), GameConstants.getInTicks(0,WyspiaExpress.ROLES_CONFIG.randomRoleTime()));
        });
    }

    private static @NotNull ArrayList<Role> getKillerRoles() {
        ArrayList<Role> killerRoles = new ArrayList<>(WatheRoles.ROLES);

        killerRoles.removeIf(r -> (
                (
                    ((ROLES_BASIC_CONFIG.get(r) != null) && (( PLAYER_COUNT < ROLES_BASIC_CONFIG.get(r).minimumPlayerSpawn() || PLAYER_COUNT > ROLES_BASIC_CONFIG.get(r).maximumPlayerSpawn()))) ||
                    Harpymodloader.VANNILA_ROLES.contains(r) ||
                    !r.canUseKiller() ||
                    HarpyModLoaderConfig.HANDLER.instance().disabled.contains(r.identifier().toString())
                )
        ));
        return killerRoles;
    }

    private static void registerRoleEffect(){
        ModdedRoleAssigned.EVENT.register((player, role)->{
            if(role.equals(EDGE_LORD)){
                // give edge lord night vision
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1 , 0, true, false, false));
            }
            if (role.equals(AVARICIOUS)) {
                AvariciousGoldHandler.gameStartTime = GAME_START_TIME;
            }
        });
        ResetPlayerEvent.EVENT.register(((playerEntity) -> {
            // remove night_vision
            playerEntity.removeStatusEffect(StatusEffects.NIGHT_VISION);
            // reset components
            PlayerDepressedComponent.KEY.get(playerEntity).reset();
            PlayerFreezeComponent.KEY.get(playerEntity).reset();
            AbilityCooldownComponent.KEY.get(playerEntity).reset();
            PlayerRolePickingComponent.KEY.get(playerEntity).reset();
            PlayerSenseDeadComponent.KEY.get(playerEntity).reset();
        }));
    }
    private static void registerStringRoleMap(){
        WatheRoles.ROLES.forEach((r) -> {
            STRING_ROLES.put(getRoleString(r), r);
        });
    }
    public static String getRoleString(Role role){
        return role.identifier().getPath().toLowerCase();
    }
    private static void registerRoleConfigs(){
        // mine
        registerRoleBasicConfig(COPYCAT, WyspiaExpress.ROLES_CONFIG.roleConfig.copycatConfig.basic);
        registerRoleBasicConfig(OUTLAW, WyspiaExpress.ROLES_CONFIG.roleConfig.outlawConfig.basic);
        registerRoleBasicConfig(NOTE_TAKER, WyspiaExpress.ROLES_CONFIG.roleConfig.noteTakerConfig.basic);
        registerRoleBasicConfig(EDGE_LORD,WyspiaExpress.ROLES_CONFIG.roleConfig.edgeLordConfig.basic);
        registerRoleBasicConfig(GAMBLER, WyspiaExpress.ROLES_CONFIG.roleConfig.gamblerConfig.basic);
        registerRoleBasicConfig(LICH, WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.basic);
        registerRoleBasicConfig(LICH_GHOUL, WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.ghoulConfig.basic);
        registerRoleBasicConfig(EDDIE_WAFFLES, WyspiaExpress.ROLES_CONFIG.roleConfig.eddieWafflesConfig.basic);
        // wathe
        registerRoleBasicConfig(WatheRoles.VIGILANTE, WyspiaExpress.ROLES_CONFIG.roleConfig.vigilanteConfig.basic);
        // Starry Express roles
        registerRoleBasicConfig(MUZZLER, WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.muzzlerConfig.basic);
        registerRoleBasicConfig(STARSTRUCK, WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.starstruckConfig.basic);
        // Stupid Express roles
        registerRoleBasicConfig(AVARICIOUS, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.avariciousConfig.basic);
        registerRoleBasicConfig(NECROMANCER, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.necromancerConfig.basic);
        registerRoleBasicConfig(AMNESIAC, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.amnesiacConfig.basic);
        registerRoleBasicConfig(THIEF, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.thiefConfig.basic);
        registerRoleBasicConfig(ARSONIST, WyspiaExpress.ROLES_CONFIG.roleConfig.stupidExpress.arsonistConfig.basic);
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
        registerRoleBasicConfig(EXECUTIONER, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.executionerConfig.basic);
        // Kin's wathe roles
        registerRoleBasicConfig(BODYMAKER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.bodymakerConfig.basic);
        registerRoleBasicConfig(CLEANER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.cleanerConfig.basic);
        registerRoleBasicConfig(HUNTER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.hunterConfig.basic);
        registerRoleBasicConfig(KIDNAPPER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.kidnapperConfig.basic);
        registerRoleBasicConfig(DRUGMAKER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.drugmakerConfig.basic);
        registerRoleBasicConfig(DETECTIVE, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.detectiveConfig.basic);
        registerRoleBasicConfig(JUDGE, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.judgeConfig.basic);
        registerRoleBasicConfig(BELLRINGER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.bellringerConfig.basic);
        registerRoleBasicConfig(ROBOT, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.robotConfig.basic);
        registerRoleBasicConfig(PHYSICIAN, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.physicianConfig.basic);
        registerRoleBasicConfig(TECHNICIAN, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.technicianConfig.basic);
        registerRoleBasicConfig(COOK,WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.cookConfig.basic);
        registerRoleBasicConfig(HACKER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.hackerConfig.basic);
        registerRoleBasicConfig(DREAMER, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.dreamerConfig.basic);
        registerRoleBasicConfig(LICENSED_VILLAIN, WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.licensedVillainConfig.basic);
    }
}
