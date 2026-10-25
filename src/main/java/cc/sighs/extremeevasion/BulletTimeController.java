package cc.sighs.extremeevasion;

import net.minecraft.Util;

public final class BulletTimeController {
    private static final double NORMAL_SPEED = 1.0D;

    private static long lastRealMillis = monotonicMillis();
    private static double virtualMillis = lastRealMillis;
    private static double speed = NORMAL_SPEED;
    private static long bulletTimeEndRealMillis;
    private static boolean initialized;

    public static synchronized boolean shouldOverrideMillis() {
        return initialized;
    }

    public static synchronized long getMillis() {
        advance(monotonicMillis());
        return (long) virtualMillis;
    }

    public static synchronized long getNanos() {
        return getMillis() * 1_000_000L;
    }

    public static synchronized boolean trigger() {
        if (!Config.enableBulletTime || Config.bulletTimeDurationMillis <= 0) {
            return false;
        }

        if (Config.bulletTimeExceptPlayer
                && TimeScaleLibCompat.applyExceptPlayer(Config.bulletTimeSpeed, Config.bulletTimeDurationMillis)) {
            return true;
        }

        triggerGlobalClock();
        return false;
    }

    /**
     * Starts the legacy global clock even when the local mode config prefers TimeScaleLib.
     */
    public static synchronized void triggerGlobal() {
        if (!Config.enableBulletTime || Config.bulletTimeDurationMillis <= 0) {
            return;
        }
        triggerGlobalClock();
    }

    private static void triggerGlobalClock() {
        long now = monotonicMillis();
        if (!initialized) {
            initialized = true;
            virtualMillis = now;
            lastRealMillis = now;
        } else {
            advance(now);
        }
        speed = Config.bulletTimeSpeed;
        bulletTimeEndRealMillis = now + Config.bulletTimeDurationMillis;
    }

    private static void advance(long now) {
        if (now <= lastRealMillis) {
            return;
        }

        if (speed != NORMAL_SPEED && bulletTimeEndRealMillis > lastRealMillis && now > bulletTimeEndRealMillis) {
            virtualMillis += (bulletTimeEndRealMillis - lastRealMillis) * speed;
            virtualMillis += now - bulletTimeEndRealMillis;
            lastRealMillis = now;
            speed = NORMAL_SPEED;
            bulletTimeEndRealMillis = 0L;
            return;
        }

        virtualMillis += (now - lastRealMillis) * speed;
        lastRealMillis = now;

        if (speed != NORMAL_SPEED && now >= bulletTimeEndRealMillis) {
            speed = NORMAL_SPEED;
            bulletTimeEndRealMillis = 0L;
        }
    }

    public static long monotonicMillis() {
        try {
            BulletTimeClock.setBypassBulletTime(true);
            return Util.getNanos() / 1_000_000L;
        } finally {
            BulletTimeClock.setBypassBulletTime(false);
        }
    }
}
