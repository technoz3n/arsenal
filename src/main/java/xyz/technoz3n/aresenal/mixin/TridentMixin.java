package xyz.technoz3n.aresenal.mixin;

import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.technoz3n.aresenal.enchantment.GrappleTrident;
import xyz.technoz3n.aresenal.enchantment.ModEnchantments;
import xyz.technoz3n.aresenal.enchantment.TridentHoming;

@Mixin(ThrownTrident.class)
public class TridentMixin implements GrappleTrident {

    @Unique
    private static final String GRAPPLE_LEVEL_KEY = "arsenal_grapple_level";

    @Unique
    private static final String HOMING_LEVEL_KEY = "arsenal_homing_level";

    @Unique
    private static final byte RETURN_LOYALTY = 3;

    @Unique
    private static final int MODE_NONE = -1;
    @Unique
    private static final int MODE_BLOCK = -2;
    @Unique
    private static final EntityDataAccessor<Integer> ARSENAL_ANCHOR_MODE =
            SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Vector3f> ARSENAL_ANCHOR_OFFSET =
            SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.VECTOR3);
    @Unique
    private static final EntityDataAccessor<Rotations> ARSENAL_ANCHOR_ROTATION =
            SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.ROTATIONS);

    @Shadow
    @Final
    private static EntityDataAccessor<Byte> ID_LOYALTY;

    @Shadow
    private ItemStack tridentItem;

    @Shadow
    private boolean dealtDamage;

    @Unique
    private int arsenal$homingTarget = -1;

    @Unique
    private ThrownTrident arsenal$self() {
        return (ThrownTrident) (Object) this;
    }


    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void arsenal$defineAnchorData(CallbackInfo ci) {
        SynchedEntityData data = arsenal$self().getEntityData();
        data.define(ARSENAL_ANCHOR_MODE, MODE_NONE);
        data.define(ARSENAL_ANCHOR_OFFSET, new Vector3f());
        data.define(ARSENAL_ANCHOR_ROTATION, new Rotations(0.0F, 0.0F, 0.0F));
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void arsenal$storeEnchantLevels(
            Level level,
            LivingEntity owner,
            ItemStack stack,
            CallbackInfo ci
    ) {
        ThrownTrident self = arsenal$self();

        int grappleLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.GRAPPLE.get(),
                stack
        );

        if (grappleLevel > 0) {
            self.getPersistentData().putInt(GRAPPLE_LEVEL_KEY, grappleLevel);
            self.getEntityData().set(ID_LOYALTY, RETURN_LOYALTY);
        }

        int homingLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.HEATSEEKER.get(),
                stack
        );

        if (homingLevel > 0) {
            self.getPersistentData().putInt(HOMING_LEVEL_KEY, homingLevel);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void arsenal$restoreLoyalty(CompoundTag tag, CallbackInfo ci) {
        ThrownTrident self = arsenal$self();

        if (self.getPersistentData().getInt(GRAPPLE_LEVEL_KEY) > 0) {
            self.getEntityData().set(ID_LOYALTY, RETURN_LOYALTY);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void arsenal$tickAttached(CallbackInfo ci) {
        ThrownTrident self = arsenal$self();
        SynchedEntityData data = self.getEntityData();

        int mode = data.get(ARSENAL_ANCHOR_MODE);

        if (mode == MODE_NONE) {
            return;
        }

        if (mode >= 0) {
            Entity anchor = self.level().getEntity(mode);

            if (anchor != null) {
                Vector3f offset = data.get(ARSENAL_ANCHOR_OFFSET);

                self.setPos(
                        anchor.getX() + offset.x(),
                        anchor.getY() + offset.y(),
                        anchor.getZ() + offset.z()
                );
            }
        }

        Rotations rotation = data.get(ARSENAL_ANCHOR_ROTATION);
        self.setXRot(rotation.getX());
        self.setYRot(rotation.getY());
        self.setDeltaMovement(Vec3.ZERO);

        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void arsenal$homing(CallbackInfo ci) {
        ThrownTrident self = arsenal$self();

        if (self.level().isClientSide) {
            return;
        }

        if (self.isNoPhysics()
                || this.dealtDamage
                || self.distanceToSqr(self.xo, self.yo, self.zo) < 1.0E-6) {
            return;
        }

        int homingLevel = self.getPersistentData().getInt(HOMING_LEVEL_KEY);

        if (homingLevel <= 0) {
            return;
        }

        this.arsenal$homingTarget = TridentHoming.steer(
                self,
                homingLevel,
                this.arsenal$homingTarget
        );
    }

    @Override
    @Unique
    public void arsenal$attachToBlock(Vec3 position, float yaw, float pitch) {
        ThrownTrident self = arsenal$self();
        SynchedEntityData data = self.getEntityData();

        self.setDeltaMovement(Vec3.ZERO);
        self.setPos(position);
        self.setYRot(yaw);
        self.setXRot(pitch);

        data.set(ARSENAL_ANCHOR_ROTATION, new Rotations(pitch, yaw, 0.0F));
        data.set(ARSENAL_ANCHOR_MODE, MODE_BLOCK);

        arsenal$syncPosition(self);
    }

    @Override
    @Unique
    public void arsenal$attachToEntity(Entity target, Vec3 offset, float yaw, float pitch) {
        ThrownTrident self = arsenal$self();
        SynchedEntityData data = self.getEntityData();

        self.setDeltaMovement(Vec3.ZERO);
        self.setPos(target.position().add(offset));
        self.setYRot(yaw);
        self.setXRot(pitch);

        data.set(
                ARSENAL_ANCHOR_OFFSET,
                new Vector3f((float) offset.x, (float) offset.y, (float) offset.z)
        );
        data.set(ARSENAL_ANCHOR_ROTATION, new Rotations(pitch, yaw, 0.0F));
        data.set(ARSENAL_ANCHOR_MODE, target.getId());

        arsenal$syncPosition(self);
    }

    @Override
    @Unique
    public void arsenal$release() {
        ThrownTrident self = arsenal$self();
        SynchedEntityData data = self.getEntityData();

        data.set(ARSENAL_ANCHOR_MODE, MODE_NONE);
        data.set(ID_LOYALTY, RETURN_LOYALTY);

        self.setNoPhysics(true);
    }

    @Override
    @Unique
    public boolean arsenal$isAttached() {
        return arsenal$self().getEntityData().get(ARSENAL_ANCHOR_MODE) != MODE_NONE;
    }

    @Override
    @Unique
    public ItemStack arsenal$copyStack() {
        return this.tridentItem.copy();
    }

    @Unique
    private static void arsenal$syncPosition(ThrownTrident trident) {
        if (trident.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcastAndSend(
                    trident,
                    new ClientboundTeleportEntityPacket(trident)
            );
        }
    }
}