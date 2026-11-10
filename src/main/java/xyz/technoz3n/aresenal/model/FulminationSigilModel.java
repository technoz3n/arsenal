
package xyz.technoz3n.aresenal.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import xyz.technoz3n.aresenal.entity.FulminationSigilEntity;

public class FulminationSigilModel extends EntityModel<FulminationSigilEntity> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(
					new ResourceLocation("arsenal", "entity/fulminationsigilmodel"),
					"main"
			);

	private final ModelPart sigil;

	public FulminationSigilModel(ModelPart root) {
		this.sigil = root.getChild("sigil");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild(
				"sigil",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-8.0F, 0.0F, -8.0F, 16.0F, 0.01F, 16.0F),
				PartPose.ZERO
		);

		return LayerDefinition.create(mesh, 16, 16);
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
		sigil.render(
				poseStack,
				vertexConsumer,
				packedLight,
				packedOverlay,
				red,
				green,
				blue,
				alpha
		);
	}
	@Override
	public void setupAnim(
			FulminationSigilEntity entity,
			float limbSwing,
			float limbSwingAmount,
			float ageInTicks,
			float netHeadYaw,
			float headPitch
	) {
		sigil.yRot = ageInTicks * 0.08F;
	}
}
