package me.sallos.moreblock.client;

import me.sallos.moreblock.client.screen.MoreBlockWorkbenchScreen;
import net.minecraft.client.Minecraft;

public final class MoreBlockWorkbenchClient {
    private MoreBlockWorkbenchClient() {
    }

    public static void openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.setScreen(new MoreBlockWorkbenchScreen());
    }
}
