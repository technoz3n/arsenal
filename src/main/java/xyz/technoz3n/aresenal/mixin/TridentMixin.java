
package xyz.technoz3n.aresenal.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.technoz3n.aresenal.enchantment.ModEnchantments;

@Mixin(ThrownTrident.class)
public class TridentMixin {

    private static final String GRAPPLE_LEVEL_KEY =
            "arsenal_grapple_level";

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )

    private void arsenal$storeGrappleLevel(
            Level level,
            LivingEntity owner,
            ItemStack stack,
            CallbackInfo ci
    ) {
        int grappleLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.GRAPPLE.get(),
                stack
        );

        if (grappleLevel > 0) {
            ((ThrownTrident) (Object) this)
                    .getPersistentData()
                    .putInt(GRAPPLE_LEVEL_KEY, grappleLevel);
        }
    }
}
