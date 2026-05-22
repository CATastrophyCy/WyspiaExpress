package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.cat.express.wyspiaexpress.RoleCategoryStatisticsManager;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.components.roles.LichReviveComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModdedMurderGameMode.class)
public abstract class HMLGameInitializeMixin {
    @Inject(method = "initializeGame", at = @At("TAIL"), cancellable = false)
    void wyspiaexpress$initializeGame(ServerWorld serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayerEntity> players, CallbackInfo ci) {
        if(!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(WyspiaExpressRoles.LICH.identifier().toString())){
            int maximumLichRevive = WyspiaExpress.ROLES_CONFIG.roleConfig.lichConfig.additionalRevive();
            var component = LichReviveComponent.KEY.get(serverWorld);
            component.setAvailableRevives(maximumLichRevive);
            for(ServerPlayerEntity serverPlayerEntity : players){
                if(gameWorldComponent.canUseKillerFeatures(serverPlayerEntity)){
                    maximumLichRevive++;
                    RoleCategoryStatisticsManager.getInstance()
                            .recordRoleCategory(serverPlayerEntity.getUuid(), serverPlayerEntity.getName().getString(), "killer");
                }
                else if(gameWorldComponent.isInnocent(serverPlayerEntity)){
                    if(gameWorldComponent.isRole(serverPlayerEntity, WatheRoles.VIGILANTE)){
                        RoleCategoryStatisticsManager.getInstance()
                                .recordRoleCategory(serverPlayerEntity.getUuid(), serverPlayerEntity.getName().getString(), "vigilante");
                    }
                    else{
                        RoleCategoryStatisticsManager.getInstance()
                                .recordRoleCategory(serverPlayerEntity.getUuid(), serverPlayerEntity.getName().getString(), "civilian");
                    }
                }
                else{
                    RoleCategoryStatisticsManager.getInstance()
                            .recordRoleCategory(serverPlayerEntity.getUuid(), serverPlayerEntity.getName().getString(), "neutral");
                }
            }
            component.setMaxRevives(maximumLichRevive);
        }

    }

    // this makes it so
    @ModifyArg(
            method = {"assignCivilianReplacingRoles", "assignKillerReplacingRoles"},
            at = @At(value = "INVOKE", target = "Lorg/agmas/harpymodloader/modded_murder/ModdedMurderGameMode;findAndAssignPlayers(ILdev/doctor4t/wathe/api/Role;Ljava/util/List;Ldev/doctor4t/wathe/cca/GameWorldComponent;Lnet/minecraft/world/World;)I"),
            index = 0
    )
    private int randomizeLimit(int desiredRoleCount, Role role, List<ServerPlayerEntity> players, GameWorldComponent gameWorldComponent, World world) {
        if (desiredRoleCount <= 0) {
            return 0;
        }
        return world.getRandom().nextInt(desiredRoleCount ) + 1 ;
    }

}
