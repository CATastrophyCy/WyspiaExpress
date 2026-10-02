package org.cat.express.wyspiaexpress.client.guidebook;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;

/** Shared gate for native buttons and custom guidebook click targets. */
final class GuidebookSounds {
    private GuidebookSounds() {}

    static void playClick(SoundManager soundManager) {
        if (GuidebookPreferences.get().clickSounds) {
            soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
