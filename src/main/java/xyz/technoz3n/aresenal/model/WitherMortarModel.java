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

public class WitherMortarModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("arsenal", "wither_mortar"),
                    "main"
            );

    private final ModelPart handle;
    private final ModelPart head;
    private final ModelPart backTripodLeg;
    private final ModelPart rightFrontTripodLeg;
    private final ModelPart leftFrontTripodLeg;
    private final ModelPart backTripodLegTip;
    private final ModelPart support;

    public WitherMortarModel(ModelPart root) {
        this.handle = root.getChild("handle");
        this.head = root.getChild("head");
        this.backTripodLeg = root.getChild("back_tripod_leg");
        this.rightFrontTripodLeg = root.getChild("right_front_tripod_leg");
        this.leftFrontTripodLeg = root.getChild("left_front_tripod_leg");
        this.backTripodLegTip = root.getChild("back_tripod_leg_tip");
        this.support = root.getChild("support");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("handle",
                CubeListBuilder.create()
                        .texOffs(24, 22)
                        .addBox(-5.5F, -1.0F, -1.0F, 11, 2, 2),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        root.addOrReplaceChild("back_tripod_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(-1.5F, 0.0F, -1.5F, 3, 10, 3),
                PartPose.offsetAndRotation(
                        0.0F, 16.0F, 0.0F,
                        1.3089969F, 0.0F, 0.0F
                ));

        root.addOrReplaceChild("support",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(-1.5F, 0.0F, -1.5F, 3, 10, 3),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        root.addOrReplaceChild("back_tripod_leg_tip",
                CubeListBuilder.create()
                        .texOffs(12, 22)
                        .addBox(-1.5F, 0.0F, -1.5F, 3, 6, 3),
                PartPose.offset(0.0F, 18.0F, 9.0F));

        root.addOrReplaceChild("right_front_tripod_leg",
                CubeListBuilder.create()
                        .texOffs(24, 22)
                        .mirror()
                        .addBox(0.0F, -1.0F, -1.0F, 11, 2, 2),
                PartPose.offsetAndRotation(
                        0.0F, 16.0F, 0.0F,
                        -0.61086524F, 2.6179938F, -0.87266463F
                ));

        root.addOrReplaceChild("left_front_tripod_leg",
                CubeListBuilder.create()
                        .texOffs(24, 22)
                        .addBox(-11.0F, -1.0F, -1.0F, 11, 2, 2),
                PartPose.offsetAndRotation(
                        0.0F, 16.0F, 0.0F,
                        -0.61086524F, -2.6179938F, 0.87266463F
                ));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        head.xRot = headPitch * ((float) Math.PI / 180.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        handle.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        backTripodLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        support.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        backTripodLegTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightFrontTripodLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftFrontTripodLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
