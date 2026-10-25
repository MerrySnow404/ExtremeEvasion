package cc.sighs.extremeevasion;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Optional bridge for TimeScaleLib; this class remains usable when the library is absent.
 */
public final class TimeScaleLibCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MOD_ID = "timescalelib";
    private static final String HANDLER_CLASS = "com.xm666.timescalelib.handler.TimeScaleHandler";
    private static Method applyScaleMethod;
    private static boolean lookupFailed;

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * Applies a scale to the world while leaving players unaffected. The library's duration is in ticks,
     * whereas ExtremeEvasion exposes a real-time duration in milliseconds.
     */
    public static boolean applyExceptPlayer(double scale, int durationMillis) {
        if (!isLoaded()) {
            if (!lookupFailed) {
                lookupFailed = true;
                LOGGER.warn("bulletTimeExceptPlayer is enabled, but TimeScaleLib is not installed; using global bullet time.");
            }
            return false;
        }

        try {
            if (applyScaleMethod == null) {
                Class<?> handler = Class.forName(HANDLER_CLASS, true, TimeScaleLibCompat.class.getClassLoader());
                applyScaleMethod = handler.getMethod("applyScale", float.class, int.class, int.class);
            }

            int durationTicks = Math.max(1, (durationMillis + 49) / 50);
            // A zero transition matches the existing global clock's immediate return to normal speed.
            applyScaleMethod.invoke(null, (float) scale, durationTicks, 0);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | LinkageError exception) {
            if (!lookupFailed) {
                lookupFailed = true;
                LOGGER.warn("TimeScaleLib was detected but its API could not be used; falling back to global bullet time.", exception);
            }
            return false;
        }
    }
}
