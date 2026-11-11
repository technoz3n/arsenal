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
                            .updateInterval(1)
                            .build("witherturretground"));

    public static final RegistryObject<EntityType<BulletWitherSkull>> BULLET_WITHER_SKULL =
            ENTITY_TYPES.register("bulletwitherskull",
                    () -> EntityType.Builder.<BulletWitherSkull>of(
                                    BulletWitherSkull::new,
                                    MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .fireImmune()
                            .build("bulletwitherskull"));

    public static final RegistryObject<EntityType<HomingWitherSkull>> HOMING_WITHER_SKULL =
            ENTITY_TYPES.register("homingwitherskull",
                    () -> EntityType.Builder.<HomingWitherSkull>of(
                                    HomingWitherSkull::new,
                                    MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .fireImmune()
                            .build("homingwitherskull"));

    public static final RegistryObject<EntityType<WitherTurret>> WITHER_TURRET =
            ENTITY_TYPES.register("witherturret",
                    () -> EntityType.Builder
                            .of(WitherTurret::new, MobCategory.MISC)
                            .sized(0.35F, 2.5F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("witherturret"));

    public static final RegistryObject<EntityType<WitherTurretMortar>> WITHER_TURRET_MORTAR =
            ENTITY_TYPES.register("witherturretmortar",
                    () -> EntityType.Builder
                            .of(WitherTurretMortar::new, MobCategory.MISC)
                            .sized(1.0F, 3.75F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("witherturretmortar"));
    public static final RegistryObject<EntityType<MortarWitherSkull>> MORTAR_WITHER_SKULL =
            ENTITY_TYPES.register("mortarwitherskull",
                    () -> EntityType.Builder.<MortarWitherSkull>of(
                                    MortarWitherSkull::new,
                                    MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .fireImmune()
                            .build("mortarwitherskull"));

    public static final RegistryObject<EntityType<FulminationSigilEntity>>
            FULMINATION_SIGIL = ENTITY_TYPES.register(
            "fulmination_sigil",
            () -> EntityType.Builder
                    .of(FulminationSigilEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("fulmination_sigil"));

    public static final RegistryObject<EntityType<Laser>> LASER =
            ENTITY_TYPES.register("laser",
                    () -> EntityType.Builder.<Laser>of(
                                    Laser::new,
                                    MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("laser"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
