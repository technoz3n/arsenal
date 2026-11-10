package xyz.technoz3n.aresenal.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import xyz.technoz3n.aresenal.ArsenalMod;
import xyz.technoz3n.aresenal.entity.Laser;

public class LaserRenderer extends ArrowRenderer<Laser> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArsenalMod.MOD_ID,
                    "textures/entity/laser.png"
            );

    public LaserRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Laser entity) {
        return TEXTURE;
    }
}