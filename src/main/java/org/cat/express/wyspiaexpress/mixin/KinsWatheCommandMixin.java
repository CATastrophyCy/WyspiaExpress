package org.cat.express.wyspiaexpress.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.BsXinQin.kinswathe.KinsWatheGameSettings;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// this is to fix Kins Wathe removing player effects at the start of the game inappropriately
// it removed feather falling from FEATHER modifier and also the NIGHT_VISION effect from EDGE_LORD in my mod
@Mixin(KinsWatheGameSettings.class)
public abstract class KinsWatheCommandMixin {
    @Inject(method = "setCommands", at = @At("HEAD"), cancellable = true)
    private static void setCommands(@NotNull MinecraftServer server, @NotNull CallbackInfo ci){
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "kill @e[type=wathe:player_body]");
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "kill @e[type=item]");
        if (FabricLoader.getInstance().isModLoaded("noellesroles")) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "kill @e[type=noellesroles:cube]");
        }
        ci.cancel();
    }
}
