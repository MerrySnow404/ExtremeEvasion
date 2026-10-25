package cc.sighs.extremeevasion;

public final class BulletTimeClock {
    private static final ThreadLocal<Boolean> BYPASS_BULLET_TIME = ThreadLocal.withInitial(() -> false);

    public static boolean isBypassBulletTime() {
        return BYPASS_BULLET_TIME.get();
    }

    public static void setBypassBulletTime(boolean bypassBulletTime) {
        BYPASS_BULLET_TIME.set(bypassBulletTime);
    }
}
