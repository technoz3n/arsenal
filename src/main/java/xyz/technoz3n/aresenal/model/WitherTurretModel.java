
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

public class WitherTurretModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(
                            "arsenal",
                            "witherturret"
                    ),
                    "main"
            );

    private final ModelPart pole1;
    private final ModelPart pole2;
    private final ModelPart head;

    public WitherTurretModel(ModelPart root) {
        this.pole1 = root.getChild("pole1");
        this.pole2 = root.getChild("pole2");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "pole1",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(
                                -1.5F, -10.0F, -1.5F,
                                3.0F, 10.0F, 3.0F
                        ),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "pole2",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(
                                -1.5F, -10.0F, -1.5F,
                                3.0F, 10.0F, 3.0F
                        ),
                PartPose.offset(0.0F, 14.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(
                                -4.0F, -4.0F, -4.0F,
                                8.0F, 8.0F, 8.0F
                        ),
                PartPose.offset(0.0F, 2.0F, 0.0F)
        );

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
        head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F);
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
        pole1.render(
                poseStack, vertexConsumer,
                packedLight, packedOverlay,
                red, green, blue, alpha
        );

        pole2.render(
                poseStack, vertexConsumer,
                packedLight, packedOverlay,
                red, green, blue, alpha
        );

        head.render(
                poseStack, vertexConsumer,
                packedLight, packedOverlay,
                red, green, blue, alpha
        );
    }
}
