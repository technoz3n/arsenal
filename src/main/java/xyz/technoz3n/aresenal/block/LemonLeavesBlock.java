package xyz.technoz3n.aresenal.block;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LemonLeavesBlock extends LeavesBlock {
    public LemonLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties
                .sound(SoundType.GRASS)
                .strength(0.2F)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        );
    }
}