package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.agmas.noellesroles.Noellesroles;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModdedMurderGameMode.class)
public class KillerAlwaysGuesserMixin {

    @Inject(method = "assignModifiers", at = @At("TAIL"), cancellable = false)
    void assignGuesser(int desiredRoleCount, ServerWorld serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayerEntity> players, @NotNull CallbackInfo ci){
        if(WyspiaExpress.SERVER_CONFIG.killerAlwaysGuesser()) {
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(serverWorld);
            for (ServerPlayerEntity player : players) {
                if (!worldModifierComponent.isModifier(player, Noellesroles.GUESSER) && gameWorldComponent.canUseKillerFeatures(player))
                    worldModifierComponent.addModifier(player.getUuid(), Noellesroles.GUESSER);
            }
        }
    }
}
