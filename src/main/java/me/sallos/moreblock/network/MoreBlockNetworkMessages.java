package me.sallos.moreblock.network;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.network.message.ApplySeatHeightDebugMessage;
import me.sallos.moreblock.network.message.OpenSeatHeightDebugScreenMessage;
import me.sallos.moreblock.network.message.SyncImportedEntityManifestLoginMessage;
import me.sallos.moreblock.network.message.SyncImportedEntityManifestToServerMessage;
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
        SyncImportedEntityManifestLoginMessage.register(2);
        Moreblock.addNetworkMessage(
                SyncImportedBlockManifestToServerMessage.class,
                SyncImportedBlockManifestToServerMessage::encode,
                SyncImportedBlockManifestToServerMessage::decode,
                SyncImportedBlockManifestToServerMessage::handle
        );
        Moreblock.addNetworkMessage(
                SyncImportedEntityManifestToServerMessage.class,
                SyncImportedEntityManifestToServerMessage::encode,
                SyncImportedEntityManifestToServerMessage::decode,
                SyncImportedEntityManifestToServerMessage::handle
        );
        Moreblock.addNetworkMessage(
                OpenSeatHeightDebugScreenMessage.class,
                OpenSeatHeightDebugScreenMessage::encode,
                OpenSeatHeightDebugScreenMessage::decode,
                OpenSeatHeightDebugScreenMessage::handle
        );
        Moreblock.addNetworkMessage(
                ApplySeatHeightDebugMessage.class,
                ApplySeatHeightDebugMessage::encode,
                ApplySeatHeightDebugMessage::decode,
                ApplySeatHeightDebugMessage::handle
        );
    }
}
