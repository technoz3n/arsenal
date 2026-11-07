package xyz.technoz3n.aresenal.item.weapon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import xyz.technoz3n.aresenal.event.RapierDashHandler;

public class RapierItem extends SwordItem {

    private static final int MAX_CHARGES = 3;
    private static final int RECHARGE_TICKS = 30;
    private static final int DEPLETED_LOCKOUT_TICKS = 20;
    private static final int THROW_THRESHOLD_TICKS = 10;
    private static final int USE_DURATION = 72000;
    private static final double DASH_STRENGTH = 3;
    private static final int DASH_ATTACK_TICKS = 8;
    private static final double MIN_DASH_STRENGTH = 0.8;
    private static final int MAX_CHARGE_TICKS = 30;

    private static final float MIN_DAMAGE_SCALE = 0.75F;
    private static final float MAX_DAMAGE_SCALE = 1.5F;

    private static final double GROUND_LIFT = 0.3;

    private static final String TAG_CHARGES = "hnsRapierCharges";
    private static final String TAG_NEXT_REGEN = "hnsRapierNextRegen";
    private static final String TAG_LOCKOUT_UNTIL = "hnsRapierLockoutUntil";

    private static final Map<UUID, Integer> LAST_HUD = new HashMap<>();

    public RapierItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    public static boolean usesRapierSpin(LivingEntity entity) {
        boolean rapier = entity.getMainHandItem().getItem() instanceof RapierItem
                || entity.getOffhandItem().getItem() instanceof RapierItem;

        boolean trident = entity.getMainHandItem().getItem() instanceof TridentItem
                || entity.getOffhandItem().getItem() instanceof TridentItem;

        return rapier && !trident;
    }

    public static void clearHud(UUID playerId) {
        LAST_HUD.remove(playerId);
    }

    private static int getCharges(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(TAG_CHARGES)) {
            return MAX_CHARGES;
        }

        return tag.getInt(TAG_CHARGES);
    }

    private static void spendCharge(ItemStack stack, long gameTime) {
        CompoundTag tag = stack.getOrCreateTag();
        int charges = Math.max(0, getCharges(stack) - 1);

        tag.putInt(TAG_CHARGES, charges);

        if (charges <= 0) {
            tag.putLong(TAG_LOCKOUT_UNTIL, gameTime + DEPLETED_LOCKOUT_TICKS);
        }

        tag.putLong(TAG_NEXT_REGEN, gameTime + RECHARGE_TICKS);
    }

    public static boolean refundCharge(ItemStack stack) {
        int charges = getCharges(stack);

        if (charges >= MAX_CHARGES) {
            return false;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_CHARGES, charges + 1);
        tag.putLong(TAG_LOCKOUT_UNTIL, 0L);
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (level.isClientSide()) {
            return;
        }

        long gameTime = level.getGameTime();
        CompoundTag tag = stack.getTag();

        int charges = getCharges(stack);
        long lockoutUntil = tag != null ? tag.getLong(TAG_LOCKOUT_UNTIL) : 0L;

        if (tag != null && charges < MAX_CHARGES && gameTime >= lockoutUntil) {
            long nextRegen = tag.contains(TAG_NEXT_REGEN)
                    ? tag.getLong(TAG_NEXT_REGEN)
                    : gameTime + RECHARGE_TICKS;

            if (gameTime >= nextRegen) {
                charges = Math.min(MAX_CHARGES, charges + 1);
                tag.putInt(TAG_CHARGES, charges);
                tag.putLong(TAG_NEXT_REGEN, gameTime + RECHARGE_TICKS);
            } else if (!tag.contains(TAG_NEXT_REGEN)) {
                tag.putLong(TAG_NEXT_REGEN, nextRegen);
            }
        }

        if (selected && entity instanceof Player player) {
            long lockoutLeft = Math.max(0L, lockoutUntil - gameTime);
            int key = charges * 10000 + (int) lockoutLeft;

            Integer last = LAST_HUD.get(player.getUUID());

            if (last == null || last != key || gameTime % 20 == 0) {
                LAST_HUD.put(player.getUUID(), key);

                String pips = "●".repeat(charges) + "○".repeat(MAX_CHARGES - charges);

                MutableComponent message;
                if (lockoutLeft > 0) {
                    message = Component.literal(
                            pips + String.format(" (recharging in %.1fs)", lockoutLeft / 20.0F)
                    );
                } else {
                    message = Component.literal(pips);
                }

                player.displayClientMessage(message, true);
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (getCharges(stack) <= 0) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        int used = getUseDuration(stack) - remainingUseDuration;

        if (used == MAX_CHARGE_TICKS && entity instanceof Player player) {
            level.playSound(
                    player,
                    player.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.4F
            );
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int ticksCharged = getUseDuration(stack) - timeLeft;

        if (ticksCharged < THROW_THRESHOLD_TICKS) {
            return;
        }

        if (getCharges(stack) <= 0) {
            return;
        }

        int cappedTicks = Math.min(ticksCharged, MAX_CHARGE_TICKS);
        float chargeRatio = (cappedTicks - THROW_THRESHOLD_TICKS)
                / (float) (MAX_CHARGE_TICKS - THROW_THRESHOLD_TICKS);

        double strength = MIN_DASH_STRENGTH
                + (DASH_STRENGTH - MIN_DASH_STRENGTH) * chargeRatio;

        Vec3 direction = player.getLookAngle().normalize();

        if (!level.isClientSide()) {
            spendCharge(stack, level.getGameTime());

            float damageScale = MIN_DAMAGE_SCALE
                    + (MAX_DAMAGE_SCALE - MIN_DAMAGE_SCALE) * chargeRatio;

            RapierDashHandler.beginDash(
                    player,
                    player.getUsedItemHand(),
                    direction,
                    damageScale,
                    DASH_ATTACK_TICKS
            );

            SoundEvent riptide = chargeRatio < 0.34F
                    ? SoundEvents.TRIDENT_RIPTIDE_1
                    : chargeRatio < 0.67F
                    ? SoundEvents.TRIDENT_RIPTIDE_2
                    : SoundEvents.TRIDENT_RIPTIDE_3;

            level.playSound(null, player.blockPosition(), riptide, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        player.getX(), player.getY() + 0.2, player.getZ(),
                        12, 0.3, 0.1, 0.3, 0.05
                );
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        12, 0.3, 0.3, 0.3, 0.1
                );
            }
        }

        if (player.onGround()) {
            player.move(MoverType.SELF, new Vec3(0.0, GROUND_LIFT, 0.0));
        }

        Vec3 dash = direction.scale(strength);
        player.push(dash.x, dash.y, dash.z);

        player.startAutoSpinAttack(DASH_ATTACK_TICKS);
    }
}