package me.sallos.moreblock.client.renderer;

import me.sallos.moreblock.client.model.ImportedEntityModel;
import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.entity.ImportedEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ImportedEntityRenderer extends GeoEntityRenderer<ImportedEntity> {
    public ImportedEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new ImportedEntityModel());
    }

    @Override
    public ResourceLocation getTextureLocation(ImportedEntity animatable) {
        ImportedEntityPacks.Definition definition = animatable.getDefinition();
        return definition == null ? ResourceLocation.fromNamespaceAndPath("minecraft", "missingno") : definition.textureLocation();
    }

    @Override
    public RenderType getRenderType(ImportedEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        ImportedEntityPacks.Definition definition = animatable.getDefinition();
        if (definition != null && definition.translucent()) {
            return RenderType.entityTranslucent(texture);
        }
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    protected float getDeathMaxRotation(ImportedEntity animatable) {
        ImportedEntityPacks.Definition definition = animatable.getDefinition();
        if (definition != null && definition.disableVanillaDeathAnimation()) {
            return 0.0f;
        }
        return super.getDeathMaxRotation(animatable);
    }
}
