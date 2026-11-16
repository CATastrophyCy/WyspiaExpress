package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.Harpymodloader;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WorldComponent implements AutoSyncedComponent {
    public static final ComponentKey<WorldComponent> KEY =
            ComponentRegistry.getOrCreate(
                    Identifier.of(WyspiaExpress.MOD_ID, "world_component"),
                    WorldComponent.class
            );
    @NotNull
    private final World world;
    private final Set<UUID> playerDead = new HashSet<UUID>();
    private final Set<Text> hackerReveals = new LinkedHashSet<>();
    private UUID roundId = UUID.randomUUID();

    public WorldComponent(@NotNull World world) {
        this.world = world;
    }
    public void sync() {
        KEY.sync(this.world);
    }

    public void reset() {
        playerDead.clear();
        startRound();
        sync();
    }

    public UUID roundId() { return roundId; }

    public void startRound() {
        // Invalidates cooldown snapshots for players who stayed offline across a round boundary.
        roundId = UUID.randomUUID();
        hackerReveals.clear();
    }

    public void recordHack(PlayerEntity target, Role role) {
        hackerReveals.add(Text.literal(target.getName().getString() + " ")
                .styled(style -> style.withItalic(false).withColor(0xFFFFFF))
                .append(Harpymodloader.getRoleName(role).copy()
                        .styled(style -> style.withItalic(false).withColor(role.color()))));
    }

    public void restoreHackerPhone(PlayerEntity player, ItemStack phone) {
        if (phone.isOf(KinsWatheItems.PHONE) && canReceiveHackerReveals(player)) restoreHackerPhone(phone);
    }

    public void restoreHackerPhones(PlayerEntity player) {
        if (!canReceiveHackerReveals(player)) return;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            restoreHackerPhone(player.getInventory().getStack(slot));
        }
        restoreHackerPhone(player.currentScreenHandler.getCursorStack());
    }

    private boolean canReceiveHackerReveals(PlayerEntity player) {
        if (hackerReveals.isEmpty() || !GameFunctions.isPlayerAliveAndSurvival(player)) return false;
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        return game.isRunning() && game.getRole(player) != null
                && (game.canUseKillerFeatures(player) || game.isRole(player, KinsWatheRoles.HACKER));
    }

    private void restoreHackerPhone(ItemStack phone) {
        if (!phone.isOf(KinsWatheItems.PHONE)) return;
        LoreComponent lore = phone.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
        List<Text> missing = hackerReveals.stream().filter(reveal -> !lore.lines().contains(reveal)).toList();
        if (missing.isEmpty()) return;
        List<Text> lines = new ArrayList<>(lore.lines());
        lines.addAll(missing);
        phone.set(DataComponentTypes.LORE, new LoreComponent(lines));
    }

   public void addPlayerDead(UUID uuid) {
        this.playerDead.add(uuid);
        this.sync();
   }
   public void removePlayerDead(UUID uuid) {
        this.playerDead.remove(uuid);
        this.sync();
   }
   public boolean isPlayerDead(UUID uuid) {
        return this.playerDead.contains(uuid);
   }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (UUID uuid : this.playerDead) {
            list.add(NbtHelper.fromUuid(uuid));
        }
        tag.put("DeadPlayers", list);
        tag.putUuid("RoundId", roundId);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
        this.playerDead.clear();
        if (tag.containsUuid("RoundId")) roundId = tag.getUuid("RoundId");
        if (tag.contains("DeadPlayers", NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList("DeadPlayers", NbtElement.INT_ARRAY_TYPE);
            for (net.minecraft.nbt.NbtElement nbtElement : list) {
                this.playerDead.add(NbtHelper.toUuid(nbtElement));
            }
        }
    }


}
