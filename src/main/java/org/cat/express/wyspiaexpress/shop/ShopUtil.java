package org.cat.express.wyspiaexpress.shop;

import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.item.FakeKnifeItem;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.item.StarryExpressItems;
import org.cat.express.wyspiaexpress.config.ShopConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShopUtil {

    public static boolean handlePurchase(@NotNull PlayerEntity player, int balance, @NotNull Item item, int price) {
        if (balance >= price && !player.getItemCooldownManager().isCoolingDown(item)) {
            if (item == WatheItems.NOTE) {
                player.giveItemStack(new ItemStack(WatheItems.NOTE, 4));
            } else if (item == WatheItems.BLACKOUT) {
                PlayerShopComponent.useBlackout(player);
            } else if (item == WatheItems.PSYCHO_MODE) {
                PlayerShopComponent.usePsychoMode(player);
            } else {
                player.giveItemStack(item.getDefaultStack());
            }

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
    public static List<ShopEntry> fromShopEntryConfigs(List<ShopConfig.ShopEntryConfig> shopConfigs ) {
        List<ShopEntry> shopEntries = new ArrayList<>();
        for( ShopConfig.ShopEntryConfig shopConfig : shopConfigs ) {
            shopEntries.add(fromShopEntryConfig(shopConfig));
        }
        return shopEntries;
    }
    public static ShopEntry fromShopEntryConfig(ShopConfig.ShopEntryConfig entry) {
        return new ShopEntry(fromEnumShopEntry(entry.item), entry.price, entry.type);
    }
    // Return the corresponding ItemStack via getDefaultStack, which creates a new instance
    public static ItemStack fromEnumShopEntry(EnumShopEntry entry ){
        ItemStack item = null;
        switch(entry){
            // Base Wathe items
            case KNIFE:
                item = WatheItems.KNIFE.getDefaultStack();
                break;
            case REVOLVER:
                item = WatheItems.REVOLVER.getDefaultStack();
                break;
            case GRENADE:
                item = WatheItems.GRENADE.getDefaultStack();
                break;
            case PSYCHO_MODE:
                item = WatheItems.PSYCHO_MODE.getDefaultStack();
                break;
            case POISON_VIAL:
                item = WatheItems.POISON_VIAL.getDefaultStack();
                break;
            case SCORPION:
                item = WatheItems.SCORPION.getDefaultStack();
                break;
            case FIRECRACKER:
                item = WatheItems.FIRECRACKER.getDefaultStack();
                break;
            case LOCKPICK:
                item = WatheItems.LOCKPICK.getDefaultStack();
                break;
            case CROWBAR:
                item = WatheItems.CROWBAR.getDefaultStack();
                break;
            case BODY_BAG:
                item = WatheItems.BODY_BAG.getDefaultStack();
                break;
            case BLACKOUT:
                item = WatheItems.BLACKOUT.getDefaultStack();
                break;
            case NOTE:
                item = WatheItems.NOTE.getDefaultStack();
                break;
            // Starryexpress
            case TAPE:
                item =  StarryExpressItems.TAPE.getDefaultStack();
                break;
            // Noelle's Roles
            case FAKE_KNIFE:
                item =  ModItems.FAKE_KNIFE.getDefaultStack();
                break;
            case DELUSION_VIAL:
                item =  ModItems.DELUSION_VIAL.getDefaultStack();
                break;
            case DEFENSE_VIAL:
                item =  ModItems.DEFENSE_VIAL.getDefaultStack();
                break;
            case ROLE_MINE:
                item = ModItems.ROLE_MINE.getDefaultStack();
                break;
            case MASTER_KEY:
                item = ModItems.MASTER_KEY.getDefaultStack();
                break;
            case FAKE_REVOLVER:
                item = ModItems.FAKE_REVOLVER.getDefaultStack();
                break;
            // Kin's Wathe
            case PAN:
                item = KinsWatheItems.PAN.getDefaultStack();
                break;
            case COOKED_PORKCHOP:
                item = Items.COOKED_PORKCHOP.getDefaultStack();
                break;
            case COOKED_CHIKEN:
                item = Items.COOKED_CHICKEN.getDefaultStack();
                break;
            case COOKED_BEEF:
                item = Items.COOKED_BEEF.getDefaultStack();
                break;
            case PILL:
                item = KinsWatheItems.PILL.getDefaultStack();
                break;
            case MEDICAL_KIT:
                item = KinsWatheItems.MEDICAL_KIT.getDefaultStack();
                break;
            case ACID_BARREL:
                item = KinsWatheItems.SULFURIC_ACID_BARREL.getDefaultStack();
                break;
            case HUNTING_KNIFE:
                item = KinsWatheItems.HUNTING_KNIFE.getDefaultStack();
                break;
            case KNOCKOUT_DRUG:
                item = KinsWatheItems.KNOCKOUT_DRUG.getDefaultStack();
                break;
            case DREAM_IMPRINT:
                item = KinsWatheItems.DREAM_IMPRINT.getDefaultStack();
                break;
            case BLOWGUN:
                item = KinsWatheItems.BLOWGUN.getDefaultStack();
                break;
            case POISON_INJECTOR:
                item = KinsWatheItems.POISON_INJECTOR.getDefaultStack();
                break;
            default:
        }
        return item;
    }
}
