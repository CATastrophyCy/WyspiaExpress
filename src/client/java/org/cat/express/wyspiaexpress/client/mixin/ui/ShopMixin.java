package org.cat.express.wyspiaexpress.client.mixin.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.components.PlayerShopComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(LimitedInventoryScreen.class)
public abstract class ShopMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;
    @Unique private int wyspiaexpress$shopVersion = -1;

    protected ShopMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void wyspiaexpress$setupShop(CallbackInfo ci) {
        var shop = PlayerShopComponent.KEY.get(player);
        wyspiaexpress$shopVersion = shop.getVersion();
        if (shop.hasCustomShop()) wyspiaexpress$createShopButtons(shop);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void wyspiaexpress$refreshShop(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var shop = PlayerShopComponent.KEY.get(player);
        if (wyspiaexpress$shopVersion == shop.getVersion()) return;
        wyspiaexpress$shopVersion = shop.getVersion();
        if (shop.hasCustomShop()) wyspiaexpress$createShopButtons(shop);
        else ((LimitedInventoryScreen) (Object) this).init(client, width, height);
    }

    @Unique
    private void wyspiaexpress$createShopButtons(PlayerShopComponent shop) {
        if (getFocused() instanceof LimitedInventoryScreen.StoreItemWidget) setFocused(null);
        ScreenAccessor accessor = (ScreenAccessor) this;
        accessor.getDrawables().removeIf(widget -> widget instanceof LimitedInventoryScreen.StoreItemWidget);
        accessor.getChildren().removeIf(widget -> widget instanceof LimitedInventoryScreen.StoreItemWidget);
        accessor.getSelectables().removeIf(widget -> widget instanceof LimitedInventoryScreen.StoreItemWidget);
        int apart = 36;
        int x = width / 2 - shop.getItems().size() * apart / 2 + 9;
        int y = (height - 32) / 2 - 46;
        for (int index = 0; index < shop.getItems().size(); index++) {
            addDrawableChild(new ShopItemWidget((LimitedInventoryScreen) (Object) this,
                    x + apart * index, y, shop.getItems().get(index), index));
        }
    }

    private static final class ShopItemWidget extends LimitedInventoryScreen.StoreItemWidget {
        private final PlayerShopComponent.ShopItem item;

        private ShopItemWidget(LimitedInventoryScreen screen, int x, int y, PlayerShopComponent.ShopItem item, int index) {
            super(screen, x, y, createDisplayEntry(item), index);
            this.item = item;
            active = !item.isSoldOut();
        }

        private static ShopEntry createDisplayEntry(PlayerShopComponent.ShopItem item) {
            var entry = item.entry();
            var stack = entry.stack().copy();
            if (item.purchaseLimit() >= 0) {
                var lore = new ArrayList<>(stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines());
                lore.add(item.isSoldOut() ? Text.translatable("shop.wyspiaexpress.sold_out").withColor(0xAAAAAA)
                        : Text.translatable("shop.wyspiaexpress.remaining", item.getRemainingPurchases(), item.purchaseLimit()).withColor(0xF0C66E));
                stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
            }
            return new ShopEntry(stack, entry.price(), entry.type());
        }

        @Override public boolean isMouseOver(double mouseX, double mouseY) {
            return visible && mouseX >= getX() && mouseX < getRight() && mouseY >= getY() && mouseY < getBottom();
        }

        @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (item.isSoldOut() && button == 0 && isMouseOver(mouseX, mouseY)) {
                onPress();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            if (item.purchaseLimit() >= 0) {
                context.draw();
                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 200);
                if (item.isSoldOut()) context.fill(getX() - 7, getY() - 10, getX() + 23, getY() + 23, 0xA0666666);
                var textRenderer = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
                String remaining = Integer.toString(item.getRemainingPurchases());
                context.drawTextWithShadow(textRenderer, remaining, getRight() - textRenderer.getWidth(remaining), getY() + 10,
                        item.isSoldOut() ? 0xBBBBBB : 0xFFFFFF);
                context.getMatrices().pop();
            }
        }
    }
}
