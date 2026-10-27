package xyz.technoz3n.aresenal;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.block.ModBlocks;
import xyz.technoz3n.aresenal.item.ModItems;

public class ArsenalModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ArsenalMod.MOD_ID);
    public static final RegistryObject<CreativeModeTab> ARSENAL_TAB  = CREATIVE_MODE_TABS.register("arsenaltab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.LEMON.get()))
                    .title(Component.translatable("arsenal:arsenaltab")).displayItems(((pParameters, pOutput) ->
                    {
                        pOutput.accept(ModBlocks.LEMON_LEAVES_ITEM.get());
                        pOutput.accept(ModBlocks.LEMON_SAPLING_ITEM.get());
                        pOutput.accept(ModItems.LEMON.get());
                        ItemStack explosiveLemon = ModItems.LEMON.get().getDefaultInstance();
                        explosiveLemon.getOrCreateTag().putBoolean("Explosive", true);
                        pOutput.accept(explosiveLemon);
                    }))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
