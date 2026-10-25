package cc.sighs.extremeevasion.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class CounterAttackGoldRenderType extends RenderType {
    private static ShaderInstance goldOverlayShader;
    private static ShaderInstance goldHaloShader;

    private static final RenderStateShard.ShaderStateShard GOLD_OVERLAY_SHADER = new RenderStateShard.ShaderStateShard(
            () -> goldOverlayShader
    );

    private static final RenderStateShard.ShaderStateShard GOLD_HALO_SHADER = new RenderStateShard.ShaderStateShard(
            () -> goldHaloShader
    );

    private static final RenderType GOLD_OVERLAY = createGoldRenderType(
            "extremeevasion_counter_attack_gold_overlay",
            GOLD_OVERLAY_SHADER
    );

    private static final RenderType GOLD_HALO = createGoldRenderType(
            "extremeevasion_counter_attack_gold_halo",
            GOLD_HALO_SHADER
    );

    private static RenderType createGoldRenderType(String name, RenderStateShard.ShaderStateShard shader) {
        return RenderType.create(
                name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(shader)
                        .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)
        );
    }

    private CounterAttackGoldRenderType() {
        super("extremeevasion_counter_attack_gold", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, () -> {
        }, () -> {
        });
    }

    public static RenderType goldOverlay() {
        return GOLD_OVERLAY;
    }

    public static RenderType goldHalo() {
        return GOLD_HALO;
    }

    public static void setGoldOverlayShader(ShaderInstance shader) {
        goldOverlayShader = shader;
    }

    public static void setGoldHaloShader(ShaderInstance shader) {
        goldHaloShader = shader;
    }
}
