package cc.sighs.extremeevasion.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class CounterAttackGoldReplay {
    private static Capture capture;

    public static void capture(ItemStack stack, BakedModel model, PoseStack poseStack, int packedOverlay, float intensity) {
        if (stack.isEmpty() || model.isCustomRenderer() || intensity <= 0.0F) {
            capture = null;
            return;
        }

        PoseStack.Pose pose = poseStack.last();
        capture = new Capture(
                stack.copy(),
                model,
                packedOverlay,
                new Matrix4f(pose.pose()),
                new Matrix3f(pose.normal()),
                new Matrix4f(RenderSystem.getModelViewMatrix()),
                new Matrix4f(RenderSystem.getProjectionMatrix()),
                intensity
        );
    }

    public static void renderAndClear() {
        Capture current = capture;
        capture = null;
        if (current == null) {
            return;
        }

        RenderSystem.backupProjectionMatrix();
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        try {
            RenderSystem.setProjectionMatrix(current.projection(), VertexSorting.ORTHOGRAPHIC_Z);
            modelViewStack.setIdentity();
            modelViewStack.mulPoseMatrix(current.modelView());
            RenderSystem.applyModelViewMatrix();

            PoseStack poseStack = new PoseStack();
            poseStack.last().pose().set(current.pose());
            poseStack.last().normal().set(current.normal());

            MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(new BufferBuilder(256));
            RenderType emissiveItem = RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);
            renderGoldPass(current, poseStack, immediate, emissiveItem, 1.08F, current.intensity() * 0.55F);
            renderGoldPass(current, poseStack, immediate, emissiveItem, 1.18F, current.intensity() * 0.38F);
            renderGoldPass(current, poseStack, immediate, emissiveItem, 1.0F, current.intensity() * 0.42F);
            immediate.endBatch();
        } finally {
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    private static void renderGoldPass(Capture capture, PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType, float scale, float intensity) {
        VertexConsumer vertexConsumer = new GoldVertexConsumer(bufferSource.getBuffer(renderType), intensity);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (BakedModel renderPass : capture.model().getRenderPasses(capture.stack(), true)) {
            itemRenderer.renderModelLists(renderPass, capture.stack(), LightTexture.FULL_BRIGHT, capture.packedOverlay(), poseStack, vertexConsumer);
        }

        poseStack.popPose();
    }

    private record Capture(
            ItemStack stack,
            BakedModel model,
            int packedOverlay,
            Matrix4f pose,
            Matrix3f normal,
            Matrix4f modelView,
            Matrix4f projection,
            float intensity
    ) {
    }

    private record GoldVertexConsumer(VertexConsumer delegate, float intensity) implements VertexConsumer {

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(255, 204, 48, Math.round(alpha * intensity));
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            delegate.vertex(x, y, z, 1.0F, 0.8F, 0.19F, alpha * intensity, u, v, overlay, light, normalX, normalY, normalZ);
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(255, 204, 48, Math.round(alpha * intensity));
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
