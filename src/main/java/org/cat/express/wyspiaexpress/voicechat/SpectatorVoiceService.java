package org.cat.express.wyspiaexpress.voicechat;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class SpectatorVoiceService {
    public enum Result {
        SUCCESS,
        UNAVAILABLE,
        NOT_CONNECTED,
        INVALID_INDEX,
        GROUP_UNAVAILABLE
    }

    private SpectatorVoiceService() {}

    public static boolean isValidIndex(int index) {
        return index >= 0 && index <= WyspiaExpress.SERVER_CONFIG.extraSpectatorsVoicechat();
    }

    public static Result join(UUID player, int index) {
        if (!isValidIndex(index)) return Result.INVALID_INDEX;
        VoicechatServerApi api = TrainVoicePlugin.SERVER_API;
        if (api == null) return Result.UNAVAILABLE;
        VoicechatConnection connection = api.getConnectionOf(player);
        if (connection == null) return Result.NOT_CONNECTED;
        Group group = getOrCreateGroup(index);
        if (group == null) return Result.GROUP_UNAVAILABLE;
        connection.setGroup(group);
        return Result.SUCCESS;
    }

    public static Result leave(UUID player) {
        VoicechatServerApi api = TrainVoicePlugin.SERVER_API;
        if (api == null) return Result.UNAVAILABLE;
        VoicechatConnection connection = api.getConnectionOf(player);
        if (connection == null) return Result.NOT_CONNECTED;
        connection.setGroup(null);
        return Result.SUCCESS;
    }

    @Nullable
    public static Group getOrCreateGroup(int index) {
        if (!isValidIndex(index)) return null;
        VoicechatServerApi api = TrainVoicePlugin.SERVER_API;
        if (api == null) return null;

        UUID id = index == 0 ? TrainVoicePlugin.GROUP_ID : UUID.nameUUIDFromBytes(
                ("wyspiaexpress:train_spectator_" + index).getBytes(StandardCharsets.UTF_8));
        // getGroup(id) can return a non-null wrapper around a missing group in Simple Voice Chat.
        // Enumerate registered groups so neither commands nor Wathe's death handler receive it.
        Group group = api.getGroups().stream()
                .filter(existing -> id.equals(existing.getId()))
                .findFirst()
                .orElse(null);
        if (group == null) {
            group = api.groupBuilder()
                    .setHidden(true)
                    .setId(id)
                    .setName(index == 0 ? "Train Spectators" : "Train Spectators " + index)
                    .setPersistent(true)
                    .setType(Group.Type.OPEN)
                    .build();
        }
        if (index == 0) TrainVoicePlugin.GROUP = group;
        return group;
    }
}
