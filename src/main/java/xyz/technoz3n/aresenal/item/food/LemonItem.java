package xyz.technoz3n.aresenal.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LemonItem extends Item {

    public LemonItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(
            ItemStack stack,
            Level level,
            LivingEntity entity
    ) {
        boolean explosive = stack.hasTag()
                && stack.getTag().getBoolean("Explosive");

        if (explosive && !level.isClientSide()
                && entity instanceof Player player) {

            level.explode(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    4.0F,
                    true,
                    Level.ExplosionInteraction.TNT
            );
        }

        return super.finishUsingItem(stack, level, entity);
    }
}