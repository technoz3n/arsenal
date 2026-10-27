package xyz.technoz3n.aresenal.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.ArsenalMod;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(
                    ForgeRegistries.RECIPE_SERIALIZERS,
                    ArsenalMod.MOD_ID
            );

    public static final RegistryObject<RecipeSerializer<?>> EXPLOSIVE_LEMON =
            SERIALIZERS.register(
                    "explosive_lemon",
                    () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(
                            ExplosiveLemonRecipe::new
                    )
            );

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}