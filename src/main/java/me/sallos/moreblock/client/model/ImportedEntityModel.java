package me.sallos.moreblock.client.model;

import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.entity.ImportedEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ImportedEntityModel extends GeoModel<ImportedEntity> {
    @Override
    public ResourceLocation getModelResource(ImportedEntity animatable) {
        ImportedEntityPacks.Definition definition = animatable.getDefinition();
        return definition == null ? fallback() : definition.geoLocation();
    }

    @Override
    public ResourceLocation getTextureResource(ImportedEntity animatable) {
        ImportedEntityPacks.Definition definition = animatable.getDefinition();
        return definition == null ? fallback() : definition.textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(ImportedEntity animatable) {
        ImportedEntityPacks.Definition definition = animatable.getDefinition();
        return definition == null ? fallback() : definition.animationLocation();
    }

    private ResourceLocation fallback() {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "missingno");
    }
}
