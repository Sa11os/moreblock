package me.sallos.moreblock.block;

import me.sallos.moreblock.block.entity.ImportedBlockEntity;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.util.HorizontalFacingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

@SuppressWarnings("null")
public class ImportedBlock extends BaseEntityBlock {
    public static final @Nonnull DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final int ROTATION_COMPENSATION_STEPS = 3;
    private static final int DEFAULT_LIGHT_LEVEL = 15;

    private final String definitionKey;

    public ImportedBlock(String definitionKey) {
        super(createProperties(definitionKey));
        this.definitionKey = definitionKey;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static BlockBehaviour.Properties createProperties(String definitionKey) {
        return BlockBehaviour.Properties.of()
                .strength(3.5f, 6.0f)
                .noOcclusion()
                .lightLevel(state -> resolveLightLevel(definitionKey));
    }

    private static int resolveLightLevel(String definitionKey) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        if (definition == null) {
            return 0;
        }
        return Math.max(0, Math.min(DEFAULT_LIGHT_LEVEL, definition.lightLevel()));
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getCounterClockWise();
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new ImportedBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return getDynamicShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return getDynamicShape(state);
    }

    private VoxelShape getDynamicShape(BlockState state) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        if (definition == null) {
            return net.minecraft.world.phys.shapes.Shapes.block();
        }
        Direction compensatedFacing = HorizontalFacingHelper.rotateClockwise(state.getValue(FACING), ROTATION_COMPENSATION_STEPS);
        return definition.horizontalShapes().get(compensatedFacing);
    }
}
