package org.cat.express.wyspiaexpress.mixin.kinswathe;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

@Mixin(DreamerComponent.class)
public abstract class DreamerComponentMixin {

    @Final @Shadow(remap = false) @NotNull
    private PlayerEntity player;

    @Shadow(remap = false)
    public UUID dreamerUUID;

    @Inject(method = "teleportToDreamer", at = @At("HEAD"), cancellable = true, remap = false)
    private void wyspiaexpress$teleportToDreamer(CallbackInfo ci) {
        if (this.dreamerUUID == null || this.player.getWorld().isClient) {
            ci.cancel();
            return;
        }

        PlayerEntity dreamer = this.player.getWorld().getPlayerByUuid(this.dreamerUUID);
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());

        PlayerPsychoComponent playerPsycho = PlayerPsychoComponent.KEY.get(this.player);
        if (GameFunctions.isPlayerAliveAndSurvival(dreamer) && GameFunctions.isPlayerAliveAndSurvival(this.player)) {

            if (gameWorld.isRole(dreamer, KinsWatheRoles.DREAMER)) {
                DreamerKillerComponent playerDreamer = DreamerKillerComponent.KEY.get(dreamer);
                if (!playerDreamer.hasBecomeKiller) {
                    playerDreamer.dreamerCounts += 1;
                    playerDreamer.sync();
                }
            }

            if (this.player.getWorld() instanceof @NotNull ServerWorld serverWorld) {

                if(WyspiaExpress.ROLES_CONFIG.roleConfig.kinsWatheRoles.dreamerConfig.enableDreamImprintTeleport()) {
                    serverWorld.spawnParticles(ParticleTypes.PORTAL, this.player.getX(), this.player.getY(), this.player.getZ(), 75, 0.5, 1.5, 0.5, 0.1);
                    serverWorld.playSound(null, this.player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    if (playerPsycho.psychoTicks <= 0) {
                        this.player.teleport(serverWorld, dreamer.getX(), dreamer.getY(), dreamer.getZ(), Set.of(), dreamer.getYaw(), dreamer.getPitch());
                    }
                }
            }
            dreamer.playSoundToPlayer(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        ci.cancel();
    }
}