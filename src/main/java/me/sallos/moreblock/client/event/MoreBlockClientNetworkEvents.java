package me.sallos.moreblock.client.event;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.network.message.SyncImportedBlockManifestToServerMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Moreblock.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MoreBlockClientNetworkEvents {
    private MoreBlockClientNetworkEvents() {
    }

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Moreblock.PACKET_HANDLER.sendToServer(new SyncImportedBlockManifestToServerMessage(ImportedBlockPacks.getPackManifest()));
    }
}