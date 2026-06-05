package me.sallos.moreblock.network.message;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPackDownloads;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.network.ImportedBlockPackSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.HandshakeHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class SyncImportedBlockManifestLoginMessage implements IntSupplier {
    private static final Object CLIENT_STATE_LOCK = new Object();
    private static final ChunkedLoginDownloadPayload.AssemblyState CLIENT_DOWNLOAD_STATE = new ChunkedLoginDownloadPayload.AssemblyState();
    private static List<ImportedBlockPacks.PackManifestEntry> clientPendingServerManifest = List.of();

    private int loginIndex;
    private final boolean hasManifest;
    private final boolean finalPacket;
    private final List<ImportedBlockPacks.PackManifestEntry> entries;
    private final ChunkedLoginDownloadPayload.DownloadChunk downloadChunk;

    public SyncImportedBlockManifestLoginMessage() {
        this(true, true, ImportedBlockPacks.getPackManifest(), null);
    }

    public SyncImportedBlockManifestLoginMessage(boolean hasManifest,
                                                 boolean finalPacket,
                                                 List<ImportedBlockPacks.PackManifestEntry> entries,
                                                 ChunkedLoginDownloadPayload.DownloadChunk downloadChunk) {
        this.hasManifest = hasManifest;
        this.finalPacket = finalPacket;
        this.entries = List.copyOf(entries);
        this.downloadChunk = downloadChunk;
    }

    public static void register(int messageId) {
        Moreblock.LOGIN_PACKET_HANDLER.messageBuilder(SyncImportedBlockManifestLoginMessage.class, messageId, NetworkDirection.LOGIN_TO_CLIENT)
                .loginIndex(SyncImportedBlockManifestLoginMessage::getLoginIndex, SyncImportedBlockManifestLoginMessage::setLoginIndex)
                .encoder(SyncImportedBlockManifestLoginMessage::encode)
                .decoder(SyncImportedBlockManifestLoginMessage::decode)
                .buildLoginPacketList(isLocal -> buildLoginPacketList())
                .consumerNetworkThread(SyncImportedBlockManifestLoginMessage::handleServerManifestOnClient)
                .add();

        Moreblock.LOGIN_PACKET_HANDLER.messageBuilder(Acknowledge.class, messageId + 1, NetworkDirection.LOGIN_TO_SERVER)
                .loginIndex(Acknowledge::getLoginIndex, Acknowledge::setLoginIndex)
                .encoder(Acknowledge::encode)
                .decoder(Acknowledge::decode)
                .consumerNetworkThread(HandshakeHandler.indexFirst((handler, message, contextSupplier) -> Acknowledge.handleClientManifestOnServer(message, contextSupplier)))
                .add();
    }

    private static List<Pair<String, SyncImportedBlockManifestLoginMessage>> buildLoginPacketList() {
        List<ImportedBlockPacks.PackManifestEntry> manifestEntries = ImportedBlockPacks.getPackManifest();
        List<ChunkedLoginDownloadPayload.DownloadChunk> chunks = ChunkedLoginDownloadPayload.split(ImportedBlockPackDownloads.buildServerBlockDownloadEntries());
        if (chunks.isEmpty()) {
            return List.of(Pair.of(
                    SyncImportedBlockManifestLoginMessage.class.getName(),
                    new SyncImportedBlockManifestLoginMessage(true, true, manifestEntries, null)
            ));
        }

        List<Pair<String, SyncImportedBlockManifestLoginMessage>> packets = new java.util.ArrayList<>(chunks.size());
        for (int index = 0; index < chunks.size(); index++) {
            boolean hasManifest = index == 0;
            boolean finalPacket = index == chunks.size() - 1;
            packets.add(Pair.of(
                    SyncImportedBlockManifestLoginMessage.class.getName(),
                    new SyncImportedBlockManifestLoginMessage(
                            hasManifest,
                            finalPacket,
                            hasManifest ? manifestEntries : List.of(),
                            chunks.get(index)
                    )
            ));
        }
        return List.copyOf(packets);
    }

    public static void encode(SyncImportedBlockManifestLoginMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.hasManifest);
        buffer.writeBoolean(message.finalPacket);
        if (message.hasManifest) {
            SyncImportedBlockManifestToServerMessage.writeManifest(message.entries, buffer);
        }
        buffer.writeBoolean(message.downloadChunk != null);
        if (message.downloadChunk != null) {
            ChunkedLoginDownloadPayload.write(message.downloadChunk, buffer);
        }
    }

    public static SyncImportedBlockManifestLoginMessage decode(FriendlyByteBuf buffer) {
        boolean hasManifest = buffer.readBoolean();
        boolean finalPacket = buffer.readBoolean();
        List<ImportedBlockPacks.PackManifestEntry> entries = hasManifest
                ? SyncImportedBlockManifestToServerMessage.readManifest(buffer)
                : List.of();
        ChunkedLoginDownloadPayload.DownloadChunk downloadChunk = buffer.readBoolean()
                ? ChunkedLoginDownloadPayload.read(buffer)
                : null;
        return new SyncImportedBlockManifestLoginMessage(hasManifest, finalPacket, entries, downloadChunk);
    }

    public static void handleServerManifestOnClient(SyncImportedBlockManifestLoginMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        List<ImportedBlockPacks.PackManifestEntry> serverManifest = List.of();
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
            ImportedBlockPackSync.ManifestComparisonResult result = ImportedBlockPackSync.compareManifests(
                    ImportedBlockPacks.getPackManifest(),
                    serverManifest
            );
            if (!result.matches()) {
                ImportedBlockPackSync.rememberClientDisconnectMessage(result.buildDisconnectComponent());
                ImportedBlockPackSync.rememberClientDownloadContext(result, downloadEntries);
                Moreblock.LOGGER.warn("本地更多方块包与服务器不一致：{}\n{}",
                        result.summary(),
                        result.buildDetails());
            }
        }

        Moreblock.LOGIN_PACKET_HANDLER.reply(new Acknowledge(
                message.getLoginIndex(),
                message.finalPacket,
                message.finalPacket ? ImportedBlockPacks.getPackManifest() : List.of()
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
        private final List<ImportedBlockPacks.PackManifestEntry> entries;

        public Acknowledge() {
            this(Integer.MIN_VALUE, false, List.of());
        }

        public Acknowledge(int loginIndex, boolean finalPacket, List<ImportedBlockPacks.PackManifestEntry> entries) {
            this.loginIndex = loginIndex;
            this.finalPacket = finalPacket;
            this.entries = List.copyOf(entries);
        }

        public static void encode(Acknowledge message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.finalPacket);
            if (message.finalPacket) {
                SyncImportedBlockManifestToServerMessage.writeManifest(message.entries, buffer);
            }
        }

        public static Acknowledge decode(FriendlyByteBuf buffer) {
            boolean finalPacket = buffer.readBoolean();
            return new Acknowledge(
                    Integer.MIN_VALUE,
                    finalPacket,
                    finalPacket ? SyncImportedBlockManifestToServerMessage.readManifest(buffer) : List.of()
            );
        }

        public static void handleClientManifestOnServer(Acknowledge message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (message.finalPacket) {
                ImportedBlockPackSync.verifyClientManifestDuringLogin(context.getNetworkManager(), message.entries);
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
