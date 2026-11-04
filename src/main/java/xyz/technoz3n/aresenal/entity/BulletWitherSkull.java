
package xyz.technoz3n.aresenal.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public class BulletWitherSkull extends WitherSkull {

    public int lifetime;
    public int explosivePower;
    public int extraDamage;
    public float speedFactor;

    public BulletWitherSkull(
            EntityType<? extends BulletWitherSkull> type,
            Level level
    ) {
        super(type, level);
    }

    public BulletWitherSkull(
            Level level,
            LivingEntity shooter,
            double dx,
            double dy,
            double dz
    ) {
        this(ModEntities.BULLET_WITHER_SKULL.get(), level);

        setOwner(shooter);
        setPos(
                shooter.getX(),
                shooter.getEyeY() - 0.1D,
                shooter.getZ()
        );

        Vec3 direction = new Vec3(dx, dy, dz).normalize();
        double speed = 0.1D;

        this.xPower = direction.x * speed;
        this.yPower = direction.y * speed;
        this.zPower = direction.z * speed;
    }

    @Override
    protected float getInertia() {
        return 0.95F;
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide) {

            if (result instanceof EntityHitResult entityHit) {
                handleEntityImpact(entityHit.getEntity());
            }

            playSound(
                    SoundEvents.GENERIC_EXPLODE,
                    2.0F,
                    (1.0F + (level().random.nextFloat()
                            - level().random.nextFloat()) * 0.2F) * 1.8F
            );

            discard();
        }
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

            DamageSource source;

            if (hit instanceof LivingEntity living
                    && living.getMobType() == net.minecraft.world.entity.MobType.UNDEAD) {

                source = damageSources().generic();

            } else {
                source = damageSources().indirectMagic(this, shooter);
            }

            hit.hurt(source, 10.0F + extraDamage);

            if (!hit.isAlive()
                    && !(hit instanceof net.minecraft.world.entity.player.Player)) {
                hit.discard();
            }

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
                    MobEffects.MOVEMENT_SLOWDOWN,
                    40,
                    3
            ));

            living.addEffect(new MobEffectInstance(
                    MobEffects.WITHER,
                    duration * 20,
                    1
            ));

            if (living.getEyeY() - 0.2D < getY()) {
                living.addEffect(new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        40,
                        0
                ));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide || !isAlive()) {
            return;
        }

        lifetime++;

        if (lifetime >= 100) {
            playSound(
                    SoundEvents.GENERIC_EXPLODE,
                    2.0F,
                    (1.0F + (level().random.nextFloat()
                            - level().random.nextFloat()) * 0.2F) * 1.8F
            );

            discard();
        }
    }
}
