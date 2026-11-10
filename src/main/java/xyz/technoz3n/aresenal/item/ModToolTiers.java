
package xyz.technoz3n.aresenal.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import xyz.technoz3n.aresenal.ArsenalMod;

import java.util.List;

public class ModToolTiers {

    public static final Tier GALAXY = TierSortingRegistry.registerTier(
            new ForgeTier(
                    4,                          // the harvest level
                    1800,                       // durability
                    9.0F,                       // mining speed
                    3.5F,                       // attack damage bonus
                    18,                         // and enchantability
                    BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> Ingredient.of(ModItems.GALAXY_INGOT.get())
            ),
            ResourceLocation.fromNamespaceAndPath(ArsenalMod.MOD_ID, "galaxy"),
            List.of(Tiers.DIAMOND),
            List.of(Tiers.NETHERITE)
    );
}
