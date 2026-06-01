package org.cat.express.wyspiaexpress.client.mixin.roles.cult;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.WyspiaexpressClient;
import org.cat.express.wyspiaexpress.client.roles.TargetAbilityUtil;
import org.cat.express.wyspiaexpress.components.AbilityCooldownComponent;
import org.cat.express.wyspiaexpress.components.roles.PlayerCultistComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(RoleNameRenderer.class)
public abstract class CultLeaderTargetHudMixin {


    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void  wyspiaexpress$getTargetPlayerHud(@NotNull TextRenderer renderer, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;
        if (WyspiaexpressClient.TARGET_PLAYER == null) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorld.isRole(player, WyspiaExpressRoles.CULT_LEADER) && WatheClient.isPlayerAliveAndInSurvival()) {
            context.getMatrices().push();
            context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F, (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
            context.getMatrices().scale(0.6F, 0.6F, 1.0F);
            Text targetInfo;
            PlayerCultistComponent targetCultistComponent = PlayerCultistComponent.KEY.get(WyspiaexpressClient.TARGET_PLAYER);
            if (targetCultistComponent.isConverted()) {
                targetInfo = Text.translatable("hud.wyspiaexpress.cult_leader.target").styled(
                        style -> style.withColor(WyspiaExpressRoles.CULT_LEADER.color()))
                        .append(Text.literal(" [ " +
                                        (int) (((float) targetCultistComponent.getConversionTick() /
                                                (WyspiaExpress.ROLES_CONFIG.roleConfig.cultLeaderConfig.conversionTime() * 20)) * 100) + "% ]")
                                .styled(style -> style.withColor(Color.GREEN.getRGB())));
            } else {
                targetInfo = Text.translatable("hud.wyspiaexpress.cult_leader.target_converted").styled(style -> style.withColor(Color.GREEN.getRGB()));
            }
            context.drawTextWithShadow(renderer, targetInfo, -renderer.getWidth(targetInfo) / 2, 32, KinsWatheRoles.HACKER.color());
            context.getMatrices().pop();
        }
    }

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void wyspiaexpress$getTargetBodyHud(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (WyspiaexpressClient.TARGET_BODY == null || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        AbilityCooldownComponent ability = AbilityCooldownComponent.KEY.get(player);
        if (gameWorld.isRole(player, WyspiaExpressRoles.CULT_LEADER) && WatheClient.isPlayerAliveAndInSurvival()) {
            if (ability.cooldown > 0
                    || gameWorld.getRole(WyspiaexpressClient.TARGET_BODY.getPlayerUuid()) == WyspiaExpressRoles.LICH_GHOUL
                    || gameWorld.getRole(WyspiaexpressClient.TARGET_BODY.getPlayerUuid()) == WyspiaExpressRoles.CULTIST)
                return;
            if(!TargetAbilityUtil.isBodyConverted(player.getWorld(), WyspiaexpressClient.TARGET_BODY))return;
            context.getMatrices().push();
            context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F, (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
            context.getMatrices().scale(0.6F, 0.6F, 1.0F);
            Text targetInfo = Text.translatable("hud.wyspiaexpress.cult_leader.revive", WyspiaexpressClient.abilityBind.getBoundKeyLocalizedText()).withColor(WyspiaExpressRoles.CULT_LEADER.color());
            context.drawTextWithShadow(renderer, targetInfo, -renderer.getWidth(targetInfo) / 2, 32, WyspiaExpressRoles.CULT_LEADER.color());
            context.getMatrices().pop();
        }
    }

}