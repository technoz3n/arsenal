package xyz.technoz3n.aresenal.enchantment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface GrappleTrident {

    void arsenal$attachToBlock(Vec3 position, float yaw, float pitch);

    void arsenal$attachToEntity(Entity target, Vec3 offset, float yaw, float pitch);

    void arsenal$release();

    boolean arsenal$isAttached();

    ItemStack arsenal$copyStack();
}