package xyz.technoz3n.aresenal.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.IEventBusEngine;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.ArsenalMod;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ArsenalMod.MOD_ID);

    public static final RegistryObject<Item> LEMON = ITEMS.register("lemon",
            () -> new Item(new Item.Properties().food(ModFoods.LEMON)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
