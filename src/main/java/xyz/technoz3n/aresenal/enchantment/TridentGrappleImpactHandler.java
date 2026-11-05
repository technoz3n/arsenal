
package xyz.technoz3n.aresenal.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

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

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {

        if (!(event.getProjectile() instanceof ThrownTrident trident)) {
            return;
        }

        if (!(trident.getOwner() instanceof ServerPlayer player)) {
            return;
        }

        if (trident.level().isClientSide) {
            return;
        }

        int grappleLevel = trident.getPersistentData()
                .getInt(GRAPPLE_LEVEL_KEY);

        if (grappleLevel <= 0) {
            return;
        }

        HitResult hitResult = event.getRayTraceResult();

        if (hitResult.getType() == HitResult.Type.MISS) {
            return;
        }

        event.cancel();

        trident.setNoPhysics(true);
        trident.setNoGravity(true);
        trident.setDeltaMovement(0, 0, 0);

        trident.setPos(
                hitResult.getLocation().x,
                hitResult.getLocation().y,
                hitResult.getLocation().z
        );

        if (hitResult instanceof BlockHitResult blockHit) {

            TridentGrappleHandler.startBlockGrapple(
                    player,
                    trident,
                    blockHit.getLocation(),
                    grappleLevel
            );

        } else if (hitResult instanceof EntityHitResult entityHit) {

            Entity target = entityHit.getEntity();
            target.hurt(
                    trident.damageSources().trident(trident, player),
                    8.0F
            );
            TridentGrappleHandler.startEntityGrapple(
                    player,
                    trident,
                    target,
                    entityHit.getLocation(),
                    grappleLevel
            );
        }
    }
}
