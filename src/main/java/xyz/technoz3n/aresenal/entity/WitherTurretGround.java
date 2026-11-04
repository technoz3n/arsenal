package xyz.technoz3n.aresenal.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import java.util.Comparator;

public class WitherTurretGround extends PathfinderMob {

    private int shootingTimer = 0;
    private int shotsRemaining = 14;

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
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void travel(Vec3 travelVector) {
    }


    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(Vec3.ZERO);

        if (level().isClientSide) {
            return;
        }

        BlockPos below = blockPosition().below();

        if (!level().getBlockState(below).is(Blocks.BEDROCK)) {
            level().setBlockAndUpdate(below, Blocks.BEDROCK.defaultBlockState());
        }

        if (shootingTimer > 0) {
            shootingTimer--;
        }

        LivingEntity target = findNearestTarget();
        setTarget(target);

        if (target != null && shootingTimer <= 0) {
            lookAt(target, 180.0F, 180.0F);

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


    private LivingEntity findNearestTarget() {
        AABB searchArea = getBoundingBox().inflate(32.0D);

        return level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity ->
                        entity != this
                                && entity.isAlive()
                                && entity.getMobType() != MobType.UNDEAD
                                && !(entity instanceof WitherTurretGround)
                                && entity.getY() <= getY() + 6.0D
                                && distanceToSqr(entity) <= 1024.0D
                                && hasLineOfSight(entity)
        ).stream().min(
                Comparator.comparingDouble(this::distanceToSqr)
        ).orElse(null);
    }


    private void fireVolley(LivingEntity target) {
        float yawRadians = getYRot() * Mth.DEG_TO_RAD;

        double aimX = target.getX();
        double aimY = target.getY() + target.getBbHeight() * 0.5D;
        double aimZ = target.getZ();

        for (int i = 0; i < 2; i++) {
            double angle = yawRadians + (i * Math.PI);

            double muzzleX = getX() + Math.cos(angle) * 1.2D;
            double muzzleY = getY() + 1.15D;
            double muzzleZ = getZ() + Math.sin(angle) * 1.2D;

            double dx = aimX - muzzleX;
            double dy = aimY - muzzleY;
            double dz = aimZ - muzzleZ;

            WitherSkull skull = new WitherSkull(
                    level(),
                    this,
                    dx,
                    dy,
                    dz
            );

            skull.setPos(muzzleX, muzzleY, muzzleZ);
            level().addFreshEntity(skull);
        }
    }
}