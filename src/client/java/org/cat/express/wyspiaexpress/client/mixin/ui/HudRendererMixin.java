package org.cat.express.wyspiaexpress.client.mixin.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.cat.express.wyspiaexpress.client.ui.DurationHud;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.text.Text;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.roles.HitmanComponent;
import org.cat.express.wyspiaexpress.components.roles.HitmanComponent.TargetStatus;
import org.cat.express.wyspiaexpress.client.ui.SurvivalHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class HudRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    public void renderExtraBars(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        if (!gameWorld.isRunning()) return;

        DurationHud.render(context, tickCounter);

        SurvivalHud.render(context);
        if (!gameWorld.isRole(client.player, WyspiaExpressRoles.HITMAN)
                || !GameFunctions.isPlayerAliveAndSurvival(client.player)) return;
        HitmanComponent hitman = HitmanComponent.KEY.get(client.player);
        if (hitman.target() == null) return;

        var renderer = client.textRenderer;
        int right = context.getScaledWindowWidth() - 6;
        int taskY = context.getScaledWindowHeight() - renderer.fontHeight - 6;
        String task = hitman.getTargetTask() == null ? "none" : hitman.getTargetTask().name().toLowerCase(Locale.ROOT);
        Text taskText = Text.translatable("hud.wyspiaexpress.hitman.task." + task,
                hitman.targetName().copy().withColor(WyspiaExpressRoles.HITMAN.color()));
        context.drawTextWithShadow(renderer, taskText, right - renderer.getWidth(taskText), taskY, 0xFFFFFF);

        var statuses = Text.empty();
        for (TargetStatus status : TargetStatus.values()) {
            if (!hitman.hasTargetStatus(status)) continue;
            if (!statuses.getSiblings().isEmpty()) statuses.append(Text.literal(", ").withColor(0xAAAAAA));
            int color = switch (status) {
                case PROTECTED -> 0x7CA8FF;
                case IN_LOVE -> WyspiaExpressRoles.LOVER_1.color();
                case POISONED -> 0xFF6B6B;
                case DELUDED -> 0xB77BFF;
            };
            statuses.append(Text.translatable("hud.wyspiaexpress.hitman.status." + status.name().toLowerCase(Locale.ROOT))
                    .withColor(color));
        }
        if (statuses.getSiblings().isEmpty()) return;
        int lineHeight = renderer.fontHeight + 2;
        var lines = renderer.wrapLines(Text.translatable("hud.wyspiaexpress.hitman.status.line", statuses), right - 6);
        int y = taskY - lineHeight * lines.size();
        for (var line : lines) {
            context.drawTextWithShadow(renderer, line, right - renderer.getWidth(line), y, 0xFFFFFF);
            y += lineHeight;
        }
    }
}
