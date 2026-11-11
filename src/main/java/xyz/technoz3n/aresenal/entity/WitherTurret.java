
package xyz.technoz3n.aresenal.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import xyz.technoz3n.aresenal.ArsenalModSounds;
import xyz.technoz3n.aresenal.entity.ModEntities;

import java.util.Comparator;
import java.util.UUID;

public class WitherTurret extends PathfinderMob {

    private int shootingTimer = 0;

    private UUID ownerUUID;

    public WitherTurret(
            EntityType<? extends PathfinderMob> type,
            Level level
    ) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 100.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public void setOwner(Player owner) {
        this.ownerUUID = owner.getUUID();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    private boolean isOwner(Entity entity) {
        return ownerUUID != null
                && entity.getUUID().equals(ownerUUID);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        } else {
            ownerUUID = null;
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean causeFallDamage(
            float fallDistance,
            float multiplier,
            DamageSource damageSource
    ) {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void travel(Vec3 travelVector) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ArsenalModSounds.WITHER_TURRET_DEATH.get();
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    private boolean canTargetEntity(LivingEntity entity) {
        if (ownerUUID != null) {
            return !(entity instanceof Player)
                    && !(entity instanceof IronGolem)
                    && !(entity instanceof SnowGolem)
                    && !(entity instanceof AgeableMob);
        }

        return entity.getMobType() != MobType.UNDEAD;
    }

    private LivingEntity findNearestTarget() {
        AABB searchArea = getBoundingBox().inflate(
                50.0D,
                100.0D,
                50.0D
        );

        return level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity ->
                        entity != this
                                && entity.isAlive()
                                && !isOwner(entity)
                                && !(entity instanceof WitherTurret)
                                && !(entity instanceof WitherTurretGround)
                                && !(entity instanceof WitherTurretMortar)
                                && canTargetEntity(entity)
                                && entity.getEyeY() >= getY() + 6.0D
                                && distanceToSqr(entity) <= 10000.0D
                                && hasLineOfSight(entity)
        ).stream().min(
                Comparator.comparingDouble(this::distanceToSqr)
        ).orElse(null);
    }

    private void fireAtTarget(LivingEntity target) {
        double muzzleX = getX();
        double muzzleY = getY() + 2.6D;
        double muzzleZ = getZ();

        double dx = target.getX() - muzzleX;
        double dy = target.getEyeY() - muzzleY;
        double dz = target.getZ() - muzzleZ;

        HomingWitherSkull skull = new HomingWitherSkull(
                ModEntities.HOMING_WITHER_SKULL.get(),
                level()
        );

        skull.setOwner(this);
        skull.setAssignedTarget(target);

        skull.setPos(muzzleX, muzzleY, muzzleZ);
        skull.shoot(dx, dy, dz, 0.8F, 0.0F);

        level().addFreshEntity(skull);
    }

    @Override
    public void tick() {
        super.tick();

        setDeltaMovement(Vec3.ZERO);

        if (level().isClientSide) {
            return;
        }

        if (tickCount % 20 == 0 && getHealth() < getMaxHealth()) {
            heal(1.0F);
        }

        BlockPos below = blockPosition().below();

        if (!level().getBlockState(below).is(Blocks.BEDROCK)
                && !level().getBlockState(below).is(Blocks.GLOWSTONE)) {
            level().setBlockAndUpdate(
                    below,
                    Blocks.BEDROCK.defaultBlockState()
            );
        }

        if (shootingTimer > 0) {
            shootingTimer--;
        }

        LivingEntity target = findNearestTarget();
        setTarget(target);

        if (target != null) {
            getLookControl().setLookAt(target, 360.0F, 360.0F);

            level().setBlockAndUpdate(
                    below,
                    Blocks.GLOWSTONE.defaultBlockState()
            );

            if (shootingTimer <= 0) {
                level().playSound(
                        null,
                        getX(),
                        getY(),
                        getZ(),
                        ArsenalModSounds.WITHER_TURRET_SHOOT.get(),
                        SoundSource.HOSTILE,
                        10.0F,
                        1.0F
                );

                fireAtTarget(target);
                shootingTimer = 20;
            }

        } else {
            setYRot(getYRot() + 10.0F);
            setYHeadRot(getYRot());

            setXRot(-45.0F);
            xRotO = -45.0F;

            yBodyRot = getYRot();

            level().setBlockAndUpdate(
                    below,
                    Blocks.BEDROCK.defaultBlockState()
            );
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();

        if (source.is(DamageTypes.FALLING_ANVIL)
                || source.is(DamageTypes.GENERIC)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.LIGHTNING_BOLT)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.FALLING_BLOCK)
                || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.CACTUS)) {
            return false;
        }

        if (attacker instanceof WitherTurret
                || attacker instanceof WitherTurretGround) {
            return false;
        }

        boolean damaged = super.hurt(source, amount);

        if (damaged
                && attacker instanceof LivingEntity livingAttacker
                && !level().isClientSide
                && !isOwner(livingAttacker)) {

            setTarget(livingAttacker);
            setLastHurtByMob(livingAttacker);

            shootingTimer = Math.max(0, shootingTimer - 2);

            AABB alertArea = getBoundingBox().inflate(100.0D);

            for (WitherTurret turret :
                    level().getEntitiesOfClass(
                            WitherTurret.class,
                            alertArea
                    )) {

                if (turret != this && turret.isAlive()) {
                    turret.setTarget(livingAttacker);
                    turret.setLastHurtByMob(livingAttacker);
                    turret.shootingTimer = Math.max(
                            0,
                            turret.shootingTimer - 2
                    );
                }
            }
        }

        return damaged;
    }
}
