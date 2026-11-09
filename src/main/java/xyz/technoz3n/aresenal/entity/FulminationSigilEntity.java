
package xyz.technoz3n.aresenal.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class FulminationSigilEntity extends Entity {

    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(
                    FulminationSigilEntity.class,
                    EntityDataSerializers.INT
            );

    public static final int TRACKING_DURATION = 90;
    public static final int TOTAL_DURATION = 100;

    public FulminationSigilEntity(
            EntityType<? extends FulminationSigilEntity> type,
            Level level
    ) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TARGET_ID, -1);
    }

    public void setTarget(LivingEntity target) {
        this.entityData.set(TARGET_ID, target.getId());
        updatePositionToTarget(target);
    }

    public int getTargetId() {
        return this.entityData.get(TARGET_ID);
    }

    private void updatePositionToTarget(LivingEntity target) {
        Vec3 targetPosition = new Vec3(
                target.getX(),
                target.getBoundingBox().minY + 0.02,
                target.getZ()
        );

        this.setPos(targetPosition);
    }

    private void playSigilSound(
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                sound,
                SoundSource.HOSTILE,
                volume,
                pitch
        );
    }

    private void detonate() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            net.minecraft.world.entity.LightningBolt lightning =
                    EntityType.LIGHTNING_BOLT.create(serverLevel);

            if (lightning != null) {
                double angle = i * (Math.PI * 2.0 / 3.0);
                double radius = 1;

                lightning.moveTo(
                        this.getX() + Math.cos(angle) * radius,
                        this.getY(),
                        this.getZ() + Math.sin(angle) * radius
                );

                lightning.setVisualOnly(true);
                serverLevel.addFreshEntity(lightning);
            }
        }

        serverLevel.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.TRIDENT_THUNDER,
                SoundSource.WEATHER,
                5.0F,
                1.0F
        );

        serverLevel.explode(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                4.5F,
                Level.ExplosionInteraction.NONE
        );
    }

    private void spawnChargingParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double centerX = this.getX();
        double centerZ = this.getZ();
        double baseY = this.getY() + 0.15;

        for (int stream = 0; stream < 3; stream++) {
            double angle =
                    this.tickCount * 0.35 + stream * (Math.PI * 2.0 / 3.0);

            double radius = 0.75;

            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;

            for (int height = 0; height <= 64; height++) {
                serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        x,
                        baseY + height,
                        z,
                        1,
                        0.0, 0.0, 0.0,
                        0.0
                );
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.tickCount <= TRACKING_DURATION && getTargetId() != -1) {
            Entity target = this.level().getEntity(getTargetId());

            if (!(target instanceof LivingEntity livingTarget)
                    || !livingTarget.isAlive()) {
                this.discard();
                return;
            }

            updatePositionToTarget(livingTarget);
        }

        if (this.tickCount == 1) {
            playSigilSound(
                    SoundEvents.BEACON_ACTIVATE,
                    10.0F,
                    1.0F
            );
        }

        if (this.tickCount < TRACKING_DURATION
                && this.tickCount % 40 == 0) {
            playSigilSound(
                    SoundEvents.BEACON_AMBIENT,
                    3.0F,
                    1.25F
            );
            for (int i = 0; i < 5; i++) {
                playSigilSound(
                    SoundEvents.CAMPFIRE_CRACKLE,
                    5.0F,
                        0.9F + random.nextFloat() * 0.5F
                );
            }
        }

        if (this.tickCount == TRACKING_DURATION) {
            for (int i = 0; i < 3; i++) {
                playSigilSound(
                        SoundEvents.GUARDIAN_ATTACK,
                        5.0F,
                        1.25F
                );
                playSigilSound(
                        SoundEvents.SQUID_SQUIRT,
                        5.0F,
                        1.25F
                );
            }
        }

        if (this.tickCount <= TRACKING_DURATION) {
            spawnChargingParticles();
        }

        if (this.tickCount >= TOTAL_DURATION) {
            detonate();
            this.discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(
                TARGET_ID,
                tag.contains("TargetId") ? tag.getInt("TargetId") : -1
        );
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TargetId", getTargetId());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
