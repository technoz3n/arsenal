package xyz.technoz3n.aresenal.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import xyz.technoz3n.aresenal.entity.ModEntities;

public class MortarWitherSkull extends WitherSkull {

    public int lifetime;
    public int explosivePower;
    public int extraDamage;
    public float speedFactor;

    public MortarWitherSkull(
            EntityType<? extends MortarWitherSkull> entityType,
            Level level
    ) {
        super(entityType, level);
        setInvulnerable(true);
    }

    public MortarWitherSkull(Level level) {
        this(ModEntities.MORTAR_WITHER_SKULL.get(), level);
    }

    public MortarWitherSkull(
            Level level,
            LivingEntity shooter,
            double x,
            double y,
            double z
    ) {
        super(ModEntities.MORTAR_WITHER_SKULL.get(), level);
        setOwner(shooter);
        setInvulnerable(true);

        Vec3 direction = new Vec3(x, y, z).normalize();
        setDeltaMovement(direction.scale(0.1D));
        xPower = x * 0.1D;
        yPower = y * 0.1D;
        zPower = z * 0.1D;
    }

    public MortarWitherSkull(
            Level level,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ
    ) {
        super(
                ModEntities.MORTAR_WITHER_SKULL.get(),
                level
        );

        setPos(x, y, z);
        setDeltaMovement(velocityX, velocityY, velocityZ);
        setInvulnerable(true);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide) {
            return;
        }

        if (result instanceof EntityHitResult entityHit) {
            handleEntityHit(entityHit.getEntity());
        }

        explode();
        discard();
    }

    private void handleEntityHit(Entity target) {
        Entity owner = getOwner();

        if (owner instanceof LivingEntity shooter) {
            if (target instanceof LivingEntity living) {
                living.setLastHurtByMob(shooter);

                float yaw = shooter.getYHeadRot() * ((float) Math.PI / 180.0F);

                living.push(
                        -Math.sin(yaw) * 1.5D,
                        0.9D,
                        Math.cos(yaw) * 1.5D
                );

                living.hurtMarked = true;
            }

            DamageSource damageSource = level().damageSources().indirectMagic(this, shooter);

            if ((target.isInvulnerableTo(damageSource)
                    || target.getBbHeight() >= 6.0F
                    || (target instanceof LivingEntity living && living.getArmorValue() > 24))
                    && !(target instanceof EnderDragon)
                    && !(target instanceof EnderDragonPart)
                    && !(target instanceof net.minecraft.world.entity.player.Player)) {

                target.hurt(damageSource, 2.14748365E9F);
            }

            if (target instanceof EnderDragon || target instanceof EnderDragonPart) {
                if (target.getBbHeight() >= 6.0F) {
                    target.hurt(damageSource, 5000.0F + extraDamage * 1000.0F);
                } else {
                    target.hurt(damageSource, 500.0F + extraDamage * 100.0F);
                }
            } else if (target instanceof LivingEntity living
                    && living.getMobType() == MobType.UNDEAD) {

                if (living.getBbHeight() >= 6.0F || living.getArmorValue() > 24) {
                    target.hurt(damageSource, 5000.0F + extraDamage * 1000.0F);
                } else {
                    target.hurt(damageSource, 500.0F + extraDamage * 100.0F);
                }
            } else {
                if (target.getBbHeight() >= 6.0F) {
                    target.hurt(damageSource, 5000.0F + extraDamage * 1000.0F);
                } else {
                    target.hurt(damageSource, 500.0F + extraDamage * 100.0F);
                }
            }

        } else {
            target.hurt(level().damageSources().magic(), 500.0F);
        }

        if (target instanceof LivingEntity living) {
            int witherDuration = switch (level().getDifficulty()) {
                case EASY, PEACEFUL -> 1200;
                case NORMAL -> 2400;
                case HARD -> 3600;
            };

            living.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 120, 3
            ));

            living.addEffect(new MobEffectInstance(
                    MobEffects.WITHER, witherDuration, 3
            ));

            living.addEffect(new MobEffectInstance(
                    MobEffects.BLINDNESS, 120, 0
            ));

            living.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, 120, 0
            ));
        }
    }

    private void explode() {
        Entity owner = getOwner();

        level().explode(
                owner != null ? owner : this,
                getX(),
                getY(),
                getZ(),
                14.0F,
                false,
                Level.ExplosionInteraction.MOB
        );

        playImpactSounds();
    }

    private void playImpactSounds() {
        level().playSound(
                null,
                getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                4.0F,
                (1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.6F
        );

        level().playSound(
                null,
                getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                0.5F,
                1.0F
        );

        level().playSound(
                null,
                getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                2.0F,
                1.0F
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        lifetime++;

        if (lifetime >= 1000) {
            explode();
            discard();
        }
    }
}