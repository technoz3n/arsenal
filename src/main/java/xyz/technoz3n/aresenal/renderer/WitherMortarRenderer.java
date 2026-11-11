package xyz.technoz3n.aresenal.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import xyz.technoz3n.aresenal.entity.WitherTurretMortar;
import xyz.technoz3n.aresenal.model.WitherMortarModel;

public class WitherMortarRenderer extends MobRenderer<WitherTurretMortar, WitherMortarModel<WitherTurretMortar>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/entity/wither/wither.png"
            );

    public WitherMortarRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherMortarModel<>(
                context.bakeLayer(WitherMortarModel.LAYER_LOCATION)
        ), 1.25F);
    }

    @Override
    protected void scale(WitherTurretMortar entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(2.5F, 2.5F, 2.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(WitherTurretMortar entity) {
        return TEXTURE;
    }
}
