package me.sallos.moreblock.network.message;

import me.sallos.moreblock.Moreblock;
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
    private int loginIndex;
    private final List<ImportedEntityPacks.PackManifestEntry> entries;

    public SyncImportedEntityManifestLoginMessage() {
        this(ImportedEntityPacks.getPackManifest());
    }

    public SyncImportedEntityManifestLoginMessage(List<ImportedEntityPacks.PackManifestEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static void register(int messageId) {
        Moreblock.LOGIN_PACKET_HANDLER.messageBuilder(SyncImportedEntityManifestLoginMessage.class, messageId, NetworkDirection.LOGIN_TO_CLIENT)
                .loginIndex(SyncImportedEntityManifestLoginMessage::getLoginIndex, SyncImportedEntityManifestLoginMessage::setLoginIndex)
                .encoder(SyncImportedEntityManifestLoginMessage::encode)
                .decoder(SyncImportedEntityManifestLoginMessage::decode)
                .buildLoginPacketList(isLocal -> List.of(Pair.of(SyncImportedEntityManifestLoginMessage.class.getName(), new SyncImportedEntityManifestLoginMessage())))
                .consumerNetworkThread(SyncImportedEntityManifestLoginMessage::handleServerManifestOnClient)
                .add();

        Moreblock.LOGIN_PACKET_HANDLER.messageBuilder(Acknowledge.class, messageId + 1, NetworkDirection.LOGIN_TO_SERVER)
                .loginIndex(Acknowledge::getLoginIndex, Acknowledge::setLoginIndex)
                .encoder(Acknowledge::encode)
                .decoder(Acknowledge::decode)
                .consumerNetworkThread(HandshakeHandler.indexFirst((handler, message, contextSupplier) -> Acknowledge.handleClientManifestOnServer(message, contextSupplier)))
                .add();
    }

    public static void encode(SyncImportedEntityManifestLoginMessage message, FriendlyByteBuf buffer) {
        SyncImportedEntityManifestToServerMessage.writeManifest(message.entries, buffer);
    }

    public static SyncImportedEntityManifestLoginMessage decode(FriendlyByteBuf buffer) {
        return new SyncImportedEntityManifestLoginMessage(SyncImportedEntityManifestToServerMessage.readManifest(buffer));
    }

    public static void handleServerManifestOnClient(SyncImportedEntityManifestLoginMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ImportedEntityPackSync.ManifestComparisonResult result = ImportedEntityPackSync.compareManifests(
                ImportedEntityPacks.getPackManifest(),
                message.entries
        );
        if (!result.matches()) {
            ImportedEntityPackSync.rememberClientDisconnectMessage(result.buildDisconnectComponent());
            Moreblock.LOGGER.warn("本地更多实体包与服务器不一致：{}\n{}",
                    result.summary(),
                    result.buildDetails());
        }
        Moreblock.LOGIN_PACKET_HANDLER.reply(new Acknowledge(message.getLoginIndex(), ImportedEntityPacks.getPackManifest()), context);
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
        private final List<ImportedEntityPacks.PackManifestEntry> entries;

        public Acknowledge() {
            this(Integer.MIN_VALUE, List.of());
        }

        public Acknowledge(int loginIndex, List<ImportedEntityPacks.PackManifestEntry> entries) {
            this.loginIndex = loginIndex;
            this.entries = List.copyOf(entries);
        }

        public static void encode(Acknowledge message, FriendlyByteBuf buffer) {
            SyncImportedEntityManifestToServerMessage.writeManifest(message.entries, buffer);
        }

        public static Acknowledge decode(FriendlyByteBuf buffer) {
            return new Acknowledge(Integer.MIN_VALUE, SyncImportedEntityManifestToServerMessage.readManifest(buffer));
        }

        public static void handleClientManifestOnServer(Acknowledge message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            ImportedEntityPackSync.verifyClientManifestDuringLogin(context.getNetworkManager(), message.entries);
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
