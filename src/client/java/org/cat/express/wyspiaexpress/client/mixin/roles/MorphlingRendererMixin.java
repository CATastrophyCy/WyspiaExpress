package org.cat.express.wyspiaexpress.client.mixin.roles;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.client.util.PlayerProfileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// The skin lookup wraps Noelle's live-target lookup, including first-person arms.
@Mixin(value = PlayerEntityRenderer.class, priority = 1100)
public abstract class MorphlingRendererMixin {
    @WrapMethod(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;")
    private Identifier wyspiaexpress$savedSkin(AbstractClientPlayerEntity player, Operation<Identifier> original) {
        var profile = PlayerProfileUtil.morph(player);
        if (profile != null && PlayerPsychoComponent.KEY.get(player).getPsychoTicks() <= 0) {
            return PlayerProfileUtil.skin(profile).texture();
        }
        return original.call(player);
    }

    @WrapOperation(method = "renderArm", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"))
    private SkinTextures wyspiaexpress$savedArmSkin(AbstractClientPlayerEntity player, Operation<SkinTextures> original) {
        var profile = PlayerProfileUtil.morph(player);
        return profile != null ? PlayerProfileUtil.skin(profile) : original.call(player);
    }
}
