package me.sallos.moreblock.mixin.client;

import me.sallos.moreblock.client.MoreBlockWorldPackGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "doWorldLoad", at = @At("HEAD"))
    private void moreblock$rememberImportedBlockPacks(String levelName, LevelStorageSource.LevelStorageAccess storageAccess, PackRepository packRepository, WorldStem worldStem, boolean demo, CallbackInfo callbackInfo) {
        MoreBlockWorldPackGuard.rememberCurrentPacks(storageAccess);
    }
}
