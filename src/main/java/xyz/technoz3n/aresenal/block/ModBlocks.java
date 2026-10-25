package xyz.technoz3n.aresenal.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.ArsenalMod;
import xyz.technoz3n.aresenal.item.ModItems;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ArsenalMod.MOD_ID);
    public static final RegistryObject<Block> LEMON_LEAVES = BLOCKS.register("lemon_leaves",
            () -> new LemonLeavesBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.OAK_LEAVES)
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Item> LEMON_LEAVES_ITEM = ModItems.ITEMS.register("lemon_leaves",
            () -> new BlockItem(LEMON_LEAVES.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
