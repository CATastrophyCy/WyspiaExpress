package org.cat.express.wyspiaexpress.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.maxhenkel.voicechat.api.Player;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class RoleNameRendererMixin {
    @Shadow private static float nametagAlpha;

    @Unique
    private static int OFF_SET = 41;
    @Unique private static PlayerEntity targetPlayer;
    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I", ordinal = 0))
    private static void b(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

        if (WatheClient.isPlayerSpectatingOrCreative() && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
            PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(targetPlayer);
            PlayerShopComponent shop = PlayerShopComponent.KEY.get(targetPlayer);
            float rawMood = mood.getMood();
            int coinAmount = shop.balance;
            int moodPercentage = (int)(rawMood * 100);

            int alpha = (int)(nametagAlpha * 255.0F);
            int white = (alpha << 24) | 0xFFFFFF;
            int moodColor = (alpha << 24) | 0x8a3b37;
            int coinColor = (alpha << 24) | 0xdbc51a;

            MutableText hudText = Text.literal("Mood: ").setStyle(Style.EMPTY.withColor(white))
                    .append(Text.literal(moodPercentage + "%").setStyle(Style.EMPTY.withColor(moodColor)))
                    .append(Text.literal(" || Coin: ").setStyle(Style.EMPTY.withColor(white)))
                    .append(Text.literal(String.valueOf(coinAmount)).setStyle(Style.EMPTY.withColor(coinColor)));

            int y = renderer.fontHeight / 2 + 1 + OFF_SET;
            context.drawTextWithShadow(renderer, hudText, -renderer.getWidth(hudText) / 2, y, white);
        }
    }
    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private static void b(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Local(name = "target") PlayerEntity target) {
        targetPlayer = target;
    }

}