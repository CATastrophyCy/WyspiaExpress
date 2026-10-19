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
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.util.GameUtil;
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


        GameUtil.teleportToReadyArea(serverPlayer);
        return TypedActionResult.success(stack);
    }
}




