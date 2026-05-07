package org.cat.express.wyspiaexpress.mixin.noellesroles;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.effect.StatusEffectInstance;
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
                    value = "NEW",
                    target = "net/minecraft/entity/effect/StatusEffectInstance"
            )
    )
    private static StatusEffectInstance wyspiaexpress$phantomDuration(
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect,
            int duration,
            int amplifier,
            boolean ambient,
            boolean showParticles,
            boolean showIcon
    ) {
        return new StatusEffectInstance(
                effect,
                WyspiaExpress.ROLES_CONFIG.roleConfig.noellesRoles.phantomConfig.duration(),
                amplifier,
                ambient,
                showParticles,
                showIcon
        );
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