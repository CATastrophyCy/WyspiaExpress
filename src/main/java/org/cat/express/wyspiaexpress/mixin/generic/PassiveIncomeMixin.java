package org.cat.express.wyspiaexpress.mixin.generic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
@Mixin(MurderGameMode.class)
public abstract class PassiveIncomeMixin {
    // passive income
    @Unique
    public PlayerEntity income_player;
    @Unique
    public Role player_role;
    @Unique
    public GameWorldComponent gameWorldComponent;
    @WrapOperation(method = "tickServerGameLoop", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    public boolean wyspiaexpress$setPassiveIncome(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player, @NotNull Operation<Boolean> original) {
        Role role = gameWorld.getRole(player);
        gameWorldComponent = gameWorld;
        player_role = role;
        income_player = player;
        if(!GameFunctions.isPlayerAliveAndSurvival(player)) return false;

        // every 10 seconds
        boolean interval = (player.getWorld().getTime() % GameConstants.getInTicks(0,10)) == 0;
        if (interval) {
            // tape drain mood
            if (WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.enableMoodLost()
                    && role.getMoodType() == Role.MoodType.REAL
                    && SilenceComponent.KEY.get(player).isSilenced()) {
                PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
                mood.setMood(mood.getMood() - WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.moodLostAmount());
            }
        }

        var config = WyspiaExpressRoles.ROLES_BASIC_CONFIG.get(role);
        if (config != null && config.passiveIncome()) return true;
        return original.call(gameWorld,player);
    }

    @WrapOperation(method = "tickServerGameLoop", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/PlayerShopComponent;addToBalance(I)V"))
    public void wyspiaexpress$onPassiveIncome(PlayerShopComponent instance, int amount, Operation<Void> original) {
        int income = 0;

        // this basically runs once every 10 seconds
        if(GameFunctions.isPlayerAliveAndSurvival(income_player)) {
            // muzzler income
            if (player_role.equals(StarryExpressRoles.MUZZLER) && WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.muzzlerConfig.enablePayout()) {
                MinecraftServer server = income_player.getServer();
                if (server != null) {
                    PlayerManager playerManager = server.getPlayerManager();
                    List<ServerPlayerEntity> players = playerManager.getPlayerList();
                    for (PlayerEntity p : players) {
                        if (GameFunctions.isPlayerAliveAndSurvival(p)
                                && gameWorldComponent.isInnocent(p)
                                && SilenceComponent.KEY.get(p).isSilenced()) {
                            income += WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.muzzlerConfig.muzzledPayout();
                        }
                    }
                }
            }

        }
         original.call(instance,amount + income);
    }
}
