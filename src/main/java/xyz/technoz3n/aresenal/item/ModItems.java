package xyz.technoz3n.aresenal.item;

import net.minecraft.world.item.EnchantedGoldenAppleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.ArsenalMod;
import xyz.technoz3n.aresenal.entity.ModEntities;
import xyz.technoz3n.aresenal.item.food.LemonItem;
import xyz.technoz3n.aresenal.item.weapon.RapierItem;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ArsenalMod.MOD_ID);

    public static final RegistryObject<Item> LEMON = ITEMS.register("lemon", () -> new LemonItem(new Item.Properties().food(ModFoods.LEMON)));
    public static final RegistryObject<Item> RAPIER = ITEMS.register("rapier", () -> new RapierItem(Tiers.DIAMOND, 5, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> GALACTIC_APPLE = ITEMS.register("galactic_apple", () -> new Item(new Item.Properties().food(ModFoods.GALACTIC_APPLE)));
    public static final RegistryObject<Item> ENCHANTED_GALACTIC_APPLE = ITEMS.register("enchanted_galactic_apple", () -> new EnchantedGoldenAppleItem(new Item.Properties().food(ModFoods.ENCHANTED_GALACTIC_APPLE).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> GALACTIC_CARROT = ITEMS.register("galactic_carrot", () -> new Item(new Item.Properties().food(ModFoods.GALACTIC_CARROT)));
    public static final RegistryObject<Item> GALACTIC_MELON = ITEMS.register("galactic_melon", () -> new Item(new Item.Properties().food(ModFoods.GALACTIC_MELON)));
    public static final RegistryObject<Item> GALAXY_INGOT = ITEMS.register("galaxy_ingot", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BEDROCK_WITHER_SPAWN_EGG = ITEMS.register("bedrock_wither_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.BEDROCK_WITHER,
                    0x000000,
                    0xFFFFFF,
                    new Item.Properties()
            ));
    public static final RegistryObject<Item> WITHER_TURRET_GROUND =
            ITEMS.register("wither_turret_ground",
                    () -> new WitherTurretGroundItem(
                            new Item.Properties().stacksTo(1)
                    ));
    public static final RegistryObject<Item> WITHER_TURRET_GROUND_SPAWN_EGG = ITEMS.register("wither_turret_ground_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.WITHER_TURRET_GROUND,
                    1315860,
                    1842204,
                    new Item.Properties()
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
