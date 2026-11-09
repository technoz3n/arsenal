package xyz.technoz3n.aresenal.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.technoz3n.aresenal.item.weapon.RapierItem;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private static final double CONTACT_INFLATE = 0.1;

    @Unique
    private static final String FULMINATION_COOLDOWN_KEY =
            "arsenal_fulmination_cooldown_until";

    @Unique
    private static final Map<UUID, Integer> LAST_FULMINATION_HUD =
            new HashMap<>();

    @Shadow
    protected int autoSpinAttackTicks;

    @Shadow
    protected abstract void setLivingEntityFlag(int flag, boolean value);

    @Inject(
            method = "checkAutoSpinAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void arsenal$rapierSpin(
            AABB before,
            AABB after,
            CallbackInfo ci
    ) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!RapierItem.usesRapierSpin(self)) {
            return;
        }

        int age = RapierItem.SPIN_TICKS - this.autoSpinAttackTicks;

        if (age >= RapierItem.CONTACT_GRACE_TICKS
                && this.autoSpinAttackTicks > RapierItem.WALL_HIT_SPIN_TICKS
                && arsenal$touchingBlock(self)) {

            this.autoSpinAttackTicks = RapierItem.WALL_HIT_SPIN_TICKS;
        }

        if (!self.level().isClientSide && this.autoSpinAttackTicks <= 0) {
            this.setLivingEntityFlag(4, false);
        }

        ci.cancel();
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void arsenal$fulminationHud(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(self instanceof Player player)) {
            return;
        }

        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long currentTime = serverLevel.getServer()
                .overworld()
                .getGameTime();

        long cooldownUntil = player.getPersistentData()
                .getLong(FULMINATION_COOLDOWN_KEY);

        long remaining = cooldownUntil - currentTime;

        UUID playerId = player.getUUID();
        Integer last = LAST_FULMINATION_HUD.get(playerId);

        if (remaining <= 0) {
            if (last != null && last != -1) {
                player.displayClientMessage(
                        Component.literal("Fulmination: Ready"),
                        true
                );

                LAST_FULMINATION_HUD.put(playerId, -1);
            }

            return;
        }

        int tenthsLeft = (int) Math.ceil(remaining / 2.0);

        if (last != null && last == tenthsLeft) {
            return;
        }

        LAST_FULMINATION_HUD.put(playerId, tenthsLeft);

        float secondsLeft = tenthsLeft / 10.0F;

        player.displayClientMessage(
                Component.literal(
                        String.format(
                                "Fulmination: Recharging (%.1fs)",
                                secondsLeft
                        )
                ),
                true
        );
    }

    @Unique
    private static boolean arsenal$touchingBlock(LivingEntity entity) {
        AABB box = entity.getBoundingBox().inflate(CONTACT_INFLATE);

        return entity.level().getBlockCollisions(entity, box).iterator().hasNext();
    }
}