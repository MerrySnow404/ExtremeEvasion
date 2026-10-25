package cc.sighs.extremeevasion.client;

import cc.sighs.extremeevasion.Config;
import cc.sighs.extremeevasion.ExtremeEvasion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class BulletTimeSoundPlayer {
    private static final SoundEvent TRIGGER_SOUND = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(ExtremeEvasion.MODID, "bullet_time_trigger")
    );

    public static void playTrigger() {
        if (!Config.enableBulletTimeTriggerSound || Config.bulletTimeTriggerSoundVolume <= 0.0F) {
            return;
        }

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                TRIGGER_SOUND,
                Config.bulletTimeTriggerSoundPitch,
                Config.bulletTimeTriggerSoundVolume
        ));
    }
}
