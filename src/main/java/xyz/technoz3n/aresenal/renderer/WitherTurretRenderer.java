
package xyz.technoz3n.aresenal.renderer;

import xyz.technoz3n.aresenal.entity.WitherTurret;
import xyz.technoz3n.aresenal.model.WitherTurretModel;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WitherTurretRenderer
        extends MobRenderer<WitherTurret, WitherTurretModel<WitherTurret>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/entity/wither/wither.png"
            );

    public WitherTurretRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new WitherTurretModel<>(
                        context.bakeLayer(
                                WitherTurretModel.LAYER_LOCATION
                        )
                ),
                1.0F
        );
    }

    @Override
    protected void scale(
            WitherTurret entity,
            PoseStack poseStack,
            float partialTick
    ) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(WitherTurret entity) {
        return TEXTURE;
    }
}
