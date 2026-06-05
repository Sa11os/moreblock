package me.sallos.moreblock.client.renderer;

import me.sallos.moreblock.block.ImportedBlock;
import me.sallos.moreblock.block.entity.ImportedBlockEntity;
import me.sallos.moreblock.client.model.ImportedBlockModel;
import me.sallos.moreblock.util.HorizontalFacingHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ImportedBlockRenderer extends GeoBlockRenderer<ImportedBlockEntity> {
    private static final int MODEL_ROTATION_COMPENSATION_STEPS = 3;

    public ImportedBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new ImportedBlockModel());
    }

    @Override
    public RenderType getRenderType(ImportedBlockEntity animatable, net.minecraft.resources.ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    protected Direction getFacing(ImportedBlockEntity animatable) {
        Direction facing = animatable.getBlockState().getValue(ImportedBlock.FACING);
        return HorizontalFacingHelper.rotateClockwise(facing, MODEL_ROTATION_COMPENSATION_STEPS);
    }
}
