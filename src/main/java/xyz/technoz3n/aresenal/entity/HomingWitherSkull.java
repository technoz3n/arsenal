
package xyz.technoz3n.aresenal.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;

public class HomingWitherSkull extends WitherSkull {

    public int lifetime;
    public int explosivePower;
    public int extraDamage;

    public float speedFactor = 0.35F;

    private LivingEntity assignedTarget;

    public HomingWitherSkull(
            EntityType<? extends HomingWitherSkull> type,
            Level level
    ) {
        super(type, level);
    }

    public void setAssignedTarget(LivingEntity target) {
        this.assignedTarget = target;
    }

    @Override
    protected float getInertia() {
        return 0.95F;
    }

    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide || !isAlive()) {
            return;
        }

        if (result instanceof EntityHitResult entityHit) {
            handleEntityImpact(entityHit.getEntity());
        }

        detonate();
    }

    private void handleEntityImpact(Entity hit) {
        Entity shooter = getOwner();

        if (shooter != null) {

            if (hit instanceof LivingEntity living) {
                if (shooter instanceof LivingEntity attacker) {
                    living.setLastHurtByMob(attacker);

                    if (living instanceof net.minecraft.world.entity.Mob mob) {
                        mob.setTarget(attacker);
                    }
                }

                living.invulnerableTime = 1;
            }

            DamageSource source = damageSources().indirectMagic(this, shooter);
            hit.hurt(source, 10.0F + extraDamage);

        } else {
            hit.hurt(damageSources().magic(), 5.0F);
        }

        if (hit instanceof LivingEntity living) {
            int duration = switch (level().getDifficulty()) {
                case PEACEFUL, EASY -> 10;
                case NORMAL -> 20;
                case HARD -> 40;
            };

            living.addEffect(new MobEffectInstance(
                    MobEffects.WITHER,
                    duration * 20,
                    1
            ));
        }
    }

    private void detonate() {
        if (level().isClientSide || !isAlive()) {
            return;
        }

        playSound(
                SoundEvents.GENERIC_EXPLODE,
                2.0F,
                1.0F
        );

        level().explode(
                this,
                getX(),
                getY(),
                getZ(),
                2.0F + explosivePower,
                false,
                level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)
                        ? Level.ExplosionInteraction.MOB
                        : Level.ExplosionInteraction.NONE
        );

        discard();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide || !isAlive()) {
            return;
        }

        lifetime++;

        if (assignedTarget != null && assignedTarget.isAlive()) {
            Vec3 targetPosition = new Vec3(
                    assignedTarget.getX(),
                    assignedTarget.getEyeY(),
                    assignedTarget.getZ()
            );

            Vec3 difference = targetPosition.subtract(position());
            double distanceSqr = difference.lengthSqr();

            if (distanceSqr < 9.0D) {
                onHit(new EntityHitResult(assignedTarget));
                return;
            }

            if (distanceSqr > 0.0001D) {
                Vec3 desiredVelocity = difference.normalize().scale(speedFactor);

                setDeltaMovement(
                        getDeltaMovement().scale(0.75D)
                                .add(desiredVelocity.scale(0.75D))
                );
            }
        }

        if (lifetime >= 200) {
            detonate();
        }
    }
}
