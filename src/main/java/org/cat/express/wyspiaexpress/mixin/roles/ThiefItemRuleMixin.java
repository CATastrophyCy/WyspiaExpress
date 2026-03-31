package org.cat.express.wyspiaexpress.mixin.roles;

import dev.doctor4t.wathe.Wathe;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWathe;
import org.cat.express.wyspiaexpress.WyspiaExpress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.role.thief.ThiefItemRules;

import java.util.List;

@Mixin(value = ThiefItemRules.class)
public abstract class ThiefItemRuleMixin {

    @Final @Shadow private static List<Identifier> CAN_TAKE;

    @Inject(method = "initCanTake", at = @At("RETURN"))
    private static void wyspiaexpress$modifyCanTakeItems(CallbackInfoReturnable<List<Identifier>> cir) {
        List<Identifier> list = cir.getReturnValue();

        list.add(Identifier.of(KinsWathe.MOD_ID, "pan"));
        list.add(Identifier.of(KinsWathe.MOD_ID, "poison_injector"));
        list.add(Identifier.of(KinsWathe.MOD_ID, "blowgun"));
        list.add(Identifier.of(KinsWathe.MOD_ID, "pill"));
        list.add(Identifier.of(KinsWathe.MOD_ID, "capture_device"));
        list.add(Identifier.of(KinsWathe.MOD_ID, "hunting_knife"));
        list.add(Identifier.of(KinsWathe.MOD_ID, "sulfuric_acid_barrel"));
        list.add(Identifier.of(KinsWathe.MOD_ID, "wrench"));
        list.add(Identifier.of(WyspiaExpress.MOD_ID, "fake_revolver"));

        list.remove(Identifier.of(Wathe.MOD_ID, "note"));
        list.remove(Identifier.of(Wathe.MOD_ID, "firecracker"));
        list.remove(Identifier.of(Wathe.MOD_ID, "lockpick"));
    }
}