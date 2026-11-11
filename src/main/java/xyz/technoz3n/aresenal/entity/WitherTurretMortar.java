
package xyz.technoz3n.aresenal.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
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

import java.util.Comparator;
import java.util.UUID;

public class WitherTurretMortar extends PathfinderMob implements RangedAttackMob {

    public int durabilityLevel;
    public int ferocityLevel;
    public int maniacLevel;
    public int unstabilityLevel;
    public int shurakinLevel;
    public int unbreakingLevel;

    public int shootingTimer;

    private UUID ownerUUID;

    public WitherTurretMortar(
            EntityType<? extends WitherTurretMortar> entityType,
            Level level
    ) {
        super(entityType, level);
        setPersistenceRequired();
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 7000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 200.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
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
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public int getArmorValue() {
        return Math.min(2 + unbreakingLevel * 7, 24);
    }

    @Override
    public void travel(Vec3 travelVector) {
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
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void checkDespawn() {
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
        return SoundEvents.WITHER_DEATH;
    }

    protected boolean canTargetEntity(LivingEntity entity) {
        if (entity == this || !entity.isAlive()) {
            return false;
        }

        if (entity instanceof WitherTurretMortar
                || entity instanceof WitherTurretGround
                || entity instanceof WitherTurret) {
            return false;
        }

        if (ownerUUID != null) {
            return !isOwner(entity)
                    && !(entity instanceof Player)
                    && !(entity instanceof IronGolem)
                    && !(entity instanceof SnowGolem)
                    && !(entity instanceof AgeableMob);
        }

        return entity.getMobType() != MobType.UNDEAD;
    }

    private void acquireTarget() {
        if (getTarget() != null) {
            return;
        }

        Vec3 look = getLookAngle();

        double offsetX = look.x * 49.0D;
        double offsetZ = look.z * 49.0D;

        AABB searchArea = getBoundingBox()
                .inflate(50.0D, 100.0D, 50.0D)
                .move(offsetX, 0.0D, offsetZ);

        for (Entity entity : level().getEntities(this, searchArea)) {
            if (entity instanceof LivingEntity living
                    && canTargetEntity(living)
                    && hasLineOfSight(living)) {

                setTarget(living);
                break;
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (shootingTimer > 0 || level().isClientSide) {
            return;
        }

        double muzzleX = getX();
        double muzzleY = getY() + 3.0D;
        double muzzleZ = getZ();

        double dx = target.getX() - muzzleX;
        double dy = target.getY() + 0.25D - muzzleY;
        double dz = target.getZ() - muzzleZ;

        MortarWitherSkull skull = new MortarWitherSkull(
                level(), this, dx, dy, dz
        );

        skull.setInvulnerable(true);

        skull.extraDamage = ferocityLevel * 2;
        skull.explosivePower = (int) (unstabilityLevel * 0.75F);
        skull.setSpeedFactor(shurakinLevel * 0.1F);

        skull.setPos(muzzleX, muzzleY, muzzleZ);
        level().addFreshEntity(skull);

        level().playSound(
                null,
                getX(), getY(), getZ(),
                ArsenalModSounds.MORTAR_HIT.get(),
                SoundSource.HOSTILE,
                0.5F,
                (1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.6F
        );

        level().playSound(
                null,
                getX(), getY(), getZ(),
                ArsenalModSounds.MORTAR_SHOT.get(),
                SoundSource.HOSTILE,
                3.0F,
                1.0F
        );

        shootingTimer = Math.max(0, 60 - maniacLevel * 10);

        BlockPos basePos = BlockPos.containing(
                getX(), getY() - 1.0D, getZ()
        );

        level().setBlock(
                basePos,
                Blocks.GLOWSTONE.defaultBlockState(),
                3
        );
    }

    @Override
    public void tick() {
        super.tick();

        setNoGravity(true);
        setOnGround(true);
        setDeltaMovement(Vec3.ZERO);

        if (level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                level().addParticle(
                        ParticleTypes.SMOKE,
                        getX() + random.nextGaussian() * 0.3D,
                        getY() + 3.0D + random.nextGaussian() * 0.3D,
                        getZ() + random.nextGaussian() * 0.3D,
                        0.0D, 0.0D, 0.0D
                );
            }
            return;
        }

        if (tickCount % 40 == 0) {
            heal(1.0F + durabilityLevel);
        }

        BlockPos basePos = BlockPos.containing(
                getX(), getY() - 1.0D, getZ()
        );

        if (shootingTimer > 0) {
            shootingTimer--;
        }

        acquireTarget();

        LivingEntity target = getTarget();

        if (target != null) {
            getLookControl().setLookAt(target, 10.0F, 180.0F);

            setYRot(getYHeadRot());
            yBodyRot = getYHeadRot();

            level().setBlock(
                    basePos,
                    Blocks.GLOWSTONE.defaultBlockState(),
                    3
            );

            performRangedAttack(target, 1.0F);

            if (!target.isAlive()
                    || target.isRemoved()
                    || distanceToSqr(target) > 10000.0D
                    || !hasLineOfSight(target)
                    || !canTargetEntity(target)) {
                setTarget(null);
            }
        } else {
            level().setBlock(
                    basePos,
                    Blocks.BEDROCK.defaultBlockState(),
                    3
            );
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }

        Entity attacker = source.getEntity();

        if (attacker instanceof WitherTurretMortar
                || attacker instanceof WitherTurretGround
                || attacker instanceof WitherTurret) {
            return false;
        }

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

        boolean damaged = super.hurt(source, amount);

        if (damaged
                && !level().isClientSide
                && attacker instanceof LivingEntity living
                && !isOwner(living)) {

            setTarget(living);
            setLastHurtByMob(living);

            AABB alertArea = getBoundingBox().inflate(100.0D);

            for (WitherTurretMortar turret :
                    level().getEntitiesOfClass(
                            WitherTurretMortar.class,
                            alertArea
                    )) {

                if (turret != this && turret.isAlive()) {
                    turret.setTarget(living);
                    turret.setLastHurtByMob(living);
                }
            }
        }

        return damaged;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }

        tag.putInt("Ench1Level", durabilityLevel);
        tag.putInt("Ench2Level", ferocityLevel);
        tag.putInt("Ench3Level", maniacLevel);
        tag.putInt("Ench4Level", unstabilityLevel);
        tag.putInt("Ench5Level", shurakinLevel);
        tag.putInt("Ench6Level", unbreakingLevel);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        } else {
            ownerUUID = null;
        }

        durabilityLevel = tag.getInt("Ench1Level");
        ferocityLevel = tag.getInt("Ench2Level");
        maniacLevel = tag.getInt("Ench3Level");
        unstabilityLevel = tag.getInt("Ench4Level");
        shurakinLevel = tag.getInt("Ench5Level");
        unbreakingLevel = tag.getInt("Ench6Level");
    }
}
