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
                        pOutput.accept(ModBlocks.GALACTIC_BLOCK_ITEM.get());
                        pOutput.accept(ModBlocks.GALACTIC_ORE_ITEM.get());
                        pOutput.accept(ModItems.GALAXY_INGOT.get());
                        pOutput.accept(ModItems.GALACTIC_DUST.get());
                        pOutput.accept(ModItems.GALAXY_NUGGET.get());
                        pOutput.accept(ModItems.GALAXY_SHIELD.get());
                        pOutput.accept(ModItems.GALAXY_AXE.get());
                        pOutput.accept(ModItems.GALAXY_HOE.get());
                        pOutput.accept(ModItems.GALAXY_PICKAXE.get());
                        pOutput.accept(ModItems.GALAXY_SHOVEL.get());
                        pOutput.accept(ModItems.GALAXY_SWORD.get());
                        pOutput.accept(ModItems.GALAXY_STAFF.get());
                        pOutput.accept(ModItems.LASER.get());
                        pOutput.accept(ModItems.GALACTIC_APPLE.get());
                        pOutput.accept(ModItems.ENCHANTED_GALACTIC_APPLE.get());
                        pOutput.accept(ModItems.GALACTIC_CARROT.get());
                        pOutput.accept(ModItems.GALACTIC_MELON.get());
                        pOutput.accept(ModItems.RAPIER.get());
                        pOutput.accept(ModItems.WITHER_TURRET_GROUND.get());
                        pOutput.accept(ModItems.WITHER_TURRET_GROUND_SPAWN_EGG.get());
                        pOutput.accept(ModItems.WITHER_TURRET.get());
                        pOutput.accept(ModItems.WITHER_TURRET_SPAWN_EGG.get());

                        pOutput.accept(ModItems.BEDROCK_WITHER_SPAWN_EGG.get());
                    }))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
