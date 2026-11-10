package xyz.technoz3n.aresenal.enchantment;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TridentHoming {

    private static final double[] RANGE = {0.0, 16.0, 24.0, 32.0};

    private static final double[] MAX_TURN_DEGREES = {0.0, 14.0, 20.0, 28.0};

    private static final double[] CONE_DOT = {0.0, 0.60, 0.50, 0.40};

    private static final double LANE_INFLATE = 1.0;

    private static final double CLOSE_RADIUS = 8.0;
    private static final double CLOSE_TURN_MULTIPLIER = 2.0;

    private static final int TURN_RAMP_TICKS = 4;

    private static final double LEAD_FACTOR = 0.85;
    private static final double MAX_LEAD_TICKS = 12.0;

    private static final double LOCK_RANGE_MULTIPLIER = 1.5;
    private static final int LOS_RECHECK_INTERVAL = 4;
    private static final double MIN_SPEED_SQR = 0.25;

    private static final boolean LOCK_PARTICLES = true;

    private TridentHoming() {
    }

    public static int steer(ThrownTrident trident, int level, int lockedId, int lockTicks) {
        level = Mth.clamp(level, 1, 3);

        Vec3 velocity = trident.getDeltaMovement();
        double speedSqr = velocity.lengthSqr();

        if (speedSqr < MIN_SPEED_SQR) {
            return -1;
        }

        double speed = Math.sqrt(speedSqr);
        Vec3 direction = velocity.scale(1.0 / speed);
        double range = RANGE[level];

        LivingEntity target = resolveLock(trident, lockedId, direction, range);

        if (target == null) {
            target = acquire(trident, direction, range, level);
        }

        if (target == null) {
            return -1;
        }

        Vec3 aim = aimPoint(trident, target, speed);
        Vec3 toAim = aim.subtract(trident.position());
        double distance = toAim.length();

        if (distance < 1.0E-4) {
            return target.getId();
        }

        Vec3 desired = toAim.scale(1.0 / distance);

        double maxTurn = Math.toRadians(MAX_TURN_DEGREES[level]);
        maxTurn *= Math.min(1.0, (lockTicks + 1) / (double) TURN_RAMP_TICKS);

        if (distance < CLOSE_RADIUS) {
            maxTurn *= CLOSE_TURN_MULTIPLIER;
        }

        Vec3 newDirection = rotateToward(direction, desired, maxTurn);

        trident.setDeltaMovement(newDirection.scale(speed));

        if (LOCK_PARTICLES && trident.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    trident.getX(), trident.getY(), trident.getZ(),
                    1, 0.05, 0.05, 0.05, 0.0
            );
        }

        return target.getId();
    }

    private static Vec3 rotateToward(Vec3 from, Vec3 to, double maxAngle) {
        double dot = Mth.clamp(from.dot(to), -1.0, 1.0);
        double angle = Math.acos(dot);

        if (angle <= maxAngle || angle < 1.0E-6) {
            return to;
        }

        double sin = Math.sin(angle);

        if (sin < 1.0E-6) {
            return from;
        }

        double t = maxAngle / angle;

        return from.scale(Math.sin((1.0 - t) * angle) / sin)
                .add(to.scale(Math.sin(t * angle) / sin))
                .normalize();
    }

    private static Vec3 aimPoint(ThrownTrident trident, LivingEntity target, double speed) {
        Vec3 center = target.getBoundingBox().getCenter();
        double distance = center.distanceTo(trident.position());
        double ticks = Math.min(distance / Math.max(speed, 0.1), MAX_LEAD_TICKS);

        Vec3 movement = new Vec3(
                target.getX() - target.xo,
                (target.getY() - target.yo) * 0.5,
                target.getZ() - target.zo
        );

        return center.add(movement.scale(ticks * LEAD_FACTOR));
    }

    private static Vec3 nearestPoint(AABB box, Vec3 point) {
        return new Vec3(
                Mth.clamp(point.x, box.minX, box.maxX),
                Mth.clamp(point.y, box.minY, box.maxY),
                Mth.clamp(point.z, box.minZ, box.maxZ)
        );
    }

    private static LivingEntity resolveLock(
            ThrownTrident trident,
            int lockedId,
            Vec3 direction,
            double range
    ) {
        if (lockedId < 0) {
            return null;
        }

        Entity entity = trident.level().getEntity(lockedId);

        if (!(entity instanceof LivingEntity target) || !isValidTarget(trident, target)) {
            return null;
        }

        Vec3 position = trident.position();
        AABB box = target.getBoundingBox();
        double distance = nearestPoint(box, position).distanceTo(position);

        if (distance > range * LOCK_RANGE_MULTIPLIER) {
            return null;
        }

        Vec3 toCenter = box.getCenter().subtract(position);
        double centerDistance = toCenter.length();

        if (distance > 0.5
                && centerDistance > 1.0E-4
                && direction.dot(toCenter.scale(1.0 / centerDistance)) < 0.0) {
            return null;
        }

        if (trident.tickCount % LOS_RECHECK_INTERVAL == 0 && !hasLineOfSight(trident, target)) {
            return null;
        }

        return target;
    }

    private static LivingEntity acquire(
            ThrownTrident trident,
            Vec3 direction,
            double range,
            int level
    ) {
        Vec3 position = trident.position();
        Vec3 rayEnd = position.add(direction.scale(range));
        AABB area = trident.getBoundingBox().inflate(range);
        double coneDot = CONE_DOT[level];

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity candidate : trident.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                e -> isValidTarget(trident, e)
        )) {
            AABB box = candidate.getBoundingBox();

            double distance = nearestPoint(box, position).distanceTo(position);

            if (distance > range) {
                continue;
            }

            Vec3 toCenter = box.getCenter().subtract(position);
            double centerDistance = toCenter.length();

            if (centerDistance < 1.0E-4) {
                continue;
            }

            double dot = direction.dot(toCenter.scale(1.0 / centerDistance));

            if (dot < 0.0) {
                continue;
            }

            boolean inPath = box.inflate(LANE_INFLATE).clip(position, rayEnd).isPresent();

            if (dot < coneDot && !inPath) {
                continue;
            }

            if (!hasLineOfSight(trident, candidate)) {
                continue;
            }

            double score = distance * (1.0 + 3.0 * (1.0 - dot));

            if (inPath) {
                score *= 0.5;
            }

            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    private static boolean isValidTarget(ThrownTrident trident, LivingEntity target) {
        Entity owner = trident.getOwner();

        if (target == owner
                || !target.isAlive()
                || target.isSpectator()
                || !target.isAttackable()) {
            return false;
        }

        if (target instanceof Player player && player.isCreative()) {
            return false;
        }

        if (owner != null && target.isAlliedTo(owner)) {
            return false;
        }

        if (target instanceof TamableAnimal pet
                && owner instanceof LivingEntity livingOwner
                && pet.isOwnedBy(livingOwner)) {
            return false;
        }

        return true;
    }

    private static boolean hasLineOfSight(ThrownTrident trident, LivingEntity target) {
        Vec3 from = trident.position();
        AABB box = target.getBoundingBox();

        return isClear(trident, from, box.getCenter())
                || isClear(trident, from, target.getEyePosition())
                || isClear(trident, from, new Vec3(target.getX(), box.minY + 0.25, target.getZ()));
    }

    private static boolean isClear(ThrownTrident trident, Vec3 from, Vec3 to) {
        return trident.level().clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                trident
        )).getType() == HitResult.Type.MISS;
    }
}