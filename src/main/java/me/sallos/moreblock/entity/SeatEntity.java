package me.sallos.moreblock.entity;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.block.ImportedBlock;
import me.sallos.moreblock.init.MoreBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

@SuppressWarnings("null")
public class SeatEntity extends Entity {
    private BlockPos seatBlockPos = BlockPos.ZERO;
    private boolean lying;
    private Direction facing = Direction.NORTH;
    private double configuredSeatHeight;
    private int sitDebugLogRemainingTicks = 5;

    public SeatEntity(EntityType<? extends SeatEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos seatBlockPos, double seatHeight) {
        this(level, seatBlockPos, seatHeight, false, Direction.NORTH);
    }

    public SeatEntity(Level level, BlockPos seatBlockPos, double height, boolean lying, Direction facing) {
        this(MoreBlockEntityTypes.SEAT.get(), level);
        this.seatBlockPos = seatBlockPos;
        this.lying = lying;
        this.facing = facing;
        this.configuredSeatHeight = height;
        setPos(seatBlockPos.getX() + 0.5d, seatBlockPos.getY() + height, seatBlockPos.getZ() + 0.5d);
    }

    public BlockPos getSeatBlockPos() {
        return seatBlockPos;
    }

    public double getConfiguredSeatHeight() {
        return configuredSeatHeight;
    }

    public void setConfiguredSeatHeight(double seatHeight) {
        configuredSeatHeight = Math.max(-2.0d, Math.min(2.0d, seatHeight));
        sitDebugLogRemainingTicks = 5;
        setPos(seatBlockPos.getX() + 0.5d, seatBlockPos.getY() + configuredSeatHeight, seatBlockPos.getZ() + 0.5d);
        for (Entity passenger : getPassengers()) {
            positionRider(passenger);
        }
    }

    @Override
    public boolean shouldRiderSit() {
        // 明确告诉客户端把乘客按“骑乘姿态”渲染，避免看起来始终像站着。
        return true;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Direction direction = facing.getAxis().isHorizontal() ? facing : getDirection();
        int[][] offsets = DismountHelper.offsetsForDirection(direction);
        BlockPos.MutableBlockPos candidatePos = new BlockPos.MutableBlockPos();

        for (Pose pose : passenger.getDismountPoses()) {
            AABB bounds = passenger.getLocalBoundsForPose(pose);

            for (int[] offset : offsets) {
                candidatePos.set(seatBlockPos.getX() + offset[0], seatBlockPos.getY(), seatBlockPos.getZ() + offset[1]);
                Vec3 safeLocation = findSafeDismountLocation(passenger, pose, bounds, candidatePos);
                if (safeLocation != null) {
                    passenger.setPose(pose);
                    return safeLocation;
                }
            }

            candidatePos.set(seatBlockPos.getX(), seatBlockPos.getY() + 1, seatBlockPos.getZ());
            Vec3 elevatedFallback = findSafeDismountLocation(passenger, pose, bounds, candidatePos);
            if (elevatedFallback != null) {
                passenger.setPose(pose);
                return elevatedFallback;
            }
        }

        return new Vec3(seatBlockPos.getX() + 0.5d, seatBlockPos.getY() + 1.0d, seatBlockPos.getZ() + 0.5d);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && (getPassengers().isEmpty() || !(level().getBlockState(seatBlockPos).getBlock() instanceof ImportedBlock))) {
            discard();
        }
        if (!level().isClientSide && sitDebugLogRemainingTicks > 0 && !getPassengers().isEmpty()) {
            Entity passenger = getPassengers().get(0);
            double blockBaseY = seatBlockPos.getY();
            Moreblock.LOGGER.info(
                    "导入方块坐下调试: seatBlockPos={}, configuredSeatHeight={}, seatEntityHeight={}, passengerFeetHeight={}, passengerEyeHeight={}, passengerBbMinHeight={}, passengerBbMaxHeight={}, passengerPose={}, passenger={}, tick={}",
                    seatBlockPos,
                    configuredSeatHeight,
                    getY() - blockBaseY,
                    passenger.getY() - blockBaseY,
                    passenger.getEyeY() - blockBaseY,
                    passenger.getBoundingBox().minY - blockBaseY,
                    passenger.getBoundingBox().maxY - blockBaseY,
                    passenger.getPose(),
                    passenger.getName().getString(),
                    tickCount
            );
            sitDebugLogRemainingTicks--;
        }
        if (lying) {
            for (Entity passenger : getPassengers()) {
                passenger.setPose(Pose.SLEEPING);
                passenger.setYRot(facing.toYRot());
                passenger.setYHeadRot(facing.toYRot());
            }
        }
    }

    @Override
    protected void positionRider(@Nonnull Entity passenger, @Nonnull MoveFunction moveFunction) {
        if (hasPassenger(passenger)) {
            Vec3 position = position();
            double passengerBeforeWorldY = passenger.getY();
            double passengerBeforeHeight = passengerBeforeWorldY - seatBlockPos.getY();
            moveFunction.accept(passenger, position.x, position.y, position.z);
            if (!level().isClientSide && sitDebugLogRemainingTicks > 0) {
                double blockBaseY = seatBlockPos.getY();
                Moreblock.LOGGER.info(
                        "导入方块坐下定位: seatBlockPos={}, configuredSeatHeight={}, targetPassengerHeight={}, seatEntityHeight={}, passengerBeforeHeight={}, passengerAfterHeight={}, passengerBeforeWorldY={}, passengerAfterWorldY={}, passenger={}",
                        seatBlockPos,
                        configuredSeatHeight,
                        position.y - blockBaseY,
                        getY() - blockBaseY,
                        passengerBeforeHeight,
                        passenger.getY() - blockBaseY,
                        passengerBeforeWorldY,
                        passenger.getY(),
                        passenger.getName().getString()
                );
            }
            if (lying) {
                passenger.setYRot(facing.toYRot());
                passenger.setYHeadRot(facing.toYRot());
            }
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    private Vec3 findSafeDismountLocation(LivingEntity passenger, Pose pose, AABB bounds, BlockPos pos) {
        double floorHeight = level().getBlockFloorHeight(pos);
        if (!DismountHelper.isBlockFloorValid(floorHeight)) {
            return null;
        }

        Vec3 location = Vec3.upFromBottomCenterOf(pos, floorHeight);
        if (!DismountHelper.canDismountTo(level(), passenger, bounds.move(location))) {
            return null;
        }
        return location;
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        seatBlockPos = BlockPos.of(tag.getLong("SeatBlockPos"));
        lying = tag.getBoolean("Lying");
        facing = Direction.from2DDataValue(tag.getInt("Facing"));
        configuredSeatHeight = tag.getDouble("ConfiguredSeatHeight");
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putLong("SeatBlockPos", seatBlockPos.asLong());
        tag.putBoolean("Lying", lying);
        tag.putInt("Facing", facing.get2DDataValue());
        tag.putDouble("ConfiguredSeatHeight", configuredSeatHeight);
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
