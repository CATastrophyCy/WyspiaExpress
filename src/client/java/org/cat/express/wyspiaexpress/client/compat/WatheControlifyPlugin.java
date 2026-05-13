package org.cat.express.wyspiaexpress.client.compat;

import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.input.ButtonInput;
import dev.isxander.controlify.controller.input.GamepadInputs;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;

public class WatheControlifyPlugin implements ControlifyEntrypoint {
    private static InputBindingSupplier limitedInventoryBinding;
    private ControlifyApi controlifyApi;

    @Override
    public void onControlifyPreInit(PreInitContext ctx) {
        // Register your binding using the internal Bind API
        limitedInventoryBinding = ControlifyBindApi.get().registerBinding(builder -> builder
                .id(Identifier.of(WyspiaExpress.MOD_ID, "open_limited_inventory"))
                .name(Text.translatable("Open Wathe inventory"))
                .description(Text.translatable("Open the working ingame Wathe inventory"))
                .category(Text.translatable("controlify.binding.category.wathe"))
                .defaultInput(new ButtonInput(GamepadInputs.NORTH_BUTTON))
                .allowedContexts(BindContext.IN_GAME)
        );

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    @Override
    public void onControlifyInit(InitContext ctx) {
        // Unused, but required by the interface
    }

    @Override
    public void onControllersDiscovered(ControlifyApi api) {
        // Capture the API instance so we can poll the current controller later
        this.controlifyApi = api;
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null || this.controlifyApi == null) {
            return;
        }
        // Match Wathe Mixin Logic
        if (WatheClient.gameComponent.getFade() > 0) {
            return;
        }
        if (!WatheClient.isPlayerAliveAndInSurvival()) {
            return;
        }
        // Get the active controller the player is currently using
        var currentControllerOpt = this.controlifyApi.getCurrentController();
        if (currentControllerOpt.isEmpty()) {
            return;
        }
        var currentController = currentControllerOpt.get();
        var bindingState = limitedInventoryBinding.on(currentController);

        if (bindingState.justPressed()) {
            client.setScreen(new LimitedInventoryScreen(client.player));
        }
    }
}