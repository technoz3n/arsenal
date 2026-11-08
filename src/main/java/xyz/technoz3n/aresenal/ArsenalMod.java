package xyz.technoz3n.aresenal;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import xyz.technoz3n.aresenal.block.ModBlocks;
import xyz.technoz3n.aresenal.enchantment.ModEnchantments;
import xyz.technoz3n.aresenal.entity.BedrockWitherBoss;
import xyz.technoz3n.aresenal.entity.ModEntities;
import xyz.technoz3n.aresenal.entity.WitherTurretGround;
import xyz.technoz3n.aresenal.item.ModItems;
import xyz.technoz3n.aresenal.model.FulminationSigilModel;
import xyz.technoz3n.aresenal.model.WitherMachineGunModel;
import xyz.technoz3n.aresenal.recipe.ModRecipes;
import xyz.technoz3n.aresenal.renderer.BedrockWitherBossRenderer;
import xyz.technoz3n.aresenal.renderer.FulminationSigilRenderer;
import xyz.technoz3n.aresenal.renderer.WitherTurretGroundRenderer;

@Mod(ArsenalMod.MOD_ID)
public class ArsenalMod
{
    public static final String MOD_ID = "arsenal";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ArsenalMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModEntities.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ArsenalModTabs.register(modEventBus);
        ArsenalModSounds.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        // modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

   /* private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.LEMON);
        }
    } */

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEntityAttributes {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(
                    ModEntities.BEDROCK_WITHER.get(),
                    BedrockWitherBoss.createAttributes().build()
            );
            event.put(
                    ModEntities.WITHER_TURRET_GROUND.get(),
                    WitherTurretGround.createAttributes().build()
            );
        }
    }
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.BEDROCK_WITHER.get(), BedrockWitherBossRenderer::new);
            event.registerEntityRenderer(ModEntities.WITHER_TURRET_GROUND.get(), WitherTurretGroundRenderer::new);
            event.registerEntityRenderer(ModEntities.BULLET_WITHER_SKULL.get(), net.minecraft.client.renderer.entity.WitherSkullRenderer::new);
            event.registerEntityRenderer(ModEntities.FULMINATION_SIGIL.get(), FulminationSigilRenderer::new);
        }
        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(WitherMachineGunModel.LAYER_LOCATION, WitherMachineGunModel::createBodyLayer);
            event.registerLayerDefinition(FulminationSigilModel.LAYER_LOCATION, FulminationSigilModel::createBodyLayer);
        }
    }
}
