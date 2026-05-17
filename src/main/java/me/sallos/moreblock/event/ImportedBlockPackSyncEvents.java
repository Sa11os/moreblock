package me.sallos.moreblock.event;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.network.ImportedBlockPackSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Moreblock.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ImportedBlockPackSyncEvents {
    private ImportedBlockPackSyncEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ImportedBlockPackSync.beginVerification(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ImportedBlockPackSync.clearPending(serverPlayer);
        }
    }
}