package me.sallos.moreblock.entity;

import me.sallos.moreblock.block.ImportedBlock;
import me.sallos.moreblock.init.MoreBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

@SuppressWarnings("null")
public class SeatEntity extends Entity {
    private BlockPos seatBlockPos = BlockPos.ZERO;

    public SeatEntity(EntityType<? extends SeatEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos seatBlockPos, double seatHeight) {
        this(MoreBlockEntityTypes.SEAT.get(), level);
        this.seatBlockPos = seatBlockPos;
        setPos(seatBlockPos.getX() + 0.5d, seatBlockPos.getY() + seatHeight, seatBlockPos.getZ() + 0.5d);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && (getPassengers().isEmpty() || !(level().getBlockState(seatBlockPos).getBlock() instanceof ImportedBlock))) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        seatBlockPos = BlockPos.of(tag.getLong("SeatBlockPos"));
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putLong("SeatBlockPos", seatBlockPos.asLong());
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
