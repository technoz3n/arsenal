package xyz.technoz3n.aresenal.enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import xyz.technoz3n.aresenal.ArsenalMod;

@Mod.EventBusSubscriber(
        modid = ArsenalMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class TridentGrappleHandler {

    private static final Map<UUID, GrappleState> ACTIVE_GRAPPLES =
            new HashMap<>();

    private static final double ARRIVAL_DISTANCE = 1.5;

    private static final int GRAPPLE_TIMEOUT_TICKS = 20 * 10;

    private static final double BASE_MAX_SPEED = 1.15;
    private static final double LEVEL_SPEED_BONUS = 0.15;

    private static final double BASE_STEERING = 0.20;
    private static final double LEVEL_STEERING_BONUS = 0.05;

    private static final double SLOWDOWN_RADIUS = 5.0;
    private static final double MIN_SPEED_FRACTION = 0.35;

    private static double maxSpeedFor(int level) {
        return BASE_MAX_SPEED + LEVEL_SPEED_BONUS * (level - 1);
    }

    private static double steeringFor(int level) {
        return BASE_STEERING + LEVEL_STEERING_BONUS * (level - 1);
    }

    private static final class GrappleState {

        private final ThrownTrident trident;
        private final Entity anchorEntity;
        private final Vec3 anchor;
        private final int level;
        private final long startTick;

        private Vec3 velocity;

        private GrappleState(
                ServerPlayer player,
                ThrownTrident trident,
                Entity anchorEntity,
                Vec3 anchor,
                int level
        ) {
            this.trident = trident;
            this.anchorEntity = anchorEntity;
            this.anchor = anchor;
            this.level = Mth.clamp(level, 1, 3);
            this.startTick = player.level().getGameTime();

            double maxSpeed = maxSpeedFor(this.level);
            Vec3 momentum = new Vec3(
                    player.getX() - player.xo,
                    player.getY() - player.yo,
                    player.getZ() - player.zo
            );

            this.velocity = momentum.lengthSqr() > maxSpeed * maxSpeed
                    ? momentum.normalize().scale(maxSpeed)
                    : momentum;
        }

        private Vec3 getAnchorPosition() {
            if (anchorEntity != null) {
                return anchorEntity.position().add(anchor);
            }

            return anchor;
        }
    }

    public static void startBlockGrapple(
            ServerPlayer player,
            ThrownTrident trident,
            Vec3 anchor,
            int level
    ) {
        register(player, new GrappleState(player, trident, null, anchor, level));
    }

    public static void startEntityGrapple(
            ServerPlayer player,
            ThrownTrident trident,
            Entity target,
            Vec3 offset,
            int level
    ) {
        register(player, new GrappleState(player, trident, target, offset, level));
    }

    private static void register(ServerPlayer player, GrappleState state) {
        GrappleState old = ACTIVE_GRAPPLES.put(player.getUUID(), state);

        if (old != null && old.trident != state.trident) {
            releaseTrident(old.trident);
        }
    }

    public static void stopGrapple(ServerPlayer player) {
        GrappleState state = ACTIVE_GRAPPLES.remove(player.getUUID());

        if (state != null) {
            releaseTrident(state.trident);
        }
    }

    public static boolean isGrappling(ServerPlayer player) {
        return ACTIVE_GRAPPLES.containsKey(player.getUUID());
    }

    private static void releaseTrident(ThrownTrident trident) {
        if (!trident.isRemoved() && trident instanceof GrappleTrident grapple) {
            grapple.arsenal$release();
        }
    }


    private static void refundAndDiscard(ServerPlayer player) {
        GrappleState state = ACTIVE_GRAPPLES.remove(player.getUUID());

        if (state == null || state.trident.isRemoved()) {
            return;
        }

        ThrownTrident trident = state.trident;

        if (trident.pickup == AbstractArrow.Pickup.ALLOWED
                && trident instanceof GrappleTrident grapple) {

            ItemStack stack = grapple.arsenal$copyStack();

            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }

        trident.discard();
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

        ThrownTrident trident = state.trident;

        if (trident.isRemoved()) {
            ACTIVE_GRAPPLES.remove(player.getUUID());
            return;
        }

        if (trident.level() != player.level()) {
            refundAndDiscard(player);
            return;
        }

        if (!player.isAlive()
                || player.isSpectator()
                || player.isPassenger()) {

            stopGrapple(player);
            return;
        }

        if (player.level().getGameTime() - state.startTick >= GRAPPLE_TIMEOUT_TICKS) {
            stopGrapple(player);
            return;
        }

        if (state.anchorEntity != null
                && (!state.anchorEntity.isAlive()
                || state.anchorEntity.level() != player.level())) {

            stopGrapple(player);
            return;
        }

        Vec3 playerCenter = player.position().add(
                0,
                player.getBbHeight() * 0.5,
                0
        );

        Vec3 toAnchor = state.getAnchorPosition().subtract(playerCenter);
        double distance = toAnchor.length();

        if (distance <= ARRIVAL_DISTANCE) {
            stopGrapple(player);
            return;
        }

        Vec3 direction = toAnchor.scale(1.0 / distance);

        double maxSpeed = maxSpeedFor(state.level);
        double steering = steeringFor(state.level);

        double slowdown = Mth.clamp(
                (distance - ARRIVAL_DISTANCE) / SLOWDOWN_RADIUS,
                MIN_SPEED_FRACTION,
                1.0
        );

        Vec3 desired = direction.scale(maxSpeed * slowdown);

        state.velocity = state.velocity.add(
                desired.subtract(state.velocity).scale(steering)
        );

        player.setDeltaMovement(state.velocity);
        player.hurtMarked = true;
        player.resetFallDistance();
    }


    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refundAndDiscard(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(
            PlayerEvent.PlayerChangedDimensionEvent event
    ) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refundAndDiscard(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refundAndDiscard(player);
        }
    }
}