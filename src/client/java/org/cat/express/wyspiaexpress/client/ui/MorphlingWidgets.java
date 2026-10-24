package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.packet.MorphC2SPacket;
import org.cat.express.wyspiaexpress.client.util.PlayerProfileUtil;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MorphlingWidgets {
    private MorphlingWidgets() {}

    public static List<ClickableWidget> create(LimitedInventoryScreen screen) {
        List<ClickableWidget> widgets = new ArrayList<>();
        var profiles = RoundResultComponent.KEY.get(screen.player.getWorld()).profiles().values().stream()
                .filter(profile -> !profile.getId().equals(screen.player.getUuid()))
                .sorted(Comparator.comparing(profile -> profile.getName(), String.CASE_INSENSITIVE_ORDER)).toList();
        for (var profile : profiles) {
            widgets.add(new ButtonWidget(0, 0, 16, 16, Text.literal(profile.getName()), button -> {
                if (MorphlingPlayerComponent.KEY.get(screen.player).getMorphTicks() == 0) {
                    ClientPlayNetworking.send(new MorphC2SPacket(profile.getId()));
                }
            }, supplier -> supplier.get()) {
                @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                    int ticks = MorphlingPlayerComponent.KEY.get(screen.player).getMorphTicks();
                    if (ticks > 0) return;
                    if (ticks < 0) context.setShaderColor(.25f, .25f, .25f, .5f);
                    context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), getX() - 7, getY() - 7, 30, 30);
                    PlayerSkinDrawer.draw(context, PlayerProfileUtil.skin(profile).texture(), getX(), getY(), 16);
                    context.setShaderColor(1, 1, 1, 1);
                    if (isHovered()) {
                        context.fill(getX(), getY(), getX() + 16, getY() + 16, 0x60FFBF49);
                        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                                getMessage(), getX() + 8, getY() - 9, 0xFFBF49);
                    }
                    if (ticks < 0) context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                            Text.literal(String.valueOf((-ticks + 19) / 20)), getX(), getY(), 0xFF7777);
                }
            });
        }
        return widgets;
    }
}
