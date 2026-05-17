package me.sallos.moreblock.network;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.network.message.SyncImportedBlockManifestLoginMessage;
import me.sallos.moreblock.network.message.SyncImportedBlockManifestToServerMessage;

public final class MoreBlockNetworkMessages {
    private static boolean registered = false;

    private MoreBlockNetworkMessages() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        SyncImportedBlockManifestLoginMessage.register(0);
        Moreblock.addNetworkMessage(
                SyncImportedBlockManifestToServerMessage.class,
                SyncImportedBlockManifestToServerMessage::encode,
                SyncImportedBlockManifestToServerMessage::decode,
                SyncImportedBlockManifestToServerMessage::handle
        );
    }
}