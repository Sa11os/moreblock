package me.sallos.moreblock.network.message;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.network.ImportedBlockPackSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.HandshakeHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.IntSupplier;

@SuppressWarnings("null")
public final class SyncImportedBlockManifestLoginMessage implements IntSupplier {
    private int loginIndex;
    private final List<ImportedBlockPacks.PackManifestEntry> entries;

    public SyncImportedBlockManifestLoginMessage() {
        this(ImportedBlockPacks.getPackManifest());
    }

    public SyncImportedBlockManifestLoginMessage(List<ImportedBlockPacks.PackManifestEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static void register(int messageId) {
        Moreblock.LOGIN_PACKET_HANDLER.messageBuilder(SyncImportedBlockManifestLoginMessage.class, messageId, NetworkDirection.LOGIN_TO_CLIENT)
                .loginIndex(SyncImportedBlockManifestLoginMessage::getLoginIndex, SyncImportedBlockManifestLoginMessage::setLoginIndex)
                .encoder(SyncImportedBlockManifestLoginMessage::encode)
                .decoder(SyncImportedBlockManifestLoginMessage::decode)
                .buildLoginPacketList(isLocal -> List.of(Pair.of(SyncImportedBlockManifestLoginMessage.class.getName(), new SyncImportedBlockManifestLoginMessage())))
                .consumerNetworkThread(SyncImportedBlockManifestLoginMessage::handleServerManifestOnClient)
                .add();

        Moreblock.LOGIN_PACKET_HANDLER.messageBuilder(Acknowledge.class, messageId + 1, NetworkDirection.LOGIN_TO_SERVER)
                .loginIndex(Acknowledge::getLoginIndex, Acknowledge::setLoginIndex)
                .encoder(Acknowledge::encode)
                .decoder(Acknowledge::decode)
                .consumerNetworkThread(HandshakeHandler.indexFirst((handler, message, contextSupplier) -> Acknowledge.handleClientManifestOnServer(message, contextSupplier)))
                .add();
    }

    public static void encode(SyncImportedBlockManifestLoginMessage message, FriendlyByteBuf buffer) {
        SyncImportedBlockManifestToServerMessage.writeManifest(message.entries, buffer);
    }

    public static SyncImportedBlockManifestLoginMessage decode(FriendlyByteBuf buffer) {
        return new SyncImportedBlockManifestLoginMessage(SyncImportedBlockManifestToServerMessage.readManifest(buffer));
    }

    public static void handleServerManifestOnClient(SyncImportedBlockManifestLoginMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ImportedBlockPackSync.ManifestComparisonResult result = ImportedBlockPackSync.compareManifests(
                ImportedBlockPacks.getPackManifest(),
                message.entries
        );
        if (!result.matches()) {
            ImportedBlockPackSync.rememberClientDisconnectMessage(result.buildDisconnectComponent());
            Moreblock.LOGGER.warn("本地更多方块包与服务器不一致：{}\n{}",
                    result.summary(),
                    result.buildDetails());
        }
        Moreblock.LOGIN_PACKET_HANDLER.reply(new Acknowledge(message.getLoginIndex(), ImportedBlockPacks.getPackManifest()), context);
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
        private final List<ImportedBlockPacks.PackManifestEntry> entries;

        public Acknowledge() {
            this(Integer.MIN_VALUE, List.of());
        }

        public Acknowledge(int loginIndex, List<ImportedBlockPacks.PackManifestEntry> entries) {
            this.loginIndex = loginIndex;
            this.entries = List.copyOf(entries);
        }

        public static void encode(Acknowledge message, FriendlyByteBuf buffer) {
            SyncImportedBlockManifestToServerMessage.writeManifest(message.entries, buffer);
        }

        public static Acknowledge decode(FriendlyByteBuf buffer) {
            return new Acknowledge(Integer.MIN_VALUE, SyncImportedBlockManifestToServerMessage.readManifest(buffer));
        }

        public static void handleClientManifestOnServer(Acknowledge message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            ImportedBlockPackSync.verifyClientManifestDuringLogin(context.getNetworkManager(), message.entries);
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
