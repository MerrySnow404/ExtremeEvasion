package cc.sighs.extremeevasion.client;

import cc.sighs.extremeevasion.Config;
import cc.sighs.extremeevasion.ExtremeEvasion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.io.IOException;

public final class BulletTimeShaderManager {
    private static final String EFFECT_NAME = ExtremeEvasion.MODID + ":bullet_time";
    private static final Minecraft MC = Minecraft.getInstance();

    private static PostChain postChain;
    private static boolean needsResize = true;
    private static int lastWindowWidth = -1;
    private static int lastWindowHeight = -1;

    public static void ensureLoaded() {
        if (postChain != null) {
            return;
        }

        try {
            postChain = new PostChain(
                    MC.getTextureManager(),
                    MC.getResourceManager(),
                    MC.getMainRenderTarget(),
                    ResourceLocation.fromNamespaceAndPath(ExtremeEvasion.MODID, "shaders/post/bullet_time.json")
            );
            needsResize = true;
        } catch (IOException exception) {
            postChain = null;
        }
    }

    public static void clean() {
        if (postChain != null) {
            postChain.close();
            postChain = null;
        }
    }

    public static void setIntensity(float intensity) {
        if (!Config.enableBulletTimeScreenShader || intensity <= 0.0F) {
            clean();
            return;
        }

        ensureLoaded();
        if (postChain == null) {
            return;
        }

        postChain.setUniform("IntensityAmount", intensity);
    }

    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> MC.execute(() -> {
            clean();
            if (BulletTimeClientState.isActive()) {
                ensureLoaded();
            }
        }));
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        float intensity = BulletTimeClientState.getIntensity();
        setIntensity(intensity);
        if (postChain == null) {
            return;
        }

        checkAndHandleResize();
        postChain.process(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        MC.getMainRenderTarget().bindWrite(false);
        CounterAttackGoldReplay.renderAndClear();
    }

    private static void checkAndHandleResize() {
        int width = MC.getWindow().getWidth();
        int height = MC.getWindow().getHeight();
        if (width != lastWindowWidth || height != lastWindowHeight) {
            lastWindowWidth = width;
            lastWindowHeight = height;
            needsResize = true;
        }

        if (needsResize) {
            postChain.resize(width, height);
            needsResize = false;
        }
    }
}
