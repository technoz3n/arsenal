package xyz.technoz3n.aresenal.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class BedrockWitherBoss extends WitherBoss {
    private WitherPhase phase = WitherPhase.NORMAL;

    private int attackCooldown;
    private int dashCooldown;
    private int transitionTicks;

    private int skullsFired;
    private boolean hasEnteredSecondPhase;

    private Vec3 flightDestination;
    private int repositionCooldown;

    private static final double FLIGHT_SPEED = 0.12D;
    private static final double FLIGHT_DRAG = 0.85D;
    private static final double TARGET_HEIGHT = 5.0D;
    private static final double DESTINATION_RADIUS = 12.0D;

    public final ServerBossEvent bWitherBossEvent = (ServerBossEvent)(new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    )).setCreateWorldFog(false).setDarkenScreen(true);

    public BedrockWitherBoss(EntityType<? extends  WitherBoss> type, Level level) {
        super(type, level);
    }

    public enum WitherPhase {
        NORMAL,
        TRANSITION,
        ARMORED
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WitherBoss.createAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.6D);
    }

    private double getBedrockHeadX(int head) {
        if (head <= 0) {
            return this.getX();
        }

        float angle = (this.yBodyRot + (float)(180 * (head - 1)))
                * (Mth.PI / 180F);

        return this.getX() + Mth.cos(angle) * 1.3D;
    }

    private double getBedrockHeadY(int head) {
        return head <= 0
                ? this.getY() + 3.0D
                : this.getY() + 2.2D;
    }

    private double getBedrockHeadZ(int head) {
        if (head <= 0) {
            return this.getZ();
        }

        float angle = (this.yBodyRot + (float)(180 * (head - 1)))
                * (Mth.PI / 180F);

        return this.getZ() + Mth.sin(angle) * 1.3D;
    }

    private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR =
            entity -> entity.getMobType() != MobType.UNDEAD
                    && entity.attackable();

    @Override
    protected void registerGoals() {
        // this.goalSelector.addGoal(0, new WitherBoss.WitherDoNothingGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2,
                new NearestAttackableTargetGoal<>(
                        this,
                        LivingEntity.class,
                        0,
                        false,
                        false,
                        LIVING_ENTITY_SELECTOR
                )
        );
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource damageSource) {
        return false;
    }

    private void fireBedrockSkull(LivingEntity target, int head, boolean blue) {
        double x = getBedrockHeadX(head);
        double y = getBedrockHeadY(head);
        double z = getBedrockHeadZ(head);

        double dx = target.getX() - x;
        double dy = target.getY(0.5D) - y;
        double dz = target.getZ() - z;

        WitherSkull skull = new WitherSkull(
                this.level(),
                this,
                dx,
                dy,
                dz
        );

        skull.setPos(x, y, z);
        skull.setDangerous(blue);

        this.level().addFreshEntity(skull);

        this.level().playSound(
                null,
                x,
                y,
                z,
                SoundEvents.WITHER_SHOOT,
                SoundSource.HOSTILE,
                10.0F,
                1.0F
        );
    }

    private void tickSkullVolley(LivingEntity target) {
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        switch (skullsFired) {
            case 0 -> fireBedrockSkull(target, 1, false);
            case 1 -> fireBedrockSkull(target, 2, false);
            case 2 -> fireBedrockSkull(target, 0, false);
            case 3 -> fireBedrockSkull(target, 0, true);
        }

        skullsFired++;

        if (skullsFired >= 4) {
            skullsFired = 0;
            attackCooldown = 60;
        } else {
            attackCooldown = 10;
        }
    }

    private void chooseFlightDestination(LivingEntity target) {
        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double distance = 3.0D
                + this.random.nextDouble() * DESTINATION_RADIUS;

        double x = target.getX() + Math.cos(angle) * distance;
        double z = target.getZ() + Math.sin(angle) * distance;
        double y = target.getY() + TARGET_HEIGHT;

        this.flightDestination = new Vec3(x, y, z);
        this.repositionCooldown = 60 + this.random.nextInt(40);
    }

    private void tickFlightMovement(LivingEntity target) {
        if (this.flightDestination == null || this.repositionCooldown <= 0) {
            chooseFlightDestination(target);
        }

        this.repositionCooldown--;

        Vec3 direction = this.flightDestination.subtract(this.position());

        if (direction.lengthSqr() < 4.0D) {
            chooseFlightDestination(target);
            direction = this.flightDestination.subtract(this.position());
        }

        Vec3 velocity = this.getDeltaMovement();

        Vec3 acceleration = direction.normalize().scale(FLIGHT_SPEED);

        this.setDeltaMovement(
                velocity.scale(FLIGHT_DRAG).add(acceleration)
        );
    }

    private void tickNormalPhase() {
        LivingEntity target = this.getTarget();

        if (target == null || !target.isAlive()) {
            this.flightDestination = null;
            return;
        }

        tickFlightMovement(target);
        tickSkullVolley(target);
    }

    @Override
    public void customServerAiStep() {
        if (this.level().isClientSide) return;

        switch (phase) {
            case NORMAL -> tickNormalPhase();
            //case TRANSITION -> tickTransition();
            //case ARMORED -> tickArmoredPhase();
        }

        this.bWitherBossEvent.setProgress(
                this.getHealth() / this.getMaxHealth()
        );
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        this.bWitherBossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        this.bWitherBossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bWitherBossEvent.setName(this.getDisplayName());
    }
}
