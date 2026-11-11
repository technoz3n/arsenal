
package xyz.technoz3n.aresenal.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.AgeableMob;
import xyz.technoz3n.aresenal.ArsenalModSounds;

import java.util.Comparator;
import java.util.UUID;

public class WitherTurretGround extends PathfinderMob {

    private int shootingTimer = 0;
    private int shotsRemaining = 14;

    private UUID ownerUUID;

    public WitherTurretGround(
            EntityType<? extends PathfinderMob> type,
            Level level
    ) {
        super(type, level);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
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
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ArsenalModSounds.WITHER_TURRET_DEATH_2.get();
    }

    @Override
    public void travel(Vec3 travelVector) {
    }

    public Vec3 getMuzzlePosition(int index, float partialTick) {
        if (index == 0) {
            return new Vec3(getX(), getY() + 0.5D, getZ());
        }

        float yaw = Mth.rotLerp(partialTick, yHeadRotO, yHeadRot);
        float angle = yaw * Mth.DEG_TO_RAD
                + (index == 1 ? 0.0F : (float) Math.PI);

        return new Vec3(
                getX() + Math.cos(angle) * 1.2D,
                getY() + 1.15D,
                getZ() + Math.sin(angle) * 1.2D
        );
    }

    private void spawnMuzzleSmoke() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i <= 2; i++) {
            Vec3 muzzle = getMuzzlePosition(i, 1.0F);

            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    muzzle.x,
                    muzzle.y,
                    muzzle.z,
                    1,
                    0.05D,
                    0.05D,
                    0.05D,
                    0.01D
            );
        }
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(Vec3.ZERO);

        if (level().isClientSide) {
            return;
        }

        spawnMuzzleSmoke();

        if (tickCount % 20 == 0 && getHealth() < getMaxHealth()) {
            heal(1.0F);
        }

        BlockPos below = blockPosition().below();

        if (!level().getBlockState(below).is(Blocks.BEDROCK)) {
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

            if (shotsRemaining <= 0 && shootingTimer <= 10) {
                shotsRemaining = 14;
            }

            level().setBlockAndUpdate(
                    blockPosition().below(),
                    Blocks.GLOWSTONE.defaultBlockState()
            );

            if (shootingTimer <= 0) {
                level().playSound(
                        null,
                        getX(),
                        getY(),
                        getZ(),
                        ArsenalModSounds.WITHER_TURRET_SHOOT_2.get(),
                        SoundSource.HOSTILE,
                        10.0F,
                        1.0F
                );

                fireVolley(target);

                shotsRemaining -= 2;

                if (shotsRemaining <= 0) {
                    shotsRemaining = 14;
                    shootingTimer = 14;
                } else {
                    shootingTimer = 2;
                }
            }
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

        if (attacker instanceof WitherTurretGround) {
            return false;
        }

        boolean damaged = super.hurt(source, amount);

        if (damaged
                && attacker instanceof LivingEntity livingAttacker
                && !level().isClientSide
                && !isOwner(livingAttacker)) {

            setTarget(livingAttacker);
            setLastHurtByMob(livingAttacker);

            shootingTimer = Math.max(0, shootingTimer - 4);

            AABB alertArea = getBoundingBox().inflate(100.0D);

            for (WitherTurretGround turret :
                    level().getEntitiesOfClass(
                            WitherTurretGround.class,
                            alertArea
                    )) {

                if (turret != this && turret.isAlive()) {
                    turret.setTarget(livingAttacker);
                    turret.setLastHurtByMob(livingAttacker);
                }
            }
        }

        return damaged;
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
        AABB searchArea = getBoundingBox().inflate(32.0D);

        return level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity ->
                        entity != this
                                && entity.isAlive()
                                && !isOwner(entity)
                                && !(entity instanceof WitherTurretGround)
                                && !(entity instanceof WitherTurret)
                                && !(entity instanceof WitherTurretMortar)
                                && canTargetEntity(entity)
                                && entity.getY() <= getY() + 6.0D
                                && distanceToSqr(entity) <= 1024.0D
                                && hasLineOfSight(entity)
        ).stream().min(
                Comparator.comparingDouble(this::distanceToSqr)
        ).orElse(null);
    }


    private void fireVolley(LivingEntity target) {
        double aimX = target.getX();
        double aimY = target.getY() + 0.5D;
        double aimZ = target.getZ();

        if (distanceToSqr(target) > 900.0D
                || target.getY() >= getY() + 5.0D) {

            aimX += random.nextDouble() * 2.0D - 1.0D;
            aimZ += random.nextDouble() * 2.0D - 1.0D;
        }

        for (int i = 1; i <= 2; i++) {
            Vec3 muzzle = getMuzzlePosition(i, 1.0F);

            double dx = aimX - muzzle.x;
            double dy = aimY - muzzle.y;
            double dz = aimZ - muzzle.z;

            BulletWitherSkull skull = new BulletWitherSkull(
                    level(),
                    this,
                    dx,
                    dy,
                    dz
            );

            skull.setPos(muzzle);
            level().addFreshEntity(skull);
        }
    }
}
