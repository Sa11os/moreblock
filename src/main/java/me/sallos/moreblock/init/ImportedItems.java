package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.config.ImportedWallDecals;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ImportedItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Moreblock.MODID);
    public static final net.minecraftforge.registries.RegistryObject<Item> MOREBLOCK_WORKBENCH = REGISTRY.register("moreblock_workbench",
            () -> new BlockItem(ImportedBlocks.MOREBLOCK_WORKBENCH.get(), new Item.Properties()));

    static {
        ImportedBlockPacks.registerItems(REGISTRY);
        ImportedWallDecals.registerItems(REGISTRY);
    }

    private ImportedItems() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static void registerApiItems() {
        ImportedBlockPacks.registerItems(REGISTRY);
        ImportedWallDecals.registerItems(REGISTRY);
    }
}