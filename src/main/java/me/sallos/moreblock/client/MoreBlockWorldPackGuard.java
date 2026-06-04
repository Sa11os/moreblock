package me.sallos.moreblock.client;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.network.ImportedBlockPackSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public final class MoreBlockWorldPackGuard {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String MANIFEST_FILE_NAME = "moreblock_block_packs.json";

    private MoreBlockWorldPackGuard() {
    }

    public static boolean confirmOrContinue(Screen parent, String levelId, Runnable continueLoad) {
        LevelStorageSource.LevelStorageAccess access = null;
        try {
            access = Minecraft.getInstance().getLevelSource().createAccess(levelId);
            List<ImportedBlockPacks.PackManifestEntry> savedManifest = readSavedManifest(access);
            if (savedManifest.isEmpty()) {
                writeCurrentManifest(access);
                return true;
            }

            ImportedBlockPackSync.ManifestComparisonResult result = ImportedBlockPackSync.compareManifests(ImportedBlockPacks.getPackManifest(), savedManifest);
            if (result.matches()) {
                writeCurrentManifest(access);
                return true;
            }

            Minecraft.getInstance().setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    continueLoad.run();
                    return;
                }
                Minecraft.getInstance().setScreen(parent);
            }, Component.translatable("screen.moreblock.world_pack_guard.title"), buildMessage(result), Component.translatable("screen.moreblock.world_pack_guard.continue"), Component.translatable("screen.moreblock.world_pack_guard.back")));
            return false;
        } catch (Exception exception) {
            Moreblock.LOGGER.warn("检查存档 MoreBlock 导入方块包清单失败，继续进入存档: {}", levelId, exception);
            return true;
        } finally {
            closeAccess(access);
        }
    }

    public static void rememberCurrentPacks(LevelStorageSource.LevelStorageAccess access) {
        try {
            writeCurrentManifest(access);
        } catch (IOException exception) {
            Moreblock.LOGGER.warn("写入存档 MoreBlock 导入方块包清单失败: {}", access.getLevelId(), exception);
        }
    }

    private static Component buildMessage(ImportedBlockPackSync.ManifestComparisonResult result) {
        return Component.translatable("screen.moreblock.world_pack_guard.message", result.buildDetails());
    }

    private static List<ImportedBlockPacks.PackManifestEntry> readSavedManifest(LevelStorageSource.LevelStorageAccess access) throws IOException {
        Path manifestFile = manifestFile(access);
        if (!Files.isRegularFile(manifestFile)) {
            return List.of();
        }

        try (Reader reader = Files.newBufferedReader(manifestFile)) {
            List<ImportedBlockPacks.PackManifestEntry> manifest = GSON.fromJson(reader, new TypeToken<List<ImportedBlockPacks.PackManifestEntry>>() {
            }.getType());
            return manifest == null ? List.of() : manifest;
        }
    }

    private static void writeCurrentManifest(LevelStorageSource.LevelStorageAccess access) throws IOException {
        Path manifestFile = manifestFile(access);
        Files.createDirectories(manifestFile.getParent());
        try (Writer writer = Files.newBufferedWriter(manifestFile)) {
            GSON.toJson(ImportedBlockPacks.getPackManifest(), writer);
        }
    }

    private static Path manifestFile(LevelStorageSource.LevelStorageAccess access) {
        return access.getLevelPath(LevelResource.ROOT).resolve(MANIFEST_FILE_NAME);
    }

    private static void closeAccess(LevelStorageSource.LevelStorageAccess access) {
        if (access == null) {
            return;
        }

        try {
            access.close();
        } catch (IOException exception) {
            Moreblock.LOGGER.warn("关闭存档访问失败: {}", access.getLevelId(), exception);
        }
    }
}
