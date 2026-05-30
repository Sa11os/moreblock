package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.config.ImportedEntityPacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@SuppressWarnings("null")
public final class MoreBlockCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Moreblock.MODID);

    public static final RegistryObject<CreativeModeTab> MORE_ITEMS = REGISTRY.register("more_items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.moreblock.more_items"))
            .icon(() -> new ItemStack(resolveTabIcon()))
            .displayItems((parameters, output) -> {
                List<ItemLike> importedItems = collectImportedItems();
                if (importedItems.isEmpty()) {
                    // 保证创造标签页始终可见，哪怕当前还没有导入任何方块
                    output.accept(Items.CHEST);
                    return;
                }
                importedItems.forEach(output::accept);
            })
            .build());

    private MoreBlockCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private static ItemLike resolveTabIcon() {
        List<ItemLike> importedItems = collectImportedItems();
        return importedItems.isEmpty() ? Items.CHEST : importedItems.get(0);
    }

    private static List<ItemLike> collectImportedItems() {
        List<ItemLike> importedItems = new java.util.ArrayList<>();
        ImportedBlockPacks.getDynamicItemRegistryObjects().stream()
                .filter(RegistryObject::isPresent)
                .map(RegistryObject::get)
                .filter(item -> {
                    ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(item);
                    return definition != null && definition.showInMoreBlockTab();
                })
                .map(item -> (ItemLike) item)
                .forEach(importedItems::add);
        ImportedEntityPacks.getDynamicEggRegistryObjects().stream()
                .filter(RegistryObject::isPresent)
                .map(RegistryObject::get)
                .filter(item -> {
                    ImportedEntityPacks.Definition definition = ImportedEntityPacks.getDefinition(item);
                    return definition != null && definition.showInMoreBlockTab();
                })
                .map(item -> (ItemLike) item)
                .forEach(importedItems::add);
        return List.copyOf(importedItems);
    }
}
