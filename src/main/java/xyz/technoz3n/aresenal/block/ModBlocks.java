package xyz.technoz3n.aresenal.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.grower.OakTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.ArsenalMod;
import xyz.technoz3n.aresenal.item.ModItems;
import xyz.technoz3n.aresenal.worldgen.LemonTreeGrower;

import java.util.Optional;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ArsenalMod.MOD_ID);
    public static final LemonTreeGrower LEMON_GROWER = new LemonTreeGrower();

    public static final RegistryObject<Block> LEMON_LEAVES = BLOCKS.register("lemon_leaves",
            () -> new LemonLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Item> LEMON_LEAVES_ITEM = ModItems.ITEMS.register("lemon_leaves",
            () -> new BlockItem(LEMON_LEAVES.get(), new Item.Properties()));
    public static final RegistryObject<Block> LEMON_SAPLING = BLOCKS.register("lemon_sapling",
            () -> new SaplingBlock(LEMON_GROWER, BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));
    public static final RegistryObject<Item> LEMON_SAPLING_ITEM = ModItems.ITEMS.register("lemon_sapling",
            () -> new BlockItem(LEMON_SAPLING.get(), new Item.Properties()));
    public static final RegistryObject<Block> GALACTIC_BLOCK = BLOCKS.register("galactic_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BELL).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Item> GALACTIC_BLOCK_ITEM = ModItems.ITEMS.register("galactic_block",
            () -> new BlockItem(GALACTIC_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Block> GALACTIC_ORE = BLOCKS.register("galactic_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final RegistryObject<Item> GALACTIC_ORE_ITEM = ModItems.ITEMS.register("galactic_ore",
            () -> new BlockItem(GALACTIC_ORE.get(), new Item.Properties()));
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
