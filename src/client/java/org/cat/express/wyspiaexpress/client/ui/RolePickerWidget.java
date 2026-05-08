package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.components.PlayerRolePickingComponent;
import org.cat.express.wyspiaexpress.packets.RolePickC2SPacket;
import org.jetbrains.annotations.NotNull;

public class RolePickerWidget extends ButtonWidget {
    private final String roleName;
    private final PlayerEntity player;
    public final LimitedInventoryScreen screen;
    public RolePickerWidget(@NotNull LimitedInventoryScreen screen, int x, int y, int width, int height, String roleName, PlayerEntity player) {

        super(x, y, width, height, Text.empty(), button -> {
            if (MinecraftClient.getInstance().player == null) return;
            ClientPlayNetworking.send(new RolePickC2SPacket(roleName));
            screen.close();

        }, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.screen = screen;
        this.roleName = roleName;
        this.player = player;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw the default Minecraft button background
        super.renderWidget(context, mouseX, mouseY, delta);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        // Calculate text scale to ensure it fits inside the button
        int textWidth = textRenderer.getWidth(this.roleName);
        float scale = 1.0f;
        int maxTextWidth = this.width - 8; // Leave a 4px margin on each side
        if (textWidth > maxTextWidth) {
            scale = (float) maxTextWidth / textWidth;
        }

        context.getMatrices().push();

        float textX = this.getX() + this.width / 2.0f;
        float textY = this.getY() + (this.height - 8 * scale) / 2.0f;

        context.getMatrices().translate(textX, textY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(this.roleName), 0, 0, 0xFFFFFF);

        context.getMatrices().pop();

        PlayerRolePickingComponent component = PlayerRolePickingComponent.KEY.get(this.player);
        int ticks = component.getTick();
        if (ticks > 0) {
            String timeStr = String.valueOf((int) Math.ceil(ticks / 20.0));
            context.drawTextWithShadow(textRenderer, timeStr, this.getX() + 2, this.getY() + 2, 0xFF5555); // Red text
        }
    }
}