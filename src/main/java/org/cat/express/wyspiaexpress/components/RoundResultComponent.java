package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.cat.express.wyspiaexpress.util.RoleUtil;
import org.cat.express.wyspiaexpress.game.gamemode.WyspiaExpressModdedGameMode;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Captured result, retained through lobby/reset rendering; never resolves UUIDs into live player entities. */
public final class RoundResultComponent implements AutoSyncedComponent {
    public static final ComponentKey<RoundResultComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "round_result"), RoundResultComponent.class);

    public enum Outcome {
        CIVILIANS("civilian", "passengers"), KILLERS("killer", "killers"), TIMEOUT("civilian", "time"),
        CULT("kinswathe.cult"), LICENSED_VILLAIN("kinswathe.villain_licensed"),
        EDDIE_WAFFLES("kinswathe.eddie_waffles"), THIEF("kinswathe.thief"),
        ARSONIST("stupid_express.arsonist"), LOVERS("stupid_express.lovers"), NEUTRAL("wyspiaexpress.neutral");

        private final String announcement;
        private final String explanation;

        Outcome(String key) { this(key, key); }
        Outcome(String announcement, String explanation) {
            this.announcement = announcement;
            this.explanation = explanation;
        }
    }

    private final World world;
    private Outcome outcome;
    private int color;
    private Set<UUID> winners = Set.of();
    private Map<UUID, Role> roles = Map.of();

    public RoundResultComponent(World world) { this.world = world; }

    public boolean hasResult() { return outcome != null; }
    public boolean ownsResult() {
        return hasResult() && GameWorldComponent.KEY.get(world).getGameMode() instanceof WyspiaExpressModdedGameMode;
    }
    public boolean didWin(UUID player) { return winners.contains(player); }
    public boolean participated(UUID player) { return roles.containsKey(player); }
    public Role role(UUID player) { return roles.get(player); }
    public Outcome outcome() { return outcome; }
    public int color() { return color; }
    public Set<UUID> winners() { return winners; }
    public MutableText title() {
        return Text.translatable("announcement.win." + outcome.announcement);
    }
    public MutableText description() {
        return Text.translatable("game.win." + outcome.explanation);
    }

    public void reset() {
        outcome = null;
        color = 0;
        winners = Set.of();
        roles = Map.of();
        KEY.sync(world);
    }

    /** First result wins. Native and external snapshots cannot replace a committed custom claim. */
    public void setVictory(Outcome outcome, int color, Collection<UUID> winners) {
        if (hasResult()) return;
        this.outcome = outcome;
        this.color = color;
        this.roles = Map.copyOf(GameWorldComponent.KEY.get(world).getRoles());
        this.winners = Set.copyOf(winners.stream().filter(roles::containsKey).toList());
        KEY.sync(world);
    }

    /** Called before Wathe snapshots/syncs its player profiles, while final roles still exist. */
    public void capture(List<ServerPlayerEntity> players, GameFunctions.WinStatus status) {
        if (hasResult()) return;
        var kin = org.BsXinQin.kinswathe.component.CustomWinnerComponent.KEY.get(world);
        var stupid = pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent.KEY.get(world);
        // Items such as the upstream Arsonist lighter can commit a result outside the mode loop.
        if (kin.hasCustomWinner()) {
            setVictory(externalOutcome(kin.getWinningTextId()), kin.getColor(),
                    kin.getWinners().stream().filter(java.util.Objects::nonNull).map(ServerPlayerEntity::getUuid).toList());
        } else if (stupid.hasCustomWinner()) {
            setVictory(externalOutcome(stupid.getWinningTextId()), stupid.getColor(),
                    stupid.getWinners().stream().filter(java.util.Objects::nonNull).map(player -> player.getUuid()).toList());
        } else {
            boolean killerVictory = status == GameFunctions.WinStatus.KILLERS;
            if (!killerVictory && status != GameFunctions.WinStatus.PASSENGERS && status != GameFunctions.WinStatus.TIME) return;
            GameWorldComponent game = GameWorldComponent.KEY.get(world);
            Outcome outcome = killerVictory ? Outcome.KILLERS
                    : status == GameFunctions.WinStatus.TIME ? Outcome.TIMEOUT : Outcome.CIVILIANS;
            setVictory(outcome, killerVictory ? 0xD23C3C : 0x4EDD35,
                    players.stream().filter(player -> killerVictory ? game.canUseKillerFeatures(player) : game.isInnocent(player))
                            .map(ServerPlayerEntity::getUuid).toList());
        }
    }

    private static Outcome externalOutcome(String textId) {
        String path = textId.substring(textId.indexOf(':') + 1);
        return switch (path) {
            case "cult" -> Outcome.CULT;
            case "licensed_villain", "villain_licensed" -> Outcome.LICENSED_VILLAIN;
            case "eddie_waffles" -> Outcome.EDDIE_WAFFLES;
            case "thief" -> Outcome.THIEF;
            case "arsonist" -> Outcome.ARSONIST;
            case "lovers" -> Outcome.LOVERS;
            default -> Outcome.NEUTRAL;
        };
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        if (outcome != null) tag.putString("Outcome", outcome.name());
        tag.putInt("Color", color);
        tag.put("Winners", toNbt(winners));
        NbtCompound roleTags = new NbtCompound();
        roles.forEach((player, role) -> roleTags.putString(player.toString(), RoleUtil.getRoleId(role)));
        tag.put("Roles", roleTags);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        outcome = tag.contains("Outcome", NbtElement.STRING_TYPE) ? Outcome.valueOf(tag.getString("Outcome")) : null;
        color = tag.getInt("Color");
        winners = fromNbt(tag.getList("Winners", NbtElement.INT_ARRAY_TYPE));
        NbtCompound roleTags = tag.getCompound("Roles");
        Map<UUID, Role> capturedRoles = new HashMap<>();
        for (String player : roleTags.getKeys()) {
            String roleId = roleTags.getString(player);
            WatheRoles.ROLES.stream().filter(role -> RoleUtil.getRoleId(role).equals(roleId)).findFirst()
                    .ifPresent(role -> capturedRoles.put(UUID.fromString(player), role));
        }
        roles = Map.copyOf(capturedRoles);
    }

    private static NbtList toNbt(Set<UUID> players) {
        NbtList list = new NbtList();
        for (UUID player : players) list.add(NbtHelper.fromUuid(player));
        return list;
    }

    private static Set<UUID> fromNbt(NbtList list) {
        Set<UUID> players = new HashSet<>();
        for (NbtElement element : list) players.add(NbtHelper.toUuid(element));
        return Set.copyOf(players);
    }
}
