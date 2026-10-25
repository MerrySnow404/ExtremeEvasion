package cc.sighs.extremeevasion;

import cc.sighs.extremeevasion.client.BulletTimeShaderManager;
import cc.sighs.extremeevasion.client.CounterAttackGoldRenderType;
import cc.sighs.extremeevasion.compat.roll.RollCompat;
import cc.sighs.extremeevasion.network.ExtremeEvasionNetwork;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.io.IOException;

// The value here should match the mod id in the NeoForge metadata.
@Mod(ExtremeEvasion.MODID)
public class ExtremeEvasion {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "extremeevasion";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExtremeEvasion(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ExtremeEvasionNetwork::register);
        modEventBus.addListener(Config::onLoad);

        NeoForge.EVENT_BUS.register(ExtremeEvasionEvents.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientModEvents::registerShadersUnchecked);
            modEventBus.addListener(BulletTimeShaderManager::onRegisterClientReloadListeners);
            NeoForge.EVENT_BUS.addListener(BulletTimeShaderManager::onRenderLevelStage);
        }

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(RollCompat::init);
    }

    public static class ClientModEvents {
        public static void registerShadersUnchecked(RegisterShadersEvent event) {
            try {
                registerShaders(event);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to register Extreme Evasion shaders", exception);
            }
        }

        public static void registerShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(MODID, "counter_attack_gold"), DefaultVertexFormat.NEW_ENTITY),
                    CounterAttackGoldRenderType::setGoldOverlayShader
            );
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(MODID, "counter_attack_halo"), DefaultVertexFormat.NEW_ENTITY),
                    CounterAttackGoldRenderType::setGoldHaloShader
            );
        }
    }
}
