package org.cat.express.wyspiaexpress.mixin.noellesroles;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Noellesroles.class)
public abstract class NoellesRolesMixin {

    @Redirect(
            method = "lambda$registerPackets$16",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"
            )
    )
    private static boolean wyspiaexpress$onPhantomAbility(ServerPlayerEntity instance, StatusEffectInstance statusEffectInstance) {
        instance.removeStatusEffect(StatusEffects.GLOWING);

        StatusEffectInstance modifiedEffect = new StatusEffectInstance(
                statusEffectInstance.getEffectType(),
                GameConstants.getInTicks(0, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.phantomConfig.duration()),
                statusEffectInstance.getAmplifier(),
                statusEffectInstance.isAmbient(),
                statusEffectInstance.shouldShowParticles(),
                statusEffectInstance.shouldShowIcon()
        );

        return instance.addStatusEffect(modifiedEffect);
    }
    @Redirect(
            method = "lambda$registerPackets$16",
            slice = @Slice(
                    from = @At(value = "NEW", target = "net/minecraft/entity/effect/StatusEffectInstance")
            ),
            at = @At(
                    value = "FIELD",
                    target = "Lorg/agmas/noellesroles/AbilityPlayerComponent;cooldown:I",
                    opcode = org.objectweb.asm.Opcodes.PUTFIELD,
                    ordinal = 0
            )
    )
    private static void wyspiaexpress$phantomCooldown(AbilityPlayerComponent component, int value) {
        component.cooldown = GameConstants.getInTicks(0, WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.phantomConfig.cooldown());
    }
}