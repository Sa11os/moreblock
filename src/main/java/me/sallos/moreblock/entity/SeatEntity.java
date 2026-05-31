package me.sallos.moreblock.entity;

import me.sallos.moreblock.block.ImportedBlock;
import me.sallos.moreblock.init.MoreBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

@SuppressWarnings("null")
public class SeatEntity extends Entity {
    private BlockPos seatBlockPos = BlockPos.ZERO;
    private boolean lying;
    private Direction facing = Direction.NORTH;

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
        setPos(seatBlockPos.getX() + 0.5d, seatBlockPos.getY() + height, seatBlockPos.getZ() + 0.5d);
    }

    public BlockPos getSeatBlockPos() {
        return seatBlockPos;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && (getPassengers().isEmpty() || !(level().getBlockState(seatBlockPos).getBlock() instanceof ImportedBlock))) {
            discard();
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
            moveFunction.accept(passenger, position.x, position.y, position.z);
            if (lying) {
                passenger.setYRot(facing.toYRot());
                passenger.setYHeadRot(facing.toYRot());
            }
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        seatBlockPos = BlockPos.of(tag.getLong("SeatBlockPos"));
        lying = tag.getBoolean("Lying");
        facing = Direction.from2DDataValue(tag.getInt("Facing"));
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putLong("SeatBlockPos", seatBlockPos.asLong());
        tag.putBoolean("Lying", lying);
        tag.putInt("Facing", facing.get2DDataValue());
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
