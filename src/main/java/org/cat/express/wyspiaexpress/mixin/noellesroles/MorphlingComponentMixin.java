package org.cat.express.wyspiaexpress.mixin.noellesroles;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(MorphlingPlayerComponent.class)
public abstract class MorphlingComponentMixin {
    @Shadow @Final private PlayerEntity player;
    @Shadow public UUID disguise;
    @Shadow public int morphTicks;
    @Shadow public abstract void sync();
    @Shadow public abstract void stopMorph();

    @WrapMethod(method = "startMorph")
    private boolean wyspiaexpress$participantTarget(UUID target, Operation<Boolean> original) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        if (!game.isRunning() || !game.isRole(player, Noellesroles.MORPHLING)
                || !GameFunctions.isPlayerAliveAndSurvival(player) || morphTicks != 0 || target == null
                || target.equals(player.getUuid())
                || !RoundResultComponent.KEY.get(player.getWorld()).participated(target)) return false;
        return original.call(target);
    }

    @WrapMethod(method = "serverTick")
    private void wyspiaexpress$keepSavedDisguise(Operation<Void> original) {
        if (morphTicks > 0 && RoundResultComponent.KEY.get(player.getWorld()).participated(disguise)) {
            if (--morphTicks == 0) stopMorph();
            sync();
            return;
        }
        original.call();
    }
}
