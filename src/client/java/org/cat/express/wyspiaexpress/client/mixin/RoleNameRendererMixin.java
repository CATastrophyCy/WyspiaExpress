package org.cat.express.wyspiaexpress.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
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
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
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

import java.util.ArrayList;
import java.util.List;

@Mixin(RoleNameRenderer.class)
public abstract class RoleNameRendererMixin {
    @Shadow private static float nametagAlpha;

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

            int y = renderer.fontHeight / 2 + 1 + OFF_SET;
            context.drawTextWithShadow(renderer, hudText, -renderer.getWidth(hudText) / 2, y, white);
            y+= renderer.fontHeight  + 1;

            PlayerCultistComponent  cultist = PlayerCultistComponent.KEY.get(targetPlayer);
            HackerComponent hacker = HackerComponent.KEY.get(targetPlayer);
            DousedPlayerComponent doused = DousedPlayerComponent.KEY.get(targetPlayer);
            List<Text> texts = new ArrayList<>();
            if(cultist.isConverted()){
                texts.add(Text.literal("Converted").setStyle(Style.EMPTY.withColor( (alpha << 24) | WyspiaExpressRoles.CULT_LEADER.color())));
            }
            if(hacker.hackingTime >= KinsWatheConfig.HANDLER.instance().HackerHackingTime * 20){
                texts.add(Text.literal("Hacked").setStyle(Style.EMPTY.withColor( (alpha << 24) | KinsWatheRoles.HACKER.color())));
            }
            if(doused.isDoused()){
                texts.add(Text.literal("Doused").setStyle(Style.EMPTY.withColor( (alpha << 24) | SERoles.ARSONIST.color())));
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