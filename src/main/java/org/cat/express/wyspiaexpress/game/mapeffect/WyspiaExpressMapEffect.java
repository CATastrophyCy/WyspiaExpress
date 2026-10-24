package org.cat.express.wyspiaexpress.game.mapeffect;

import dev.doctor4t.wathe.api.WatheMapEffects;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.TrainWorldComponent;
import dev.doctor4t.wathe.game.mapeffect.HarpyExpressNightTrainMapEffect;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.items.InvisibleArmor;

import java.util.Collections;
import java.util.List;

/** The current night-train setup, with Wyspia's armor and letter patches applied directly. */
public final class WyspiaExpressMapEffect extends HarpyExpressNightTrainMapEffect {
    public WyspiaExpressMapEffect(Identifier identifier) {
        super(identifier);
    }

    @Override
    public void initializeMapEffects(ServerWorld world, List<ServerPlayerEntity> players) {
        TrainWorldComponent train = TrainWorldComponent.KEY.get(world);
        train.setSnow(true);
        train.setFog(true);
        train.setHud(true);
        train.setSpeed(130);
        train.setTime(0);
        train.setTimeOfDay(TrainWorldComponent.TimeOfDay.NIGHT);

        Collections.shuffle(players);
        for (int index = 0; index < players.size(); index++) {
            ServerPlayerEntity player = players.get(index);
            int room = index % 7 + 1;
            ItemStack key = new ItemStack(WatheItems.KEY);
            key.set(DataComponentTypes.LORE, new LoreComponent(Text.literal("Room " + room)
                    .getWithStyle(Style.EMPTY.withItalic(false).withColor(0xFF8C00))));
            player.giveItemStack(key);
            player.equipStack(EquipmentSlot.HEAD, new ItemStack(InvisibleArmor.INVISIBLE_HELMET));
            player.equipStack(EquipmentSlot.CHEST, new ItemStack(InvisibleArmor.INVISIBLE_CHESTPLATE));
            player.equipStack(EquipmentSlot.LEGS, new ItemStack(InvisibleArmor.INVISIBLE_LEGGINGS));
            player.equipStack(EquipmentSlot.FEET, new ItemStack(InvisibleArmor.INVISIBLE_BOOTS));
        }
    }

    @Override
    public void finalizeMapEffects(ServerWorld world, List<ServerPlayerEntity> players) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(world);
        gameWorld.setMapEffect(WatheMapEffects.HARPY_EXPRESS_LOBBY);
        gameWorld.getMapEffect().initializeMapEffects(world, players);
    }
}
