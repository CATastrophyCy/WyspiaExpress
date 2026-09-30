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
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.BlackoutDuration;

/** Active durations below the round clock, with separate rows for ability and blackout. */
public final class DurationHud {
    private static final int TOP = 28;
    private static final int GAP = 4;
    private static final int TEXT_HEIGHT = 20;
    private static final int BAR_HEIGHT = 27;
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
        int y = TOP;
        AbilityDuration ability = GameFunctions.isPlayerAliveAndSurvival(player) ? getAbilityDuration(client, role) : null;
        if (ability != null && (ability.infinite() || ability.ticks() > 0)) {
            Text countdown = ability.infinite() ? Text.literal("∞")
                    : Text.translatable("hud.wyspiaexpress.duration.seconds", seconds(ability.ticks()));
            drawPanel(context, client, y, ability.label(), countdown, ability.color(), ability.color(), -1);
            y += TEXT_HEIGHT + GAP;
        }

        if (player.isCreative() || player.isSpectator() || gameWorld.canUseKillerFeatures(player)
                || WyspiaExpressRoles.KILLER_SIDED_NEUTRALS.contains(role)) {
            BlackoutDuration blackout = (BlackoutDuration) WorldBlackoutComponent.KEY.get(player.getWorld());
            if (blackout.getBlackoutRemainingTicks() > 0) {
                float remaining = Math.max(0, blackout.getBlackoutRemainingTicks() - tickCounter.getTickDelta(false));
                float fraction = remaining / Math.max(1, blackout.getBlackoutDurationTicks());
                Text countdown = Text.translatable("hud.wyspiaexpress.duration.blackout_range",
                        seconds(blackout.getBlackoutMinimumRemainingTicks()), seconds(blackout.getBlackoutRemainingTicks()));
                drawPanel(context, client, y, Text.translatable("hud.wyspiaexpress.duration.blackout"),
                        countdown, 0xFFFFDA75, NUMBER_COLOR, fraction);
            }
        }
    }

    private static AbilityDuration getAbilityDuration(MinecraftClient client, Role role) {
        var player = client.player;
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
                                  Text countdown, int accent, int numberColor, float fraction) {
        var renderer = client.textRenderer;
        countdown = countdown.copy().styled(style -> style.withBold(true).withColor(numberColor));
        int numberWidth = renderer.getWidth(countdown);
        int availableWidth = context.getScaledWindowWidth() - 16;
        int width = Math.min(availableWidth, Math.max(160, renderer.getWidth(label) + numberWidth + 28));
        int x = (context.getScaledWindowWidth() - width) / 2;
        int height = fraction < 0 ? TEXT_HEIGHT : BAR_HEIGHT;

        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER);
        context.fill(x, y, x + width, y + height, BACKGROUND);
        context.fill(x, y, x + 2, y + height, accent);
        Text visibleLabel = Text.literal(renderer.trimToWidth(label, Math.max(0, width - numberWidth - 24)).getString());
        context.drawTextWithShadow(renderer, visibleLabel, x + 8, y + 5, accent);
        context.drawTextWithShadow(renderer, countdown, x + width - numberWidth - 8, y + 5, numberColor);

        if (fraction >= 0) {
            int barWidth = width - 16;
            context.fill(x + 8, y + 19, x + width - 8, y + 23, 0xFF30394A);
            int fillWidth = MathHelper.ceil(barWidth * MathHelper.clamp(fraction, 0, 1));
            if (fillWidth > 0) context.fill(x + 8, y + 19, x + 8 + fillWidth, y + 23, accent);
        }
    }

    private record AbilityDuration(Text label, int ticks, boolean infinite, int color) {}
}
