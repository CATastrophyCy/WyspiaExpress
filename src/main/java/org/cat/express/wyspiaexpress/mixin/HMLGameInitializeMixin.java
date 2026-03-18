package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.roles.ReanimatorReviveComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModdedMurderGameMode.class)
public class HMLGameInitializeMixin {
    @Inject(method = "initializeGame", at = @At("TAIL"), cancellable = false)
    void initializeGame(ServerWorld serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayerEntity> players, CallbackInfo ci) {
        if(!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(WyspiaExpressRoles.REANIMATOR.identifier().toString())){
            int maximumReanimatorRevive = WyspiaExpress.ROLES_CONFIG.roleConfig.reanimatorConfig.additionalRevive();
            var component = ReanimatorReviveComponent.KEY.get(serverWorld);
            component.setAvailableRevives(maximumReanimatorRevive);
            for(ServerPlayerEntity serverPlayerEntity : players){
                if(gameWorldComponent.canUseKillerFeatures(serverPlayerEntity)){
                    maximumReanimatorRevive++;
                }
            }
            component.setMaxRevives(maximumReanimatorRevive);
        }
    }
}
