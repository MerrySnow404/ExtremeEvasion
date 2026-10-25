package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.Config;
import cc.sighs.extremeevasion.client.*;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class CounterAttackItemRendererMixin {
    @Unique
    private static final ThreadLocal<Boolean> extremeevasion$renderCounterAttackItem = ThreadLocal.withInitial(() -> false);
    @Unique
    private static final ThreadLocal<Float> extremeevasion$counterAttackIntensity = ThreadLocal.withInitial(() -> 0.0F);

    @Shadow
    public abstract void renderModelLists(BakedModel model, ItemStack stack, int packedLight, int packedOverlay, PoseStack poseStack, VertexConsumer vertexConsumer);

    @Inject(
            method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("HEAD")
    )
    private void extremeevasion$beginCounterAttackItemRender(@Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, @Nullable Level level, int packedLight, int packedOverlay, int seed, CallbackInfo ci) {
        float intensity = CounterAttackClientState.getVisualIntensity();
        boolean renderCounterAttackItem = intensity > 0.0F
                && entity == Minecraft.getInstance().player
                && extremeevasion$isHandContext(displayContext)
                && !stack.isEmpty();
        extremeevasion$renderCounterAttackItem.set(renderCounterAttackItem);
        extremeevasion$counterAttackIntensity.set(renderCounterAttackItem ? intensity : 0.0F);
    }

    @Inject(
            method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("RETURN")
    )
    private void extremeevasion$endCounterAttackItemRender(@Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, @Nullable Level level, int packedLight, int packedOverlay, int seed, CallbackInfo ci) {
        extremeevasion$renderCounterAttackItem.set(false);
        extremeevasion$counterAttackIntensity.set(0.0F);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
                    ordinal = 1
            )
    )
    private void extremeevasion$renderCounterAttackGoldFilter(ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, BakedModel model, CallbackInfo ci) {
        float intensity = extremeevasion$counterAttackIntensity.get();
        if (!Config.enableExtremeCounterGoldShader || !extremeevasion$renderCounterAttackItem.get() || intensity <= 0.0F || stack.isEmpty() || model.isCustomRenderer()) {
            return;
        }

        if (OculusCompat.isShaderPackInUse()) {
            RenderType emissiveItem = RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);
            CounterAttackGoldReplay.capture(stack, model, poseStack, packedOverlay, intensity);
            if (Config.enableBulletTimeScreenShader && BulletTimeClientState.getIntensity() > 0.0F) {
                return;
            }

            OculusCompat.flushHandBuffer();
            MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(new BufferBuilder(256));
            extremeevasion$renderGoldPass(stack, poseStack, immediate, packedOverlay, model, emissiveItem, 1.08F, intensity * 0.55F, true);
            extremeevasion$renderGoldPass(stack, poseStack, immediate, packedOverlay, model, emissiveItem, 1.18F, intensity * 0.38F, true);
            extremeevasion$renderGoldPass(stack, poseStack, immediate, packedOverlay, model, emissiveItem, 1.0F, intensity * 0.42F, true);
            immediate.endBatch();
            return;
        }

        MultiBufferSource.BufferSource bufferSourceBatch = bufferSource instanceof MultiBufferSource.BufferSource batch ? batch : null;
        if (bufferSourceBatch != null) {
            bufferSourceBatch.endBatch();
        }

        extremeevasion$renderGoldPass(stack, poseStack, bufferSource, packedOverlay, model, CounterAttackGoldRenderType.goldHalo(), 1.09F, intensity, false);
        extremeevasion$renderGoldPass(stack, poseStack, bufferSource, packedOverlay, model, CounterAttackGoldRenderType.goldHalo(), 1.18F, intensity, false);
        extremeevasion$renderGoldPass(stack, poseStack, bufferSource, packedOverlay, model, CounterAttackGoldRenderType.goldHalo(), 1.32F, intensity, false);
        if (bufferSourceBatch != null) {
            bufferSourceBatch.endBatch(CounterAttackGoldRenderType.goldHalo());
        }

        extremeevasion$renderGoldPass(stack, poseStack, bufferSource, packedOverlay, model, CounterAttackGoldRenderType.goldOverlay(), 1.0F, intensity, false);
        if (bufferSourceBatch != null) {
            bufferSourceBatch.endBatch(CounterAttackGoldRenderType.goldOverlay());
        }
    }

    @Unique
    private void extremeevasion$renderGoldPass(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay, BakedModel model, RenderType renderType, float scale, float intensity, boolean tintGold) {
        VertexConsumer vertexConsumer = new CounterAttackGoldVertexConsumer(bufferSource.getBuffer(renderType), intensity, tintGold);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        for (BakedModel renderPass : model.getRenderPasses(stack, true)) {
            renderModelLists(renderPass, stack, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, vertexConsumer);
        }
        poseStack.popPose();
    }

    @Unique
    private static boolean extremeevasion$isHandContext(ItemDisplayContext displayContext) {
        return displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Unique
    private record CounterAttackGoldVertexConsumer(VertexConsumer delegate, float intensity,
                                                   boolean tintGold) implements VertexConsumer {

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            if (tintGold) {
                delegate.color(255, 204, 48, Math.round(alpha * intensity));
            } else {
                delegate.color(red, green, blue, Math.round(alpha * intensity));
            }
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
            if (tintGold) {
                delegate.vertex(x, y, z, 1.0F, 0.8F, 0.19F, alpha * intensity, u, v, overlay, light, normalX, normalY, normalZ);
            } else {
                delegate.vertex(x, y, z, red, green, blue, alpha * intensity, u, v, overlay, light, normalX, normalY, normalZ);
            }
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            if (tintGold) {
                delegate.defaultColor(255, 204, 48, Math.round(alpha * intensity));
            } else {
                delegate.defaultColor(red, green, blue, Math.round(alpha * intensity));
            }
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
