package me.sallos.moreblock.client;

import me.sallos.moreblock.client.screen.SeatHeightDebugScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SeatHeightDebugClient {
    private SeatHeightDebugClient() {
    }

    public static void openScreen(String displayName, String registryName, double seatHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.setScreen(new SeatHeightDebugScreen(displayName, registryName, seatHeight));
    }
}
