package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ImportedBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, Moreblock.MODID);

    static {
        ImportedBlockPacks.registerBlocks(REGISTRY);
    }

    private ImportedBlocks() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}