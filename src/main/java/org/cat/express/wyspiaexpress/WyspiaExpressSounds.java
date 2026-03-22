package org.cat.express.wyspiaexpress;

import dev.doctor4t.ratatouille.util.registrar.SoundEventRegistrar;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.sound.SoundEvent;

public interface WyspiaExpressSounds {
    static SoundEventRegistrar registrar = new SoundEventRegistrar(WyspiaExpress.MOD_ID);

    // Items
    SoundEvent ITEM_MEGAPHONE_REPORT = registrar.create("item.megaphone.report");


    static void init() {
        registrar.registerEntries();
    }
}
