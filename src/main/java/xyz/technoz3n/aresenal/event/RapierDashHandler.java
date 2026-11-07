package xyz.technoz3n.aresenal.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import xyz.technoz3n.aresenal.ArsenalMod;
import xyz.technoz3n.aresenal.item.weapon.RapierItem;

@Mod.EventBusSubscriber(modid = ArsenalMod.MOD_ID)
public class RapierDashHandler {

    private static final int MAX_PIERCE_TARGETS = 5;

    private static final int MAX_REFUNDS_PER_DASH = 1;

    private static final double HIT_MARGIN = 0.3;

    private static final double KNOCKBACK = 0.8;

    private static final double TRAIL_SPACING = 0.6;
    private static final int MAX_TRAIL_POINTS = 16;
    private static final double MIN_SLAM_VELOCITY = 1.2;
    private static final float SLAM_EXPLOSION_RADIUS = 4.5F;

    private static final class ActiveDash {
        private final InteractionHand hand;
        private final Vec3 direction;
        private final float damageScale;
        private final Set<UUID> alreadyHit = new HashSet<>();

        private int ticksLeft;
        private int slamTicksLeft = 30;
        private int refunds;

        private boolean attackActive = true;
        private Vec3 lastPos;
        private double preMoveY;
        private boolean wasOnGround;


        private ActiveDash(
                InteractionHand hand,
                Vec3 direction,
                float damageScale,
                int ticksLeft,
                Vec3 startPos,
                boolean wasOnGround
        ) {
            this.hand = hand;
            this.direction = direction;
            this.damageScale = damageScale;
            this.ticksLeft = ticksLeft;
            this.lastPos = startPos;
            this.wasOnGround = wasOnGround;
        }
    }

    private static final Map<UUID, ActiveDash> ACTIVE_DASHES = new HashMap<>();

    public static void beginDash(
            Player player,
            InteractionHand hand,
            Vec3 direction,
            float damageScale,
            int durationTicks
    ) {
        ACTIVE_DASHES.put(
                player.getUUID(),
                new ActiveDash(hand, direction, damageScale, durationTicks + 2, player.position(), player.onGround())
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase == TickEvent.Phase.START) {
            if (event.player instanceof ServerPlayer player) {
                ActiveDash dash = ACTIVE_DASHES.get(player.getUUID());

                if (dash != null) {
                    dash.preMoveY = player.getDeltaMovement().y;
                }
            }

            return;
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        ActiveDash dash = ACTIVE_DASHES.get(player.getUUID());

        if (dash == null) {
            return;
        }

        if (!player.isAlive()) {
            ACTIVE_DASHES.remove(player.getUUID());
            return;
        }

        ServerLevel level = player.serverLevel();

        boolean landed = !dash.wasOnGround && player.onGround();

        if (landed && dash.preMoveY <= -MIN_SLAM_VELOCITY) {
            level.explode(
                    player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SLAM_EXPLOSION_RADIUS,
                    Level.ExplosionInteraction.MOB
            );

            ACTIVE_DASHES.remove(player.getUUID());
            return;
        }

        dash.wasOnGround = player.onGround();

        if (!dash.attackActive) {
            dash.slamTicksLeft--;

            if (dash.slamTicksLeft <= 0) {
                ACTIVE_DASHES.remove(player.getUUID());
            }

            return;
        }

        Vec3 now = player.position();
        double halfHeight = player.getBbHeight() * 0.5;

        spawnTrail(
                level,
                dash.lastPos.add(0, halfHeight, 0),
                now.add(0, halfHeight, 0)
        );

        AABB box = player.getBoundingBox();
        AABB sweep = box
                .minmax(box.move(dash.lastPos.subtract(now)))
                .inflate(HIT_MARGIN);

        Vec3 from = dash.lastPos;

        List<LivingEntity> targets = new ArrayList<>(
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        sweep,
                        e -> e != player
                                && e.isAlive()
                                && !e.isSpectator()
                                && e.isAttackable()
                                && !player.isAlliedTo(e)
                )
        );

        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(from)));

        for (LivingEntity target : targets) {

            if (dash.alreadyHit.size() >= MAX_PIERCE_TARGETS) {
                break;
            }

            if (dash.alreadyHit.add(target.getUUID())) {
                hit(level, player, dash, target);
            }
        }

        dash.lastPos = now;
        dash.ticksLeft--;

        boolean pierceCapReached =
                dash.alreadyHit.size() >= MAX_PIERCE_TARGETS;

        if (pierceCapReached || dash.ticksLeft <= 0 || !player.isAutoSpinAttack()) {
            dash.attackActive = false;
            dash.slamTicksLeft = 30;
        }
    }

    private static void hit(
            ServerLevel level,
            ServerPlayer player,
            ActiveDash dash,
            LivingEntity target
    ) {
        ItemStack stack = player.getItemInHand(dash.hand);

        float base = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float enchantBonus = EnchantmentHelper.getDamageBonus(stack, target.getMobType());
        float damage = (base + enchantBonus) * dash.damageScale;

        boolean hurt = target.hurt(player.damageSources().playerAttack(player), damage);

        if (!hurt) {
            return;
        }

        target.knockback(KNOCKBACK, -dash.direction.x, -dash.direction.z);

        int fireAspect = EnchantmentHelper.getFireAspect(player);

        if (fireAspect > 0) {
            target.setSecondsOnFire(fireAspect * 4);
        }

        EnchantmentHelper.doPostHurtEffects(target, player);
        EnchantmentHelper.doPostDamageEffects(player, target);

        level.playSound(
                null,
                target.blockPosition(),
                SoundEvents.PLAYER_ATTACK_STRONG,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        level.sendParticles(
                ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY(0.5), target.getZ(),
                1, 0.0, 0.0, 0.0, 0.0
        );

        level.sendParticles(
                ParticleTypes.CRIT,
                target.getX(), target.getY(0.5), target.getZ(),
                10, 0.3, 0.3, 0.3, 0.2
        );

        boolean holdingRapier = stack.getItem() instanceof RapierItem;

        if (target.isDeadOrDying()
                && holdingRapier
                && dash.refunds < MAX_REFUNDS_PER_DASH
                && RapierItem.refundCharge(stack)) {

            dash.refunds++;

            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.7F,
                    1.4F
            );
        }

        if (holdingRapier) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(dash.hand));
        }
    }

    private static void spawnTrail(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();

        if (length < 1.0E-3) {
            return;
        }

        int points = Mth_clamp((int) Math.ceil(length / TRAIL_SPACING), 1, MAX_TRAIL_POINTS);
        Vec3 step = delta.scale(1.0 / points);
        Vec3 pos = from;

        for (int i = 0; i < points; i++) {
            pos = pos.add(step);

            level.sendParticles(
                    ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    1, 0.10, 0.10, 0.10, 0.0
            );

            level.sendParticles(
                    ParticleTypes.SONIC_BOOM,
                    pos.x, pos.y, pos.z,
                    2, 0.15, 0.15, 0.15, 0.05
            );
        }
    }

    private static int Mth_clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();

        ACTIVE_DASHES.remove(id);
        RapierItem.clearHud(id);
    }
}