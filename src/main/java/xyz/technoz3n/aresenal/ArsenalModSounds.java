package xyz.technoz3n.aresenal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ArsenalModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(
                    ForgeRegistries.SOUND_EVENTS,
                    ArsenalMod.MOD_ID
            );

    public static final RegistryObject<SoundEvent> WITHER_TURRET_DEATH =
            SOUND_EVENTS.register("turretdeath",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArsenalMod.MOD_ID, "turretdeath")
                    ));

    public static final RegistryObject<SoundEvent> WITHER_TURRET_SHOOT =
            SOUND_EVENTS.register("turretshoot",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArsenalMod.MOD_ID, "turretshoot")
                    ));

    public static final RegistryObject<SoundEvent> WITHER_TURRET_DEATH_2 =
            SOUND_EVENTS.register("turretdeath2",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArsenalMod.MOD_ID, "turretdeath2")
                    ));

    public static final RegistryObject<SoundEvent> WITHER_TURRET_SHOOT_2 =
            SOUND_EVENTS.register("turretshoot2",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArsenalMod.MOD_ID, "turretshoot2")
                    ));

    public static final RegistryObject<SoundEvent> MORTAR_SHOT =
            SOUND_EVENTS.register("mortarshot",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArsenalMod.MOD_ID, "mortarshot")
                    ));

    public static final RegistryObject<SoundEvent> MORTAR_HIT =
            SOUND_EVENTS.register("mortarhit",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArsenalMod.MOD_ID, "mortarhit")
                    ));

    public static final RegistryObject<SoundEvent> MORTAR_DEATH =
            SOUND_EVENTS.register("mortardeath",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArsenalMod.MOD_ID, "mortardeath")
                    ));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}