package org.cat.express.wyspiaexpress.config;


import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import java.util.ArrayList;
import java.util.List;


// uses owo-lib annotation system to auto generate config file and also syncing
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "wyspiaexpress-server", wrapperName = "WyspiaExpressServerConfig")
public class ServerConfig {

    @SectionHeader("role_config")

    @Comment("""
            Valid shop entries:
                Format: ITEM_NAME;PRICE;PURCHASE_LIMIT;TYPE
                Example: KNIFE;50;1;WEAPON
                Type can take values from: WEAPON, TOOL, POISON; it only affects the ui in shop
                PURCHASE_LIMIT is still WIP, not implemented yet
           \s
            Valid items:\s
            KNIFE, REVOLVER, GRENADE, PSYCHO_MODE, POISON_VIAL, SCORPION, FIRECRACKER, LOCKPICK,
            CROWBAR, BODY_BAG, BLACKOUT, NOTE,
            TAPE,
            FAKE_KNIFE, DELUSION_VIAL, DEFENSE_VIAL, ROLE_MINE, MASTER_KEY, FAKE_REVOLVER,
            PAN, COOKED_PORKCHOP, COOKED_CHIKEN, COOKED_BEEF, PILL, MEDICAL_KIT, ACID_BARREL, HUNTING_KNIFE, KNOCKOUT_DRUG, DREAM_IMPRINT, BLOWGUN, POISON_INJECTOR,
            FUN_BOX
           \s""")
    public List<String> exampleShopEntries = new ArrayList<>(List.of("KNIFE;50;-1;WEAPON", "PSYCHO_MODE;300;-1;WEAPON"));


    @Comment("Configuration for roles.")
    @Nest public RoleConfig roleConfig = new RoleConfig();

    @Comment("Configuration for items")
    @Nest public ItemConfig itemConfig = new ItemConfig();
}
