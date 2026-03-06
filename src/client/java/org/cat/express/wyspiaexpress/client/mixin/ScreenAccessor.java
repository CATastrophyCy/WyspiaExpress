package org.cat.express.wyspiaexpress.client.mixin;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// A wrapper trying to get access to private variables for ShopMixin to clear widgets
@Mixin(Screen.class)
public interface ScreenAccessor {

    @Accessor("drawables")
    List<Drawable> getDrawables();

    @Accessor("children")
    List<Element> getChildren();

    @Accessor("selectables")
    List<Selectable> getSelectables();
}
