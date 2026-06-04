package me.sallos.moreblock.network.message;

import me.sallos.moreblock.wall.WallDecalSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncWallDecalMessage(WallDecalSystem.DecalPlacement placement, boolean remove) {
    public static SyncWallDecalMessage upsert(WallDecalSystem.DecalPlacement placement) {
        return new SyncWallDecalMessage(placement, false);
    }

    public static SyncWallDecalMessage remove(WallDecalSystem.DecalPlacement placement) {
        return new SyncWallDecalMessage(placement, true);
    }

    public static void encode(SyncWallDecalMessage message, FriendlyByteBuf buffer) {
        WallDecalSystem.DecalPlacement placement = message.placement();
        buffer.writeBoolean(message.remove());
        buffer.writeResourceLocation(placement.dimension());
        buffer.writeBlockPos(placement.pos());
        buffer.writeEnum(placement.face());
        buffer.writeResourceLocation(placement.texture());
    }

    public static SyncWallDecalMessage decode(FriendlyByteBuf buffer) {
        boolean remove = buffer.readBoolean();
        ResourceLocation dimension = buffer.readResourceLocation();
        BlockPos pos = buffer.readBlockPos();
        Direction face = buffer.readEnum(Direction.class);
        ResourceLocation texture = buffer.readResourceLocation();
        return new SyncWallDecalMessage(new WallDecalSystem.DecalPlacement(dimension, pos, face, texture), remove);
    }

    public static void handle(SyncWallDecalMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (message.remove()) {
                WallDecalSystem.remove(message.placement());
            } else {
                WallDecalSystem.put(message.placement());
            }
        });
        context.setPacketHandled(true);
    }
}
