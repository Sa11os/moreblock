package me.sallos.moreblock.block;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.api.event.MoreBlockEvents;
import me.sallos.moreblock.api.event.MoreBlockInteractionResult;
import me.sallos.moreblock.block.entity.ImportedBlockEntity;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.entity.SeatEntity;
import me.sallos.moreblock.util.HorizontalFacingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

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
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .strength(3.5f, 6.0f)
                .lightLevel(state -> resolveLightLevel(definitionKey));
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        return definition != null && definition.translucent() ? properties.noOcclusion() : properties;
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
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        if (definition != null) {
            MoreBlockEvents.firePlaceBlock(definition, level, pos, state, placer, stack);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
            if (definition != null) {
                MoreBlockEvents.fireRemoveBlock(definition, level, pos, state, newState, movedByPiston);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        if (definition == null) {
            return InteractionResult.PASS;
        }

        MoreBlockInteractionResult apiResult = MoreBlockEvents.fireUseBlock(definition, state, level, pos, player, hand, hit);
        if (apiResult != MoreBlockInteractionResult.PASS) {
            return apiResult.toMinecraftResult();
        }
        if (!definition.supportsSitting() && !definition.supportsLying()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (definition.supportsLying()) {
            return useAsBed(state, level, pos, player, definition);
        }
        if (player.isPassenger() || hasSeat(level, pos)) {
            return InteractionResult.CONSUME;
        }

        SeatEntity seat = new SeatEntity(level, pos, definition.seatHeight());
        level.addFreshEntity(seat);
        player.startRiding(seat);
        return InteractionResult.CONSUME;
    }

    private InteractionResult useAsBed(BlockState state, Level level, BlockPos pos, Player player, ImportedBlockPacks.Definition definition) {
        if (player.isPassenger() || hasSeat(level, pos)) {
            Moreblock.LOGGER.info("导入方块床交互被占用: block={}, pos={}, passenger={}, occupied={}", definition.registryName(), pos, player.isPassenger(), hasSeat(level, pos));
            return InteractionResult.CONSUME;
        }
        Moreblock.LOGGER.info("导入方块床开始尝试睡眠: block={}, pos={}, player={}, dayTime={}, dimension={}", definition.registryName(), pos, player.getGameProfile().getName(), level.getDayTime(), level.dimension().location());
        var sleepResult = player.startSleepInBed(pos);
        if (sleepResult.left().isPresent()) {
            Player.BedSleepingProblem problem = sleepResult.left().get();
            Moreblock.LOGGER.info("导入方块床原版睡眠返回问题: block={}, pos={}, player={}, problem={}", definition.registryName(), pos, player.getGameProfile().getName(), problem);
            if (problem == Player.BedSleepingProblem.NOT_POSSIBLE_NOW) {
                Moreblock.beginDaytimeLying(player, pos, definition);
                Moreblock.LOGGER.info("导入方块床白天改用真正睡姿: block={}, pos={}, player={}, compensation={}",
                        definition.registryName(),
                        pos,
                        player.getGameProfile().getName(),
                        definition.lyingRotationCompensation());
                return InteractionResult.CONSUME;
            }
            if (problem.getMessage() != null) {
                player.displayClientMessage(problem.getMessage(), true);
            }
        } else {
            Moreblock.LOGGER.info("导入方块床原版睡眠成功: block={}, pos={}, player={}", definition.registryName(), pos, player.getGameProfile().getName());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, @Nullable Entity player) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        return definition != null && definition.supportsLying();
    }

    @Override
    public void setBedOccupied(BlockState state, Level level, BlockPos pos, LivingEntity sleeper, boolean occupied) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        if (definition != null && definition.supportsLying()) {
            Moreblock.LOGGER.info("导入方块床占用状态变化: block={}, pos={}, sleeper={}, occupied={}", definition.registryName(), pos, sleeper.getName().getString(), occupied);
        }
    }

    @Override
    public Direction getBedDirection(BlockState state, LevelReader level, BlockPos pos) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        return resolveLyingFacing(state, definition);
    }

    @Override
    public Optional<Vec3> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation, @Nullable LivingEntity entity) {
        return Optional.empty();
    }

    private Direction resolveLyingFacing(BlockState state, @Nullable ImportedBlockPacks.Definition definition) {
        Direction facing = state.getValue(FACING);
        if (definition == null) {
            return facing;
        }
        return HorizontalFacingHelper.rotateClockwise(facing, definition.lyingRotationCompensation());
    }

    private boolean hasSeat(Level level, BlockPos pos) {
        AABB bounds = new AABB(pos).inflate(0.1d, 1.0d, 0.1d);
        return !level.getEntitiesOfClass(SeatEntity.class, bounds, seat -> seat.isAlive() && !seat.getPassengers().isEmpty()).isEmpty();
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return getDynamicShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity != null && entity.getVehicle() instanceof SeatEntity seat && pos.equals(seat.getSeatBlockPos())) {
                // 坐在当前方块上的乘客不应再被自身碰撞箱顶高。
                return Shapes.empty();
            }
        }
        return getDynamicShape(state);
    }

    private VoxelShape getDynamicShape(BlockState state) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        if (definition == null) {
            return net.minecraft.world.phys.shapes.Shapes.block();
        }
        Direction compensatedFacing = HorizontalFacingHelper.rotateClockwise(state.getValue(FACING), ROTATION_COMPENSATION_STEPS);
        return ImportedBlockPacks.getHorizontalShapes(definition.registryName()).get(compensatedFacing);
    }
}
