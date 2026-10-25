package cc.sighs.extremeevasion.client;

public final class CounterAttackClientState {
    private static boolean active;
    private static long transitionStartNanos;
    private static float transitionStartIntensity;
    private static float targetIntensity;

    public static boolean isActive() {
        return active;
    }

    public static boolean shouldRender() {
        return getVisualIntensity() > 0.0F;
    }

    public static float getVisualIntensity() {
        float elapsed = (System.nanoTime() - transitionStartNanos) / (180.0F * 1_000_000.0F);
        if (elapsed >= 1.0F) {
            return targetIntensity;
        }

        float progress = Math.max(0.0F, elapsed);
        float smoothed = progress * progress * (3.0F - 2.0F * progress);
        return transitionStartIntensity + (targetIntensity - transitionStartIntensity) * smoothed;
    }

    public static void setActive(boolean active) {
        if (CounterAttackClientState.active == active && targetIntensity == (active ? 1.0F : 0.0F)) {
            return;
        }

        transitionStartIntensity = getVisualIntensity();
        transitionStartNanos = System.nanoTime();
        targetIntensity = active ? 1.0F : 0.0F;
        CounterAttackClientState.active = active;
    }
}
