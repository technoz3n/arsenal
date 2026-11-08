package xyz.technoz3n.aresenal.enchantment;

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

    private static final double[] RANGE = {0.0, 10.0, 14.0, 18.0};

    private static final double[] TURN_BLEND = {0.0, 0.20, 0.32, 0.45};

    private static final double ACQUIRE_CONE_DOT = 0.7;

    private static final int SEARCH_INTERVAL = 4;

    private static final double LOCK_RANGE_MULTIPLIER = 1.5;

    private static final double MIN_SPEED_SQR = 0.25;

    private TridentHoming() {
    }


    public static int steer(ThrownTrident trident, int level, int lockedId) {
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

        if (target == null && trident.tickCount % SEARCH_INTERVAL == 0) {
            target = acquire(trident, direction, range);
        }

        if (target == null) {
            return -1;
        }

        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(trident.position());
        double distance = toTarget.length();

        if (distance < 1.0E-4) {
            return target.getId();
        }

        Vec3 desired = toTarget.scale(1.0 / distance);

        Vec3 newDirection = direction
                .add(desired.subtract(direction).scale(TURN_BLEND[level]))
                .normalize();

        trident.setDeltaMovement(newDirection.scale(speed));

        return target.getId();
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

        Vec3 to = target.getBoundingBox().getCenter().subtract(trident.position());
        double distance = to.length();

        if (distance > range * LOCK_RANGE_MULTIPLIER) {
            return null;
        }

        if (distance > 1.0E-4 && direction.dot(to.scale(1.0 / distance)) < 0.0) {
            return null;
        }

        if (trident.tickCount % SEARCH_INTERVAL == 0 && !hasLineOfSight(trident, target)) {
            return null;
        }

        return target;
    }

    private static LivingEntity acquire(ThrownTrident trident, Vec3 direction, double range) {
        Vec3 position = trident.position();
        AABB area = trident.getBoundingBox().inflate(range);

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity candidate : trident.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                e -> isValidTarget(trident, e)
        )) {
            Vec3 to = candidate.getBoundingBox().getCenter().subtract(position);
            double distance = to.length();

            if (distance > range || distance < 1.0E-4) {
                continue;
            }

            double dot = direction.dot(to.scale(1.0 / distance));

            if (dot < ACQUIRE_CONE_DOT) {
                continue;
            }

            if (!hasLineOfSight(trident, candidate)) {
                continue;
            }

            double score = distance * (2.0 - dot);

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
        Vec3 to = target.getBoundingBox().getCenter();

        return trident.level().clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                trident
        )).getType() == HitResult.Type.MISS;
    }
}