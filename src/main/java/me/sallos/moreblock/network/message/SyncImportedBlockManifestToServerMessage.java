package me.sallos.moreblock.network.message;

import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.network.ImportedBlockPackSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("null")
public final class SyncImportedBlockManifestToServerMessage {
    private static final int MAX_ENTRY_COUNT = 512;
    private static final int MAX_REGISTRY_NAME_LENGTH = 128;
    private static final int MAX_DISPLAY_NAME_LENGTH = 128;
    private static final int MAX_SOURCE_NAME_LENGTH = 128;
    private static final int MAX_FINGERPRINT_LENGTH = 64;

    private final List<ImportedBlockPacks.PackManifestEntry> entries;

    public SyncImportedBlockManifestToServerMessage(List<ImportedBlockPacks.PackManifestEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static void encode(SyncImportedBlockManifestToServerMessage message, FriendlyByteBuf buffer) {
        writeManifest(message.entries, buffer);
    }

    public static SyncImportedBlockManifestToServerMessage decode(FriendlyByteBuf buffer) {
        return new SyncImportedBlockManifestToServerMessage(readManifest(buffer));
    }

    public static void writeManifest(List<ImportedBlockPacks.PackManifestEntry> entries, FriendlyByteBuf buffer) {
        buffer.writeVarInt(entries.size());
        for (ImportedBlockPacks.PackManifestEntry entry : entries) {
            buffer.writeUtf(entry.registryName(), MAX_REGISTRY_NAME_LENGTH);
            buffer.writeUtf(entry.displayName(), MAX_DISPLAY_NAME_LENGTH);
            buffer.writeUtf(entry.sourceName(), MAX_SOURCE_NAME_LENGTH);
            buffer.writeUtf(entry.fingerprint(), MAX_FINGERPRINT_LENGTH);
        }
    }

    public static List<ImportedBlockPacks.PackManifestEntry> readManifest(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_ENTRY_COUNT) {
            throw new IllegalArgumentException("更多方块包清单数量非法: " + size);
        }

        List<ImportedBlockPacks.PackManifestEntry> entries = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            entries.add(new ImportedBlockPacks.PackManifestEntry(
                    buffer.readUtf(MAX_REGISTRY_NAME_LENGTH),
                    buffer.readUtf(MAX_DISPLAY_NAME_LENGTH),
                    buffer.readUtf(MAX_SOURCE_NAME_LENGTH),
                    buffer.readUtf(MAX_FINGERPRINT_LENGTH)
            ));
        }
        return entries;
    }

    public static void handle(SyncImportedBlockManifestToServerMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                ImportedBlockPackSync.verifyClientManifest(sender, message.entries);
            }
        });
        context.setPacketHandled(true);
    }
}
