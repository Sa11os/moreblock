package me.sallos.moreblock.network.message;

import me.sallos.moreblock.client.SeatHeightDebugClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("null")
public final class OpenSeatHeightDebugScreenMessage {
    private static final int MAX_DISPLAY_NAME_LENGTH = 128;
    private static final int MAX_REGISTRY_NAME_LENGTH = 128;

    private final String displayName;
    private final String registryName;
    private final double seatHeight;

    public OpenSeatHeightDebugScreenMessage(String displayName, String registryName, double seatHeight) {
        this.displayName = displayName;
        this.registryName = registryName;
        this.seatHeight = seatHeight;
    }

    public static void encode(OpenSeatHeightDebugScreenMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.displayName, MAX_DISPLAY_NAME_LENGTH);
        buffer.writeUtf(message.registryName, MAX_REGISTRY_NAME_LENGTH);
        buffer.writeDouble(message.seatHeight);
    }

    public static OpenSeatHeightDebugScreenMessage decode(FriendlyByteBuf buffer) {
        return new OpenSeatHeightDebugScreenMessage(
                buffer.readUtf(MAX_DISPLAY_NAME_LENGTH),
                buffer.readUtf(MAX_REGISTRY_NAME_LENGTH),
                buffer.readDouble()
        );
    }

    public static void handle(OpenSeatHeightDebugScreenMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                SeatHeightDebugClient.openScreen(message.displayName, message.registryName, message.seatHeight)));
        context.setPacketHandled(true);
    }
}
