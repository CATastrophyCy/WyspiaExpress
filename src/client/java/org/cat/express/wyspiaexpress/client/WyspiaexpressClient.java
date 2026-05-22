package org.cat.express.wyspiaexpress.client;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.items.ItemToolTip;
import org.cat.express.wyspiaexpress.client.roles.LichUtil;
import org.cat.express.wyspiaexpress.client.roles.NoTargetAbilityUtil;
import org.cat.express.wyspiaexpress.packets.VersionCheckNetwork;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;


public class WyspiaexpressClient implements ClientModInitializer {
    public static KeyBinding abilityBind;
    @Override
    public void onInitializeClient() {
        registerItemToolTips();
        registerItemsBlood();
        registerAbilityKey();
        registerAbilityPacket();
        ClientLoginNetworking.registerGlobalReceiver(
                VersionCheckNetwork.VERSION_QUERY_ID,
                (client, handler, buf, listenerAdder) -> {
                    String clientVersion = WyspiaExpress.getVersion();
                    PacketByteBuf reply = PacketByteBufs.create();
                    reply.writeString(clientVersion);
                    return CompletableFuture.completedFuture(reply);
                }
        );
    }

    private static void registerItemToolTips(){
        ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
            ItemToolTip.addItemtip(WyspiaExpressItems.FAKE_REVOLVER, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.FUN_BOX, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.MEGAPHONE, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.OUTLAW_REVOLVER, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.TAPE, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.SENSE_DEAD, itemStack, list);
        }));
    }
    private static void registerAbilityKey(){
        if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
            if (abilityBind == null) ClientTickEvents.START_CLIENT_TICK.register(client -> {
                abilityBind = NoellesrolesClient.abilityBind;
            });
        } else if (!FabricLoader.getInstance().isModLoaded("noellesroles") && FabricLoader.getInstance().isModLoaded("starexpress")) {
            abilityBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + WyspiaExpress.MOD_ID + ".ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.wathe.keybinds"));
        } else {
            abilityBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + WyspiaExpress.MOD_ID + ".ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.wathe.keybinds"));
        }
    }
    private static void registerAbilityPacket(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (abilityBind == null) return;
            if (abilityBind.isPressed()) {
                GameWorldComponent gameWorld = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
                Role role = gameWorld.getRole(client.player);
                if(role == null) return;
                if(role == WyspiaExpressRoles.LICH){
                    LichUtil.sendLichPacket(client);
                }
                if(NoTargetAbilityUtil.isValid(role)){
                    NoTargetAbilityUtil.sendPacket(client);
                }
            }
        });
    }
    private static void registerItemsBlood(){
        wathe_blood.Weapons.addWeapon(WyspiaExpressItems.OUTLAW_REVOLVER, 3.0F, 1.0F, 9, 12, new Vec3d(0.5F, 0.5F, 0.5F));
        wathe_blood.Weapons.addWeapon(KinsWatheItems.HUNTING_KNIFE, 1.0F, 0.3, 7, 8, new Vec3d(0.3, 0.3, 0.3));
    }
}
