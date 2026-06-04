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
    private static final int PANEL_HEIGHT = 226;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 7;
    private static final int CATEGORY_X = 10;
    private static final int PAGE_WIDTH = 170;
    private static final int ITEM_WIDTH = 180;
    private static final int PREVIEW_WIDTH = 64;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int ROW_CONTENT_HEIGHT = ROW_HEIGHT - 2;
    private static final int CONTENT_PANEL_TOP = 24;
    private static final int LIST_TOP = 44;
    private static final int LIST_PANEL_BOTTOM = 170;
    private static final int PREVIEW_PANEL_BOTTOM = 194;
    private static final int CRAFT_BUTTON_Y = 198;
    private static final float LIST_TEXT_NUDGE_Y = 0.5f;
    private static final float LIST_ITEM_NUDGE_Y = 0.5f;

    private final List<Category> categories = new ArrayList<>();
    private int categoryIndex;
    private int pageIndex;
    private int itemIndex;
    private int pageScrollOffset;
    private int itemScrollOffset;
    private int pageX = CATEGORY_X;
    private int itemX = CATEGORY_X + PAGE_WIDTH + 12;
    private int previewX = CATEGORY_X + PAGE_WIDTH + 12 + ITEM_WIDTH + 18;
    private Button craftButton;
    private Button blockCategoryButton;
    private Button wallDecalCategoryButton;

    public MoreBlockWorkbenchScreen() {
        super(Component.translatable("screen.moreblock.workbench.title"));
        collectEntries();
    }

    @Override
    protected void init() {
        recalculateLayout();
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        blockCategoryButton = addRenderableWidget(Button.builder(Component.translatable("screen.moreblock.workbench.category.blocks"), button -> selectCategory(0))
                .bounds(left + PANEL_WIDTH - 172, top + 4, 78, 18)
                .build());
        wallDecalCategoryButton = addRenderableWidget(Button.builder(Component.translatable("screen.moreblock.workbench.category.wall_decals"), button -> selectCategory(1))
                .bounds(left + PANEL_WIDTH - 90, top + 4, 78, 18)
                .build());
        craftButton = addRenderableWidget(Button.builder(Component.translatable("screen.moreblock.workbench.craft"), button -> craftSelected())
                .bounds(left + 458, top + CRAFT_BUTTON_Y, 92, 20)
                .build());
        updateCategoryButtons();
        updateCraftButton();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        guiGraphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xE0202020);
        guiGraphics.fill(left + pageX - 4, top + CONTENT_PANEL_TOP, left + pageX + PAGE_WIDTH + 2, top + LIST_PANEL_BOTTOM, 0xD0303030);
        guiGraphics.fill(left + itemX - 4, top + CONTENT_PANEL_TOP, left + itemX + ITEM_WIDTH + 6, top + LIST_PANEL_BOTTOM, 0xD0303030);
        guiGraphics.fill(left + previewX - 4, top + CONTENT_PANEL_TOP, left + PANEL_WIDTH - 6, top + PREVIEW_PANEL_BOTTOM, 0xD02A2A2A);
        guiGraphics.drawString(font, title, left + 8, top + 8, 0xFFFFFF, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.page"), left + pageX, top + CONTENT_PANEL_TOP + 4, 0xFFD37F, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.list"), left + itemX, top + CONTENT_PANEL_TOP + 4, 0xFFD37F, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.workbench.preview"), left + previewX, top + CONTENT_PANEL_TOP + 4, 0xFFD37F, false);
        renderPages(guiGraphics, left, top, mouseX, mouseY);
        renderItems(guiGraphics, left, top, mouseX, mouseY);
        renderPreview(guiGraphics, left, top);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHoveredEntryTooltip(guiGraphics, mouseX, mouseY, left, top);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        if (clickList(mouseX, mouseY, left + pageX, top + LIST_TOP, PAGE_WIDTH, currentPages().size(), pageScrollOffset, index -> {
            pageIndex = index;
            itemIndex = 0;
            itemScrollOffset = 0;
        })) {
            updateCraftButton();
            return true;
        }
        if (clickList(mouseX, mouseY, left + itemX, top + LIST_TOP, ITEM_WIDTH, currentItems().size(), itemScrollOffset, index -> itemIndex = index)) {
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
        if (isInside(mouseX, mouseY, left + pageX, top + LIST_TOP, PAGE_WIDTH, VISIBLE_ROWS * ROW_HEIGHT)) {
            pageScrollOffset = clampScroll(pageScrollOffset + direction, currentPages().size());
            return true;
        }
        if (isInside(mouseX, mouseY, left + itemX, top + LIST_TOP, ITEM_WIDTH, VISIBLE_ROWS * ROW_HEIGHT)) {
            itemScrollOffset = clampScroll(itemScrollOffset + direction, currentItems().size());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderPages(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + pageX;
        int y = top + LIST_TOP;
        List<PageGroup> pages = currentPages();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = pageScrollOffset + row;
            int rowY = y + row * ROW_HEIGHT;
            if (index >= pages.size()) {
                renderEmptyRow(guiGraphics, x, rowY, PAGE_WIDTH - SCROLLBAR_WIDTH - 2);
                continue;
            }
            PageGroup page = pages.get(index);
            boolean hovered = isRowHovered(mouseX, mouseY, x, y, PAGE_WIDTH, row, index, pages.size());
            renderTextRow(guiGraphics, x, rowY, PAGE_WIDTH - SCROLLBAR_WIDTH - 2, "▸ " + page.name(), index == pageIndex, hovered, 0xD8E8FF);
        }
        renderScrollbar(guiGraphics, x + PAGE_WIDTH - SCROLLBAR_WIDTH, y, VISIBLE_ROWS * ROW_HEIGHT - 2, pages.size(), VISIBLE_ROWS, pageScrollOffset);
    }

    private void renderItems(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + itemX;
        int y = top + LIST_TOP;
        List<Entry> items = currentItems();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = itemScrollOffset + row;
            int rowY = y + row * ROW_HEIGHT;
            if (index >= items.size()) {
                renderEmptyRow(guiGraphics, x, rowY, ITEM_WIDTH - SCROLLBAR_WIDTH - 2);
                continue;
            }
            Entry entry = items.get(index);
            boolean selected = index == itemIndex;
            boolean hovered = isRowHovered(mouseX, mouseY, x, y, ITEM_WIDTH, row, index, items.size());
            guiGraphics.fill(x, rowY, x + ITEM_WIDTH - SCROLLBAR_WIDTH - 2, rowY + ROW_CONTENT_HEIGHT, rowBackgroundColor(selected, hovered));
            renderShiftedItem(guiGraphics, entry.stack(), x + 2, rowY, centeredContentOffset(16.0f) + LIST_ITEM_NUDGE_Y);
            renderShiftedText(guiGraphics, fitText(entry.name(), ITEM_WIDTH - 28 - SCROLLBAR_WIDTH - 2), x + 22, rowY, centeredContentOffset((float) font.lineHeight) + LIST_TEXT_NUDGE_Y, selected ? 0xFFFFFF : 0xCFCFCF);
        }
        renderScrollbar(guiGraphics, x + ITEM_WIDTH - SCROLLBAR_WIDTH, y, VISIBLE_ROWS * ROW_HEIGHT - 2, items.size(), VISIBLE_ROWS, itemScrollOffset);
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

    private void renderTextRow(GuiGraphics guiGraphics, int x, int y, int width, String text, boolean selected, boolean hovered, int color) {
        guiGraphics.fill(x, y, x + width, y + ROW_CONTENT_HEIGHT, rowBackgroundColor(selected, hovered));
        renderShiftedText(guiGraphics, fitText(text, width - 10), x + 5, y, centeredContentOffset((float) font.lineHeight) + LIST_TEXT_NUDGE_Y, selected ? 0xFFFFFF : color);
    }

    private void renderEmptyRow(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x, y, x + width, y + ROW_CONTENT_HEIGHT, 0x80383838);
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int x, int y, int height, int totalCount, int visibleCount, int scrollOffset) {
        guiGraphics.fill(x, y, x + SCROLLBAR_WIDTH, y + height, 0x60404040);
        if (totalCount <= 0) {
            return;
        }
        if (totalCount <= visibleCount) {
            guiGraphics.fill(x, y, x + SCROLLBAR_WIDTH, y + height, 0x50808080);
            return;
        }

        int thumbHeight = Math.max(12, Math.round((float) height * (float) visibleCount / (float) totalCount));
        int maxScroll = Math.max(1, totalCount - visibleCount);
        int travel = Math.max(0, height - thumbHeight);
        int thumbY = y + Math.round((float) travel * (float) scrollOffset / (float) maxScroll);
        guiGraphics.fill(x, thumbY, x + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xC0B8C6D9);
    }

    private void renderHoveredEntryTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int top) {
        int hoveredItemIndex = hoveredListIndex(mouseX, mouseY, left + itemX, top + LIST_TOP, ITEM_WIDTH, currentItems().size(), itemScrollOffset);
        if (hoveredItemIndex < 0) {
            return;
        }
        List<Entry> items = currentItems();
        if (hoveredItemIndex >= items.size()) {
            return;
        }
        guiGraphics.renderTooltip(font, items.get(hoveredItemIndex).stack(), mouseX, mouseY);
    }

    private int rowBackgroundColor(boolean selected, boolean hovered) {
        if (selected) {
            return hovered ? 0xB07082A0 : 0xA0607088;
        }
        return hovered ? 0x90708090 : 0x80505050;
    }

    private boolean isRowHovered(int mouseX, int mouseY, int x, int y, int width, int row, int index, int size) {
        if (index < 0 || index >= size) {
            return false;
        }
        int rowY = y + row * ROW_HEIGHT;
        return isInside(mouseX, mouseY, x, rowY, width, ROW_HEIGHT);
    }

    private int hoveredListIndex(int mouseX, int mouseY, int x, int y, int width, int size, int scrollOffset) {
        if (!isInside(mouseX, mouseY, x, y, width, VISIBLE_ROWS * ROW_HEIGHT)) {
            return -1;
        }
        int row = (mouseY - y) / ROW_HEIGHT;
        int index = scrollOffset + row;
        if (index < 0 || index >= size) {
            return -1;
        }
        return index;
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

    private void updateCategoryButtons() {
        if (blockCategoryButton != null) {
            blockCategoryButton.active = categoryIndex != 0;
        }
        if (wallDecalCategoryButton != null) {
            wallDecalCategoryButton.active = categoryIndex != 1;
        }
    }

    private int clampScroll(int offset, int size) {
        return Math.max(0, Math.min(Math.max(0, size - VISIBLE_ROWS), offset));
    }

    private float centeredContentOffset(float contentHeight) {
        return Math.max(0.0f, (ROW_CONTENT_HEIGHT - contentHeight) * 0.5f);
    }

    private void renderShiftedText(GuiGraphics guiGraphics, String text, int x, int y, float offsetY, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, offsetY, 0.0f);
        guiGraphics.drawString(font, text, x, y, color, false);
        guiGraphics.pose().popPose();
    }

    private void renderShiftedItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, float offsetY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, offsetY, 0.0f);
        guiGraphics.renderItem(stack, x, y);
        guiGraphics.pose().popPose();
    }

    private void recalculateLayout() {
        pageX = CATEGORY_X;
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

    private void selectCategory(int index) {
        if (index < 0 || index >= categories.size() || categoryIndex == index) {
            updateCategoryButtons();
            return;
        }
        categoryIndex = index;
        pageIndex = 0;
        itemIndex = 0;
        pageScrollOffset = 0;
        itemScrollOffset = 0;
        updateCategoryButtons();
        updateCraftButton();
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
