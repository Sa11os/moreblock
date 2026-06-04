package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.config.ImportedWallDecals;
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
                    // 保证创造标签页始终可见，哪怕当前还没有导入任何内容
                    output.accept(Items.CHEST);
                    return;
                }
                importedItems.forEach(output::accept);
            })
            .build());

    public static final RegistryObject<CreativeModeTab> WALL_DECALS = REGISTRY.register("wall_decals", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.moreblock.wall_decals"))
            .icon(() -> new ItemStack(resolveWallDecalTabIcon()))
            .displayItems((parameters, output) -> {
                List<ItemLike> wallDecals = collectWallDecalItems(null);
                if (wallDecals.isEmpty()) {
                    output.accept(resolveWallDecalTabIcon());
                    return;
                }
                wallDecals.forEach(output::accept);
            })
            .build());

    private MoreBlockCreativeTabs() {
    }

    static {
        for (ImportedBlockPacks.ItemPageDefinition itemPage : ImportedBlockPacks.getItemPages()) {
            ITEM_PAGE_TABS.put(itemPage.id(), REGISTRY.register(itemPage.registryName(), () -> CreativeModeTab.builder()
                    .title(Component.translatable(itemPage.translationKey()))
                    .icon(() -> new ItemStack(resolveItemPageIcon(itemPage.id(), itemPage.iconRegistryName(), null)))
                    .displayItems((parameters, output) -> {
                        List<ItemLike> pageItems = new java.util.ArrayList<>();
                        pageItems.addAll(collectImportedBlockItems(itemPage.id()));
                        pageItems.addAll(collectWallDecalItems(itemPage.id()));
                        if (pageItems.isEmpty()) {
                            output.accept(resolveItemPageIcon(itemPage.id(), itemPage.iconRegistryName(), null));
                            return;
                        }
                        pageItems.forEach(output::accept);
                    })
                    .build()));
        }
        for (ImportedWallDecals.ItemPageDefinition itemPage : ImportedWallDecals.getItemPages()) {
            if (ITEM_PAGE_TABS.containsKey(itemPage.id())) {
                continue;
            }
            ITEM_PAGE_TABS.put(itemPage.id(), REGISTRY.register(itemPage.registryName(), () -> CreativeModeTab.builder()
                    .title(Component.translatable(itemPage.translationKey()))
                    .icon(() -> new ItemStack(resolveItemPageIcon(itemPage.id(), null, itemPage.iconSourceId())))
                    .displayItems((parameters, output) -> collectWallDecalItems(itemPage.id()).forEach(output::accept))
                    .build()));
        }
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private static ItemLike resolveTabIcon() {
        List<ItemLike> importedItems = collectImportedItems();
        return importedItems.isEmpty() ? resolveWallDecalTabIcon() : importedItems.get(0);
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

    private static List<ItemLike> collectWallDecalItems(String itemPageId) {
        List<ItemLike> importedItems = new java.util.ArrayList<>();
        ImportedWallDecals.getDynamicItemRegistryObjects().stream()
                .filter(RegistryObject::isPresent)
                .map(RegistryObject::get)
                .filter(item -> {
                    ImportedWallDecals.Definition definition = ImportedWallDecals.getDefinition(item);
                    if (definition == null) {
                        return false;
                    }
                    String actualItemPageId = definition.itemPageId();
                    if (actualItemPageId == null || actualItemPageId.isBlank()) {
                        actualItemPageId = ImportedWallDecals.DEFAULT_ITEM_PAGE_ID;
                    }
                    return itemPageId == null
                            ? ImportedWallDecals.DEFAULT_ITEM_PAGE_ID.equals(actualItemPageId)
                            : itemPageId.equals(actualItemPageId);
                })
                .map(item -> (ItemLike) item)
                .forEach(importedItems::add);
        return List.copyOf(importedItems);
    }

    private static ItemLike resolveWallDecalTabIcon() {
        ItemLike zoneCoffee = resolveWallDecalIcon("zone_coffee", null);
        if (zoneCoffee != null) {
            return zoneCoffee;
        }
        List<ItemLike> wallDecals = collectWallDecalItems(null);
        return wallDecals.isEmpty() ? Items.PAINTING : wallDecals.get(0);
    }

    private static ItemLike resolveItemPageIcon(String itemPageId, String blockIconRegistryName, String wallDecalIconSourceId) {
        if (blockIconRegistryName != null) {
            Optional<RegistryObject<net.minecraft.world.item.Item>> itemRegistryObject =
                    ImportedBlockPacks.getDynamicItemRegistryObject(blockIconRegistryName);
            if (itemRegistryObject.isPresent() && itemRegistryObject.get().isPresent()) {
                return itemRegistryObject.get().get();
            }
        }

        ItemLike wallDecalIcon = resolveWallDecalIcon(wallDecalIconSourceId, itemPageId);
        if (wallDecalIcon != null) {
            return wallDecalIcon;
        }

        List<ItemLike> pageItems = collectImportedBlockItems(itemPageId);
        if (!pageItems.isEmpty()) {
            return pageItems.get(0);
        }
        pageItems = collectWallDecalItems(itemPageId);
        return pageItems.isEmpty() ? Items.CHEST : pageItems.get(0);
    }

    private static ItemLike resolveWallDecalIcon(String iconSourceId, String itemPageId) {
        List<ItemLike> pageItems = collectWallDecalItems(itemPageId);
        if (iconSourceId != null && !iconSourceId.isBlank()) {
            for (RegistryObject<net.minecraft.world.item.Item> registryObject : ImportedWallDecals.getDynamicItemRegistryObjects()) {
                if (!registryObject.isPresent()) {
                    continue;
                }
                ImportedWallDecals.Definition definition = ImportedWallDecals.getDefinition(registryObject.get());
                if (definition != null
                        && (iconSourceId.equals(definition.sourceConfigId()) || iconSourceId.equals(definition.registryName()))) {
                    return registryObject.get();
                }
            }
        }
        if (pageItems.isEmpty()) {
            return null;
        }
        return pageItems.get(0);
    }
}
