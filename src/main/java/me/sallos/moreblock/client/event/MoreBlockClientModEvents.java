package me.sallos.moreblock.client.event;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.client.renderer.ImportedEntityRenderer;
import me.sallos.moreblock.client.renderer.ImportedBlockRenderer;
import me.sallos.moreblock.client.renderer.SeatEntityRenderer;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.config.ImportedWallDecals;
import me.sallos.moreblock.init.ImportedBlockEntities;
import me.sallos.moreblock.init.MoreBlockEntityTypes;
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
import net.minecraft.client.renderer.entity.EntityRenderers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Moreblock.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MoreBlockClientModEvents {
    private MoreBlockClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(MoreBlockEntityTypes.SEAT.get(), SeatEntityRenderer::new);
            ImportedEntityPacks.getDynamicEntityTypeRegistryObjects().values().stream()
                    .filter(RegistryObject::isPresent)
                    .forEach(registryObject -> EntityRenderers.register(registryObject.get(), ImportedEntityRenderer::new));
            if (ImportedBlockPacks.hasDefinitions()) {
                BlockEntityRenderers.register(Objects.requireNonNull(ImportedBlockEntities.getImportedBlockType()), ImportedBlockRenderer::new);
            }
        });
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }

        if (ImportedBlockPacks.hasDefinitions()) {
            registerDynamicPack(event, ImportedBlockPacks.getGeneratedPackRoot(), "moreblock_config_blocks", "MoreBlock Imported Blocks");
        }
        if (ImportedEntityPacks.hasDefinitions()) {
            registerDynamicPack(event, ImportedEntityPacks.getGeneratedPackRoot(), "moreblock_config_entities", "MoreBlock Imported Entities");
        }
        if (ImportedWallDecals.hasDefinitions()) {
            registerDynamicPack(event, ImportedWallDecals.getGeneratedPackRoot(), "moreblock_config_wall_decals", "MoreBlock Imported Wall Decals");
        }
    }

    private static void registerDynamicPack(AddPackFindersEvent event, Path packRoot, String packId, String title) {
        if (!Files.isDirectory(packRoot)) {
            Moreblock.LOGGER.warn("导入运行时资源目录不存在，跳过客户端挂载: {}", packRoot);
            return;
        }

        Moreblock.LOGGER.info("准备挂载导入运行时资源: {}", packRoot);
        event.addRepositorySource(consumer -> {
            Pack pack = createDynamicConfigPack(packId, title, packRoot);
            if (pack != null) {
                Moreblock.LOGGER.info("导入运行时资源挂载成功: {}", packRoot);
                consumer.accept(pack);
            } else {
                Moreblock.LOGGER.warn("导入运行时资源挂载失败: {}", packRoot);
            }
        });
    }

    private static Pack createDynamicConfigPack(String packId, String title, Path packRoot) {
        try {
            return Pack.readMetaAndCreate(
                    packId,
                    Objects.requireNonNull(Component.literal(title)),
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
