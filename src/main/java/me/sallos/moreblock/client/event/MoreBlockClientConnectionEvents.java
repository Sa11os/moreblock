package me.sallos.moreblock.client.event;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.client.screen.MoreBlockPackDownloadScreen;
import me.sallos.moreblock.network.ImportedBlockPackSync;
import me.sallos.moreblock.network.ImportedEntityPackSync;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Moreblock.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MoreBlockClientConnectionEvents {
    private MoreBlockClientConnectionEvents() {
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof DisconnectedScreen)) {
            return;
        }

        Component message = ImportedBlockPackSync.consumeRememberedClientDisconnectMessage();
        ImportedBlockPackSync.DownloadContext downloadContext = null;
        Component title = null;
        if (message != null) {
            downloadContext = ImportedBlockPackSync.consumeRememberedClientDownloadContext();
            title = Objects.requireNonNull(Component.translatable("disconnect.moreblock.configured_pack.title"));
        } else {
            message = ImportedEntityPackSync.consumeRememberedClientDisconnectMessage();
            if (message != null) {
                downloadContext = ImportedEntityPackSync.consumeRememberedClientDownloadContext();
                title = Objects.requireNonNull(Component.translatable("disconnect.moreblock.configured_entity_pack.title"));
            }
        }
        if (message == null) {
            return;
        }

        Screen currentScreen = event.getCurrentScreen();
        Screen parent = currentScreen == null ? new TitleScreen() : Objects.requireNonNull(currentScreen);
        if (downloadContext != null && !downloadContext.entries().isEmpty()) {
            event.setNewScreen(new MoreBlockPackDownloadScreen(parent, message, downloadContext));
            return;
        }
        event.setNewScreen(new DisconnectedScreen(parent, title, message));
    }
}
