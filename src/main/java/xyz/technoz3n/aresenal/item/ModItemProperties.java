package xyz.technoz3n.aresenal.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.technoz3n.aresenal.ArsenalMod;
import xyz.technoz3n.aresenal.item.ModItems;

@Mod.EventBusSubscriber(
        modid = ArsenalMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModItemProperties {
    @SubscribeEvent
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.GALAXY_STAFF.get(),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) {
                            return 0.0F;
                        }

                        return entity.getUseItem() != stack
                                ? 0.0F
                                : (float) (stack.getUseDuration()
                                - entity.getUseItemRemainingTicks()) / 20.0F;
                    }
            );

            ItemProperties.register(
                    ModItems.GALAXY_STAFF.get(),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "pulling"),
                    (stack, level, entity, seed) ->
                            entity != null
                                    && entity.isUsingItem()
                                    && entity.getUseItem() == stack
                                    ? 1.0F
                                    : 0.0F
            );
        });
    }
}