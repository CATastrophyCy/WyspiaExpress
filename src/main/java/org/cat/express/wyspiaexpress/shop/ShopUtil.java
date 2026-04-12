package org.cat.express.wyspiaexpress.shop;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;
import org.agmas.noellesroles.ModItems;
import org.aussiebox.starexpress.item.StarryExpressItems;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.config.ShopConfig;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ShopUtil {
    public static final List<Item> FUN_BOX_RARE_POOL = new ArrayList<>();
    public static final List<Item> FUN_BOX_NORMAL_POOL = new ArrayList<>();

    public static boolean handlePurchase(@NotNull PlayerEntity player, int balance, @NotNull Item item, int price) {
        if (balance >= price && !player.getItemCooldownManager().isCoolingDown(item)) {
            giveItem(player, item);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(WatheSounds.UI_SHOP_BUY), SoundCategory.PLAYERS, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F, serverPlayer.getRandom().nextLong()));
            }

            return true;
        } else {
            player.sendMessage(Text.translatable("shop.purchase_failed").withColor(11141120), true);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(WatheSounds.UI_SHOP_BUY_FAIL), SoundCategory.PLAYERS, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F, serverPlayer.getRandom().nextLong()));
            }

            return false;
        }
    }

    private static void openFunBox(@NotNull PlayerEntity player){
        if(FUN_BOX_RARE_POOL.isEmpty() || FUN_BOX_NORMAL_POOL.isEmpty()){
            initFunBoxPool();
        }
        if(player.getWorld().getRandom().nextDouble() < WyspiaExpress.ROLES_CONFIG.roleConfig.gamblerConfig.missChance()) {
            PlayerShopComponent.KEY.get(player).addToBalance(WyspiaExpress.ROLES_CONFIG.roleConfig.gamblerConfig.missCompensationCoin());
            return; // loss the gamble
        }
        if( player.getWorld().getRandom().nextDouble() < WyspiaExpress.ROLES_CONFIG.roleConfig.gamblerConfig.goodPoolChance()){
            PlayerInventory inv = player.getInventory();
            Set<Item> hotbarItems = new HashSet<>();
            // Collect unique items from hotbar (slots 0-8)
            for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty()) {
                    hotbarItems.add(stack.getItem());
                }
            }
            // don't give item that they already have
            List<Item> filtered = FUN_BOX_RARE_POOL.stream()
                    .filter(item -> !hotbarItems.contains(item))
                    .toList();
            if(!filtered.isEmpty()) {
                int random = player.getWorld().getRandom().nextInt(filtered.size());
                giveItem(player, filtered.get(random));
                return;
            }
        }
        // bad pool
        Item item = FUN_BOX_NORMAL_POOL.get(player.getWorld().getRandom().nextInt(FUN_BOX_NORMAL_POOL.size()));
        giveItem(player, item);
    }

    private static void initFunBoxPool(){
        if(FUN_BOX_RARE_POOL.isEmpty()) {
            List<EnumShopEntry> items = WyspiaExpress.ROLES_CONFIG.roleConfig.gamblerConfig.goodPool();
            for (EnumShopEntry entry : items) {
                Item item = ShopUtil.fromEnumShopEntry(entry);
                FUN_BOX_RARE_POOL.add(item);
            }
        }
        if(FUN_BOX_NORMAL_POOL.isEmpty()) {
            List<EnumShopEntry> items = WyspiaExpress.ROLES_CONFIG.roleConfig.gamblerConfig.badPool();
            for (EnumShopEntry entry : items) {
                Item item = ShopUtil.fromEnumShopEntry(entry);
                FUN_BOX_NORMAL_POOL.add(item);
            }
        }
    }
    public static void handlePotionRefresh(PlayerEntity player) {
        player.getItemCooldownManager().set(KinsWatheItems.ICON_POTION_EFFECT_REFRESH,
                GameConstants.ITEM_COOLDOWNS.get(KinsWatheItems.ICON_POTION_EFFECT_REFRESH));
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        for(ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            if (serverPlayer != null && gameWorld.canUseKillerFeatures(serverPlayer)) {
                PlayerEffectComponent playerEffect = PlayerEffectComponent.KEY.get(serverPlayer);
                serverPlayer.sendMessage(Text.translatable("tip.kinswathe.hacker.potion_effect_refresh").withColor(Color.YELLOW.getRGB()), true);
                serverPlayer.playSoundToPlayer(SoundEvents.ENTITY_ALLAY_ITEM_GIVEN, SoundCategory.PLAYERS, 1.0F, 1.0F);

                for(StatusEffectInstance effect : serverPlayer.getStatusEffects()) {
                    var types =  effect.getEffectType();
                    if (! types.equals(StatusEffects.INVISIBILITY) && ! types.equals(StatusEffects.NIGHT_VISION)) {
                        serverPlayer.removeStatusEffect(effect.getEffectType());
                    }
                }
                playerEffect.reset();
            }
        }
    }
    public static List<ShopEntry> fromShopEntryConfigs(List<ShopConfig.ShopEntryConfig> shopConfigs ) {
        List<ShopEntry> shopEntries = new ArrayList<>();
        for( ShopConfig.ShopEntryConfig shopConfig : shopConfigs ) {
            shopEntries.add(fromShopEntryConfig(shopConfig));
        }
        return shopEntries;
    }
    public static ShopEntry fromShopEntryConfig(ShopConfig.ShopEntryConfig entry) {
        return new ShopEntry(fromEnumShopEntry(entry.item).getDefaultStack(), entry.price, entry.type);
    }
    private static void giveItem(@NotNull PlayerEntity player, Item item){
        if(item.equals(WatheItems.NOTE)){
            player.giveItemStack(new ItemStack(WatheItems.NOTE, 4));
        } else if (item == WatheItems.BLACKOUT) {
            PlayerShopComponent.useBlackout(player);
        } else if (item == WatheItems.PSYCHO_MODE) {
            PlayerShopComponent.usePsychoMode(player);
        } else if (item == KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH) {
            HackerComponent.refreshWeaponCooldown(player);
        } else if (item == KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH) {
            HackerComponent.refreshAbilityCooldown(player);
        } else if (item == KinsWatheItems.ICON_POTION_EFFECT_REFRESH) {
            handlePotionRefresh(player);
        } else if (item == KinsWatheItems.ICON_POWER_RESTORATION) {
            TechnicianComponent.stopBlackout(player);
        } else if (item == WyspiaExpressItems.FUN_BOX) {
            openFunBox(player);
        } else {
            player.giveItemStack(item.getDefaultStack());
        }
    }
    // Return the corresponding ItemStack via getDefaultStack, which creates a new instance
    public static Item fromEnumShopEntry(EnumShopEntry entry ){
        Item item = null;
        switch(entry){
            // Base Wathe items
            case KNIFE:
                item = WatheItems.KNIFE;
                break;
            case REVOLVER:
                item = WatheItems.REVOLVER;
                break;
            case GRENADE:
                item = WatheItems.GRENADE;
                break;
            case PSYCHO_MODE:
                item = WatheItems.PSYCHO_MODE;
                break;
            case POISON_VIAL:
                item = WatheItems.POISON_VIAL;
                break;
            case SCORPION:
                item = WatheItems.SCORPION;
                break;
            case FIRECRACKER:
                item = WatheItems.FIRECRACKER;
                break;
            case LOCKPICK:
                item = WatheItems.LOCKPICK;
                break;
            case CROWBAR:
                item = WatheItems.CROWBAR;
                break;
            case BODY_BAG:
                item = WatheItems.BODY_BAG;
                break;
            case BLACKOUT:
                item = WatheItems.BLACKOUT;
                break;
            case NOTE:
                item = WatheItems.NOTE;
                break;
            // Starryexpress
            case TAPE:
                item =  StarryExpressItems.TAPE;
                break;
            // Noelle's Roles
            case FAKE_KNIFE:
                item =  ModItems.FAKE_KNIFE;
                break;
            case DELUSION_VIAL:
                item =  ModItems.DELUSION_VIAL;
                break;
            case DEFENSE_VIAL:
                item =  ModItems.DEFENSE_VIAL;
                break;
            case ROLE_MINE:
                item = ModItems.ROLE_MINE;
                break;
            case MASTER_KEY:
                item = ModItems.MASTER_KEY;
                break;
            case FAKE_REVOLVER:
                item = WyspiaExpressItems.FAKE_REVOLVER;
                break;
            // Kin's Wathe
            case PAN:
                item = KinsWatheItems.PAN;
                break;
            case COOKED_PORKCHOP:
                item = Items.COOKED_PORKCHOP;
                break;
            case COOKED_CHIKEN:
                item = Items.COOKED_CHICKEN;
                break;
            case COOKED_BEEF:
                item = Items.COOKED_BEEF;
                break;
            case PILL:
                item = KinsWatheItems.PILL;
                break;
            case MEDICAL_KIT:
                item = KinsWatheItems.MEDICAL_KIT;
                break;
            case ACID_BARREL:
                item = KinsWatheItems.SULFURIC_ACID_BARREL;
                break;
            case HUNTING_KNIFE:
                item = KinsWatheItems.HUNTING_KNIFE;
                break;
            case KNOCKOUT_DRUG:
                item = KinsWatheItems.KNOCKOUT_DRUG;
                break;
            case DREAM_IMPRINT:
                item = KinsWatheItems.DREAM_IMPRINT;
                break;
            case BLOWGUN:
                item = KinsWatheItems.BLOWGUN;
                break;
            case POISON_INJECTOR:
                item = KinsWatheItems.POISON_INJECTOR;
                break;
            case WRENCH:
                item = KinsWatheItems.WRENCH;
                break;
            case CAPTURE_DEVICE:
                item = KinsWatheItems.CAPTURE_DEVICE;
                break;
            case ICON_POWER_RESTORATION:
                item = KinsWatheItems.ICON_POWER_RESTORATION;
                break;
            case ICON_WEAPON_COOLDOWN_REFRESH:
                item = KinsWatheItems.ICON_WEAPON_COOLDOWN_REFRESH;
                break;
            case ICON_ABILITY_COOLDOWN_REFRESH:
                item = KinsWatheItems.ICON_ABILITY_COOLDOWN_REFRESH;
                break;
            case ICON_POTION_EFFECT_REFRESH:
                item = KinsWatheItems.ICON_POTION_EFFECT_REFRESH;
                break;
            // customs
            case FUN_BOX:
                item = WyspiaExpressItems.FUN_BOX;
                break;
            case MEGAPHONE:
                item = WyspiaExpressItems.MEGAPHONE;
            default:
        }
        return item;
    }
}
