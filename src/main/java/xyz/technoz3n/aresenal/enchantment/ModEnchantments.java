package xyz.technoz3n.aresenal.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.ArsenalMod;


public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ArsenalMod.MOD_ID);
    public static final RegistryObject<Enchantment> GRAPPLE =
            ENCHANTMENTS.register("grapple", GrappleEnchantment::new);
    public static final RegistryObject<Enchantment> HEATSEEKER =
            ENCHANTMENTS.register("homing", HeatseekerEnchantment::new);
    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
