package org.cat.express.wyspiaexpress.client.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.config.ShopConfig;
import org.cat.express.wyspiaexpress.config.WyspiaExpressServerConfig;
import org.cat.express.wyspiaexpress.shop.ShopUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class ShopMixin extends LimitedHandledScreen<PlayerScreenHandler> {

    @Shadow @Final @NotNull
    public ClientPlayerEntity player;
    // Constructors in mixins should ideally be protected
    protected ShopMixin(@NotNull PlayerScreenHandler handler, @NotNull PlayerInventory inventory, @NotNull Text title) {
        super(handler, inventory, title);
    }
    // we run this when return, this ovewrrites all shop logic injected by other mods
    // if any of the injected mods calls cancel then this would break
    @Inject(method = "init", at = @At("RETURN"))
    protected void injectCustomShopLogic(CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        Role playerRole = gameWorld.getRole(this.player);
        var basicConfig = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(playerRole);
        // first check if we have this role with config and if it has shop enabled
        if (basicConfig != null ) {
            // Remove all previously created shop items
            ScreenAccessor accessor = (ScreenAccessor)(Object) this;
            accessor.getDrawables().removeIf(w -> w instanceof LimitedInventoryScreen.StoreItemWidget);
            accessor.getChildren().removeIf(w -> w instanceof LimitedInventoryScreen.StoreItemWidget);
            accessor.getSelectables().removeIf(w -> w instanceof LimitedInventoryScreen.StoreItemWidget);
            if(basicConfig.enableShop()) {
                // Replaces the shop with our own
                List<ShopEntry> entries = ShopUtil.fromShopEntryConfigs(ShopConfig.fromStrings(basicConfig.shopEntries()));
                // Need to add logic to count the times bough of each item to the player via component, and here we only draw those that he can still buy
                int apart = 36;
                int x = this.width / 2 - (entries.size()) * apart / 2 + 9;
                int shouldBeY = (this.height - 32) / 2;
                int y = shouldBeY - 46;
                for (int i = 0; i < entries.size(); ++i) {
                    this.addDrawableChild(new LimitedInventoryScreen.StoreItemWidget(
                            (LimitedInventoryScreen) (Object) this, x + apart * i, y, entries.get(i), i
                    ));
                }

            }
        }
    }
}
