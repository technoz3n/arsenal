
package xyz.technoz3n.aresenal.item.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xyz.technoz3n.aresenal.entity.ModEntities;
import xyz.technoz3n.aresenal.entity.Laser;
import xyz.technoz3n.aresenal.item.ModItems;

import java.util.function.Predicate;

public class GalaxyStaffItem extends BowItem {

    public GalaxyStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return stack -> stack.is(ModItems.LASER.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack ammo = player.getProjectile(stack);

        if (!player.getAbilities().instabuild && ammo.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(
            ItemStack stack,
            Level level,
            LivingEntity living,
            int timeLeft
    ) {
        if (!(living instanceof Player player) || level.isClientSide) {
            return;
        }

        int charge = getUseDuration(stack) - timeLeft;

        float power = charge / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        power = Math.min(power, 1.0F);

        if (power < 0.1F) {
            return;
        }

        ItemStack ammo = player.getProjectile(stack);
        boolean creative = player.getAbilities().instabuild;

        if (!creative && ammo.isEmpty()) {
            return;
        }

        Laser laser = new Laser(ModEntities.LASER.get(), level);

        laser.setOwner(player);

        laser.setPos(
                player.getX(),
                player.getEyeY() - 0.1,
                player.getZ()
        );

        laser.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0.0F,
                power * 5.0F,
                0.0F
        );

        level.addFreshEntity(laser);

        if (!creative) {
            ammo.shrink(1);
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS,
                5.0F,
                2.0F
        );

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                5.0F,
                0.5F
        );

        player.awardStat(Stats.ITEM_USED.get(this));
    }
}
