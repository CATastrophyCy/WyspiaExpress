package org.cat.express.wyspiaexpress.mixin.kinswathe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.cat.express.wyspiaexpress.components.WorldComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(HackerComponent.class)
public class HackerComponentMixin {
    @Shadow @Final public PlayerEntity player;

    @WrapOperation(method = "addRoleOnPhone", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;getPlayerList()Ljava/util/List;"))
    private List<ServerPlayerEntity> updatePhoneCursors(PlayerManager manager,
            Operation<List<ServerPlayerEntity>> original, @Local Role targetRole) {
        WorldComponent round = WorldComponent.KEY.get(player.getWorld());
        round.recordHack(player, targetRole);
        List<ServerPlayerEntity> players = original.call(manager);
        for (ServerPlayerEntity recipient : players) {
            round.restoreHackerPhone(recipient, recipient.currentScreenHandler.getCursorStack());
        }
        return players;
    }
}
