package me.sallos.moreblock.client.screen;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.config.ImportedWallDecals;
import me.sallos.moreblock.network.message.CraftMoreBlockWorkbenchItemMessage;
import me.sallos.moreblock.workbench.MoreBlockWorkbenchCrafting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

@OnlyIn(Dist.CLIENT)
public final class MoreBlockWorkbenchScreen extends Screen {
    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 248;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 8;
    private static final int CATEGORY_X = 10;
    private static final int MIN_CATEGORY_WIDTH = 76;
    private static final int MAX_CATEGORY_WIDTH = 132;
    private static final int PAGE_WIDTH = 170;
    private static final int ITEM_WIDTH = 180;
    private static final int PREVIEW_WIDTH = 64;

    private final List<Category> categories = new ArrayList<>();
    private int categoryIndex;
    private int pageIndex;
    private int itemIndex;
    private int pageScrollOffset;
    private int itemScrollOffset;
    private int categoryWidth = MIN_CATEGORY_WIDTH;
    private int pageX = CATEGORY_X + MIN_CATEGORY_WIDTH + 6;
    private int itemX = CATEGORY_X + MIN_CATEGORY_WIDTH + 6 + PAGE_WIDTH + 6;
    private int previewX = CATEGORY_X + MIN_CATEGORY_WIDTH + 6 + PAGE_WIDTH + 6 + ITEM_WIDTH + 10;
    private Button craftButton;

    public MoreBlockWorkbenchScreen() {
        super(Component.translatable("screen.moreblock.workbench.title"));
        collectEntries();
    }

    @Override
    protected void init() {
        recalculateLayout();
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        craftButton = addRenderableWidget(Button.builder(Component.translatable("screen.moreblock.workbench.craft"), button -> craftSelected())
                .bounds(left + 458, top + 216, 92, 20)
                .build());
        updateCraftButton();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        guiGraphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xE0202020);
        guiGraphics.fill(left + 6, top + 24, left + CATEGORY_X + categoryWidth + 4, top + 184, 0xD0303030);
        guiGraphics.fill(left + pageX - 4, top + 24, left + pageX + PAGE_WIDTH + 2, top + 184, 0xD0303030);
        guiGraphics.fill(left + itemX - 4, top + 24, left + itemX + ITEM_WIDTH + 6, top + 184, 0xD0303030);
        guiGraphics.fill(left + previewX - 4, top + 24, left + PANEL_WIDTH - 6, top + 210, 0xD02A2A2A);
        guiGraphics.drawString(font, title, left + 8, top + 8, 0xFFFFFF, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.category"), left + CATEGORY_X, top + 28, 0xFFD37F, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.page"), left + pageX, top + 28, 0xFFD37F, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.list"), left + itemX, top + 28, 0xFFD37F, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.preview"), left + previewX, top + 28, 0xFFD37F, false);
        renderCategories(guiGraphics, left, top);
        renderPages(guiGraphics, left, top);
        renderItems(guiGraphics, left, top);
        renderPreview(guiGraphics, left, top);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        if (clickList(mouseX, mouseY, left + CATEGORY_X, top + 44, categoryWidth, categories.size(), 0, index -> {
            categoryIndex = index;
            pageIndex = 0;
            itemIndex = 0;
            pageScrollOffset = 0;
            itemScrollOffset = 0;
        })) {
            updateCraftButton();
            return true;
        }
        if (clickList(mouseX, mouseY, left + pageX, top + 44, PAGE_WIDTH, currentPages().size(), pageScrollOffset, index -> {
            pageIndex = index;
            itemIndex = 0;
            itemScrollOffset = 0;
        })) {
            updateCraftButton();
            return true;
        }
        if (clickList(mouseX, mouseY, left + itemX, top + 44, ITEM_WIDTH, currentItems().size(), itemScrollOffset, index -> itemIndex = index)) {
            updateCraftButton();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int direction = -(int) Math.signum(delta);
        if (isInside(mouseX, mouseY, left + pageX, top + 44, PAGE_WIDTH, VISIBLE_ROWS * ROW_HEIGHT)) {
            pageScrollOffset = clampScroll(pageScrollOffset + direction, currentPages().size());
            return true;
        }
        if (isInside(mouseX, mouseY, left + itemX, top + 44, ITEM_WIDTH, VISIBLE_ROWS * ROW_HEIGHT)) {
            itemScrollOffset = clampScroll(itemScrollOffset + direction, currentItems().size());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderCategories(GuiGraphics guiGraphics, int left, int top) {
        int x = left + CATEGORY_X;
        int y = top + 44;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int rowY = y + row * ROW_HEIGHT;
            if (row >= categories.size()) {
                renderEmptyRow(guiGraphics, x, rowY, categoryWidth);
                continue;
            }
            Category category = categories.get(row);
            renderTextRow(guiGraphics, x, rowY, categoryWidth, category.name(), row == categoryIndex, 0xD8E8FF);
        }
    }

    private void renderPages(GuiGraphics guiGraphics, int left, int top) {
        int x = left + pageX;
        int y = top + 44;
        List<PageGroup> pages = currentPages();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = pageScrollOffset + row;
            int rowY = y + row * ROW_HEIGHT;
            if (index >= pages.size()) {
                renderEmptyRow(guiGraphics, x, rowY, PAGE_WIDTH);
                continue;
            }
            PageGroup page = pages.get(index);
            renderTextRow(guiGraphics, x, rowY, PAGE_WIDTH, "▸ " + page.name(), index == pageIndex, 0xD8E8FF);
        }
    }

    private void renderItems(GuiGraphics guiGraphics, int left, int top) {
        int x = left + itemX;
        int y = top + 44;
        List<Entry> items = currentItems();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = itemScrollOffset + row;
            int rowY = y + row * ROW_HEIGHT;
            if (index >= items.size()) {
                renderEmptyRow(guiGraphics, x, rowY, ITEM_WIDTH);
                continue;
            }
            Entry entry = items.get(index);
            boolean selected = index == itemIndex;
            guiGraphics.fill(x, rowY, x + ITEM_WIDTH, rowY + ROW_HEIGHT - 2, selected ? 0xA0607088 : 0x80505050);
            guiGraphics.renderItem(entry.stack(), x + 2, rowY + 1);
            guiGraphics.drawString(font, fitText(entry.name(), ITEM_WIDTH - 28), x + 22, rowY + 5, selected ? 0xFFFFFF : 0xCFCFCF, false);
        }
    }

    private void renderPreview(GuiGraphics guiGraphics, int left, int top) {
        Entry entry = currentEntry();
        if (entry == null) {
            guiGraphics.drawCenteredString(font, Component.translatable("screen.moreblock.workbench.empty"), left + previewX + 28, top + 110, 0xA0A0A0);
            return;
        }
        guiGraphics.renderItem(entry.stack(), left + previewX + 24, top + 56);
        guiGraphics.renderItemDecorations(font, entry.stack(), left + previewX + 24, top + 56);
        guiGraphics.drawString(font, fitText(entry.name(), PREVIEW_WIDTH), left + previewX, top + 88, 0xFFFFFF, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.cost"), left + previewX, top + 118, 0xCFCFCF, false);
        guiGraphics.renderItem(new ItemStack(entry.ingredient()), left + previewX, top + 136);
        guiGraphics.drawString(font, Component.literal("x1").withStyle(ChatFormatting.GRAY), left + previewX + 20, top + 141, 0xCFCFCF, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.folder"), left + previewX, top + 168, 0xCFCFCF, false);
        guiGraphics.drawString(font, fitText(currentPageName(), PREVIEW_WIDTH), left + previewX, top + 182, 0xA8D0FF, false);
    }

    private void renderTextRow(GuiGraphics guiGraphics, int x, int y, int width, String text, boolean selected, int color) {
        guiGraphics.fill(x, y, x + width, y + ROW_HEIGHT - 2, selected ? 0xA0607088 : 0x80505050);
        guiGraphics.drawString(font, fitText(text, width - 10), x + 5, y + 5, selected ? 0xFFFFFF : color, false);
    }

    private void renderEmptyRow(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x, y, x + width, y + ROW_HEIGHT - 2, 0x80383838);
    }

    private boolean clickList(double mouseX, double mouseY, int x, int y, int width, int size, int scrollOffset, IntConsumer handler) {
        if (!isInside(mouseX, mouseY, x, y, width, VISIBLE_ROWS * ROW_HEIGHT)) {
            return false;
        }
        int row = ((int) mouseY - y) / ROW_HEIGHT;
        int index = scrollOffset + row;
        if (index < 0 || index >= size) {
            return false;
        }
        handler.accept(index);
        return true;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void craftSelected() {
        Entry entry = currentEntry();
        if (entry == null) {
            return;
        }
        Moreblock.PACKET_HANDLER.sendToServer(new CraftMoreBlockWorkbenchItemMessage(entry.type(), entry.registryName()));
    }

    private void updateCraftButton() {
        if (craftButton != null) {
            craftButton.active = currentEntry() != null;
        }
    }

    private int clampScroll(int offset, int size) {
        return Math.max(0, Math.min(Math.max(0, size - VISIBLE_ROWS), offset));
    }

    private void recalculateLayout() {
        int calculatedWidth = categories.stream()
                .map(Category::name)
                .mapToInt(font::width)
                .max()
                .orElse(MIN_CATEGORY_WIDTH - 10) + 14;
        categoryWidth = Math.max(MIN_CATEGORY_WIDTH, Math.min(MAX_CATEGORY_WIDTH, calculatedWidth));
        pageX = CATEGORY_X + categoryWidth + 14;
        itemX = pageX + PAGE_WIDTH + 12;
        previewX = itemX + ITEM_WIDTH + 18;
    }

    private String fitText(String text, int width) {
        return font.plainSubstrByWidth(text == null ? "" : text, Math.max(0, width));
    }

    private List<PageGroup> currentPages() {
        if (categories.isEmpty()) {
            return List.of();
        }
        return categories.get(Math.max(0, Math.min(categoryIndex, categories.size() - 1))).pages();
    }

    private List<Entry> currentItems() {
        List<PageGroup> pages = currentPages();
        if (pages.isEmpty()) {
            return List.of();
        }
        return pages.get(Math.max(0, Math.min(pageIndex, pages.size() - 1))).entries();
    }

    private Entry currentEntry() {
        List<Entry> items = currentItems();
        if (items.isEmpty()) {
            return null;
        }
        return items.get(Math.max(0, Math.min(itemIndex, items.size() - 1)));
    }

    private String currentPageName() {
        List<PageGroup> pages = currentPages();
        if (pages.isEmpty()) {
            return "";
        }
        return pages.get(Math.max(0, Math.min(pageIndex, pages.size() - 1))).name();
    }

    private void collectEntries() {
        categories.clear();
        categories.add(new Category(Component.translatable("screen.moreblock.workbench.category.blocks").getString(), buildBlockPages()));
        categories.add(new Category(Component.translatable("screen.moreblock.workbench.category.wall_decals").getString(), buildWallDecalPages()));
    }

    private List<PageGroup> buildBlockPages() {
        Map<String, PageGroup> pages = new LinkedHashMap<>();
        Map<String, String> pageNames = buildBlockPageNameLookup();
        ImportedBlockPacks.getDynamicItemRegistryObjects().stream()
                .filter(RegistryObject::isPresent)
                .map(RegistryObject::get)
                .forEach(item -> {
                    ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(item);
                    if (definition == null) {
                        return;
                    }
                    String pageKey = definition.itemPageId() == null || definition.itemPageId().isBlank() ? "__default_blocks__" : definition.itemPageId().trim();
                    String pageName = resolveBlockPageName(definition, pageNames);
                    pages.computeIfAbsent(pageKey, ignored -> new PageGroup(pageName, new ArrayList<>()))
                            .entries()
                            .add(new Entry(
                                    MoreBlockWorkbenchCrafting.TYPE_BLOCK,
                                    definition.registryName(),
                                    new ItemStack(item),
                                    ImportedBlockPacks.resolveDisplayName(definition),
                                    Items.IRON_INGOT
                            ));
                });
        return List.copyOf(pages.values());
    }

    private List<PageGroup> buildWallDecalPages() {
        Map<String, PageGroup> pages = new LinkedHashMap<>();
        Map<String, String> pageNames = buildWallDecalPageNameLookup();
        ImportedWallDecals.getDynamicItemRegistryObjects().stream()
                .filter(RegistryObject::isPresent)
                .map(RegistryObject::get)
                .forEach(item -> {
                    ImportedWallDecals.Definition definition = ImportedWallDecals.getDefinition(item);
                    if (definition == null) {
                        return;
                    }
                    String pageKey = definition.itemPageId() == null || definition.itemPageId().isBlank() ? "__default_wall_decals__" : definition.itemPageId().trim();
                    String pageName = resolveWallDecalPageName(definition, pageNames);
                    pages.computeIfAbsent(pageKey, ignored -> new PageGroup(pageName, new ArrayList<>()))
                            .entries()
                            .add(new Entry(
                                    MoreBlockWorkbenchCrafting.TYPE_WALL_DECAL,
                                    definition.registryName(),
                                    new ItemStack(item),
                                    ImportedWallDecals.resolveDisplayName(definition),
                                    Items.PAPER
                            ));
                });
        return List.copyOf(pages.values());
    }

    private Map<String, String> buildBlockPageNameLookup() {
        Map<String, String> pageNames = new LinkedHashMap<>();
        for (ImportedBlockPacks.ItemPageDefinition itemPage : ImportedBlockPacks.getItemPages()) {
            String displayName = firstNonBlank(itemPage.zhCnName(), itemPage.enUsName(), itemPage.id());
            if (displayName != null) {
                pageNames.put(itemPage.id(), displayName);
            }
        }
        return pageNames;
    }

    private Map<String, String> buildWallDecalPageNameLookup() {
        Map<String, String> pageNames = new LinkedHashMap<>();
        for (ImportedWallDecals.ItemPageDefinition itemPage : ImportedWallDecals.getItemPages()) {
            String displayName = firstNonBlank(itemPage.zhCnName(), itemPage.enUsName(), itemPage.id());
            if (displayName != null) {
                pageNames.put(itemPage.id(), displayName);
            }
        }
        return pageNames;
    }

    private String resolveBlockPageName(ImportedBlockPacks.Definition definition, Map<String, String> pageNames) {
        if (definition.itemPageId() == null || definition.itemPageId().isBlank()) {
            return Component.translatable("screen.moreblock.workbench.page.default_blocks").getString();
        }
        String pageName = pageNames.get(definition.itemPageId().trim());
        if (pageName != null && !pageName.isBlank()) {
            return pageName;
        }
        if (definition.itemPageZhCnName() != null && !definition.itemPageZhCnName().isBlank()) {
            return definition.itemPageZhCnName();
        }
        if (definition.itemPageEnUsName() != null && !definition.itemPageEnUsName().isBlank()) {
            return definition.itemPageEnUsName();
        }
        return definition.itemPageId();
    }

    private String resolveWallDecalPageName(ImportedWallDecals.Definition definition, Map<String, String> pageNames) {
        if (definition.itemPageId() == null || definition.itemPageId().isBlank()) {
            return Component.translatable("screen.moreblock.workbench.page.default_wall_decals").getString();
        }
        String pageName = pageNames.get(definition.itemPageId().trim());
        if (pageName != null && !pageName.isBlank()) {
            return pageName;
        }
        if (definition.itemPageZhCnName() != null && !definition.itemPageZhCnName().isBlank()) {
            return definition.itemPageZhCnName();
        }
        if (definition.itemPageEnUsName() != null && !definition.itemPageEnUsName().isBlank()) {
            return definition.itemPageEnUsName();
        }
        return definition.itemPageId();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private record Category(String name, List<PageGroup> pages) {
    }

    private record PageGroup(String name, List<Entry> entries) {
    }

    private record Entry(String type, String registryName, ItemStack stack, String name, Item ingredient) {
    }
}
