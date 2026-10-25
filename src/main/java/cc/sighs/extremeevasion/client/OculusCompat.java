package cc.sighs.extremeevasion.client;

import java.lang.reflect.Method;

public final class OculusCompat {
    private static Method getInstanceMethod;
    private static Method isShaderPackInUseMethod;
    private static Object handRendererInstance;
    private static Method getHandBufferSourceMethod;
    private static Method readyUpHandBufferMethod;
    private static Method endBatchHandBufferMethod;
    private static boolean initialized;

    public static boolean isShaderPackInUse() {
        tryInitialize();
        if (getInstanceMethod == null || isShaderPackInUseMethod == null) {
            return false;
        }

        try {
            Object irisApi = getInstanceMethod.invoke(null);
            return Boolean.TRUE.equals(isShaderPackInUseMethod.invoke(irisApi));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    public static boolean flushHandBuffer() {
        tryInitialize();
        if (handRendererInstance == null || getHandBufferSourceMethod == null
                || readyUpHandBufferMethod == null || endBatchHandBufferMethod == null) {
            return false;
        }

        try {
            Object handBufferSource = getHandBufferSourceMethod.invoke(handRendererInstance);
            readyUpHandBufferMethod.invoke(handBufferSource);
            endBatchHandBufferMethod.invoke(handBufferSource);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    private static void tryInitialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            getInstanceMethod = irisApiClass.getMethod("getInstance");
            isShaderPackInUseMethod = irisApiClass.getMethod("isShaderPackInUse");
        } catch (ReflectiveOperationException | RuntimeException exception) {
            getInstanceMethod = null;
            isShaderPackInUseMethod = null;
        }

        try {
            Class<?> handRendererClass = Class.forName("net.irisshaders.iris.pathways.HandRenderer");
            handRendererInstance = handRendererClass.getField("INSTANCE").get(null);
            getHandBufferSourceMethod = handRendererClass.getMethod("getBufferSource");

            Object handBufferSource = getHandBufferSourceMethod.invoke(handRendererInstance);
            Class<?> handBufferSourceClass = handBufferSource.getClass();
            readyUpHandBufferMethod = handBufferSourceClass.getMethod("readyUp");
            endBatchHandBufferMethod = findEndBatchMethod(handBufferSourceClass);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            handRendererInstance = null;
            getHandBufferSourceMethod = null;
            readyUpHandBufferMethod = null;
            endBatchHandBufferMethod = null;
        }
    }

    private static Method findEndBatchMethod(Class<?> handBufferSourceClass) throws NoSuchMethodException {
        try {
            return handBufferSourceClass.getMethod("endBatch");
        } catch (NoSuchMethodException exception) {
            return handBufferSourceClass.getMethod("m_109911_");
        }
    }
}
