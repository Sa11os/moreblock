package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.block.MoreBlockWorkbenchBlock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ImportedBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, Moreblock.MODID);
    public static final net.minecraftforge.registries.RegistryObject<Block> MOREBLOCK_WORKBENCH = REGISTRY.register("moreblock_workbench",
            () -> new MoreBlockWorkbenchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)));

    static {
        ImportedBlockPacks.registerBlocks(REGISTRY);
    }

    private ImportedBlocks() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static void registerApiBlocks() {
        ImportedBlockPacks.registerBlocks(REGISTRY);
    }
}