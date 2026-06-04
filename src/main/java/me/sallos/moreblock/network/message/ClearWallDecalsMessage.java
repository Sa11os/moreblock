package me.sallos.moreblock.network.message;

import me.sallos.moreblock.wall.WallDecalSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClearWallDecalsMessage(ResourceLocation dimension) {
    public static void encode(ClearWallDecalsMessage message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.dimension());
    }

    public static ClearWallDecalsMessage decode(FriendlyByteBuf buffer) {
        return new ClearWallDecalsMessage(buffer.readResourceLocation());
    }

    public static void handle(ClearWallDecalsMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> WallDecalSystem.clearDimension(message.dimension()));
        context.setPacketHandled(true);
    }
}
