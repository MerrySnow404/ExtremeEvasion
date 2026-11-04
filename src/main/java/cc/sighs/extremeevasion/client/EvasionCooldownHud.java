package cc.sighs.extremeevasion.client;

import cc.sighs.extremeevasion.Config;
import cc.sighs.extremeevasion.ExtremeEvasion;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = ExtremeEvasion.MODID, value = Dist.CLIENT)
public final class EvasionCooldownHud {
    private static final int SEGMENTS = 64;
    private static final int BASE_SIZE = 32;
    private static final int TEXTURE_SIZE = 64;
    private static final ResourceLocation OUTER_RING = new ResourceLocation(ExtremeEvasion.MODID, "textures/gui/outer_ring.png");
    private static final ResourceLocation INNER_RING = new ResourceLocation(ExtremeEvasion.MODID, "textures/gui/inner_ring.png");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        float progress = EvasionCooldownClientState.getProgress();
        if (progress <= 0.0F) {
            return;
        }

        int size = Math.max(1, (int) Math.round(BASE_SIZE * Config.extremeEvasionCooldownGuiScale));
        int left = (int) Math.round(event.getWindow().getGuiScaledWidth() * Config.extremeEvasionCooldownGuiRatioX)
                - size - 8 + Config.extremeEvasionCooldownGuiOffsetX;
        int top = (int) Math.round(event.getWindow().getGuiScaledHeight() * Config.extremeEvasionCooldownGuiRatioY)
                - size - 8 + Config.extremeEvasionCooldownGuiOffsetY;
        event.getGuiGraphics().blit(OUTER_RING, left + 1, top + 1, 0, 0, size - 2, size - 2, TEXTURE_SIZE, TEXTURE_SIZE);
        drawProgressRing(event, left, top, size, Math.round(SEGMENTS * progress));
    }

    private static void drawProgressRing(RenderGuiOverlayEvent.Post event, int left, int top, int size, int segments) {
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
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int segment = 0; segment < segments; segment++) {
            double startAngle = -Math.PI / 2.0 + Math.PI * 2.0 * segment / SEGMENTS;
            double endAngle = -Math.PI / 2.0 + Math.PI * 2.0 * (segment + 1) / SEGMENTS;
            addRingVertex(builder, pose, left, top, size, 0.28, startAngle);
            addRingVertex(builder, pose, left, top, size, 0.46, startAngle);
            addRingVertex(builder, pose, left, top, size, 0.46, endAngle);
            addRingVertex(builder, pose, left, top, size, 0.28, endAngle);
        }
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void addRingVertex(BufferBuilder builder, Matrix4f pose, int left, int top, int size, double radius, double angle) {
        float normalizedX = (float) (0.5 + Math.cos(angle) * radius);
        float normalizedY = (float) (0.5 + Math.sin(angle) * radius);
        float x = left + normalizedX * size;
        float y = top + normalizedY * size;
        builder.vertex(pose, x, y, 0).uv(normalizedX, normalizedY).endVertex();
    }
}
