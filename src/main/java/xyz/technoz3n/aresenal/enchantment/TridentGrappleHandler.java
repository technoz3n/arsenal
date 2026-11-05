
package xyz.technoz3n.aresenal.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import xyz.technoz3n.aresenal.ArsenalMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = ArsenalMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class TridentGrappleHandler {

    private static final Map<UUID, GrappleState> ACTIVE_GRAPPLES =
            new HashMap<>();

    private static final double ARRIVAL_DISTANCE = 1.5;
    private static final double BASE_PULL_STRENGTH = 0.12;
    private static final double LEVEL_PULL_BONUS = 0.08;

    private static class GrappleState {

        private final ThrownTrident trident;
        private final Entity anchorEntity;
        private final Vec3 blockAnchor;
        private final Vec3 entityOffset;
        private final int level;

        private GrappleState(
                ThrownTrident trident,
                Entity anchorEntity,
                Vec3 blockAnchor,
                Vec3 entityOffset,
                int level
        ) {
            this.trident = trident;
            this.anchorEntity = anchorEntity;
            this.blockAnchor = blockAnchor;
            this.entityOffset = entityOffset;
            this.level = level;
        }

        private Vec3 getAnchorPosition() {
            if (anchorEntity != null) {
                return anchorEntity.position().add(entityOffset);
            }

            return blockAnchor;
        }
    }

    public static void startBlockGrapple(
            ServerPlayer player,
            ThrownTrident trident,
            Vec3 anchor,
            int level
    ) {
        ACTIVE_GRAPPLES.put(
                player.getUUID(),
                new GrappleState(
                        trident,
                        null,
                        anchor,
                        Vec3.ZERO,
                        level
                )
        );
    }

    public static void startEntityGrapple(
            ServerPlayer player,
            ThrownTrident trident,
            Entity target,
            Vec3 impactPosition,
            int level
    ) {
        Vec3 entityOffset = impactPosition.subtract(target.position());

        ACTIVE_GRAPPLES.put(
                player.getUUID(),
                new GrappleState(
                        trident,
                        target,
                        null,
                        entityOffset,
                        level
                )
        );
    }

    public static void stopGrapple(ServerPlayer player) {
        ACTIVE_GRAPPLES.remove(player.getUUID());
    }

    public static boolean isGrappling(ServerPlayer player) {
        return ACTIVE_GRAPPLES.containsKey(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        GrappleState state = ACTIVE_GRAPPLES.get(player.getUUID());

        if (state == null) {
            return;
        }

        if (!player.isAlive()
                || state.trident.isRemoved()
                || state.trident.level() != player.level()) {
            stopGrapple(player);
            return;
        }

        if (state.anchorEntity != null
                && (!state.anchorEntity.isAlive()
                || state.anchorEntity.level() != player.level())) {
            stopGrapple(player);
            return;
        }

        Vec3 anchor = state.getAnchorPosition();
        state.trident.setPos(anchor.x, anchor.y, anchor.z);

        Vec3 playerCenter = player.position().add(
                0,
                player.getBbHeight() * 0.5,
                0
        );

        Vec3 difference = anchor.subtract(playerCenter);
        double distance = difference.length();

        if (distance <= ARRIVAL_DISTANCE) {
            stopGrapple(player);
            return;
        }

        Vec3 direction = difference.normalize();

        int level = Math.max(1, Math.min(3, state.level));

        double pullStrength = BASE_PULL_STRENGTH
                + LEVEL_PULL_BONUS * (level - 1);

        Vec3 newVelocity = player.getDeltaMovement()
                .add(direction.scale(pullStrength));

        player.setDeltaMovement(newVelocity);
        player.hurtMarked = true;
    }
}
