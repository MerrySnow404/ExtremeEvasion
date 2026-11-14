package cc.sighs.extremeevasion.client;

import cc.sighs.extremeevasion.Config;
import cc.sighs.extremeevasion.ExtremeEvasion;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = ExtremeEvasion.MODID, value = Dist.CLIENT)
public final class EvasionCooldownHud {
    private static final int SEGMENTS = 64;
    private static final int BASE_SIZE = 32;
    private static final ResourceLocation OUTER_RING = ResourceLocation.fromNamespaceAndPath(
            ExtremeEvasion.MODID,
            "textures/gui/outer_ring.png"
    );
    private static final ResourceLocation INNER_RING = ResourceLocation.fromNamespaceAndPath(
            ExtremeEvasion.MODID,
            "textures/gui/inner_ring.png"
    );

    private EvasionCooldownHud() {
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        EvasionCooldownClientState.tick();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if (!Config.enableExtremeEvasionCooldownGui) {
            return;
        }
        float progress = EvasionCooldownClientState.getProgress();
        if (!EvasionCooldownClientState.isActive()) {
            return;
        }

        int size = Math.max(1, (int) Math.round(BASE_SIZE * Config.extremeEvasionCooldownGuiScale));
        int left = (int) Math.round(event.getGuiGraphics().guiWidth() * Config.extremeEvasionCooldownGuiRatioX)
                - size + Config.extremeEvasionCooldownGuiOffsetX;
        int top = (int) Math.round(event.getGuiGraphics().guiHeight() * Config.extremeEvasionCooldownGuiRatioY)
                - size + Config.extremeEvasionCooldownGuiOffsetY;
        event.getGuiGraphics().blit(
                OUTER_RING,
                left,
                top,
                0,
                0,
                size,
                size,
                size,
                size
        );
        if (progress > 0.0F) {
            drawProgressRing(event, left, top, size, Math.round(SEGMENTS * progress));
        }
    }

    private static void drawProgressRing(RenderGuiEvent.Post event, int left, int top, int size, int segments) {
        if (segments <= 0) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, INNER_RING);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4f pose = event.getGuiGraphics().pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX
        );
        for (int segment = 0; segment < segments; segment++) {
            double startAngle = -Math.PI / 2.0 + Math.PI * 2.0 * segment / SEGMENTS;
            double endAngle = -Math.PI / 2.0 + Math.PI * 2.0 * (segment + 1) / SEGMENTS;
            builder.addVertex(pose, left + size * 0.5F, top + size * 0.5F, 0).setUv(0.5F, 0.5F);
            addRingVertex(builder, pose, left, top, size, startAngle);
            addRingVertex(builder, pose, left, top, size, endAngle);
        }
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void addRingVertex(
            BufferBuilder builder,
            Matrix4f pose,
            int left,
            int top,
            int size,
            double angle
    ) {
        // Cover the texture square; its alpha channel defines both ring edges.
        double radius = 0.5 / Math.max(Math.abs(Math.cos(angle)), Math.abs(Math.sin(angle)));
        float normalizedX = (float) (0.5 + Math.cos(angle) * radius);
        float normalizedY = (float) (0.5 + Math.sin(angle) * radius);
        float x = left + normalizedX * size;
        float y = top + normalizedY * size;
        builder.addVertex(pose, x, y, 0).setUv(normalizedX, normalizedY);
    }
}
