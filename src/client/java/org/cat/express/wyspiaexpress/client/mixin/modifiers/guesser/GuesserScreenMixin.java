package org.cat.express.wyspiaexpress.client.mixin.modifiers.guesser;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.client.roles.bodymaker.BodymakerDeathReasonWidget;
import org.BsXinQin.kinswathe.client.roles.bodymaker.BodymakerPlayerWidget;
import org.BsXinQin.kinswathe.client.roles.bodymaker.BodymakerRoleWidget;
import org.BsXinQin.kinswathe.client.roles.judge.JudgePlayerWidget;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.client.ui.MorphlingPlayerWidget;
import org.agmas.noellesroles.client.ui.SwapperPlayerWidget;
import org.agmas.noellesroles.client.ui.guesser.GuesserPlayerWidget;
import org.agmas.noellesroles.client.ui.guesser.GuesserRoleWidget;
import org.cat.express.wyspiaexpress.WyspiaExpressRoles;
import org.cat.express.wyspiaexpress.client.ui.GuesserWidgets;
import org.cat.express.wyspiaexpress.client.ui.InventoryAbilityPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class GuesserScreenMixin extends Screen {
    @Unique private InventoryAbilityPanel wyspiaexpress$abilityPanel;
    protected GuesserScreenMixin() { super(Text.empty()); }

    // After every mod has added its controls, including Bodymaker's subsequent stages.
    @WrapMethod(method = "init")
    private void wyspiaexpress$layoutInventoryAbilities(Operation<Void> original) {
        String selected = wyspiaexpress$abilityPanel == null ? null : wyspiaexpress$abilityPanel.selectedId();
        original.call();
        var screen = (LimitedInventoryScreen) (Object) this;
        var groups = new LinkedHashMap<String, List<ClickableWidget>>();
        for (Element child : List.copyOf(children())) {
            if (child instanceof GuesserPlayerWidget || child instanceof GuesserRoleWidget) { remove(child); continue; }
            String id = child instanceof MorphlingPlayerWidget ? "morphling"
                    : child instanceof SwapperPlayerWidget ? "swapper"
                    : child instanceof JudgePlayerWidget ? "judge"
                    : child instanceof BodymakerPlayerWidget || child instanceof BodymakerDeathReasonWidget || child instanceof BodymakerRoleWidget ? "bodymaker" : null;
            if (id != null && child instanceof ClickableWidget widget) {
                groups.computeIfAbsent(id, key -> new ArrayList<>()).add(widget); remove(child);
            }
        }
        var modifier = WorldModifierComponent.KEY.get(screen.player.getWorld());
        GuesserWidgets guesser = modifier.isModifier(screen.player, WyspiaExpressRoles.GUESSER) ? new GuesserWidgets(screen) : null;
        if (guesser != null && guesser.group().widgets.isEmpty()) guesser = null;
        if (groups.isEmpty() && guesser == null) { wyspiaexpress$abilityPanel = null; return; }
        var panels = new ArrayList<InventoryAbilityPanel.Group>();
        groups.forEach((id, widgets) -> panels.add(new InventoryAbilityPanel.Group(id,
                Text.translatable("gui.wyspiaexpress.inventory." + id), widgets, true)));
        if (guesser != null) panels.add(guesser.group());
        wyspiaexpress$abilityPanel = new InventoryAbilityPanel(screen, panels, selected);
        addDrawableChild(wyspiaexpress$abilityPanel);
        if (guesser != null) guesser.attach(wyspiaexpress$abilityPanel);
        wyspiaexpress$abilityPanel.captureInitialFocus();
    }
}
