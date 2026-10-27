package xyz.technoz3n.aresenal.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import xyz.technoz3n.aresenal.item.ModItems;

public class ExplosiveLemonRecipe extends CustomRecipe {

    public ExplosiveLemonRecipe(
            ResourceLocation id,
            CraftingBookCategory category
    ) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        boolean lemon = false;
        boolean gunpowder = false;
        boolean fireCharge = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(ModItems.LEMON.get()) && !lemon) {
                lemon = true;
            } else if (stack.is(Items.GUNPOWDER) && !gunpowder) {
                gunpowder = true;
            } else if (stack.is(Items.FIRE_CHARGE) && !fireCharge) {
                fireCharge = true;
            } else {
                return false;
            }
        }

        return lemon && gunpowder && fireCharge;
    }

    @Override
    public ItemStack assemble(
            CraftingContainer inv,
            RegistryAccess registryAccess
    ) {
        ItemStack result = ModItems.LEMON.get().getDefaultInstance();
        result.getOrCreateTag().putBoolean("Explosive", true);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.EXPLOSIVE_LEMON.get();
    }
}