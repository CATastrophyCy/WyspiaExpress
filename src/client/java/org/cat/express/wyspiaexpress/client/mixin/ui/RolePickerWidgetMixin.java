package org.cat.express.wyspiaexpress.client.mixin.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.client.ui.guesser.GuesserPlayerWidget;
import org.agmas.noellesroles.client.ui.guesser.GuesserRoleWidget;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.ui.RolePickerWidget;
import org.cat.express.wyspiaexpress.components.PlayerRolePickingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(LimitedInventoryScreen.class)
public abstract class RolePickerWidgetMixin extends LimitedHandledScreen<PlayerScreenHandler>{

    @Shadow @Final public ClientPlayerEntity player;

    public RolePickerWidgetMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }


    @Inject(method = "init", at = @At("HEAD"))
    void renderRolePicker(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        GuesserPlayerWidget.selectedPlayer = null;
        if (gameWorldComponent.isRole(player, WyspiaExpressRoles.COPYCAT)) {
            GuesserRoleWidget.stopClosing = false;
            PlayerRolePickingComponent component = PlayerRolePickingComponent.KEY.get(player);
            List<String> entries = component.getRoles();
            int buttonWidth = 70;
            int buttonHeight = 20;
            int gap = 10;
            // Calculate total width of all buttons combined to center them perfectly
            int totalWidth = (entries.size() * buttonWidth) + ((entries.size() - 1) * gap);
            int startX = (((LimitedInventoryScreen)(Object)this).width / 2) - (totalWidth / 2);

            int shouldBeY = (this.height - 32) / 2;
            int y = shouldBeY - 46;
            for (int i = 0; i < entries.size(); ++i) {
                String roleName = entries.get(i);
                int currentX = startX + i * (buttonWidth + gap);
                RolePickerWidget widget = new RolePickerWidget( ((LimitedInventoryScreen) (Object) this), currentX, y, buttonWidth, buttonHeight, roleName, player);
                this.addDrawableChild(widget);
            }

        }
    }

}