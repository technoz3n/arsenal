package xyz.technoz3n.aresenal.item;

import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.ArsenalMod;
import xyz.technoz3n.aresenal.entity.ModEntities;
import xyz.technoz3n.aresenal.item.food.LemonItem;
import xyz.technoz3n.aresenal.item.weapon.GalaxyStaffItem;
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
    public static final RegistryObject<Item> GALACTIC_DUST = ITEMS.register("galactic_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GALAXY_NUGGET = ITEMS.register("galaxy_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GALAXY_AXE = ITEMS.register("galaxy_axe", () -> new AxeItem(ModToolTiers.GALAXY, 6.0F, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> GALAXY_HOE = ITEMS.register("galaxy_hoe", () -> new HoeItem(ModToolTiers.GALAXY, 0, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> GALAXY_PICKAXE = ITEMS.register("galaxy_pickaxe", () -> new PickaxeItem(ModToolTiers.GALAXY, 1, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> GALAXY_SHIELD = ITEMS.register("galaxy_shield", () ->  new Item((new Item.Properties()).stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> GALAXY_SHOVEL = ITEMS.register("galaxy_shovel", () -> new ShovelItem(ModToolTiers.GALAXY, 1.5F, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> GALAXY_SWORD = ITEMS.register("galaxy_sword", () -> new SwordItem(ModToolTiers.GALAXY, 3, -2.4F, new Item.Properties()));
    public  static final RegistryObject<Item> GALAXY_STAFF = ITEMS.register("galaxy_staff", () -> new GalaxyStaffItem(new Item.Properties().durability(798)));
    public static final RegistryObject<Item> LASER = ITEMS.register("laser", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BEDROCK_WITHER_SPAWN_EGG = ITEMS.register("bedrock_wither_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.BEDROCK_WITHER,
                    0x000000,
                    0xFFFFFF,
                    new Item.Properties()
            ));
    public static final RegistryObject<Item> WITHER_TURRET =
            ITEMS.register("wither_turret",
                    () -> new WitherTurretItem(
                            new Item.Properties().stacksTo(1)
                    ));
    public static final RegistryObject<Item> WITHER_TURRET_SPAWN_EGG = ITEMS.register("wither_turret_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.WITHER_TURRET,
                    1315860,
                    1842204,
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
    public static final RegistryObject<Item> WITHER_TURRET_MORTAR =
            ITEMS.register("wither_turret_mortar",
                    () -> new WitherTurretMortarItem(
                            new Item.Properties().stacksTo(1)
                    ));
    public static final RegistryObject<Item> WITHER_TURRET_MORTAR_SPAWN_EGG = ITEMS.register("wither_turret_mortar_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.WITHER_TURRET_MORTAR,
                    1315860,
                    1842204,
                    new Item.Properties()
            ));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
