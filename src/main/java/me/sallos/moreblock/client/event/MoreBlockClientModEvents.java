package me.sallos.moreblock.client.event;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.client.renderer.ImportedBlockRenderer;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.init.ImportedBlockEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Moreblock.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MoreBlockClientModEvents {
    private MoreBlockClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (ImportedBlockPacks.hasDefinitions()) {
                BlockEntityRenderers.register(Objects.requireNonNull(ImportedBlockEntities.getImportedBlockType()), ImportedBlockRenderer::new);
            }
        });
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !ImportedBlockPacks.hasDefinitions()) {
            return;
        }

        Path packRoot = ImportedBlockPacks.getGeneratedPackRoot();
        if (!Files.isDirectory(packRoot)) {
            Moreblock.LOGGER.warn("导入方块运行时资源目录不存在，跳过客户端挂载: {}", packRoot);
            return;
        }

        Moreblock.LOGGER.info("准备挂载导入方块客户端运行时资源: {}", packRoot);
        event.addRepositorySource(consumer -> {
            Pack pack = createDynamicConfigPack(packRoot);
            if (pack != null) {
                Moreblock.LOGGER.info("导入方块客户端运行时资源挂载成功: {}", packRoot);
                consumer.accept(pack);
            } else {
                Moreblock.LOGGER.warn("导入方块客户端运行时资源挂载失败: {}", packRoot);
            }
        });
    }

    private static Pack createDynamicConfigPack(Path packRoot) {
        try {
            return Pack.readMetaAndCreate(
                    "moreblock_config_blocks",
                    Objects.requireNonNull(Component.literal("MoreBlock Imported Blocks")),
                    true,
                    id -> new PathPackResources(id, Objects.requireNonNull(packRoot), false),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    Objects.requireNonNull(PackSource.DEFAULT)
            );
        } catch (Exception exception) {
            Moreblock.LOGGER.error("创建导入方块运行时资源失败: {}", packRoot, exception);
            return null;
        }
    }
}
