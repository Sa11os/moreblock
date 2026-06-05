package me.sallos.moreblock.network.message;

import me.sallos.moreblock.entity.SeatEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ApplySeatHeightDebugMessage {
    private final double seatHeight;

    public ApplySeatHeightDebugMessage(double seatHeight) {
        this.seatHeight = Mth.clamp(seatHeight, -2.0d, 2.0d);
    }

    public static void encode(ApplySeatHeightDebugMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.seatHeight);
    }

    public static ApplySeatHeightDebugMessage decode(FriendlyByteBuf buffer) {
        return new ApplySeatHeightDebugMessage(buffer.readDouble());
    }

    public static void handle(ApplySeatHeightDebugMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) {
                return;
            }
            if (sender.getVehicle() instanceof SeatEntity seatEntity && seatEntity.hasPassenger(sender)) {
                // 仅允许调试玩家当前正在乘坐的座椅，避免误改其他实体。
                seatEntity.setConfiguredSeatHeight(message.seatHeight);
            }
        });
        context.setPacketHandled(true);
    }
}
