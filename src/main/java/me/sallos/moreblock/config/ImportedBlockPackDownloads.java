package me.sallos.moreblock.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sallos.moreblock.Moreblock;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ImportedBlockPackDownloads {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Path CONFIG_NAMESPACE_ROOT = FMLPaths.CONFIGDIR.get().resolve(Moreblock.MODID);
    private static final Path CONFIG_FILE = CONFIG_NAMESPACE_ROOT.resolve("block_pack_download_server.json");
    private static final Path CLIENT_BLOCK_ROOT = CONFIG_NAMESPACE_ROOT.resolve("block");
    private static final Path CLIENT_ENTITY_ROOT = CONFIG_NAMESPACE_ROOT.resolve("entity");
    private static final Path BACKUP_ROOT = CONFIG_NAMESPACE_ROOT.resolve(".backup");
    private static final int DEFAULT_MAX_PACK_BYTES = 32 * 1024 * 1024;
    private static final DateTimeFormatter BACKUP_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private static boolean bootstrapped = false;
    private static boolean enabled = true;
    private static int maxPackBytes = DEFAULT_MAX_PACK_BYTES;

    private ImportedBlockPackDownloads() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        try {
            Files.createDirectories(CONFIG_NAMESPACE_ROOT);
            if (Files.notExists(CONFIG_FILE)) {
                writeDefaultConfig();
            }
            readConfig();
        } catch (Exception exception) {
            Moreblock.LOGGER.error("读取导入包下载配置失败，已回退默认启用", exception);
            enabled = true;
            maxPackBytes = DEFAULT_MAX_PACK_BYTES;
        }
        bootstrapped = true;
    }

    public static boolean isEnabled() {
        bootstrap();
        return enabled;
    }

    public static List<DownloadEntry> buildServerBlockDownloadEntries() {
        bootstrap();
        if (!enabled) {
            return List.of();
        }

        Map<String, ImportedBlockPacks.PackManifestEntry> manifestByRegistryName = new LinkedHashMap<>();
        for (ImportedBlockPacks.PackManifestEntry entry : ImportedBlockPacks.getPackManifest()) {
            manifestByRegistryName.put(entry.registryName(), entry);
        }

        List<DownloadEntry> entries = new ArrayList<>();
        for (ImportedBlockPacks.Definition definition : ImportedBlockPacks.getDefinitions()) {
            ImportedBlockPacks.PackManifestEntry manifestEntry = manifestByRegistryName.get(definition.registryName());
            addDownloadEntry(entries, "block", definition.registryName(), definition.displayName(), manifestEntry, definition.packDirectory(), CLIENT_BLOCK_ROOT, definition.sourceZipName());
        }
        return List.copyOf(entries);
    }

    public static List<DownloadEntry> buildServerEntityDownloadEntries() {
        bootstrap();
        if (!enabled) {
            return List.of();
        }

        Map<String, ImportedEntityPacks.PackManifestEntry> manifestByRegistryName = new LinkedHashMap<>();
        for (ImportedEntityPacks.PackManifestEntry entry : ImportedEntityPacks.getPackManifest()) {
            manifestByRegistryName.put(entry.registryName(), entry);
        }

        List<DownloadEntry> entries = new ArrayList<>();
        for (ImportedEntityPacks.Definition definition : ImportedEntityPacks.getDefinitions()) {
            ImportedEntityPacks.PackManifestEntry manifestEntry = manifestByRegistryName.get(definition.registryName());
            addDownloadEntry(entries, "entity", definition.registryName(), definition.displayName(), manifestEntry, definition.packDirectory(), CLIENT_ENTITY_ROOT, definition.sourceZipName());
        }
        return List.copyOf(entries);
    }

    public static void saveDownloadedPack(DownloadEntry entry) throws IOException {
        Path root = switch (entry.packType()) {
            case "entity" -> CLIENT_ENTITY_ROOT;
            case "block" -> CLIENT_BLOCK_ROOT;
            default -> throw new IOException("未知导入包类型: " + entry.packType());
        };
        Files.createDirectories(root);
        String actualHash = sha256(entry.content());
        if (!actualHash.equalsIgnoreCase(entry.archiveSha256())) {
            throw new IOException("下载文件校验失败: " + entry.displayName());
        }

        if ("directory".equals(entry.storageType())) {
            Path targetDirectory = resolveSafePath(root, entry.relativePath());
            backupExistingPath(targetDirectory, entry.packType());
            unzipToDirectory(entry.content(), targetDirectory);
            return;
        }

        Path target = resolveSafePath(root, entry.relativePath());
        backupExistingPath(target, entry.packType());
        Files.createDirectories(target.getParent());
        Path temp = target.getParent().resolve(target.getFileName() + ".download");
        Files.write(temp, entry.content());
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void addDownloadEntry(List<DownloadEntry> entries, String packType, String registryName, String displayName,
                                         Object manifestEntry, Path packDirectory, Path configRoot, String sourceZipName) {
        if (packDirectory == null || !Files.isDirectory(packDirectory) || manifestEntry == null) {
            return;
        }
        try {
            String sourceName;
            String fingerprint;
            if (manifestEntry instanceof ImportedBlockPacks.PackManifestEntry blockEntry) {
                sourceName = blockEntry.sourceName();
                fingerprint = blockEntry.fingerprint();
            } else if (manifestEntry instanceof ImportedEntityPacks.PackManifestEntry entityEntry) {
                sourceName = entityEntry.sourceName();
                fingerprint = entityEntry.fingerprint();
            } else {
                return;
            }
            boolean fromZip = sourceZipName != null && !sourceZipName.isBlank();
            String storageType = fromZip ? "zip" : "directory";
            String relativePath = fromZip ? safeRelativePath(sourceZipName) : safeRelativePath(configRoot, packDirectory);
            String fileName = fromZip ? fileNameFromRelativePath(relativePath) : packDirectory.getFileName().toString();
            byte[] content = fromZip ? Files.readAllBytes(resolveSafePath(configRoot, relativePath)) : zipPackDirectory(packDirectory, maxPackBytes);
            if (content.length > maxPackBytes) {
                Moreblock.LOGGER.warn("跳过推送导入包 {}:{}，大小 {} 超过限制 {}", packType, registryName, content.length, maxPackBytes);
                return;
            }
            entries.add(new DownloadEntry(
                    packType,
                    storageType,
                    registryName,
                    displayName,
                    sourceName,
                    fingerprint,
                    fileName,
                    relativePath,
                    sha256(content),
                    content
            ));
        } catch (Exception exception) {
            Moreblock.LOGGER.warn("生成推送导入包失败: {}:{}", packType, registryName, exception);
        }
    }

    private static void backupExistingPath(Path target, String packType) throws IOException {
        if (Files.notExists(target)) {
            return;
        }
        Path backupDirectory = BACKUP_ROOT.resolve(BACKUP_TIME_FORMAT.format(LocalDateTime.now())).resolve(packType);
        Files.createDirectories(backupDirectory);
        Files.move(target, backupDirectory.resolve(target.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void unzipToDirectory(byte[] content, Path targetDirectory) throws IOException {
        Files.createDirectories(targetDirectory);
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path target = targetDirectory.resolve(entry.getName()).normalize();
                if (!target.startsWith(targetDirectory)) {
                    throw new IOException("导入包压缩内容路径非法: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
                }
                zip.closeEntry();
            }
        }
    }

    private static Path resolveSafePath(Path root, String relativePath) throws IOException {
        Path target = root.resolve(relativePath == null || relativePath.isBlank() ? "moreblock_pack" : relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("下载保存路径非法: " + relativePath);
        }
        return target;
    }

    private static String safeRelativePath(Path root, Path path) throws IOException {
        Path relative = root.relativize(path);
        String normalized = relative.toString().replace('\\', '/');
        String[] parts = normalized.split("/");
        List<String> safeParts = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                safeParts.add(safePathSegment(part));
            }
        }
        if (safeParts.isEmpty()) {
            throw new IOException("导入包相对路径为空: " + path);
        }
        return String.join("/", safeParts);
    }

    private static String safeRelativePath(String relativePath) throws IOException {
        String normalized = relativePath.replace('\\', '/');
        String[] parts = normalized.split("/");
        List<String> safeParts = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                safeParts.add(safePathSegment(part));
            }
        }
        if (safeParts.isEmpty()) {
            throw new IOException("导入包相对路径为空: " + relativePath);
        }
        return String.join("/", safeParts);
    }

    private static String fileNameFromRelativePath(String relativePath) {
        int slashIndex = relativePath.lastIndexOf('/');
        return slashIndex < 0 ? relativePath : relativePath.substring(slashIndex + 1);
    }

    private static String safePathSegment(String value) throws IOException {
        if (value == null || value.isBlank()) {
            throw new IOException("导入包路径片段为空");
        }
        String sanitized = value.replace('\\', '_').replace('/', '_')
                .replace(':', '_').replace('*', '_').replace('?', '_')
                .replace('"', '_').replace('<', '_').replace('>', '_').replace('|', '_')
                .trim();
        if (sanitized.isBlank() || sanitized.equals(".") || sanitized.equals("..")) {
            throw new IOException("导入包路径片段非法: " + value);
        }
        return sanitized;
    }

    private static void writeDefaultConfig() throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", true);
        root.addProperty("max_pack_bytes", DEFAULT_MAX_PACK_BYTES);
        Files.writeString(CONFIG_FILE, GSON.toJson(root), StandardCharsets.UTF_8);
    }

    private static void readConfig() throws IOException {
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            enabled = !root.has("enabled") || root.get("enabled").getAsBoolean();
            maxPackBytes = root.has("max_pack_bytes") ? Math.max(1024, root.get("max_pack_bytes").getAsInt()) : DEFAULT_MAX_PACK_BYTES;
        }
    }

    private static byte[] zipPackDirectory(Path sourceDirectory, int maxBytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8);
             var paths = Files.walk(sourceDirectory)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String entryName = sourceDirectory.relativize(path).toString().replace('\\', '/');
                zip.putNextEntry(new ZipEntry(entryName));
                zip.write(Files.readAllBytes(path));
                zip.closeEntry();
                if (output.size() > maxBytes) {
                    break;
                }
            }
        }
        return output.toByteArray();
    }

    private static String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 Java 环境不支持 SHA-256", exception);
        }
    }

    public record DownloadEntry(
            String packType,
            String storageType,
            String registryName,
            String displayName,
            String sourceName,
            String fingerprint,
            String fileName,
            String relativePath,
            String archiveSha256,
            byte[] content
    ) {
    }
}
