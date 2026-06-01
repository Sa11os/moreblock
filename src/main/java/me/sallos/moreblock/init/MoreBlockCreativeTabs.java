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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("null")
public final class MoreBlockCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Moreblock.MODID);
    private static final Map<String, RegistryObject<CreativeModeTab>> ITEM_PAGE_TABS = new LinkedHashMap<>();

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

    static {
        for (ImportedBlockPacks.ItemPageDefinition itemPage : ImportedBlockPacks.getItemPages()) {
            ITEM_PAGE_TABS.put(itemPage.id(), REGISTRY.register(itemPage.registryName(), () -> CreativeModeTab.builder()
                    .title(Component.translatable(itemPage.translationKey()))
                    .icon(() -> new ItemStack(resolveItemPageIcon(itemPage)))
                    .displayItems((parameters, output) -> {
                        List<ItemLike> pageItems = collectImportedBlockItems(itemPage.id());
                        if (pageItems.isEmpty()) {
                            output.accept(resolveItemPageIcon(itemPage));
                            return;
                        }
                        pageItems.forEach(output::accept);
                    })
                    .build()));
        }
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
        importedItems.addAll(collectImportedBlockItems(null));
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

    private static List<ItemLike> collectImportedBlockItems(String itemPageId) {
        List<ItemLike> importedItems = new java.util.ArrayList<>();
        ImportedBlockPacks.getDynamicItemRegistryObjects().stream()
                .filter(RegistryObject::isPresent)
                .map(RegistryObject::get)
                .filter(item -> {
                    ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(item);
                    return definition != null
                            && definition.showInMoreBlockTab()
                            && (itemPageId == null
                            ? (definition.itemPageId() == null || definition.itemPageId().isBlank())
                            : itemPageId.equals(definition.itemPageId()));
                })
                .map(item -> (ItemLike) item)
                .forEach(importedItems::add);
        return List.copyOf(importedItems);
    }

    private static ItemLike resolveItemPageIcon(ImportedBlockPacks.ItemPageDefinition itemPage) {
        Optional<RegistryObject<net.minecraft.world.item.Item>> itemRegistryObject =
                ImportedBlockPacks.getDynamicItemRegistryObject(itemPage.iconRegistryName());
        if (itemRegistryObject.isPresent() && itemRegistryObject.get().isPresent()) {
            return itemRegistryObject.get().get();
        }

        List<ItemLike> pageItems = collectImportedBlockItems(itemPage.id());
        return pageItems.isEmpty() ? Items.CHEST : pageItems.get(0);
    }
}
