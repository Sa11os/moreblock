package me.sallos.moreblock.client.screen;

import me.sallos.moreblock.config.ImportedBlockPackDownloads;
import me.sallos.moreblock.network.ImportedBlockPackSync;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class MoreBlockPackDownloadScreen extends Screen {
    private static final int LIST_WIDTH = 220;
    private static final int ROW_HEIGHT = 24;
    private static final int DETAIL_ROW_HEIGHT = 22;

    private final Screen parent;
    private final Component reason;
    private final ImportedBlockPackSync.DownloadContext downloadContext;
    private Component status = Component.translatable("screen.moreblock.pack_download.ready");
    private int completed;
    private int selectedIndex;
    private int scrollOffset;
    private int detailScrollOffset;
    private boolean downloaded;

    public MoreBlockPackDownloadScreen(Screen parent, Component reason, ImportedBlockPackSync.DownloadContext downloadContext) {
        super(Component.translatable("screen.moreblock.pack_download.title"));
        this.parent = parent == null ? new TitleScreen() : parent;
        this.reason = reason;
        this.downloadContext = downloadContext;
    }

    @Override
    protected void init() {
        int bottom = height - 32;
        Button downloadButton = addRenderableWidget(Button.builder(Component.translatable("screen.moreblock.pack_download.download"), button -> downloadPacks())
                .bounds(width - 320, bottom, 150, 20)
                .build());
        downloadButton.active = !downloaded && entryCount() > 0;
        addRenderableWidget(Button.builder(Component.literal("关闭客户端"), button -> {
                    if (minecraft != null) {
                        minecraft.stop();
                    }
                })
                .bounds(width - 160, bottom, 140, 20)
                .build());
        selectedIndex = Mth.clamp(selectedIndex, 0, Math.max(0, entryCount() - 1));
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScrollOffset());
        detailScrollOffset = Mth.clamp(detailScrollOffset, 0, maxDetailScrollOffset());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int listX = 16;
        int listTop = 52;
        int listBottom = height - 44;
        int detailX = listX + LIST_WIDTH + 18;
        int detailRight = width - 20;

        guiGraphics.drawString(font, title, 16, 18, 0xFFFFFF, false);
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.pack_download.summary", entryCount()), 16, 34, 0xFFD37F, false);
        renderList(guiGraphics, listX, listTop, listBottom, mouseX, mouseY);
        renderDetails(guiGraphics, detailX, listTop, detailRight, listBottom);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderList(GuiGraphics guiGraphics, int x, int top, int bottom, int mouseX, int mouseY) {
        guiGraphics.fill(x - 1, top - 1, x + LIST_WIDTH + 1, bottom + 1, 0xAA101010);
        guiGraphics.fill(x, top, x + LIST_WIDTH, bottom, 0xCC202020);
        List<ImportedBlockPackDownloads.DownloadEntry> entries = entries();
        int visibleRows = Math.max(1, (bottom - top) / ROW_HEIGHT);
        for (int row = 0; row < visibleRows; row++) {
            int index = scrollOffset + row;
            if (index >= entries.size()) {
                break;
            }
            int rowY = top + row * ROW_HEIGHT;
            boolean selected = index == selectedIndex;
            boolean hovered = mouseX >= x && mouseX <= x + LIST_WIDTH && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            guiGraphics.fill(x + 1, rowY + 1, x + LIST_WIDTH - 1, rowY + ROW_HEIGHT - 1, selected ? 0xAA4A6FA5 : hovered ? 0x664A4A4A : 0x00000000);
            ImportedBlockPackDownloads.DownloadEntry entry = entries.get(index);
            guiGraphics.drawString(font, packTypeLabel(entry.packType()) + " " + trim(entry.displayName(), 22), x + 6, rowY + 4, selected ? 0xFFFFFF : 0xD0D0D0, false);
            guiGraphics.drawString(font, trim(entry.registryName(), 28), x + 6, rowY + 14, 0x909090, false);
        }

        if (entries.size() > visibleRows) {
            int barHeight = Math.max(16, (bottom - top) * visibleRows / entries.size());
            int barY = top + (bottom - top - barHeight) * scrollOffset / maxScrollOffset();
            guiGraphics.fill(x + LIST_WIDTH - 5, barY, x + LIST_WIDTH - 2, barY + barHeight, 0xFFC0C0C0);
        }
    }

    private void renderDetails(GuiGraphics guiGraphics, int x, int top, int right, int bottom) {
        guiGraphics.fill(x - 1, top - 1, right + 1, bottom + 1, 0xAA101010);
        guiGraphics.fill(x, top, right, bottom, 0xCC181818);
        int textX = x + 12;
        int y = top + 12;
        guiGraphics.drawString(font, Component.translatable("screen.moreblock.pack_download.progress", completed, entryCount()), textX, y, 0xC8C8C8, false);
        y += 16;
        int wrapWidth = Math.max(80, right - textX - 12);
        guiGraphics.drawWordWrap(font, status, textX, y, wrapWidth, downloaded ? 0x7CFF7C : 0xFFD37F);
        y += wrappedHeight(status, wrapWidth) + 16;

        ImportedBlockPackDownloads.DownloadEntry entry = selectedEntry();
        if (entry == null) {
            guiGraphics.drawWordWrap(font, reason, textX, y, Math.max(80, right - textX - 12), 0xC8C8C8);
            return;
        }

        guiGraphics.drawString(font, Component.literal(entry.displayName()), textX, y, 0xFFFFFF, false);
        y += 22;
        renderDetailRows(guiGraphics, x + 8, right - 8, y, bottom, entry);
    }

    private void renderDetailRows(GuiGraphics guiGraphics, int left, int right, int top, int bottom, ImportedBlockPackDownloads.DownloadEntry entry) {
        String[][] rows = new String[][]{
                {"类型", packTypeLabel(entry.packType())},
                {"注册名", entry.registryName()},
                {"来源", entry.sourceName()},
                {"保存形式", "directory".equals(entry.storageType()) ? "文件夹" : "zip"},
                {"保存路径", entry.relativePath()},
                {"指纹", trim(entry.fingerprint(), 48)},
                {"大小", formatBytes(entry.content().length)}
        };
        int visibleRows = Math.max(1, (bottom - top - 8) / DETAIL_ROW_HEIGHT);
        detailScrollOffset = Mth.clamp(detailScrollOffset, 0, Math.max(0, rows.length - visibleRows));
        for (int row = 0; row < visibleRows; row++) {
            int index = detailScrollOffset + row;
            if (index >= rows.length) {
                break;
            }
            drawDetailRow(guiGraphics, left, right - 8, top + row * DETAIL_ROW_HEIGHT, rows[index][0], rows[index][1], index);
        }
        if (rows.length > visibleRows) {
            int barHeight = Math.max(16, (bottom - top) * visibleRows / rows.length);
            int barY = top + (bottom - top - barHeight) * detailScrollOffset / Math.max(1, rows.length - visibleRows);
            guiGraphics.fill(right - 4, barY, right - 1, barY + barHeight, 0xFFC0C0C0);
        }
    }

    private int drawDetailRow(GuiGraphics guiGraphics, int left, int right, int y, String name, String value, int index) {
        int rowHeight = 22;
        guiGraphics.fill(left, y - 3, right, y + rowHeight - 3, index % 2 == 0 ? 0x66303030 : 0x66262626);
        guiGraphics.drawString(font, name, left + 8, y + 4, 0xA0A0A0, false);
        guiGraphics.drawString(font, value == null ? "" : trim(value, Math.max(16, (right - left - 94) / 6)), left + 84, y + 4, 0xD8D8D8, false);
        return y + rowHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = 16;
        int listTop = 52;
        int listBottom = height - 44;
        if (mouseX >= listX && mouseX <= listX + LIST_WIDTH && mouseY >= listTop && mouseY <= listBottom) {
            int row = ((int) mouseY - listTop) / ROW_HEIGHT;
            int index = scrollOffset + row;
            if (index >= 0 && index < entryCount()) {
                if (selectedIndex != index) {
                    detailScrollOffset = 0;
                }
                selectedIndex = index;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listX = 16;
        int listTop = 52;
        int listBottom = height - 44;
        if (mouseX >= listX && mouseX <= listX + LIST_WIDTH && mouseY >= listTop && mouseY <= listBottom) {
            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScrollOffset());
            return true;
        }
        int detailX = listX + LIST_WIDTH + 18;
        int detailRight = width - 20;
        if (mouseX >= detailX && mouseX <= detailRight && mouseY >= listTop && mouseY <= listBottom) {
            detailScrollOffset = Mth.clamp(detailScrollOffset - (int) Math.signum(delta), 0, maxDetailScrollOffset());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private List<ImportedBlockPackDownloads.DownloadEntry> entries() {
        return downloadContext == null ? List.of() : downloadContext.entries();
    }

    private int entryCount() {
        return entries().size();
    }

    private ImportedBlockPackDownloads.DownloadEntry selectedEntry() {
        List<ImportedBlockPackDownloads.DownloadEntry> entries = entries();
        if (entries.isEmpty()) {
            return null;
        }
        selectedIndex = Mth.clamp(selectedIndex, 0, entries.size() - 1);
        return entries.get(selectedIndex);
    }

    private int maxScrollOffset() {
        int visibleRows = Math.max(1, (height - 96) / ROW_HEIGHT);
        return Math.max(0, entryCount() - visibleRows);
    }

    private int maxDetailScrollOffset() {
        ImportedBlockPackDownloads.DownloadEntry entry = selectedEntry();
        if (entry == null) {
            return 0;
        }
        int detailTop = 52 + 12 + 16 + wrappedHeight(status, Math.max(80, width - (16 + LIST_WIDTH + 18) - 32)) + 16 + 22;
        int detailBottom = height - 44;
        int visibleRows = Math.max(1, (detailBottom - detailTop - 8) / DETAIL_ROW_HEIGHT);
        return Math.max(0, 7 - visibleRows);
    }

    private int wrappedHeight(Component component, int width) {
        return Math.max(10, font.split(component, width).size() * 10);
    }

    private String packTypeLabel(String packType) {
        return "entity".equals(packType) ? "实体包" : "方块包";
    }

    private String trim(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text == null ? "" : text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String formatBytes(int bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format(java.util.Locale.ROOT, "%.2f MiB", bytes / 1024.0d / 1024.0d);
        }
        if (bytes >= 1024) {
            return String.format(java.util.Locale.ROOT, "%.2f KiB", bytes / 1024.0d);
        }
        return bytes + " B";
    }

    private void downloadPacks() {
        if (entries().isEmpty()) {
            status = Component.translatable("screen.moreblock.pack_download.none");
            rebuildWidgets();
            return;
        }

        completed = 0;
        try {
            for (ImportedBlockPackDownloads.DownloadEntry entry : entries()) {
                status = Component.translatable("screen.moreblock.pack_download.current", entry.displayName());
                ImportedBlockPackDownloads.saveDownloadedPack(entry);
                completed++;
            }
            downloaded = true;
            status = Component.translatable("screen.moreblock.pack_download.done", completed);
        } catch (IOException exception) {
            status = Component.literal("下载失败: " + exception.getMessage());
        }
        rebuildWidgets();
    }
}
