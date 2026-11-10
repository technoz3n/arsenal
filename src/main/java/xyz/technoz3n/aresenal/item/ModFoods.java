package xyz.technoz3n.aresenal.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    public static final FoodProperties LEMON = new FoodProperties.Builder().nutrition(4).saturationMod(1.0F).build();
    public static final FoodProperties GALACTIC_APPLE = new FoodProperties.Builder().nutrition(4).saturationMod(1.2F).effect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1), 1.0F).effect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0), 1.0F).alwaysEat().build();
    public static final FoodProperties ENCHANTED_GALACTIC_APPLE = new FoodProperties.Builder().nutrition(4).saturationMod(1.2F).effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F).effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0), 1.0F).effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0), 1.0F).effect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3), 1.0F).alwaysEat().build();
    public static final FoodProperties GALACTIC_CARROT = new FoodProperties.Builder().nutrition(6).saturationMod(1.2F).build();
    public static final FoodProperties GALACTIC_MELON = new FoodProperties.Builder().nutrition(4).saturationMod(1.2F).build();
}
