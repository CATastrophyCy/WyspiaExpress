package org.cat.express.wyspiaexpress;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.api.event.CanSeePoison;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.aussiebox.starexpress.StarryExpressModifiers;
import org.cat.express.wyspiaexpress.components.*;
import org.cat.express.wyspiaexpress.components.roles.PlayerCultistComponent;
import org.cat.express.wyspiaexpress.config.WyspiaExpressRolesConfig;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.jetbrains.annotations.NotNull;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
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
        registerRoleLimit();
        registerAnnouncements();
        registerRoleAssigned();
        registerModifierAssigned();
        registerStringRoleMap();
        initHiddenList();
        initNeutralList();
        // allow spectator and creative mode to see poison
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
        AllowPlayerDeath.EVENT.register(((victim, killer,identifier) -> {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.getWorld());


            // disallow follower from killing their leader
            if (killer != null) {
                if (gameWorldComponent.isRole(victim, LICH) && gameWorldComponent.isRole(killer, LICH_GHOUL))
                    return false;
                if (gameWorldComponent.isRole(victim, CULT_LEADER) && gameWorldComponent.isRole(killer, CULTIST))
                    return false;
            }

            return true;
        }));
    }
    public static int GAME_START_TIME = -1;
    public static int ROUND_PLAYER_COUNT = 0; // amount of players at the start of a round
    private static final HashMap<String, Role> ROLES = new HashMap<>();
    private static final HashMap<String, Role> NON_MURDER_ROLES = new HashMap<>();
    private static final HashMap<String, Modifier> MODIFIERS = new HashMap<>();

    public static Set<Role> TRUE_NEUTRALS = new HashSet<>();
    public static Set<Role> KILLER_SIDED_NEUTRALS = new HashSet<>();

    public static Set<Role> HIDDEN_ROLES = new HashSet<>();
    public static Set<Modifier> HIDDEN_MODIFIERS = new HashSet<>();

    public static final HashMap<Role, WyspiaExpressRolesConfig.RoleBasicConfig> ROLES_BASIC_CONFIG = new HashMap<>();

    public static final List<Role> COPYCAT_ROLES = new ArrayList<>();

    public static final HashMap<String, Role> STRING_ROLES = new HashMap<>();
    public static final HashMap<String, Modifier> STRING_MODIFIERS= new HashMap<>();

    public static HashMap<String, Role> getRoles() {return ROLES;}

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
            -1,
            true
    ));
    public static Role CULT_LEADER = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "cult_leader"),
            0x9E75FF,
            false,
            false,
            Role.MoodType.FAKE,
            WatheRoles.CIVILIAN.getMaxSprintTime() * 3 / 2,
            true
    ));
    public static Role CULTIST = registerRole(new Role(
            Identifier.of(WyspiaExpress.MOD_ID, "cultist"),
            0xD5A3FF,
            false,
            false,
            Role.MoodType.FAKE,
            WatheRoles.CIVILIAN.getMaxSprintTime() * 3 / 2,
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
    private static void initHiddenList(){
        // hidden roles
        HIDDEN_ROLES.add(COPYCAT);
        // licensed villain is hidden due to bug
        HIDDEN_ROLES.add(LICENSED_VILLAIN);

        // secondary revive roles
        HIDDEN_ROLES.add(CULTIST);
        HIDDEN_ROLES.add(LICH_GHOUL);
        // obsolete roles
        HIDDEN_ROLES.add(MUZZLER);
        HIDDEN_ROLES.add(BETTER_VIGILANTE);
        HIDDEN_ROLES.add(AWESOME_BINGLUS);
        HIDDEN_ROLES.add(NOTE_TAKER);
        HIDDEN_ROLES.add(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES);
        HIDDEN_ROLES.add(NECROMANCER);
        HIDDEN_ROLES.add(JESTER);
        HIDDEN_ROLES.add(VOODOO);
        // hidden modifiers
        if(WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.killerAlwaysGuesser())
            HIDDEN_MODIFIERS.add(GUESSER);
        HIDDEN_MODIFIERS.add(BOMBER);
        HIDDEN_MODIFIERS.add(SEModifiers.LOVERS);
        HIDDEN_MODIFIERS.add(MAGNATE);
        HIDDEN_MODIFIERS.add(TASKMASTER);
        HIDDEN_MODIFIERS.add(VIOLATOR);
        HIDDEN_MODIFIERS.add(StarryExpressModifiers.ALLERGIC);
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
                for(int i = 0 ; i < WyspiaExpress.MODIFIERS_CONFIG.bomberConfig.smokeBombAmount(); i++){
                    player.giveItemStack(WyspiaExpressItems.SMOKE_BOMB.getDefaultStack());
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
            if (KinsWatheConfig.HANDLER.instance().EnableWatheModify) {
                ShopUtil.addCoin(player, - (KinsWatheConfig.HANDLER.instance().InitialKillerIncome - 100));
            }

            Set<String> roleIDs = new HashSet<>();
            while(roleIDs.size() < WyspiaExpress.ROLES_CONFIG.pickRoles()){
                String roleID;
                if(COPYCAT_ROLES.isEmpty()){
                    ArrayList<Role> killerRoles = getKillerRoles();
                    if (killerRoles.isEmpty()) {
                        roleIDs.add(getRoleName(WatheRoles.KILLER));
                        break;
                    }
                    COPYCAT_ROLES.addAll(killerRoles);
                    Collections.shuffle(COPYCAT_ROLES);
                }
                roleID = getRoleName(COPYCAT_ROLES.getFirst());
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
                    !roleMeetPlayerRequirement(r)||
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
            if(role.equals(LICH)){
                AbilityCooldownComponent.KEY.get(player).setAbilityCooldown(WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.startingCooldown());
            }
        });
        ResetPlayerEvent.EVENT.register(((playerEntity) -> {

            playerEntity.removeStatusEffect(StatusEffects.NIGHT_VISION);
            playerEntity.removeStatusEffect(StatusEffects.SLOWNESS);
            playerEntity.removeStatusEffect(StatusEffects.BLINDNESS);
            // reset components
            PlayerDepressedComponent.KEY.get(playerEntity).reset();
            PlayerFreezeComponent.KEY.get(playerEntity).reset();
            AbilityCooldownComponent.KEY.get(playerEntity).reset();
            PlayerRolePickingComponent.KEY.get(playerEntity).reset();
            PlayerSenseDeadComponent.KEY.get(playerEntity).reset();
            PlayerHearDeadComponent.KEY.get(playerEntity).reset();
            PlayerCultistComponent.KEY.get(playerEntity).reset();
            PlayerMovementComponent.KEY.get(playerEntity).reset();
        }));
    }
    private static void registerStringRoleMap(){
        WatheRoles.ROLES.forEach((r) -> {
            STRING_ROLES.put(getRoleName(r), r);
        });
        HMLModifiers.MODIFIERS.forEach((m) -> {
            STRING_MODIFIERS.put(getModifierName(m), m);
        });
    }
    private static void registerRoleLimit(){
        for( Role role : ROLES_BASIC_CONFIG.keySet()){
            var config = ROLES_BASIC_CONFIG.get(role);
            Harpymodloader.setRoleMaximum(role,config.maximumSpawn());
        }
    }
    public static boolean roleMeetPlayerRequirement(Role role){
        return roleMeetPlayerRequirement(role, ROUND_PLAYER_COUNT);
    }
    public static boolean roleMeetPlayerRequirement(Role role, int playerCount){
        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if(config == null) return true;
        return playerCount >= config.minimumPlayerSpawn()
                && playerCount <= config.maximumPlayerSpawn();
    }
    public static String getRoleName(Role role){
        if(role == null) return "Unknown role";
        return role.identifier().getPath().toLowerCase();
    }
    public static String getRoleId(Role role){
        if(role == null) return "Unknown role";
        return role.identifier().toString();
    }
    public static String getModifierId(Modifier modifier){
        if(modifier == null) return "Unknown role";
        return modifier.identifier().toString();
    }
    public static String getModifierName(Modifier modifier){
        if(modifier == null) return "Unknown modifier";
        return modifier.identifier().getPath().toLowerCase();
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
        registerRoleBasicConfig(CULT_LEADER, WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.basic);
        registerRoleBasicConfig(CULTIST, WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.cultistConfig.basic);
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
