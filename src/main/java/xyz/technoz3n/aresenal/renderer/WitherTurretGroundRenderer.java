package xyz.technoz3n.aresenal.renderer;

import xyz.technoz3n.aresenal.entity.WitherTurretGround;
import xyz.technoz3n.aresenal.model.WitherMachineGunModel;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WitherTurretGroundRenderer
        extends MobRenderer<WitherTurretGround, WitherMachineGunModel<WitherTurretGround>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/wither/wither.png");

    public WitherTurretGroundRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new WitherMachineGunModel<>(
                        context.bakeLayer(WitherMachineGunModel.LAYER_LOCATION)
                ),
                1.0F
        );
    }

    @Override
    protected void scale(
            WitherTurretGround entity,
            PoseStack poseStack,
            float partialTick
    ) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }

    @Override
    protected void setupRotations(
            WitherTurretGround entity,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick
    ) {
        super.setupRotations(
                entity,
                poseStack,
                ageInTicks,
                rotationYaw,
                partialTick
        );
    }

    @Override
    public ResourceLocation getTextureLocation(WitherTurretGround entity) {
        return TEXTURE;
    }
}