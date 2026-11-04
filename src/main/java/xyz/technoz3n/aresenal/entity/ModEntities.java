package xyz.technoz3n.aresenal.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.aresenal.ArsenalMod;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ArsenalMod.MOD_ID);

    public static final RegistryObject<EntityType<BedrockWitherBoss>> BEDROCK_WITHER =
            ENTITY_TYPES.register("bedrock_wither", () -> EntityType.Builder.of(BedrockWitherBoss::new, MobCategory.MONSTER)
                    .sized(0.9F, 3.5F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build("bedrock_wither"));

    public static final RegistryObject<EntityType<WitherTurretGround>> WITHER_TURRET_GROUND =
            ENTITY_TYPES.register("witherturretground",
                    () -> EntityType.Builder
                            .of(WitherTurretGround::new, MobCategory.MISC)
                            .sized(0.75F, 1.375F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build("witherturretground"));

    public static final RegistryObject<EntityType<BulletWitherSkull>> BULLET_WITHER_SKULL =
            ENTITY_TYPES.register("wither_turret_skull",
                    () -> EntityType.Builder.<BulletWitherSkull>of(
                                    BulletWitherSkull::new,
                                    MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .fireImmune()
                            .build("bulletwitherskull"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
