package org.cat.express.wyspiaexpress.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.PlayerDepressedComponent;
import org.cat.express.wyspiaexpress.components.PlayerFreezeComponent;
import org.cat.express.wyspiaexpress.components.roles.PlayerCultistComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.role.arsonist.cca.DousedPlayerComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(RoleNameRenderer.class)
public abstract class RoleNameRendererMixin {
    @Shadow private static float nametagAlpha;
    @Unique private static final UUID DELUSION_MARKER = UUID.fromString("00000000-0000-0000-dead-c0de00000000"); // unique string used by Kinswathe

    @Unique
    private static int OFF_SET = 41;
    @Unique private static PlayerEntity targetPlayer;
    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I", ordinal = 0))
    private static void b(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        GameWorldComponent worldComponent = GameWorldComponent.KEY.get(player.getWorld());

        if (worldComponent.isRunning() && WatheClient.isPlayerSpectatingOrCreative() && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
            PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(targetPlayer);
            PlayerShopComponent shop = PlayerShopComponent.KEY.get(targetPlayer);
            PlayerFreezeComponent freeze = PlayerFreezeComponent.KEY.get(targetPlayer);
            PlayerDepressedComponent  depressed = PlayerDepressedComponent.KEY.get(targetPlayer);
            float rawMood = mood.getMood();
            int coinAmount = shop.balance;
            int moodPercentage = (int)(rawMood * 100);
            float rawFreeze = ((float)freeze.getFreezeTick()) / (2 * GameConstants.getInTicks(0, WyspiaExpress.SERVER_CONFIG.freezeTimer()));
            int freezePercentage = 100 - (int)(rawFreeze * 100);
            float rawDepression = ((float)depressed.getDepressionTick()) / (2 * GameConstants.getInTicks(0, WyspiaExpress.SERVER_CONFIG.depressedTimer()));
            int depressionPercentage = 100 - (int)(rawDepression * 100);
            int alpha = (int)(nametagAlpha * 255.0F);
            int white = (alpha << 24) | 0xFFFFFF;
            int moodColor = (alpha << 24) | 0x8a3b37;
            int coinColor = (alpha << 24) | 0xdbc51a;

            MutableText hudText = Text.literal("Mood: ").setStyle(Style.EMPTY.withColor(white))
                    .append(Text.literal(moodPercentage + "%").setStyle(Style.EMPTY.withColor(moodColor)))
                    .append(Text.literal(" || Coin: ").setStyle(Style.EMPTY.withColor(white)))
                    .append(Text.literal(String.valueOf(coinAmount)).setStyle(Style.EMPTY.withColor(coinColor)))
                    .append(Text.literal(" || Freeze: ").setStyle(Style.EMPTY.withColor(white)))
                    .append(Text.literal(freezePercentage + "%").setStyle(Style.EMPTY.withColor((alpha << 24) | 0x2B52CC)))
                    .append(Text.literal(" || Depression: ").setStyle(Style.EMPTY.withColor(white)))
                    .append(Text.literal(depressionPercentage + "%").setStyle(Style.EMPTY.withColor( (alpha << 24 ) | 0xA712E0)));

            PlayerCultistComponent  cultist = PlayerCultistComponent.KEY.get(targetPlayer);
            if(cultist.getConversionTick() > 0 && !cultist.isConverted()){
                hudText
                    .append(Text.literal(" || Conversion: ").setStyle(Style.EMPTY.withColor(white)))
                    .append(Text.literal( (int) (cultist.getConversionProgress() * 100) + "%").setStyle(Style.EMPTY.withColor( (alpha << 24) | WyspiaExpressRoles.CULT_LEADER.color())));
            }
            HackerComponent hacker = HackerComponent.KEY.get(targetPlayer);
            if( hacker.hackingTime > 0 && hacker.hackingTime < KinsWatheConfig.HANDLER.instance().HackerHackingTime * 20){
                hudText
                        .append(Text.literal(" || Hacking: ").setStyle(Style.EMPTY.withColor(white)))
                        .append(Text.literal(( (hacker.hackingTime * 100) / (KinsWatheConfig.HANDLER.instance().HackerHackingTime * 20))
                                + "%").setStyle(Style.EMPTY.withColor( (alpha << 24) | KinsWatheRoles.HACKER.color())));
            }
            int y = renderer.fontHeight / 2 + 1 + OFF_SET;
            context.drawTextWithShadow(renderer, hudText, -renderer.getWidth(hudText) / 2, y, white);
            y+= renderer.fontHeight  + 1;


            List<Text> texts = new ArrayList<>();
            PlayerPoisonComponent playerPoisonComponent = PlayerPoisonComponent.KEY.get(targetPlayer);

            if (playerPoisonComponent.poisonTicks > 0) {
                if(playerPoisonComponent.poisoner != null && playerPoisonComponent.poisoner.equals(DELUSION_MARKER)) {
                    texts.add(Text.literal("Deluded " + playerPoisonComponent.poisonTicks / 20 + "s").setStyle(Style.EMPTY.withColor( (alpha << 24) | 0x9300FF))); ;
                }
                else{
                    texts.add(Text.literal("Poisoned " + playerPoisonComponent.poisonTicks / 20 + "s").setStyle(Style.EMPTY.withColor( (alpha << 24) | Color.RED.getRGB())));
                }
            }

            if(cultist.isConverted()){
                texts.add(Text.literal("Converted").setStyle(Style.EMPTY.withColor( (alpha << 24) | WyspiaExpressRoles.CULT_LEADER.color())));
            }

            if(hacker.hackingTime >= KinsWatheConfig.HANDLER.instance().HackerHackingTime * 20){
                texts.add(Text.literal("Hacked").setStyle(Style.EMPTY.withColor( (alpha << 24) | KinsWatheRoles.HACKER.color())));
            }

            MorphlingPlayerComponent morphlingPlayerComponent = MorphlingPlayerComponent.KEY.get(targetPlayer);
            if( worldComponent.isRole(targetPlayer, Noellesroles.MORPHLING) && morphlingPlayerComponent.morphTicks > 0 ) {
                texts.add(Text.literal("Transformed").setStyle(Style.EMPTY.withColor( (alpha << 24) | Noellesroles.MORPHLING.color())));
            }

            DousedPlayerComponent doused = DousedPlayerComponent.KEY.get(targetPlayer);
            if(doused.isDoused()){
                texts.add(Text.literal("Doused").setStyle(Style.EMPTY.withColor( (alpha << 24) | SERoles.ARSONIST.color())));
            }

            SilenceComponent silenceComponent = SilenceComponent.KEY.get(targetPlayer);
            if(silenceComponent.isSilenced()){
                texts.add(Text.literal("Muzzled").setStyle(Style.EMPTY.withColor( (alpha << 24) |  0x4A3A54)));
            }

            BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(targetPlayer);
            PhysicianComponent physicianComponent = PhysicianComponent.KEY.get(targetPlayer);
            DreamerComponent dreamerComponent = DreamerComponent.KEY.get(targetPlayer);
            if( bartenderPlayerComponent.armor > 0 || physicianComponent.physicianArmor > 0) {
                texts.add(Text.literal("Protected").setStyle(Style.EMPTY.withColor( (alpha << 24) |  Color.BLUE.getRGB())));
            }
            if( dreamerComponent.dreamArmor > 0) {
                texts.add(Text.literal("Dreamed").setStyle(Style.EMPTY.withColor( (alpha << 24) |  0xFF6BFA)));
            }

            if(!texts.isEmpty()){
                MutableText finalText = (MutableText)texts.getFirst();
                texts.removeFirst();
                for(Text text : texts){
                    finalText.append(Text.literal(" || ").setStyle(Style.EMPTY.withColor(white)));
                    finalText.append(text);
                }
                context.drawTextWithShadow(renderer, finalText, -renderer.getWidth(finalText) / 2, y, white);
            }
        }
    }
    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private static void b(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Local(name = "target") PlayerEntity target) {
        targetPlayer = target;
    }

}