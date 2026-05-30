package me.sallos.moreblock.network.message;

import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.network.ImportedEntityPackSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("null")
public final class SyncImportedEntityManifestToServerMessage {
    private static final int MAX_ENTRY_COUNT = 512;
    private static final int MAX_REGISTRY_NAME_LENGTH = 128;
    private static final int MAX_DISPLAY_NAME_LENGTH = 128;
    private static final int MAX_SOURCE_NAME_LENGTH = 128;
    private static final int MAX_FINGERPRINT_LENGTH = 64;

    private final List<ImportedEntityPacks.PackManifestEntry> entries;

    public SyncImportedEntityManifestToServerMessage(List<ImportedEntityPacks.PackManifestEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static void encode(SyncImportedEntityManifestToServerMessage message, FriendlyByteBuf buffer) {
        writeManifest(message.entries, buffer);
    }

    public static SyncImportedEntityManifestToServerMessage decode(FriendlyByteBuf buffer) {
        return new SyncImportedEntityManifestToServerMessage(readManifest(buffer));
    }

    public static void writeManifest(List<ImportedEntityPacks.PackManifestEntry> entries, FriendlyByteBuf buffer) {
        buffer.writeVarInt(entries.size());
        for (ImportedEntityPacks.PackManifestEntry entry : entries) {
            buffer.writeUtf(entry.registryName(), MAX_REGISTRY_NAME_LENGTH);
            buffer.writeUtf(entry.displayName(), MAX_DISPLAY_NAME_LENGTH);
            buffer.writeUtf(entry.sourceName(), MAX_SOURCE_NAME_LENGTH);
            buffer.writeUtf(entry.fingerprint(), MAX_FINGERPRINT_LENGTH);
        }
    }

    public static List<ImportedEntityPacks.PackManifestEntry> readManifest(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_ENTRY_COUNT) {
            throw new IllegalArgumentException("更多实体包清单数量非法: " + size);
        }

        List<ImportedEntityPacks.PackManifestEntry> entries = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            entries.add(new ImportedEntityPacks.PackManifestEntry(
                    buffer.readUtf(MAX_REGISTRY_NAME_LENGTH),
                    buffer.readUtf(MAX_DISPLAY_NAME_LENGTH),
                    buffer.readUtf(MAX_SOURCE_NAME_LENGTH),
                    buffer.readUtf(MAX_FINGERPRINT_LENGTH)
            ));
        }
        return entries;
    }

    public static void handle(SyncImportedEntityManifestToServerMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                ImportedEntityPackSync.verifyClientManifest(sender, message.entries);
            }
        });
        context.setPacketHandled(true);
    }
}
