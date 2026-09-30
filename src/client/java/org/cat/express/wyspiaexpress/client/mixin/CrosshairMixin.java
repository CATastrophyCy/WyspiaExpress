package org.cat.express.wyspiaexpress.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.doctor4t.wathe.client.gui.CrosshairRenderer;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.cat.express.wyspiaexpress.WyspiaExpressItems;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// copied from Kinswathe and modified
@Mixin(value = CrosshairRenderer.class, priority = 500)
public class CrosshairMixin {

    @Unique private static final Identifier CROSSHAIR = Identifier.of("wathe", "hud/crosshair");
    @Unique private static final Identifier CROSSHAIR_TARGET = Identifier.of("wathe", "hud/crosshair_target");
    @Unique private static final Identifier KNIFE_ATTACK = Identifier.of("wathe", "hud/knife_attack");
    @Unique private static final Identifier KNIFE_PROGRESS = Identifier.of("wathe", "hud/knife_progress");
    @Unique private static final Identifier KNIFE_BACKGROUND = Identifier.of("wathe", "hud/knife_background");

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private static void renderCrosshair(@NotNull MinecraftClient client, @NotNull ClientPlayerEntity player, @NotNull DrawContext context, @NotNull RenderTickCounter tickCounter, @NotNull CallbackInfo ci) {
        if (!client.options.getPerspective().isFirstPerson()) return;
        boolean target = false;
        ItemStack mainHandStack = player.getMainHandStack();
        ItemCooldownManager manager = player.getItemCooldownManager();
        if (    mainHandStack.isOf(WatheItems.REVOLVER) ||
                mainHandStack.isOf(KinsWatheItems.BLOWGUN) ||
                mainHandStack.isOf(WyspiaExpressItems.OUTLAW_REVOLVER)
        ) {
            ci.cancel();
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2F, context.getScaledWindowHeight() / 2F, 0);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            HitResult hitResult = ProjectileUtil.getCollision(player, entity -> entity instanceof @NotNull PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer) && !targetPlayer.isInvisible(), 15.0F);
            if (!manager.isCoolingDown(mainHandStack.getItem()) && hitResult instanceof @NotNull EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof @NotNull PlayerEntity) {
                target = true;
            }
            renderCrosshair(context, target);
        }
        else if (mainHandStack.isOf(WatheItems.KNIFE) ||
                mainHandStack.isOf(KinsWatheItems.HUNTING_KNIFE) ||
                mainHandStack.isOf(KinsWatheItems.PAN) ||
                mainHandStack.isOf(KinsWatheItems.POISON_INJECTOR) ||
                mainHandStack.isOf(WyspiaExpressItems.RITUAL_DAGGER)) {
            ci.cancel();
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2F, context.getScaledWindowHeight() / 2F, 0);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            HitResult hitResult = ProjectileUtil.getCollision(player, entity -> entity instanceof @NotNull PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer) && !targetPlayer.isInvisible(), 3.0F);
            if (!manager.isCoolingDown(mainHandStack.getItem()) && hitResult instanceof @NotNull EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof @NotNull PlayerEntity) {
                target = true;
                context.drawGuiTexture(KNIFE_ATTACK, -5, 5, 10, 7);
            } else {
                float progress = 1 - manager.getCooldownProgress(mainHandStack.getItem(), tickCounter.getTickDelta(true));
                context.drawGuiTexture(KNIFE_BACKGROUND, -5, 5, 10, 7);
                context.drawGuiTexture(KNIFE_PROGRESS, 10, 7, 0, 0, -5, 5, (int) (progress * 10.0F), 7);
            }
            renderCrosshair(context, target);
        }
    }

    @Unique
    private static void renderCrosshair(@NotNull DrawContext context, boolean target) {
        context.getMatrices().push();
        context.getMatrices().translate(-1.5f, -1.5f, 0);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        context.drawGuiTexture(target ? CROSSHAIR_TARGET : CROSSHAIR, 0, 0, 3, 3);
        context.getMatrices().pop();
        context.getMatrices().pop();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}