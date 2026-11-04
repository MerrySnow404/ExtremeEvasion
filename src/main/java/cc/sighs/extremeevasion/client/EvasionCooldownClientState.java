package cc.sighs.extremeevasion.client;

public final class EvasionCooldownClientState {
    private static int totalTicks;
    private static int remainingTicks;

    private EvasionCooldownClientState() {
    }

    public static void start(int ticks) {
        totalTicks = Math.max(0, ticks);
        remainingTicks = totalTicks;
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static float getProgress() {
        if (totalTicks <= 0) {
            return 0.0F;
        }
        return (float) remainingTicks / totalTicks;
    }
}
