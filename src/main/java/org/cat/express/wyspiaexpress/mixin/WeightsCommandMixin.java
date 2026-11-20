package org.cat.express.wyspiaexpress.mixin;

import dev.doctor4t.wathe.command.GameSettingsCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameSettingsCommand.class)
public class WeightsCommandMixin {
    @ModifyArg(method = "lambda$register$4", at = @At(value = "INVOKE",
            target = "Lorg/ladysnake/cca/api/v3/component/ComponentKey;get(Ljava/lang/Object;)Lorg/ladysnake/cca/api/v3/component/Component;",
            ordinal = 1), remap = false)
    private static Object wyspiaexpress$useScoreboard(Object source) {
        return ((ServerCommandSource) source).getServer().getScoreboard();
    }
}
