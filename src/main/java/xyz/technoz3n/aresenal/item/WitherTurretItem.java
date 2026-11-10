
package xyz.technoz3n.aresenal.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import xyz.technoz3n.aresenal.entity.ModEntities;
import xyz.technoz3n.aresenal.entity.WitherTurret;

public class WitherTurretItem extends Item {

    public WitherTurretItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player == null) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = context.getClickedPos()
                .relative(context.getClickedFace());

        if (!level.getBlockState(spawnPos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        WitherTurret turret = ModEntities.WITHER_TURRET
                .get()
                .create(level);

        if (turret == null) {
            return InteractionResult.FAIL;
        }

        turret.setOwner(player);

        turret.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                player.getYRot(),
                0.0F
        );

        if (!level.noCollision(turret)) {
            return InteractionResult.FAIL;
        }

        if (!level.addFreshEntity(turret)) {
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
