package me.sallos.moreblock.client.renderer;

import me.sallos.moreblock.entity.SeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class SeatEntityRenderer extends EntityRenderer<SeatEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/unknown_pack.png");

    public SeatEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(@Nonnull SeatEntity entity, @Nonnull net.minecraft.client.renderer.culling.Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return false;
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull SeatEntity entity) {
        return TEXTURE;
    }
}
