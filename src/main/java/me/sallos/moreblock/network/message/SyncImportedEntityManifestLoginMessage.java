package me.sallos.moreblock.network.message;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPackDownloads;
import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.network.ImportedEntityPackSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.HandshakeHandler;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@SuppressWarnings("null")
public final class SyncImportedEntityManifestLoginMessage implements IntSupplier {
    private static final Object CLIENT_STATE_LOCK = new Object();
    private static final ChunkedLoginDownloadPayload.AssemblyState CLIENT_DOWNLOAD_STATE = new ChunkedLoginDownloadPayload.AssemblyState();
    private static List<ImportedEntityPacks.PackManifestEntry> clientPendingServerManifest = List.of();

    private int loginIndex;
    private final boolean hasManifest;
    private final boolean finalPacket;
    private final List<ImportedEntityPacks.PackManifestEntry> entries;
    private final ChunkedLoginDownloadPayload.DownloadChunk downloadChunk;

    public SyncImportedEntityManifestLoginMessage() {
        this(true, true, ImportedEntityPacks.getPackManifest(), null);
    }

    public SyncImportedEntityManifestLoginMessage(boolean hasManifest,
                                                  boolean finalPacket,
                                                  List<ImportedEntityPacks.PackManifestEntry> entries,
                                                  ChunkedLoginDownloadPayload.DownloadChunk downloadChunk) {
        this.hasManifest = hasManifest;
        this.finalPacket = finalPacket;
        this.entries = List.copyOf(entries);
        this.downloadChunk = downloadChunk;
    }

    public static void register(int messageId) {
        Moreblock.LOGIN_PACKET_HANDLER.messageBuilder(SyncImportedEntityManifestLoginMessage.class, messageId, NetworkDirection.LOGIN_TO_CLIENT)
                .loginIndex(SyncImportedEntityManifestLoginMessage::getLoginIndex, SyncImportedEntityManifestLoginMessage::setLoginIndex)
                .encoder(SyncImportedEntityManifestLoginMessage::encode)
                .decoder(SyncImportedEntityManifestLoginMessage::decode)
                .buildLoginPacketList(isLocal -> buildLoginPacketList())
                .consumerNetworkThread(SyncImportedEntityManifestLoginMessage::handleServerManifestOnClient)
                .add();

        Moreblock.LOGIN_PACKET_HANDLER.messageBuilder(Acknowledge.class, messageId + 1, NetworkDirection.LOGIN_TO_SERVER)
                .loginIndex(Acknowledge::getLoginIndex, Acknowledge::setLoginIndex)
                .encoder(Acknowledge::encode)
                .decoder(Acknowledge::decode)
                .consumerNetworkThread(HandshakeHandler.indexFirst((handler, message, contextSupplier) -> Acknowledge.handleClientManifestOnServer(message, contextSupplier)))
                .add();
    }

    private static List<Pair<String, SyncImportedEntityManifestLoginMessage>> buildLoginPacketList() {
        List<ImportedEntityPacks.PackManifestEntry> manifestEntries = ImportedEntityPacks.getPackManifest();
        List<ChunkedLoginDownloadPayload.DownloadChunk> chunks = ChunkedLoginDownloadPayload.split(ImportedBlockPackDownloads.buildServerEntityDownloadEntries());
        if (chunks.isEmpty()) {
            return List.of(Pair.of(
                    SyncImportedEntityManifestLoginMessage.class.getName(),
                    new SyncImportedEntityManifestLoginMessage(true, true, manifestEntries, null)
            ));
        }

        List<Pair<String, SyncImportedEntityManifestLoginMessage>> packets = new java.util.ArrayList<>(chunks.size());
        for (int index = 0; index < chunks.size(); index++) {
            boolean hasManifest = index == 0;
            boolean finalPacket = index == chunks.size() - 1;
            packets.add(Pair.of(
                    SyncImportedEntityManifestLoginMessage.class.getName(),
                    new SyncImportedEntityManifestLoginMessage(
                            hasManifest,
                            finalPacket,
                            hasManifest ? manifestEntries : List.of(),
                            chunks.get(index)
                    )
            ));
        }
        return List.copyOf(packets);
    }

    public static void encode(SyncImportedEntityManifestLoginMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.hasManifest);
        buffer.writeBoolean(message.finalPacket);
        if (message.hasManifest) {
            SyncImportedEntityManifestToServerMessage.writeManifest(message.entries, buffer);
        }
        buffer.writeBoolean(message.downloadChunk != null);
        if (message.downloadChunk != null) {
            ChunkedLoginDownloadPayload.write(message.downloadChunk, buffer);
        }
    }

    public static SyncImportedEntityManifestLoginMessage decode(FriendlyByteBuf buffer) {
        boolean hasManifest = buffer.readBoolean();
        boolean finalPacket = buffer.readBoolean();
        List<ImportedEntityPacks.PackManifestEntry> entries = hasManifest
                ? SyncImportedEntityManifestToServerMessage.readManifest(buffer)
                : List.of();
        ChunkedLoginDownloadPayload.DownloadChunk downloadChunk = buffer.readBoolean()
                ? ChunkedLoginDownloadPayload.read(buffer)
                : null;
        return new SyncImportedEntityManifestLoginMessage(hasManifest, finalPacket, entries, downloadChunk);
    }

    public static void handleServerManifestOnClient(SyncImportedEntityManifestLoginMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        List<ImportedEntityPacks.PackManifestEntry> serverManifest = List.of();
        List<ImportedBlockPackDownloads.DownloadEntry> downloadEntries = List.of();
        boolean shouldCompare = false;

        synchronized (CLIENT_STATE_LOCK) {
            if (message.hasManifest) {
                clientPendingServerManifest = List.copyOf(message.entries);
                CLIENT_DOWNLOAD_STATE.reset();
            }
            if (message.downloadChunk != null) {
                CLIENT_DOWNLOAD_STATE.append(message.downloadChunk);
            }
            if (message.finalPacket) {
                serverManifest = clientPendingServerManifest;
                downloadEntries = CLIENT_DOWNLOAD_STATE.snapshotCompletedEntries();
                clientPendingServerManifest = List.of();
                CLIENT_DOWNLOAD_STATE.reset();
                shouldCompare = true;
            }
        }

        if (shouldCompare) {
            ImportedEntityPackSync.ManifestComparisonResult result = ImportedEntityPackSync.compareManifests(
                    ImportedEntityPacks.getPackManifest(),
                    serverManifest
            );
            if (!result.matches()) {
                ImportedEntityPackSync.rememberClientDisconnectMessage(result.buildDisconnectComponent());
                ImportedEntityPackSync.rememberClientDownloadContext(result, downloadEntries);
                Moreblock.LOGGER.warn("本地更多实体包与服务器不一致：{}\n{}",
                        result.summary(),
                        result.buildDetails());
            }
        }

        Moreblock.LOGIN_PACKET_HANDLER.reply(new Acknowledge(
                message.getLoginIndex(),
                message.finalPacket,
                message.finalPacket ? ImportedEntityPacks.getPackManifest() : List.of()
        ), context);
        context.setPacketHandled(true);
    }

    public int getLoginIndex() {
        return loginIndex;
    }

    public void setLoginIndex(int loginIndex) {
        this.loginIndex = loginIndex;
    }

    @Override
    public int getAsInt() {
        return getLoginIndex();
    }

    public static final class Acknowledge implements IntSupplier {
        private int loginIndex;
        private final boolean finalPacket;
        private final List<ImportedEntityPacks.PackManifestEntry> entries;

        public Acknowledge() {
            this(Integer.MIN_VALUE, false, List.of());
        }

        public Acknowledge(int loginIndex, boolean finalPacket, List<ImportedEntityPacks.PackManifestEntry> entries) {
            this.loginIndex = loginIndex;
            this.finalPacket = finalPacket;
            this.entries = List.copyOf(entries);
        }

        public static void encode(Acknowledge message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.finalPacket);
            if (message.finalPacket) {
                SyncImportedEntityManifestToServerMessage.writeManifest(message.entries, buffer);
            }
        }

        public static Acknowledge decode(FriendlyByteBuf buffer) {
            boolean finalPacket = buffer.readBoolean();
            return new Acknowledge(
                    Integer.MIN_VALUE,
                    finalPacket,
                    finalPacket ? SyncImportedEntityManifestToServerMessage.readManifest(buffer) : List.of()
            );
        }

        public static void handleClientManifestOnServer(Acknowledge message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (message.finalPacket) {
                ImportedEntityPackSync.verifyClientManifestDuringLogin(context.getNetworkManager(), message.entries);
            }
            context.setPacketHandled(true);
        }

        public int getLoginIndex() {
            return loginIndex;
        }

        public void setLoginIndex(int loginIndex) {
            this.loginIndex = loginIndex;
        }

        @Override
        public int getAsInt() {
            return getLoginIndex();
        }
    }
}
