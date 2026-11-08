package xyz.technoz3n.aresenal.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class FulminationEnchantment extends Enchantment {

    public FulminationEnchantment() {
        super(
                Rarity.RARE,
                EnchantmentCategory.TRIDENT,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND}
        );
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other)
                && other != Enchantments.CHANNELING
                && other != Enchantments.RIPTIDE
                && other != ModEnchantments.GRAPPLE.get();
    }
}