package cc.sighs.extremeevasion.client;

import cc.sighs.extremeevasion.Config;

public final class BulletTimeClientState {
    private static long startNanos;
    private static long durationNanos;

    public static void start(long durationMillis) {
        startNanos = System.nanoTime();
        durationNanos = Math.max(0L, durationMillis) * 1_000_000L;
    }

    public static float getIntensity() {
        if (durationNanos <= 0L) {
            return 0.0F;
        }

        long elapsedNanos = System.nanoTime() - startNanos;
        if (elapsedNanos < 0L || elapsedNanos >= durationNanos) {
            return 0.0F;
        }

        double elapsedMillis = elapsedNanos / 1_000_000.0D;
        double durationMillis = durationNanos / 1_000_000.0D;
        double fadeMillis = Math.min(Config.bulletTimeVisualFadeMillis, durationMillis * 0.5D);
        double fadeIn = fadeMillis <= 0.0D ? 1.0D : elapsedMillis / fadeMillis;
        double fadeOut = fadeMillis <= 0.0D ? 1.0D : (durationMillis - elapsedMillis) / fadeMillis;
        double linear = Math.clamp(Math.min(fadeIn, fadeOut), 0.0D, 1.0D);
        return (float) (linear * linear * (3.0D - 2.0D * linear));
    }

    public static boolean isActive() {
        return getIntensity() > 0.0F;
    }
}
