
package xyz.technoz3n.aresenal.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import xyz.technoz3n.aresenal.entity.FulminationSigilEntity;
import xyz.technoz3n.aresenal.model.FulminationSigilModel;

public class FulminationSigilRenderer
        extends EntityRenderer<FulminationSigilEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arsenal", "textures/entity/sigil.png");

    private final FulminationSigilModel model;

    public FulminationSigilRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.model = new FulminationSigilModel(
                context.bakeLayer(FulminationSigilModel.LAYER_LOCATION)
        );

        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            FulminationSigilEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();

        float age = entity.tickCount + partialTicks;

        float shrinkProgress = Mth.clamp(
                (age - (FulminationSigilEntity.TOTAL_DURATION - 10))
                        / 10.0F,
                0.0F,
                1.0F
        );

        shrinkProgress = shrinkProgress * shrinkProgress
                * (3.0F - 2.0F * shrinkProgress);

        float scale = Mth.lerp(shrinkProgress, 3.5F, 0.05F);

        poseStack.scale(scale, scale, scale);

        model.setupAnim(entity, 0.0F, 0.0F, age, 0.0F, 0.0F);

        model.renderToBuffer(
                poseStack,
                buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        poseStack.popPose();

        super.render(
                entity, entityYaw, partialTicks, poseStack, buffer, packedLight
        );
    }

    @Override
    public boolean shouldRender(
            FulminationSigilEntity entity,
            net.minecraft.client.renderer.culling.Frustum frustum,
            double camX,
            double camY,
            double camZ
    ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(FulminationSigilEntity entity) {
        return TEXTURE;
    }
}
