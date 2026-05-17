package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ImportedItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Moreblock.MODID);

    static {
        ImportedBlockPacks.registerItems(REGISTRY);
    }

    private ImportedItems() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}