package org.cat.express.wyspiaexpress.client.mixin.modifiers.guesser;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.ui.guesser.GuesserPlayerWidget;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Environment(EnvType.CLIENT)
@Mixin(LimitedInventoryScreen.class)
public abstract class GuesserScreenMixin extends Screen{
    protected GuesserScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void wyspiaexpress$removeMimicWidget(CallbackInfo ci) {
        List<GuesserPlayerWidget> remainingWidgets = new ArrayList<>();

        ClientWorld world = MinecraftClient.getInstance().world;
        if(world == null) return;
        for (Element child : List.copyOf(this.children())) {
            if (child instanceof GuesserPlayerWidget guesserWidget) {
                boolean isMimic = false;

                UUID targetUUID = guesserWidget.targetUUID;
                if (targetUUID != null) {
                    PlayerEntity targetPlayer = world.getPlayerByUuid(targetUUID);
                    if (targetPlayer != null) {
                        isMimic = GameWorldComponent.KEY.get(world).isRole(targetPlayer, Noellesroles.MIMIC);
                    }
                }
                if (isMimic) {
                    // Remove the mimic from the screen
                    this.remove(guesserWidget);
                } else {
                    // Keep track of the others to fix the layout gap
                    remainingWidgets.add(guesserWidget);
                }
            }
        }
        if(remainingWidgets.size() < WyspiaExpress.MODIFIERS_CONFIG.guesserConfig.minPlayer()){
            for(GuesserPlayerWidget guesserPlayerWidget : remainingWidgets){
                this.remove(guesserPlayerWidget);
            }
            return;
        }
        // Recalculate positions so there isn't a missing gap where the Mimic used to be
        int spacing = 36;
        int count = remainingWidgets.size();
        if (count > 0) {
            int startX = this.width / 2 - (count * spacing) / 2 + 9;
            for (int i = 0; i < count; i++) {
                remainingWidgets.get(i).setX(startX + i * spacing);
            }
        }
    }
}