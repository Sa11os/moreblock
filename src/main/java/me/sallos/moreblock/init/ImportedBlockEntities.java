package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.block.entity.ImportedBlockEntity;
import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ImportedBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Moreblock.MODID);
    private static final RegistryObject<BlockEntityType<ImportedBlockEntity>> IMPORTED_BLOCK = ImportedBlockPacks.registerDynamicBlockEntity(REGISTRY);

    private ImportedBlockEntities() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static BlockEntityType<ImportedBlockEntity> getImportedBlockType() {
        if (IMPORTED_BLOCK == null) {
            throw new IllegalStateException("动态导入方块实体类型尚未注册");
        }
        return IMPORTED_BLOCK.get();
    }
}
