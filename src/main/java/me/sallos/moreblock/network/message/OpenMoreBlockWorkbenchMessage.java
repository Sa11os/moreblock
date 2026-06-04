package me.sallos.moreblock.network.message;

import me.sallos.moreblock.client.MoreBlockWorkbenchClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenMoreBlockWorkbenchMessage() {
    public static void encode(OpenMoreBlockWorkbenchMessage message, FriendlyByteBuf buffer) {
    }

    public static OpenMoreBlockWorkbenchMessage decode(FriendlyByteBuf buffer) {
        return new OpenMoreBlockWorkbenchMessage();
    }

    public static void handle(OpenMoreBlockWorkbenchMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MoreBlockWorkbenchClient::openScreen));
        context.setPacketHandled(true);
    }
}
