package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.text.Text;

/** Navigation sections; future sections can own their own content/filter state. */
enum GuidebookTab {
    ROLES;

    Text title() { return Text.translatable("gui.wyspiaexpress.guidebook.tab." + name().toLowerCase(java.util.Locale.ROOT)); }
}
