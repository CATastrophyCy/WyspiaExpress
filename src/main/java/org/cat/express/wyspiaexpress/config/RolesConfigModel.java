package org.cat.express.wyspiaexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.Sync;

import java.util.ArrayList;
import java.util.List;

@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "wyspiaexpress/roles", wrapperName = "WyspiaExpressRolesConfig")
public class RolesConfigModel {

    @Comment("""
    === ROLE CONFIGURATION GUIDE ===
    Inside each role's 'basic' config, you can configure:
    - enableShop: Whether this role has access to the shop
    - passiveIncome: Whether they generate passive income. Has no effect on other mod's role that has passive income
    - taskIncome: Amount of income from tasks. Know that civilian and some roles have default 50 income, and this adds on top of that
    - items & itemAmount: Starting items and their quantities
    - minimumPlayerSpawn (1-20): Min players required to spawn this role
    - maximumSpawn (0-20): Max number of this role allowed
   \s
    Valid shop entries:
        Format: ITEM_NAME;PRICE;PURCHASE_LIMIT;TYPE
        Example: ["KNIFE;50;-1;WEAPON", "POISON_VIAL;100;-1;POISON"]
        Type can take values from: WEAPON, TOOL, POISON; it only affects the ui in shop
        PURCHASE_LIMIT is still WIP, not implemented yet

    Valid items:\s
        KNIFE, REVOLVER, GRENADE, PSYCHO_MODE, POISON_VIAL, SCORPION, FIRECRACKER, LOCKPICK,
        CROWBAR, BODY_BAG, BLACKOUT, NOTE, TAPE, FAKE_KNIFE, DELUSION_VIAL, DEFENSE_VIAL,\s
        ROLE_MINE, MASTER_KEY, FAKE_REVOLVER, PAN, COOKED_PORKCHOP, COOKED_CHIKEN,\s
        COOKED_BEEF, PILL, MEDICAL_KIT, ACID_BARREL, HUNTING_KNIFE, KNOCKOUT_DRUG,\s
        DREAM_IMPRINT, BLOWGUN, POISON_INJECTOR, WRENCH, CAPTURE_DEVICE,\s
        ICON_POWER_RESTORATION, ICON_WEAPON_COOLDOWN_REFRESH, ICON_ABILITY_COOLDOWN_REFRESH, ICON_POTION_EFFECT_REFRESH,
        FUN_BOX, MEGAPHONE, OUTLAW_REVOLVER
   \s""")
    @Nest public RoleConfig roleConfig = new RoleConfig();
}
