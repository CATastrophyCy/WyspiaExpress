package org.cat.express.wyspiaexpress.items;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;

public class TpReadyItem extends Item{
    public TpReadyItem(@NotNull Settings settings) {
        super(settings);
    }
    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x20BB20)));
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            return TypedActionResult.pass(stack);
        }
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        GameWorldComponent.GameStatus status = gwc.getGameStatus();
        boolean gameRunning = status == GameWorldComponent.GameStatus.ACTIVE
                || status == GameWorldComponent.GameStatus.STARTING
                || status == GameWorldComponent.GameStatus.STOPPING;
        if (gameRunning) {
            return TypedActionResult.fail(stack);
        }
        if (!(user instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.pass(stack);
        }


        TeleportTarget target = new TeleportTarget(serverPlayer.getServerWorld(),
                new Vec3d(WyspiaExpress.SERVER_CONFIG.readyTrainTp.x(), WyspiaExpress.SERVER_CONFIG.readyTrainTp.y(), WyspiaExpress.SERVER_CONFIG.readyTrainTp.z()),
                Vec3d.ZERO, WyspiaExpress.SERVER_CONFIG.readyTrainTp.yaw(), WyspiaExpress.SERVER_CONFIG.readyTrainTp.pitch(), TeleportTarget.NO_OP);

        serverPlayer.teleportTo(target);
        return TypedActionResult.success(stack);
    }
}




