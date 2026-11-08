package xyz.technoz3n.aresenal.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Unique
    private static final String FULMINATION_HIT_KEY =
            "arsenal_fulmination_hit";

    @Unique
    private static final String FULMINATION_COOLDOWN_KEY =
            "arsenal_fulmination_cooldown_until";

    @Unique
    private static final long FULMINATION_COOLDOWN_TICKS = 80L;

    @Inject(
            method = "playerTouch(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("TAIL")
    )
    private void arsenal$fulminationCooldown(
            Player player,
            CallbackInfo ci
    ) {
        AbstractArrow self = (AbstractArrow) (Object) this;

        if (!(self instanceof ThrownTrident)) {
            return;
        }

        if (self.level().isClientSide) {
            return;
        }

        if (!self.isRemoved()) {
            return;
        }

        if (!self.getPersistentData().getBoolean(FULMINATION_HIT_KEY)) {
            return;
        }

        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long currentTime = serverLevel.getServer()
                .overworld()
                .getGameTime();

        player.getPersistentData().putLong(
                FULMINATION_COOLDOWN_KEY,
                currentTime + FULMINATION_COOLDOWN_TICKS
        );
    }
}