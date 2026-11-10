
package xyz.technoz3n.aresenal.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xyz.technoz3n.aresenal.item.ModItems;

public class Laser extends AbstractArrow {

    public Laser(EntityType<? extends Laser> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void onHit(net.minecraft.world.phys.HitResult result) {
        super.onHit(result);

        if (!level().isClientSide) {
            level().explode(
                    this,
                    getX(),
                    getY(),
                    getZ(),
                    4.0F,
                    Level.ExplosionInteraction.MOB
            );

            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
    }
    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.LASER.get());
    }
    @Override
    protected void doPostHurtEffects(net.minecraft.world.entity.LivingEntity target) {
        super.doPostHurtEffects(target);
    }
}
