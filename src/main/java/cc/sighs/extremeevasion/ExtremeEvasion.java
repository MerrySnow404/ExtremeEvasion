package cc.sighs.extremeevasion;

import cc.sighs.extremeevasion.client.CounterAttackGoldRenderType;
import cc.sighs.extremeevasion.compat.roll.RollCompat;
import cc.sighs.extremeevasion.network.ExtremeEvasionNetwork;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.IOException;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExtremeEvasion.MODID)
public class ExtremeEvasion {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "extremeevasion";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public ExtremeEvasion() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ExtremeEvasionNetwork.register();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ENTITY_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(RollCompat::init);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), new ResourceLocation(MODID, "counter_attack_gold"), DefaultVertexFormat.NEW_ENTITY),
                    CounterAttackGoldRenderType::setGoldOverlayShader
            );
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), new ResourceLocation(MODID, "counter_attack_halo"), DefaultVertexFormat.NEW_ENTITY),
                    CounterAttackGoldRenderType::setGoldHaloShader
            );
        }
    }
}
