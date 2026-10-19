package org.cat.express.wyspiaexpress.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.ComponentV3;

import java.util.ArrayList;
import java.util.UUID;

public final class ItemCooldownComponent implements ComponentV3 {
    public static final ComponentKey<ItemCooldownComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "item_cooldowns"), ItemCooldownComponent.class);

    private final PlayerEntity player;
    private NbtCompound pendingCooldowns;
    private UUID savedRoundId;

    public ItemCooldownComponent(PlayerEntity player) { this.player = player; }

    private UUID currentRoundId(ServerPlayerEntity player) {
        return WorldComponent.KEY.get(player.getServer().getOverworld()).roundId();
    }

    public void restore() {
        if (!(player instanceof ServerPlayerEntity serverPlayer) || pendingCooldowns == null) return;
        if (currentRoundId(serverPlayer).equals(savedRoundId)) {
            for (String key : pendingCooldowns.getKeys()) {
                Identifier id = Identifier.tryParse(key);
                int ticks = pendingCooldowns.getInt(key);
                if (id != null && Registries.ITEM.containsId(id) && ticks > 0) {
                    player.getItemCooldownManager().set(Registries.ITEM.get(id), ticks);
                }
            }
        }
        pendingCooldowns = null;
        savedRoundId = null;
    }

    public void reset() {
        pendingCooldowns = null;
        savedRoundId = null;
        if (player instanceof ServerPlayerEntity) {
            var manager = player.getItemCooldownManager();
            for (var item : new ArrayList<>(manager.entries.keySet())) manager.remove(item);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        // Preserve loaded data if the player is saved again before JOIN restores it.
        if (pendingCooldowns != null) {
            tag.put("Items", pendingCooldowns.copy());
            if (savedRoundId != null) tag.putUuid("RoundId", savedRoundId);
            return;
        }
        var manager = player.getItemCooldownManager();
        NbtCompound items = new NbtCompound();
        manager.entries.forEach((item, entry) -> {
            int remaining = entry.endTick - manager.tick;
            if (remaining > 0) items.putInt(Registries.ITEM.getId(item).toString(), remaining);
        });
        tag.put("Items", items);
        tag.putUuid("RoundId", currentRoundId(serverPlayer));
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        pendingCooldowns = tag.getCompound("Items").copy();
        savedRoundId = tag.containsUuid("RoundId") ? tag.getUuid("RoundId") : null;
    }
}
