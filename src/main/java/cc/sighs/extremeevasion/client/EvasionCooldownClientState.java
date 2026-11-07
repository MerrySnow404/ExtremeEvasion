package cc.sighs.extremeevasion.client;

import cc.sighs.extremeevasion.ExtremeEvasion;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExtremeEvasion.MODID)
public final class EvasionCooldownClientState {
    private static int totalTicks;
    private static int remainingTicks;
    private static boolean active;

    public static void start(int ticks) {
        totalTicks = Math.max(0, ticks);
        remainingTicks = totalTicks;
        active = totalTicks > 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks > 0 && !Minecraft.getInstance().isPaused()) {
            remainingTicks--;
        }
    }

    public static float getProgress() {
        return totalTicks <= 0 ? 0.0F : (float) remainingTicks / totalTicks;
    }

    public static boolean isActive() {
        return active;
    }
}
