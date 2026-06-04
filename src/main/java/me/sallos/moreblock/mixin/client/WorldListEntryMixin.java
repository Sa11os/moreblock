package me.sallos.moreblock.mixin.client;

import me.sallos.moreblock.client.MoreBlockWorldPackGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListEntryMixin {
    @Shadow
    public abstract String getLevelName();

    @Shadow
    private SelectWorldScreen screen;

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void moreblock$confirmMissingImportedBlockPacks(CallbackInfo callbackInfo) {
        if (!MoreBlockWorldPackGuard.confirmOrContinue(screen, getLevelName(), () -> Minecraft.getInstance().createWorldOpenFlows().loadLevel(screen, getLevelName()))) {
            callbackInfo.cancel();
        }
    }
}
