package org.cat.express.wyspiaexpress.components;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.TaskCompletePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;

import java.util.ArrayList;
import java.util.List;

public final class PlayerTaskComponent implements AutoSyncedComponent, ClientTickingComponent {
    public static final ComponentKey<PlayerTaskComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(WyspiaExpress.MOD_ID, "tasks"), PlayerTaskComponent.class);
    private final PlayerEntity player;
    private PlayerMoodComponent.Task lastTask;
    private int nextTaskTicks = GameConstants.getInTicks(0, WyspiaExpress.SERVER_CONFIG.moodTasks.firstTaskDelaySeconds());
    private boolean clientWaiting = true;

    public PlayerTaskComponent(PlayerEntity player) { this.player = player; }

    public interface TaskCompletionAccess {
        void wyspiaexpress$restoreTaskMood(float restoration);
    }

    private PlayerMoodComponent mood() { return PlayerMoodComponent.KEY.get(player); }
    public PlayerMoodComponent.Task lastTask() { return lastTask; }
    public void rememberTask(PlayerMoodComponent.Task type) { lastTask = type; }
    public boolean isWaiting() { return player.getWorld().isClient ? clientWaiting : mood().tasks.isEmpty(); }
    public int nextTaskTicks() {
        return isWaiting() ? nextTaskTicks : 0;
    }

    public void sync() {
        if (!player.getWorld().isClient) KEY.sync(player);
    }

    public void reset() {
        lastTask = null;
        clientWaiting = true;
        nextTaskTicks = GameConstants.getInTicks(0, WyspiaExpress.SERVER_CONFIG.moodTasks.firstTaskDelaySeconds());
        sync();
    }
    public void tickTasks(PlayerMoodComponent component) {
        if (!canTick()) return;
        drainMood(component);

        // The wait only advances when there is no active task.
        if (component.tasks.isEmpty()) {
            if (nextTaskTicks > 0) nextTaskTicks--;
            if (nextTaskTicks == 0) assignTask(chooseTaskType());
        }

        boolean completed = false;
        for (PlayerMoodComponent.TrainTask task : List.copyOf(component.tasks.values())) {
            task.tick(player);
            if (!task.isFulfilled(player)) continue;
            ((TaskCompletionAccess) component).wyspiaexpress$restoreTaskMood(
                    WyspiaExpress.SERVER_CONFIG.moodTasks.completionRestoration());
            if (player instanceof ServerPlayerEntity serverPlayer)
                ServerPlayNetworking.send(serverPlayer, new TaskCompletePayload());
            component.tasks.remove(task.getType());
            completed = true;
        }

        if (completed) {
            if (component.tasks.isEmpty()) {
                int minimum = GameConstants.getInTicks(0, WyspiaExpress.SERVER_CONFIG.moodTasks.minTaskDelaySeconds());
                int maximum = Math.max(minimum,
                        GameConstants.getInTicks(0, WyspiaExpress.SERVER_CONFIG.moodTasks.maxTaskDelaySeconds()));
                nextTaskTicks = minimum + player.getRandom().nextInt(maximum - minimum + 1);
            }
            component.sync();
            sync();
        }
    }

    private boolean canTick() {
        return GameWorldComponent.KEY.get(player.getWorld()).isRunning()
                && GameFunctions.isPlayerAliveAndSurvival(player);
    }
    public void drainMood(PlayerMoodComponent component) {
        if (!canTick()) return;
        if (component.tasks.isEmpty() && !WyspiaExpress.SERVER_CONFIG.moodTasks.passiveMoodDrain()) return;
        float drain = (float) (WyspiaExpress.SERVER_CONFIG.moodTasks.passiveDrainPerSecond() / 20.0);
        if (drain > 0) component.setMood(component.getMood() - Math.max(1, component.tasks.size()) * drain);
    }
    private PlayerMoodComponent.Task chooseTaskType() {
        var candidates = new ArrayList<PlayerMoodComponent.Task>();
        double totalWeight = 0;
        for (PlayerMoodComponent.Task type : PlayerMoodComponent.Task.values()) {
            if (WyspiaExpress.SERVER_CONFIG.moodTasks.preventConsecutiveTasks() && type == lastTask) continue;
            candidates.add(type);
            totalWeight += taskWeight(type);
        }
        double roll = player.getRandom().nextDouble() * totalWeight;
        for (PlayerMoodComponent.Task type : candidates) {
            roll -= taskWeight(type);
            if (roll < 0) return type;
        }
        return candidates.getLast();
    }

    private double taskWeight(PlayerMoodComponent.Task type) {
        return 1.0 / Math.max(1, mood().timesGotten.getOrDefault(type, 1));
    }
    public void forceTask(PlayerMoodComponent.Task type) {
        mood().tasks.clear();
        assignTask(type);
    }

    private void assignTask(PlayerMoodComponent.Task type) {
        PlayerMoodComponent component = mood();
        PlayerMoodComponent.TrainTask task = switch (type) {
            case SLEEP -> new PlayerMoodComponent.SleepTask(GameConstants.SLEEP_TASK_DURATION);
            case OUTSIDE -> new PlayerMoodComponent.OutsideTask(GameConstants.OUTSIDE_TASK_DURATION);
            case EAT -> new PlayerMoodComponent.EatTask();
            case DRINK -> new PlayerMoodComponent.DrinkTask();
        };
        component.tasks.put(type, task);
        component.timesGotten.merge(type, 2, (previous, ignored) -> previous + 1);
        rememberTask(type);
        nextTaskTicks = 0;
        component.sync();
        sync();
    }

    public boolean setNextTaskSeconds(int seconds) {
        if (!mood().tasks.isEmpty()) return false;
        nextTaskTicks = Math.multiplyExact(seconds, 20);
        sync();
        return true;
    }

    public Text hudText() {
        if (mood().tasks.isEmpty()) {
            Text seconds = Text.literal(String.valueOf((int) Math.ceil(nextTaskTicks() / 20.0))).withColor(0xFFDA75);
            return Text.translatable("hud.wyspiaexpress.task.waiting", seconds);
        }
        var names = Text.empty();
        for (PlayerMoodComponent.Task type : PlayerMoodComponent.Task.values()) {
            if (!mood().tasks.containsKey(type)) continue;
            if (!names.getString().isEmpty()) names.append(", ");
            names.append(taskName(type).copy().withColor(0x8FCEFF));
        }
        return Text.translatable("hud.wyspiaexpress.task.current", names);
    }

    public static Text taskName(PlayerMoodComponent.Task type) {
        return Text.translatable("task.wyspiaexpress." + type.name().toLowerCase(java.util.Locale.ROOT));
    }

    @Override public void clientTick() {
        if (clientWaiting && nextTaskTicks > 0 && canTick()) nextTaskTicks--;
    }

    @Override public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(isWaiting());
        buf.writeVarInt(nextTaskTicks());
    }

    @Override public void applySyncPacket(RegistryByteBuf buf) {
        clientWaiting = buf.readBoolean();
        nextTaskTicks = Math.max(0, buf.readVarInt());
    }

    @Override public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putInt("nextTaskTicks", nextTaskTicks);
        if (lastTask != null) tag.putString("lastTask", lastTask.name());
        NbtCompound history = new NbtCompound();
        mood().timesGotten.forEach((type, count) -> history.putInt(type.name(), count));
        tag.put("history", history);
    }

    @Override public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        if (tag.contains("nextTaskTicks", NbtElement.INT_TYPE))
            nextTaskTicks = Math.max(0, tag.getInt("nextTaskTicks"));
        lastTask = null;
        try { lastTask = PlayerMoodComponent.Task.valueOf(tag.getString("lastTask")); }
        catch (IllegalArgumentException ignored) { }
        if (tag.contains("history", NbtElement.COMPOUND_TYPE)) {
            mood().timesGotten.clear();
            NbtCompound history = tag.getCompound("history");
            for (PlayerMoodComponent.Task type : PlayerMoodComponent.Task.values()) {
                if (history.contains(type.name(), NbtElement.INT_TYPE))
                    mood().timesGotten.put(type, Math.max(1, history.getInt(type.name())));
            }
        }
    }
}
