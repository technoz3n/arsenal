package xyz.technoz3n.aresenal.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import xyz.technoz3n.aresenal.ArsenalMod;

@Mod.EventBusSubscriber(
        modid = ArsenalMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class TridentGrappleImpactHandler {

    private static final String GRAPPLE_LEVEL_KEY =
            "arsenal_grapple_level";

    private static final float IMPACT_DAMAGE = 8.0F;

    private static final double MAX_GRAPPLE_DISTANCE = 32.0;

    private static final double BLOCK_SURFACE_OFFSET = 0.05;

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {

        if (!(event.getProjectile() instanceof ThrownTrident trident)) {
            return;
        }

        if (trident.level().isClientSide) {
            return;
        }

        if (!(trident.getOwner() instanceof ServerPlayer player)) {
            return;
        }

        if (!(trident instanceof GrappleTrident grapple)) {
            return;
        }

        int grappleLevel = trident.getPersistentData()
                .getInt(GRAPPLE_LEVEL_KEY);

        if (grappleLevel <= 0) {
            return;
        }

        if (grapple.arsenal$isAttached()) {
            event.cancel();
            return;
        }

        HitResult hitResult = event.getRayTraceResult();

        if (hitResult.getType() == HitResult.Type.MISS) {
            return;
        }

        if (!(hitResult instanceof BlockHitResult)
                && !(hitResult instanceof EntityHitResult)) {
            return;
        }

        if (hitResult instanceof EntityHitResult entityHit) {

            Entity target = entityHit.getEntity();

            if (target == player || !target.isAlive()) {
                return;
            }
        }

        Vec3 impactPosition = hitResult.getLocation();

        Vec3 playerCenter = player.position().add(
                0,
                player.getBbHeight() * 0.5,
                0
        );

        if (playerCenter.distanceTo(impactPosition) > MAX_GRAPPLE_DISTANCE) {
            return;
        }

        Vec3 velocity = trident.getDeltaMovement();

        Vec3 direction = velocity.lengthSqr() > 1.0E-8
                ? velocity.normalize()
                : trident.getLookAngle();

        float yaw = (float) (Mth.atan2(direction.x, direction.z)
                * (180.0D / Math.PI));

        float pitch = (float) (Mth.atan2(direction.y, direction.horizontalDistance())
                * (180.0D / Math.PI));

        event.cancel();

        trident.setDeltaMovement(Vec3.ZERO);

        if (hitResult instanceof BlockHitResult) {

            Vec3 anchor = impactPosition.subtract(
                    direction.scale(BLOCK_SURFACE_OFFSET)
            );

            grapple.arsenal$attachToBlock(anchor, yaw, pitch);

            TridentGrappleHandler.startBlockGrapple(
                    player,
                    trident,
                    anchor,
                    grappleLevel
            );

        } else if (hitResult instanceof EntityHitResult entityHit) {

            Entity target = entityHit.getEntity();

            Vec3 offset = impactPosition.subtract(target.position());

            target.hurt(
                    trident.damageSources().trident(trident, player),
                    IMPACT_DAMAGE
            );

            if (!target.isAlive()) {
                trident.setPos(impactPosition);
                grapple.arsenal$release();
                return;
            }

            grapple.arsenal$attachToEntity(target, offset, yaw, pitch);

            TridentGrappleHandler.startEntityGrapple(
                    player,
                    trident,
                    target,
                    offset,
                    grappleLevel
            );
        }
    }
}