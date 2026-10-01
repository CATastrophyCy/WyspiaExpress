package org.cat.express.wyspiaexpress.mixin.kinswathe;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.cat.express.wyspiaexpress.guidebook.KinsGuideConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ConfigWorldComponent.class)
public abstract class KinsGuideConfigMixin implements KinsGuideConfig {
    @Shadow @Final private World world;
    @Shadow public abstract void sync();
    @Unique private Map<String, Double> wyspiaexpress$values = Map.of();

    @Override public Map<String, Double> wyspiaexpress$guideValues() {
        return world.isClient ? wyspiaexpress$values : KinsGuideConfig.serverValues();
    }

    @Inject(method = "writeToNbt", at = @At("TAIL"))
    private void wyspiaexpress$write(NbtCompound tag, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        NbtCompound values = new NbtCompound();
        wyspiaexpress$guideValues().forEach(values::putDouble);
        tag.put("WyspiaGuideValues", values);
    }

    @Inject(method = "readFromNbt", at = @At("TAIL"))
    private void wyspiaexpress$read(NbtCompound tag, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        var values = tag.getCompound("WyspiaGuideValues");
        Map<String, Double> loaded = new HashMap<>();
        for (String key : values.getKeys()) loaded.put(key, values.getDouble(key));
        wyspiaexpress$values = Map.copyOf(loaded);
    }

    @Inject(method = "serverTick", at = @At("TAIL"), remap = false)
    private void wyspiaexpress$refresh(CallbackInfo ci) {
        if (world.getTime() % 20 != 0) return;
        var values = KinsGuideConfig.serverValues();
        if (!values.equals(wyspiaexpress$values)) {
            wyspiaexpress$values = values;
            sync();
        }
    }
}
