package org.cat.express.wyspiaexpress.mixin.roles;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.tag.WatheItemTags;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(value = ItemEntity.class, priority = 1100)
public abstract class HitmanGunPickupMixin {
    @Shadow private int pickupDelay;
    @Shadow private UUID owner;

    @WrapMethod(method = "onPlayerCollision")
    private void pickUpGun(PlayerEntity player, Operation<Void> original) {
        ItemEntity item = (ItemEntity) (Object) this;
        var stack = item.getStack();
        if (!WyspiaExpress.ROLES_CONFIG.roleConfig.hitmanConfig.canPickUpGuns()
                || !GameWorldComponent.KEY.get(player.getWorld()).isRole(player, WyspiaExpressRoles.HITMAN)
                || !GameFunctions.isPlayerAliveAndSurvival(player) || !stack.isIn(WatheItemTags.GUNS)) {
            original.call(player);
            return;
        }
        if (item.getWorld().isClient || pickupDelay != 0 || player.equals(item.getOwner())
                || (owner != null && !owner.equals(player.getUuid()))
                || player.getInventory().contains(other -> other.isIn(WatheItemTags.GUNS))) return;

        var type = stack.getItem();
        int count = stack.getCount();
        if (!player.getInventory().insertStack(stack)) return;
        player.sendPickup(item, count);
        if (stack.isEmpty()) {
            item.discard();
            stack.setCount(count);
        }
        player.increaseStat(Stats.PICKED_UP.getOrCreateStat(type), count);
        player.triggerItemPickedUpByEntityCriteria(item);
    }
}
