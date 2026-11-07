package cc.sighs.extremeevasion.client;

import net.minecraft.client.Minecraft;

public final class EvasionCooldownClientState {
    private static int totalTicks;
    private static int remainingTicks;
    private static boolean active;

    private EvasionCooldownClientState() {
    }

    public static void start(int ticks) {
        totalTicks = Math.max(0, ticks);
        remainingTicks = totalTicks;
        active = totalTicks > 0;
    }

    public static void tick() {
        if (remainingTicks > 0 && !Minecraft.getInstance().isPaused()) {
            remainingTicks--;
        }
    }

    public static float getProgress() {
        if (totalTicks <= 0) {
            return 0.0F;
        }
        return (float) remainingTicks / totalTicks;
    }

    public static boolean isActive() {
        return active;
    }
}
