package xyz.technoz3n.aresenal.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.technoz3n.aresenal.item.weapon.RapierItem;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    protected int autoSpinAttackTicks;

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

        if (self.horizontalCollision) {
            this.autoSpinAttackTicks = Math.min(
                    this.autoSpinAttackTicks,
                    RapierItem.WALL_HIT_SPIN_TICKS
            );
        }
        ci.cancel();
    }
}