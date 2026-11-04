package xyz.technoz3n.aresenal.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class WitherMachineGunModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation("arsenal", "witherturretground"),
                    "main"
            );

    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart support;
    private final ModelPart base;
    private final ModelPart pole;

    public WitherMachineGunModel(ModelPart root) {
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.support = root.getChild("support");
        this.base = root.getChild("base");
        this.pole = root.getChild("pole");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head1",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-3.0F, -3.0F, -3.0F,
                                6.0F, 6.0F, 6.0F),
                PartPose.offset(-10.0F, 14.0F, 0.0F));

        root.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -4.0F,
                                8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 26.0F, 0.0F));

        root.addOrReplaceChild("head2",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-3.0F, -3.0F, -3.0F,
                                6.0F, 6.0F, 6.0F),
                PartPose.offset(10.0F, 14.0F, 0.0F));

        root.addOrReplaceChild("support",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-10.0F, 0.0F, -1.5F,
                                20.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));

        root.addOrReplaceChild("pole",
                CubeListBuilder.create()
                        .texOffs(12, 22)
                        .addBox(-1.5F, 0.0F, -1.5F,
                                3.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        float pitch = headPitch * ((float) Math.PI / 180F);
        float yaw = netHeadYaw * ((float) Math.PI / 180F);

        head1.xRot = pitch;
        head2.xRot = pitch;

        head1.yRot = yaw;
        head2.yRot = yaw;

        support.xRot = pitch;
        pole.yRot = yaw / 2.0F;
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        head1.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
        head2.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
        support.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
        pole.render(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}