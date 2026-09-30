package org.cat.express.wyspiaexpress.mixin.generic;

import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.cat.express.wyspiaexpress.components.BlackoutDuration;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WorldBlackoutComponent.class)
public abstract class BlackoutDurationMixin implements AutoSyncedComponent, ClientTickingComponent, BlackoutDuration {
    @Shadow @Final private World world;
    @Shadow private int ticks;
    @Shadow @Final private List<WorldBlackoutComponent.BlackoutDetails> blackouts;

    @Unique private int wyspiaexpress$totalTicks;
    @Unique private int wyspiaexpress$lastSyncedTicks;
    @Unique private int wyspiaexpress$minimumTicks;

    @Inject(method = "triggerBlackout", at = @At("RETURN"))
    private void wyspiaexpress$startDuration(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            this.wyspiaexpress$totalTicks = this.ticks;
            this.wyspiaexpress$minimumTicks = this.blackouts.stream()
                    .mapToInt(detail -> ((BlackoutDetailsAccessor) detail).wyspiaexpress$getTime())
                    .min().orElse(0);
            this.wyspiaexpress$syncDuration();
        }
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private void wyspiaexpress$updateDuration(CallbackInfo ci) {
        if (this.wyspiaexpress$minimumTicks > 0) this.wyspiaexpress$minimumTicks--;
        // Clients count down between corrections; send immediately when the blackout ends.
        if (this.ticks != this.wyspiaexpress$lastSyncedTicks && (this.ticks == 0 || this.ticks % 20 == 0)) {
            this.wyspiaexpress$syncDuration();
        }
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void wyspiaexpress$resetDuration(CallbackInfo ci) {
        // Power restoration clears the lights before their original timers expire.
        this.ticks = 0;
        this.wyspiaexpress$totalTicks = 0;
        this.wyspiaexpress$minimumTicks = 0;
        this.wyspiaexpress$syncDuration();
    }

    @Inject(method = "writeToNbt", at = @At("TAIL"))
    private void wyspiaexpress$saveDuration(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        tag.putInt("wyspiaexpress:blackout_duration", this.wyspiaexpress$totalTicks);
        tag.putInt("wyspiaexpress:blackout_minimum_remaining", this.wyspiaexpress$minimumTicks);
    }

    @Inject(method = "readFromNbt", at = @At("TAIL"))
    private void wyspiaexpress$restoreDuration(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        // Wathe saves each light's timer, but does not restore its overall remaining time.
        this.ticks = 0;
        int minimum = Integer.MAX_VALUE;
        var blackouts = tag.getList("blackouts", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < blackouts.size(); i++) {
            int remaining = blackouts.getCompound(i).getInt("time");
            this.ticks = Math.max(this.ticks, remaining);
            minimum = Math.min(minimum, remaining);
        }
        this.wyspiaexpress$totalTicks = Math.max(this.ticks, tag.getInt("wyspiaexpress:blackout_duration"));
        this.wyspiaexpress$minimumTicks = Math.max(0, Math.min(this.ticks,
                tag.contains("wyspiaexpress:blackout_minimum_remaining", NbtElement.INT_TYPE)
                        ? tag.getInt("wyspiaexpress:blackout_minimum_remaining")
                        : (minimum == Integer.MAX_VALUE ? 0 : minimum)));
    }

    @Unique
    private void wyspiaexpress$syncDuration() {
        if (!this.world.isClient) {
            this.wyspiaexpress$lastSyncedTicks = this.ticks;
            WorldBlackoutComponent.KEY.sync(this.world);
        }
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        // HUD updates need only the timers, not the potentially large per-light state.
        buf.writeInt(this.ticks);
        buf.writeInt(this.wyspiaexpress$totalTicks);
        buf.writeInt(this.wyspiaexpress$minimumTicks);
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        this.ticks = Math.max(0, buf.readInt());
        this.wyspiaexpress$totalTicks = Math.max(this.ticks, buf.readInt());
        this.wyspiaexpress$minimumTicks = Math.max(0, Math.min(this.ticks, buf.readInt()));
    }

    @Override
    public void clientTick() {
        if (this.ticks > 0) this.ticks--;
        if (this.wyspiaexpress$minimumTicks > 0) this.wyspiaexpress$minimumTicks--;
    }

    @Override
    public int getBlackoutRemainingTicks() {
        return this.ticks;
    }

    @Override
    public int getBlackoutDurationTicks() {
        return this.wyspiaexpress$totalTicks;
    }

    @Override
    public int getBlackoutMinimumRemainingTicks() {
        return this.wyspiaexpress$minimumTicks;
    }
}
