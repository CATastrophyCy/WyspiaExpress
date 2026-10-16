package org.cat.express.wyspiaexpress.client.ui;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.BlackoutDuration;

import java.util.ArrayList;

public final class DurationHud {
    private static final int MIN_TOP = 28;
    private static final int GAP = 4;
    private static final int TEXT_HEIGHT = 20;
    private static final int BACKGROUND = 0xE6101520;
    private static final int BORDER = 0xFF667085;
    private static final int NUMBER_COLOR = 0xFFFFFFFF;

    private DurationHud() {}

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null || client.options.hudHidden) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        if (!gameWorld.isRunning()) return;
        Role role = gameWorld.getRole(player);
        var panels = new ArrayList<DurationPanel>(3);
        AbilityDuration ability = GameFunctions.isPlayerAliveAndSurvival(player) ? getAbilityDuration(client, role) : null;
        if (ability != null && (ability.infinite() || ability.ticks() > 0)) {
            Text countdown = ability.infinite() ? Text.literal("∞")
                    : Text.translatable("hud.wyspiaexpress.duration.seconds", seconds(ability.ticks()));
            panels.add(new DurationPanel(ability.label(), countdown, ability.color(), ability.color()));
        }

        AbilityDuration glowing = GameFunctions.isPlayerAliveAndSurvival(player)
                ? fromEffect("glowing", player.getStatusEffect(StatusEffects.GLOWING), 0xFFFFE680) : null;
        if (glowing != null && (glowing.infinite() || glowing.ticks() > 0)) {
            Text countdown = glowing.infinite() ? Text.literal("∞")
                    : Text.translatable("hud.wyspiaexpress.duration.seconds", seconds(glowing.ticks()));
            panels.add(new DurationPanel(glowing.label(), countdown, glowing.color(), glowing.color()));
        }

        if (GameFunctions.isPlayerSpectatingOrCreative(player) || gameWorld.canUseKillerFeatures(player)
                || WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) {
            BlackoutDuration blackout = (BlackoutDuration) WorldBlackoutComponent.KEY.get(player.getWorld());
            if (blackout.getBlackoutRemainingTicks() > 0) {
                int minimum = blackout.getBlackoutMinimumRemainingTicks();
                // Once lights begin returning, count down until the remaining blackout ends.
                int remaining = minimum > 0 ? minimum : blackout.getBlackoutRemainingTicks();
                Text countdown = Text.translatable("hud.wyspiaexpress.duration.seconds", seconds(remaining));
                panels.add(new DurationPanel(Text.translatable("hud.wyspiaexpress.duration.blackout"),
                        countdown, minimum > 0 ? 0xFFFFDA75 : 0xFF8FCEFF,
                        minimum > 0 ? NUMBER_COLOR : 0xFF8FCEFF));
            }
        }

        int width = 0;
        for (DurationPanel panel : panels) {
            width = Math.max(width, client.textRenderer.getWidth(panel.label())
                    + client.textRenderer.getWidth(panel.countdown()) + 24);
        }
        width = Math.min(width, context.getScaledWindowWidth() - 16);
        int y = Math.max(MIN_TOP, context.getScaledWindowHeight() / 4);
        for (DurationPanel panel : panels) {
            drawPanel(context, client, y, panel.label(), panel.countdown(), panel.accent(), panel.numberColor(), width);
            y += TEXT_HEIGHT + GAP;
        }
    }

    private static AbilityDuration getAbilityDuration(MinecraftClient client, Role role) {
        var player = client.player;
        if(player == null)
            return null;
        if (role == Noellesroles.PHANTOM) {
            return fromEffect("phantom", player.getStatusEffect(StatusEffects.INVISIBILITY), 0xFF000000 | role.color());
        }
        if (role == Noellesroles.MORPHLING) {
            return new AbilityDuration(Text.translatable("hud.wyspiaexpress.duration.morphling"),
                    MorphlingPlayerComponent.KEY.get(player).getMorphTicks(), false, 0xFF000000 | role.color());
        }
        if (role == KinsWatheRoles.ROBOT) {
            return fromEffect("robot", player.getStatusEffect(StatusEffects.NIGHT_VISION), 0xFF000000 | role.color());
        }
        if (role == StarryExpressRoles.STARSTRUCK) {
            return new AbilityDuration(Text.translatable("hud.wyspiaexpress.duration.starstruck"),
                    StarstruckComponent.KEY.get(player).ticks, false, 0xFF000000 | role.color());
        }
        return null;
    }

    private static AbilityDuration fromEffect(String key, StatusEffectInstance effect, int color) {
        if (effect == null) return null;
        return new AbilityDuration(Text.translatable("hud.wyspiaexpress.duration." + key),
                effect.getDuration(), effect.isInfinite(), color);
    }

    private static int seconds(int ticks) {
        return (int) Math.ceil(ticks / 20.0);
    }

    private static void drawPanel(DrawContext context, MinecraftClient client, int y, Text label,
                                  Text countdown, int accent, int numberColor, int width) {
        var renderer = client.textRenderer;
        int numberWidth = renderer.getWidth(countdown);
        int x = context.getScaledWindowWidth() - width;
        int height = TEXT_HEIGHT;

        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER);
        context.fill(x, y, x + width, y + height, BACKGROUND);
        context.fill(x, y, x + 2, y + height, accent);
        Text visibleLabel = Text.literal(renderer.trimToWidth(label, Math.max(0, width - numberWidth - 24)).getString());
        context.drawTextWithShadow(renderer, countdown, x + 8, y + 5, numberColor);
        context.drawTextWithShadow(renderer, visibleLabel, x + width - renderer.getWidth(visibleLabel) - 8, y + 5, accent);
    }

    private record AbilityDuration(Text label, int ticks, boolean infinite, int color) {}

    private record DurationPanel(Text label, Text countdown, int accent, int numberColor) {
        private DurationPanel {
            countdown = countdown.copy().styled(style -> style.withBold(true).withColor(numberColor));
        }
    }
}
