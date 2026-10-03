package org.cat.express.wyspiaexpress.client;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressEntities;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.items.ItemToolTip;
import org.cat.express.wyspiaexpress.client.guidebook.GuidebookDefinitions;
import org.cat.express.wyspiaexpress.client.guidebook.GuidebookSources;
import org.cat.express.wyspiaexpress.client.guidebook.WyspiaGuidebookScreen;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.cat.express.wyspiaexpress.client.roles.NoTargetAbilityUtil;
import org.cat.express.wyspiaexpress.client.roles.TargetAbilityUtil;
import org.cat.express.wyspiaexpress.client.ui.InventoryAbilityPanel;
import org.cat.express.wyspiaexpress.packets.VersionCheckNetwork;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;


public class WyspiaexpressClient implements ClientModInitializer {
    public static KeyBinding abilityBind;
    public static KeyBinding guidebookBind;
    public static PlayerBodyEntity TARGET_BODY = null;
    public static PlayerEntity TARGET_PLAYER = null;
    @Override
    public void onInitializeClient() {
        registerGuidebook();
        registerInventoryFocus();
        registerCooldownRefresh();
        registerItemToolTips();
        registerItemsBlood();
        registerAbilityKey();
        registerAbilityPacket();
        registerEntityRenderer();
        ClientLoginNetworking.registerGlobalReceiver(
                VersionCheckNetwork.VERSION_QUERY_ID,
                (client, handler, buf, listenerAdder) -> {
                    String clientVersion = WyspiaExpress.getVersion();
                    PacketByteBuf reply = PacketByteBufs.create();
                    reply.writeString(clientVersion);
                    return CompletableFuture.completedFuture(reply);
                }
        );
        String forceCrawlVersion = WyspiaExpress.getForceCrawlVersion();
        if (!forceCrawlVersion.equals("UNKNOWN")) {
            ClientLoginNetworking.registerGlobalReceiver(
                    VersionCheckNetwork.VERSION_QUERY_FORCE_CRAWL_ID,
                    (client, handler, buf, listenerAdder) -> {
                        String clientVersion = WyspiaExpress.getForceCrawlVersion();
                        PacketByteBuf reply = PacketByteBufs.create();
                        reply.writeString(clientVersion);
                        return CompletableFuture.completedFuture(reply);
                    }
            );
        }
    }
    private static void registerInventoryFocus() {

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof LimitedInventoryScreen)) return;
            ScreenMouseEvents.beforeMouseClick(screen).register((current, x, y, button) -> {
                for (var child : current.children()) {
                    if (child instanceof InventoryAbilityPanel panel) panel.blurForClick(x, y);
                }
            });
        });
    }
    private static void registerCooldownRefresh() {
        // Cooldown tables used to refresh from item hovers. Keep them current when config mirrors
        // change or a connection is established, so tooltips and guide previews stay read-only.
        WyspiaExpress.ITEMS_CONFIG.forEachOption(option -> {
            if (option.key().name().toLowerCase(java.util.Locale.ROOT).endsWith("cooldown")) {
                option.observe(ignored -> MinecraftClient.getInstance().execute(WyspiaExpressItems::registerItemsCooldown));
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WyspiaExpressItems.registerItemsCooldown());
    }
    private static void registerGuidebook() {
        GuidebookSources.init();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(GuidebookDefinitions.INSTANCE);
        guidebookBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wyspiaexpress.guidebook",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F, "category.wathe.keybinds"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guidebookBind.wasPressed()) {
                if (client.world != null && client.player != null && client.currentScreen == null) {
                    client.setScreen(new WyspiaGuidebookScreen());
                }
            }
        });
    }
    private static void registerEntityRenderer(){
            EntityRendererRegistry.register(WyspiaExpressEntities.GRENADE, FlyingItemEntityRenderer::new);
            EntityRendererRegistry.register(WyspiaExpressEntities.SMOKE_BOMB, FlyingItemEntityRenderer::new);
    }
    private static void registerItemToolTips(){
        ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
            ItemToolTip.addItemtip(WyspiaExpressItems.FAKE_REVOLVER, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.FUN_BOX, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.MEGAPHONE, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.OUTLAW_REVOLVER, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.RITUAL_DAGGER, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.TAPE, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.SENSE_DEAD, itemStack, list);
            ItemToolTip.addItemtip(WyspiaExpressItems.SMOKE_BOMB, itemStack, list);
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
                    TargetAbilityUtil.sendLichPacket(client);
                }
                if(role == WyspiaExpressRoles.CULT_LEADER){
                    TargetAbilityUtil.sendCultLeaderPacket(client);
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
