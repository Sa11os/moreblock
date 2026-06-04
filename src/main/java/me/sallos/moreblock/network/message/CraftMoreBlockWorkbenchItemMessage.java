package me.sallos.moreblock.network.message;

import me.sallos.moreblock.workbench.MoreBlockWorkbenchCrafting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CraftMoreBlockWorkbenchItemMessage(String type, String registryName) {
    public static void encode(CraftMoreBlockWorkbenchItemMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.type());
        buffer.writeUtf(message.registryName());
    }

    public static CraftMoreBlockWorkbenchItemMessage decode(FriendlyByteBuf buffer) {
        return new CraftMoreBlockWorkbenchItemMessage(buffer.readUtf(), buffer.readUtf());
    }

    public static void handle(CraftMoreBlockWorkbenchItemMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                MoreBlockWorkbenchCrafting.craft(context.getSender(), message.type(), message.registryName());
            }
        });
        context.setPacketHandled(true);
    }
}
