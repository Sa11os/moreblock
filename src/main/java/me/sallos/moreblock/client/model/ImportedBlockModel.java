package me.sallos.moreblock.client.model;

import me.sallos.moreblock.block.ImportedBlock;
import me.sallos.moreblock.block.entity.ImportedBlockEntity;
import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import software.bernie.geckolib.model.GeoModel;

public class ImportedBlockModel extends GeoModel<ImportedBlockEntity> {
    @Override
    public ResourceLocation getAnimationResource(ImportedBlockEntity animatable) {
        ImportedBlockPacks.Definition definition = getDefinition(animatable);
        return definition == null ? fallback() : definition.animationLocation();
    }

    @Override
    public ResourceLocation getModelResource(ImportedBlockEntity animatable) {
        ImportedBlockPacks.Definition definition = getDefinition(animatable);
        return definition == null ? fallback() : definition.modelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(ImportedBlockEntity animatable) {
        ImportedBlockPacks.Definition definition = getDefinition(animatable);
        return definition == null ? fallback() : definition.textureLocation();
    }

    private ImportedBlockPacks.Definition getDefinition(ImportedBlockEntity animatable) {
        Block block = animatable.getBlockState().getBlock();
        if (block instanceof ImportedBlock configGeoBlock) {
            return ImportedBlockPacks.getDefinition(configGeoBlock.getDefinitionKey());
        }
        return null;
    }

    private ResourceLocation fallback() {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "missingno");
    }
}
