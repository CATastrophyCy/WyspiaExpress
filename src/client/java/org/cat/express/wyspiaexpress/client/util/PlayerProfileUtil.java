package org.cat.express.wyspiaexpress.client.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import dev.doctor4t.wathe.client.WatheClient;
import org.cat.express.wyspiaexpress.components.RoundResultComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerProfileUtil {
    private static Map<UUID, GameProfile> roster = Map.of();
    private static final Map<UUID, PlayerListEntry> skins = new HashMap<>();

    private PlayerProfileUtil() {}

    public static SkinTextures skin(GameProfile profile) {
        var current = RoundResultComponent.KEY.get(MinecraftClient.getInstance().world).profiles();
        if (roster != current) { roster = current; skins.clear(); }
        return skins.computeIfAbsent(profile.getId(), id -> new PlayerListEntry(profile, false)).getSkinTextures();
    }

    public static GameProfile morph(PlayerEntity player) {
        // Keep Noelle's existing depressed-player hallucinations ahead of Morphling disguises.
        if (WatheClient.moodComponent != null && WatheClient.moodComponent.isLowerThanDepressed()
                && ConfigWorldComponent.KEY.get(player.getWorld()).insaneSeesMorphs
                && NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE != null
                && NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE.containsKey(player.getUuid())) return null;
        var morph = MorphlingPlayerComponent.KEY.get(player);
        return morph.getMorphTicks() > 0 ? RoundResultComponent.KEY.get(player.getWorld()).profile(morph.disguise) : null;
    }
}
