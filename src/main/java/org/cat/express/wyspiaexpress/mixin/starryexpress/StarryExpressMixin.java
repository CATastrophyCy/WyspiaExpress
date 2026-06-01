package org.cat.express.wyspiaexpress.mixin.starryexpress;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import org.aussiebox.starexpress.ModSounds;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.StarryExpressConstants;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.AbilityComponent;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.aussiebox.starexpress.packet.AbilityC2SPacket;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static org.aussiebox.starexpress.StarryExpress.STARSTRUCK_SPARKLE;

@Mixin(StarryExpress.class)
public class StarryExpressMixin {

    /**
     * @author CAT
     * @reason Improve how tape removal is checked
     */
    @Overwrite
    public void registerEvents() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (! (entity instanceof PlayerEntity victim)) return ActionResult.PASS;

            if (StarryExpress.CONFIG.muzzlerConfig.tapeTearCheckCount() == 0 || !player.getMainHandStack().isEmpty()) return ActionResult.PASS;

            SilenceComponent victimSilence = SilenceComponent.KEY.get(victim);
            if (!victimSilence.isSilenced() || victimSilence.getSilencedTicks() <= WyspiaExpress.ITEMS_CONFIG.itemConfig.tapeConfig.removeCountdown()
                    || SilenceComponent.KEY.get(player).isSilenced()) return ActionResult.PASS;

            victimSilence.setTearChecks(victimSilence.getTearChecks() + 1);
            victimSilence.sync();
            victim.getWorld().playSound(victim, victim.getX(), victim.getY(), victim.getZ(), ModSounds.ITEM_TAPE_APPLY, SoundCategory.PLAYERS, 1.0F, 2.0F);

            PlayerMoodComponent victimMood = PlayerMoodComponent.KEY.get(victim);
            victimMood.setMood(victimMood.getMood() - StarryExpress.CONFIG.muzzlerConfig.tapeTearMoodChange());
            if (victimMood.isLowerThanDepressed() && StarryExpress.CONFIG.muzzlerConfig.killIfCheckedAtZero()) {
                GameFunctions.killPlayer(victim, true, level.getPlayerByUuid(victimSilence.getSilencer()), StarryExpressConstants.SILENCED_TAPE_REMOVED_DEATH_REASON);
            }
            if (victimSilence.getTearChecks() >= StarryExpress.CONFIG.muzzlerConfig.tapeTearCheckCount()) {
                victimSilence.reset();
            }
            return ActionResult.SUCCESS;
        });
    }

    /**
     * @author You
     * @reason Made starstruck ability sound and effect configurable
     */
    @Overwrite
    public void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(AbilityC2SPacket.TYPE, (payload, context) -> {
            AbilityComponent abilityComponent = AbilityComponent.KEY.get(context.player());
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(context.player().getWorld());
            PlayerEntity player = context.player();

            if (GameFunctions.isPlayerAliveAndSurvival(player)
                && gameWorldComponent.isRole(context.player(), StarryExpressRoles.STARSTRUCK)
                && abilityComponent.cooldown <= 0
            ) {
                abilityComponent.setCooldown(StarryExpress.CONFIG.starstruckConfig.abilityCooldown() * 20);
                StarstruckComponent.KEY.get(context.player()).setTicks(StarryExpress.CONFIG.starstruckConfig.abilityDuration() * 20);
                if(WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.starstruckConfig.enableAbilitySound()){
                    player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
                else{
                    player.playSoundToPlayer(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }

                if(WyspiaExpress.ROLES_CONFIG.roleConfig.starryExpress.starstruckConfig.enableAbilitySound()){
                    ServerWorld level = context.player().getServerWorld();
                    level.spawnParticles(STARSTRUCK_SPARKLE, player.getX(), player.getY(), player.getZ(),
                            75, 0.5F, 1.5F, 0.5F, 0.0F);
                }

            }
        });
    }
}