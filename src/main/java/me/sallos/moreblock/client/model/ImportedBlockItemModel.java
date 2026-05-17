package me.sallos.moreblock.client.model;

import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.item.ImportedBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ImportedBlockItemModel extends GeoModel<ImportedBlockItem> {
    @Override
    public ResourceLocation getAnimationResource(ImportedBlockItem animatable) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(animatable.getDefinitionKey());
        return definition == null ? fallback() : definition.animationLocation();
    }

    @Override
    public ResourceLocation getModelResource(ImportedBlockItem animatable) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(animatable.getDefinitionKey());
        return definition == null ? fallback() : definition.modelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(ImportedBlockItem animatable) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(animatable.getDefinitionKey());
        return definition == null ? fallback() : definition.textureLocation();
    }

    private ResourceLocation fallback() {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "missingno");
    }
}
