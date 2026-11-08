package xyz.technoz3n.aresenal.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.technoz3n.aresenal.item.weapon.RapierItem;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private static final double CONTACT_INFLATE = 0.1;

    @Shadow
    protected int autoSpinAttackTicks;

    @Shadow
    protected abstract void setLivingEntityFlag(int flag, boolean value);

    @Inject(
            method = "checkAutoSpinAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void arsenal$rapierSpin(
            AABB before,
            AABB after,
            CallbackInfo ci
    ) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!RapierItem.usesRapierSpin(self)) {
            return;
        }

        int age = RapierItem.SPIN_TICKS - this.autoSpinAttackTicks;

        if (age >= RapierItem.CONTACT_GRACE_TICKS
                && this.autoSpinAttackTicks > RapierItem.WALL_HIT_SPIN_TICKS
                && arsenal$touchingBlock(self)) {

            this.autoSpinAttackTicks = RapierItem.WALL_HIT_SPIN_TICKS;
        }

        if (!self.level().isClientSide && this.autoSpinAttackTicks <= 0) {
            this.setLivingEntityFlag(4, false);
        }

        ci.cancel();
    }

    @Unique
    private static boolean arsenal$touchingBlock(LivingEntity entity) {
        AABB box = entity.getBoundingBox().inflate(CONTACT_INFLATE);

        return entity.level().getBlockCollisions(entity, box).iterator().hasNext();
    }
}