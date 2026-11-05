package org.cat.express.wyspiaexpress.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.PlayerDepressedComponent;
import org.cat.express.wyspiaexpress.components.PlayerFreezeComponent;
import org.cat.express.wyspiaexpress.components.PlayerTaskComponent;
import org.cat.express.wyspiaexpress.components.PlayerConfigComponent;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;
import org.cat.express.wyspiaexpress.components.PsychoModeState;
import org.cat.express.wyspiaexpress.components.roles.LoversComponent;
import org.cat.express.wyspiaexpress.components.roles.HitmanComponent;
import org.cat.express.wyspiaexpress.components.roles.PlayerCultistComponent;
import org.cat.express.wyspiaexpress.util.GameUtil;
import org.cat.express.wyspiaexpress.client.util.PlayerProfileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.role.arsonist.cca.DousedPlayerComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Wrap Noelle's live-player name lookup so disconnected disguises use the saved profile first.
@Mixin(value = RoleNameRenderer.class, priority = 1100)
public abstract class RoleNameRendererMixin {
    @Shadow private static float nametagAlpha;
    @Unique private static final UUID DELUSION_MARKER = UUID.fromString("00000000-0000-0000-dead-c0de00000000"); // unique string used by Kinswathe

    @Unique private static PlayerEntity targetPlayer;

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;getLightLevel(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I"), require = 2)
    private static int wyspiaexpress$nametagVisibility(World world, LightType type, BlockPos pos,
                                                     Operation<Integer> original,
                                                     @Local(argsOnly = true) ClientPlayerEntity player) {
        if (GameFunctions.isPlayerSpectatingOrCreative(player)) return 15;

        if (GameUtil.isUnderSmokeEffect(player)) return 0;

        if (ProjectileUtil.getCollision(player, entity -> entity instanceof PlayerEntity, 2f)
                instanceof EntityHitResult hit && hit.getEntity() instanceof PlayerEntity target
                && GameUtil.isUnderSmokeEffect(target)) return 0;

        return original.call(world, type, pos);
    }

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private static Text wyspiaexpress$savedMorphName(PlayerEntity player, Operation<Text> original) {
        var profile = PlayerProfileUtil.morph(player);
        return profile != null && !player.isInvisible() ? Text.literal(profile.getName()) : original.call(player);
    }

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void renderSpectatorHud(TextRenderer renderer, ClientPlayerEntity player, DrawContext context,
                                           RenderTickCounter tickCounter, CallbackInfo ci) {
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        if (!game.isRunning() || !WatheClient.isPlayerSpectatingOrCreative()
                || !PlayerConfigComponent.KEY.get(player).spectatorInfo()
                || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer) || nametagAlpha <= 0.05f) return;

        Role role = game.getRole(targetPlayer);
        List<Text> stats = new ArrayList<>();
        stats.add(valueText("Mood: ", (int) (PlayerMoodComponent.KEY.get(targetPlayer).getMood() * 100) + "%", 0xE9B3A5));
        stats.add(valueText("Coin: ", String.valueOf(PlayerShopComponent.KEY.get(targetPlayer).balance), 0xEBD28E));
        if (WyspiaExpress.GENERAL_CONFIG.spectatorSpecialHud()) {
            stats.add(PlayerTaskComponent.KEY.get(targetPlayer).hudText());
        }
        List<Text> details = new ArrayList<>();
        MutableText survival = Text.empty();
        int freezeTicks = PlayerFreezeComponent.KEY.get(targetPlayer).getFreezeTick();
        if (WyspiaExpress.RULES_CONFIG.freeze() && freezeTicks > 0) {
            survival.append(valueText("Freeze: ", remainingPercent(freezeTicks, WyspiaExpress.RULES_CONFIG.freezeTimer()) + "%", 0x8FCDF2));
        }
        int depressionTicks = PlayerDepressedComponent.KEY.get(targetPlayer).getDepressionTick();
        if (WyspiaExpress.RULES_CONFIG.depressionKilling() && depressionTicks > 0) {
            if (!survival.getString().isEmpty()) survival.append("   ");
            survival.append(valueText("Depression: ", remainingPercent(depressionTicks, WyspiaExpress.RULES_CONFIG.depressedTimer()) + "%", 0xC88FE6));
        }
        if (!survival.getString().isEmpty()) details.add(survival);

        PlayerCultistComponent cultist = PlayerCultistComponent.KEY.get(targetPlayer);
        if (cultist.getConversionTick() > 0 && !cultist.isConverted()) {
            details.add(valueText("Conversion: ", (int) (cultist.getConversionProgress() * 100) + "%", WyspiaExpressRoles.CULT_LEADER.color()));
        }
        HackerComponent hacker = HackerComponent.KEY.get(targetPlayer);
        int hackingDuration = KinsWatheConfig.HANDLER.instance().HackerHackingTime * 20;
        if (hacker.hackingTime > 0 && hacker.hackingTime < hackingDuration) {
            details.add(valueText("Hacking: ", hacker.hackingTime * 100 / hackingDuration + "%", KinsWatheRoles.HACKER.color()));
        }
        if (LoversComponent.isLover(role)) {
            LoversComponent lovers = LoversComponent.KEY.get(player.getWorld());
            details.add(Text.translatable("hud.wyspiaexpress.spectator.lives")
                    .append(Text.literal(String.valueOf(lovers.getLives(targetPlayer.getUuid())))
                            .withColor(WyspiaExpressRoles.LOVER_1.color())));
            UUID partner = lovers.getPartner(targetPlayer.getUuid());
            if (partner != null) {
                var profile = RoundResultComponent.KEY.get(player.getWorld()).profile(partner);
                if (profile != null) {
                    details.add(Text.translatable("hud.wyspiaexpress.spectator.partner",
                            Text.literal(profile.getName()).withColor(WyspiaExpressRoles.LOVER_1.color())));
                }
            }
        }
        if (role == WyspiaExpressRoles.HITMAN) {
            HitmanComponent hitman = HitmanComponent.KEY.get(targetPlayer);
            if (hitman.target() != null) {
                details.add(Text.translatable("hud.wyspiaexpress.hitman.target",
                        hitman.targetName().copy().withColor(role.color())));
            }
        }
        if (role == KinsWatheRoles.DREAMER) {
            DreamerKillerComponent dreamer = DreamerKillerComponent.KEY.get(targetPlayer);
            details.add(Text.translatable("hud.wyspiaexpress.spectator.deluded")
                    .append(Text.literal(dreamer.dreamerCounts + " / " + dreamer.dreamerRequired).withColor(role.color())));
        }

        int abilityCooldown = getAbilityCooldown(targetPlayer, role);
        if (abilityCooldown > 0) {
            details.add(Text.translatable("hud.wyspiaexpress.spectator.ability_cooldown",
                    Text.literal(String.valueOf((int) Math.ceil(abilityCooldown / 20.0))).withColor(role.color())));
        }
        PsychoModeState psycho = PsychoModeState.get(targetPlayer);
        if (psycho.getPsychoTicks() > 0) {
            details.add(Text.translatable("hud.wyspiaexpress.status.psycho",
                    (int) Math.ceil(psycho.getPsychoTicks() / 20.0), psycho.getBlockedHits()).withColor(0xFF6B6B));
        }
        var glowing = targetPlayer.getStatusEffect(StatusEffects.GLOWING);
        if (glowing != null && !glowing.isInfinite() && glowing.getDuration() > 0) {
            details.add(Text.translatable("hud.wyspiaexpress.status.glowing", (int) Math.ceil(glowing.getDuration() / 20.0))
                    .withColor(KinsWatheRoles.JUDGE.color()));
        }
        var nightVision = targetPlayer.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (nightVision != null && !nightVision.isInfinite() && nightVision.getDuration() > 0) {
            details.add(Text.translatable("hud.wyspiaexpress.status.night_vision", (int) Math.ceil(nightVision.getDuration() / 20.0))
                    .withColor(KinsWatheRoles.ROBOT.color()));
        }
        PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(targetPlayer);
        if (poison.poisonTicks > 0) {
            double seconds = Math.ceil(poison.poisonTicks / 2.0) / 10.0;
            boolean deluded = DELUSION_MARKER.equals(poison.poisoner);
            details.add(Text.literal((deluded ? "Deluded " : "Poisoned ") + seconds + "s")
                    .withColor(deluded ? 0xB77BFF : 0xFF6B6B));
        }
        if (cultist.isConverted()) {
            details.add(Text.literal("Converted").withColor(WyspiaExpressRoles.CULT_LEADER.color()));
        }
        if (hacker.hackingTime > 0 && hacker.hackingTime >= hackingDuration) {
            details.add(Text.literal("Hacked").withColor(KinsWatheRoles.HACKER.color()));
        }
        if (role == Noellesroles.MORPHLING && MorphlingPlayerComponent.KEY.get(targetPlayer).morphTicks > 0) {
            details.add(Text.literal("Transformed").withColor(role.color()));
        }
        StarstruckComponent starstruck = StarstruckComponent.KEY.get(targetPlayer);
        if (starstruck.ticks > 0) {
            details.add(Text.translatable("hud.wyspiaexpress.status.starstruck", (int) Math.ceil(starstruck.ticks / 20.0))
                    .withColor(StarryExpressRoles.STARSTRUCK.color()));
        }
        if (DousedPlayerComponent.KEY.get(targetPlayer).isDoused()) {
            details.add(Text.literal("Doused").withColor(SERoles.ARSONIST.color()));
        }
        if (SilenceComponent.KEY.get(targetPlayer).isSilenced()) {
            details.add(Text.literal("Muzzled").withColor(0xBEAFC9));
        }
        if (BartenderPlayerComponent.KEY.get(targetPlayer).armor > 0 || PhysicianComponent.KEY.get(targetPlayer).physicianArmor > 0) {
            details.add(Text.literal("Protected").withColor(0x7CA8FF));
        }
        if (DreamerComponent.KEY.get(targetPlayer).dreamArmor > 0) {
            details.add(Text.literal("Dreamed").withColor(0xFF6BFA));
        }

        float scale = 0.7f;
        int width = Math.max(1, (int) (Math.min(188, context.getScaledWindowWidth() * 0.375f) / scale));
        List<HudLine> lines = new ArrayList<>();
        int height = layoutEntries(renderer, lines, stats, 0, width);
        if (!details.isEmpty()) height = layoutEntries(renderer, lines, details, height + 3, width);
        scale = Math.min(scale, (context.getScaledWindowHeight() - 32f) / height);
        int top = Math.max(8, Math.min(context.getScaledWindowHeight() / 3,
                context.getScaledWindowHeight() - 24 - (int) Math.ceil(height * scale)));
        int color = ((int) (nametagAlpha * 255) << 24) | 0xDDDAD3;
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(8, top, 0);
        matrices.scale(scale, scale, 1);
        int contentWidth = lines.stream().mapToInt(line -> line.x() + renderer.getWidth(line.text())).max().orElse(0);
        context.fill(-4, -3, contentWidth + 4, height + 2, ((int) (nametagAlpha * 92) << 24) | 0x1C1B19);
        for (HudLine line : lines) {
            OrderedText text = visitor -> line.text().accept((index, style, codePoint) -> visitor.accept(index,
                    style.getColor() == null ? style : style.withColor(readableColor(style.getColor().getRgb())), codePoint));
            context.drawTextWithShadow(renderer, text, line.x(), line.y(), color);
        }
        matrices.pop();
    }

    @Unique
    private static MutableText valueText(String label, String value, int color) {
        return Text.literal(label).append(Text.literal(value).withColor(color));
    }

    @Unique
    private static int remainingPercent(int ticks, int seconds) {
        return 100 - Math.min(100, (int) (ticks * 100.0 / Math.max(1, seconds * 40.0)));
    }

    @Unique
    private static int readableColor(int color) {
        int red = (color >> 16) & 255, green = (color >> 8) & 255, blue = color & 255;
        double brightness = red * 0.2126 + green * 0.7152 + blue * 0.0722;
        if (brightness >= 155) return color & 0xFFFFFF;
        double tint = (155 - brightness) / (255 - brightness);
        return ((red + (int) ((255 - red) * tint)) << 16)
                | ((green + (int) ((255 - green) * tint)) << 8)
                | (blue + (int) ((255 - blue) * tint));
    }

    @Unique
    private static int layoutEntries(TextRenderer renderer, List<HudLine> lines, List<Text> entries, int top, int width) {
        int rowHeight = renderer.fontHeight + 1;
        int x = 0, y = top;
        for (Text entry : entries) {
            int entryWidth = renderer.getWidth(entry);
            if (x > 0 && x + entryWidth > width) {
                x = 0;
                y += rowHeight;
            }
            if (entryWidth > width) {
                for (OrderedText line : renderer.wrapLines(entry, width)) {
                    lines.add(new HudLine(line, 0, y));
                    y += rowHeight;
                }
            } else {
                lines.add(new HudLine(entry.asOrderedText(), x, y));
                x += entryWidth + 10;
            }
        }
        return x > 0 ? y + rowHeight : y;
    }

    public record HudLine(OrderedText text, int x, int y) {}

    @Unique
    private static int getAbilityCooldown(PlayerEntity player, Role role) {
        if (role == null) return 0;
        if (role == Noellesroles.MORPHLING) return -MorphlingPlayerComponent.KEY.get(player).getMorphTicks();
        return switch (role.identifier().getNamespace()) {
            case WyspiaExpress.MOD_ID -> AbilityCooldownComponent.KEY.get(player).cooldown;
            case "noellesroles" -> org.agmas.noellesroles.AbilityPlayerComponent.KEY.get(player).cooldown;
            case "kinswathe" -> org.BsXinQin.kinswathe.component.AbilityPlayerComponent.KEY.get(player).cooldown;
            case "starexpress" -> org.aussiebox.starexpress.cca.AbilityComponent.KEY.get(player).cooldown;
            case "stupid_express" -> pro.fazeclan.river.stupid_express.cca.AbilityCooldownComponent.KEY.get(player).getCooldown();
            default -> 0;
        };
    }

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private static void b(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Local(name = "target") PlayerEntity target) {
        targetPlayer = target;
    }
}
