package me.sallos.moreblock.network.message;

import me.sallos.moreblock.config.ImportedBlockPackDownloads;
import net.minecraft.network.FriendlyByteBuf;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("null")
public final class ChunkedLoginDownloadPayload {
    private static final int MAX_DOWNLOAD_FRAGMENT_BYTES = 256 * 1024;
    private static final int MAX_PACK_TYPE_LENGTH = 16;
    private static final int MAX_STORAGE_TYPE_LENGTH = 16;
    private static final int MAX_REGISTRY_NAME_LENGTH = 128;
    private static final int MAX_DISPLAY_NAME_LENGTH = 128;
    private static final int MAX_SOURCE_NAME_LENGTH = 128;
    private static final int MAX_FINGERPRINT_LENGTH = 64;
    private static final int MAX_FILE_NAME_LENGTH = 128;
    private static final int MAX_RELATIVE_PATH_LENGTH = 256;
    private static final int MAX_ARCHIVE_SHA256_LENGTH = 64;
    private static final int MAX_DOWNLOAD_FRAGMENT_READ_BYTES = 512 * 1024;

    private ChunkedLoginDownloadPayload() {
    }

    public static List<DownloadChunk> split(List<ImportedBlockPackDownloads.DownloadEntry> entries) {
        if (entries.isEmpty()) {
            return List.of();
        }

        List<DownloadChunk> chunks = new ArrayList<>();
        for (ImportedBlockPackDownloads.DownloadEntry entry : entries) {
            byte[] content = entry.content();
            if (content.length == 0) {
                chunks.add(new DownloadChunk(
                        entry.packType(),
                        entry.storageType(),
                        entry.registryName(),
                        entry.displayName(),
                        entry.sourceName(),
                        entry.fingerprint(),
                        entry.fileName(),
                        entry.relativePath(),
                        entry.archiveSha256(),
                        0,
                        0,
                        true,
                        new byte[0]
                ));
                continue;
            }

            for (int offset = 0; offset < content.length; offset += MAX_DOWNLOAD_FRAGMENT_BYTES) {
                int length = Math.min(MAX_DOWNLOAD_FRAGMENT_BYTES, content.length - offset);
                chunks.add(new DownloadChunk(
                        entry.packType(),
                        entry.storageType(),
                        entry.registryName(),
                        entry.displayName(),
                        entry.sourceName(),
                        entry.fingerprint(),
                        entry.fileName(),
                        entry.relativePath(),
                        entry.archiveSha256(),
                        content.length,
                        offset,
                        offset + length >= content.length,
                        Arrays.copyOfRange(content, offset, offset + length)
                ));
            }
        }
        return List.copyOf(chunks);
    }

    public static void write(DownloadChunk chunk, FriendlyByteBuf buffer) {
        buffer.writeUtf(chunk.packType(), MAX_PACK_TYPE_LENGTH);
        buffer.writeUtf(chunk.storageType(), MAX_STORAGE_TYPE_LENGTH);
        buffer.writeUtf(chunk.registryName(), MAX_REGISTRY_NAME_LENGTH);
        buffer.writeUtf(chunk.displayName(), MAX_DISPLAY_NAME_LENGTH);
        buffer.writeUtf(chunk.sourceName(), MAX_SOURCE_NAME_LENGTH);
        buffer.writeUtf(chunk.fingerprint(), MAX_FINGERPRINT_LENGTH);
        buffer.writeUtf(chunk.fileName(), MAX_FILE_NAME_LENGTH);
        buffer.writeUtf(chunk.relativePath(), MAX_RELATIVE_PATH_LENGTH);
        buffer.writeUtf(chunk.archiveSha256(), MAX_ARCHIVE_SHA256_LENGTH);
        buffer.writeVarInt(chunk.totalContentLength());
        buffer.writeVarInt(chunk.chunkOffset());
        buffer.writeBoolean(chunk.lastChunk());
        buffer.writeByteArray(chunk.chunkContent());
    }

    public static DownloadChunk read(FriendlyByteBuf buffer) {
        return new DownloadChunk(
                buffer.readUtf(MAX_PACK_TYPE_LENGTH),
                buffer.readUtf(MAX_STORAGE_TYPE_LENGTH),
                buffer.readUtf(MAX_REGISTRY_NAME_LENGTH),
                buffer.readUtf(MAX_DISPLAY_NAME_LENGTH),
                buffer.readUtf(MAX_SOURCE_NAME_LENGTH),
                buffer.readUtf(MAX_FINGERPRINT_LENGTH),
                buffer.readUtf(MAX_FILE_NAME_LENGTH),
                buffer.readUtf(MAX_RELATIVE_PATH_LENGTH),
                buffer.readUtf(MAX_ARCHIVE_SHA256_LENGTH),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readByteArray(MAX_DOWNLOAD_FRAGMENT_READ_BYTES)
        );
    }

    public static final class AssemblyState {
        private final List<ImportedBlockPackDownloads.DownloadEntry> completedEntries = new ArrayList<>();
        private EntryAssembly activeEntry;

        public void reset() {
            completedEntries.clear();
            activeEntry = null;
        }

        public void append(DownloadChunk chunk) {
            if (activeEntry == null || !activeEntry.matches(chunk)) {
                if (activeEntry != null) {
                    throw new IllegalArgumentException("下载分片顺序非法: " + chunk.registryName());
                }
                activeEntry = new EntryAssembly(chunk);
            }
            activeEntry.append(chunk);
            if (activeEntry.isComplete()) {
                completedEntries.add(activeEntry.finish());
                activeEntry = null;
            }
        }

        public List<ImportedBlockPackDownloads.DownloadEntry> snapshotCompletedEntries() {
            if (activeEntry != null) {
                throw new IllegalStateException("下载分片未接收完整");
            }
            return List.copyOf(completedEntries);
        }
    }

    public record DownloadChunk(
            String packType,
            String storageType,
            String registryName,
            String displayName,
            String sourceName,
            String fingerprint,
            String fileName,
            String relativePath,
            String archiveSha256,
            int totalContentLength,
            int chunkOffset,
            boolean lastChunk,
            byte[] chunkContent
    ) {
        public String entryKey() {
            return packType + ":" + relativePath;
        }
    }

    private static final class EntryAssembly {
        private final DownloadChunk firstChunk;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        private EntryAssembly(DownloadChunk firstChunk) {
            this.firstChunk = firstChunk;
        }

        private boolean matches(DownloadChunk chunk) {
            return firstChunk.entryKey().equals(chunk.entryKey())
                    && firstChunk.archiveSha256().equals(chunk.archiveSha256())
                    && firstChunk.totalContentLength() == chunk.totalContentLength();
        }

        private void append(DownloadChunk chunk) {
            if (chunk.totalContentLength() < 0) {
                throw new IllegalArgumentException("下载分片总长度非法: " + chunk.totalContentLength());
            }
            if (chunk.chunkOffset() != output.size()) {
                throw new IllegalArgumentException("下载分片偏移非法: " + chunk.registryName() + "@" + chunk.chunkOffset());
            }
            output.writeBytes(chunk.chunkContent());
            if (output.size() > chunk.totalContentLength()) {
                throw new IllegalArgumentException("下载分片累计长度超出声明总长度: " + chunk.registryName());
            }
            if (chunk.lastChunk() && output.size() != chunk.totalContentLength()) {
                throw new IllegalArgumentException("下载分片组装长度不完整: " + chunk.registryName());
            }
        }

        private boolean isComplete() {
            return output.size() == firstChunk.totalContentLength();
        }

        private ImportedBlockPackDownloads.DownloadEntry finish() {
            return new ImportedBlockPackDownloads.DownloadEntry(
                    firstChunk.packType(),
                    firstChunk.storageType(),
                    firstChunk.registryName(),
                    firstChunk.displayName(),
                    firstChunk.sourceName(),
                    firstChunk.fingerprint(),
                    firstChunk.fileName(),
                    firstChunk.relativePath(),
                    firstChunk.archiveSha256(),
                    output.toByteArray()
            );
        }
    }
}
