package me.sallos.moreblock.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.item.ImportedWallDecalItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

@SuppressWarnings("null")
public final class ImportedWallDecals {
    public static final String DEFAULT_ITEM_PAGE_ID = "wall_decals";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Path CONFIG_NAMESPACE_ROOT = FMLPaths.CONFIGDIR.get().resolve(Moreblock.MODID);
    private static final Path CONFIG_ROOT = CONFIG_NAMESPACE_ROOT.resolve("wall_decal");
    private static final Path CONFIG_PLACEHOLDER_FILE = CONFIG_ROOT.resolve(".keep");
    private static final Path SOURCE_WALL_DECAL_ROOT = Path.of("Misc", "墙面贴画");
    private static final Path GENERATED_PACK_ROOT = FMLPaths.CONFIGDIR.get().resolve(Moreblock.MODID).resolve(".generated_wall_decal_pack");

    private static final List<Definition> DEFINITIONS = new ArrayList<>();
    private static final Map<String, Definition> DEFINITIONS_BY_KEY = new LinkedHashMap<>();
    private static final Map<Item, Definition> DEFINITIONS_BY_ITEM = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<Item>> DYNAMIC_ITEMS = new LinkedHashMap<>();
    private static boolean bootstrapped = false;

    private ImportedWallDecals() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }

        DEFINITIONS.clear();
        DEFINITIONS_BY_KEY.clear();
        DEFINITIONS_BY_ITEM.clear();
        try {
            ensureConfigRoot();
            rebuildGeneratedPack();
        } catch (Exception exception) {
            Moreblock.LOGGER.error("初始化墙纸贴画目录失败: {}", CONFIG_ROOT, exception);
        }
        bootstrapped = true;
        Moreblock.LOGGER.info("墙纸贴画扫描完成，共读取 {} 个配置", DEFINITIONS.size());
    }

    public static synchronized Map<String, RegistryObject<Item>> registerItems(DeferredRegister<Item> registry) {
        bootstrap();
        for (Definition definition : DEFINITIONS) {
            if (DYNAMIC_ITEMS.containsKey(definition.registryName())) {
                continue;
            }
            RegistryObject<Item> registryObject = registry.register(definition.registryName(),
                    () -> new ImportedWallDecalItem(new Item.Properties(), definition.registryName()));
            DYNAMIC_ITEMS.put(definition.registryName(), registryObject);
        }
        return Collections.unmodifiableMap(DYNAMIC_ITEMS);
    }

    public static synchronized List<RegistryObject<Item>> getDynamicItemRegistryObjects() {
        bootstrap();
        return List.copyOf(DYNAMIC_ITEMS.values());
    }

    public static synchronized Optional<RegistryObject<Item>> getDynamicItemRegistryObject(String registryName) {
        bootstrap();
        return Optional.ofNullable(DYNAMIC_ITEMS.get(registryName));
    }

    public static synchronized Definition getDefinition(String registryName) {
        bootstrap();
        return DEFINITIONS_BY_KEY.get(registryName);
    }

    public static synchronized Definition getDefinition(Item item) {
        bootstrap();
        Definition cached = DEFINITIONS_BY_ITEM.get(item);
        if (cached != null) {
            return cached;
        }
        for (Map.Entry<String, RegistryObject<Item>> entry : DYNAMIC_ITEMS.entrySet()) {
            if (entry.getValue().isPresent() && entry.getValue().get() == item) {
                Definition definition = DEFINITIONS_BY_KEY.get(entry.getKey());
                if (definition != null) {
                    DEFINITIONS_BY_ITEM.put(item, definition);
                }
                return definition;
            }
        }
        return null;
    }

    public static synchronized List<ItemPageDefinition> getItemPages() {
        bootstrap();
        return buildItemPages();
    }

    private static List<ItemPageDefinition> buildItemPages() {
        Map<String, ItemPageDefinition> pages = new LinkedHashMap<>();
        for (Definition definition : DEFINITIONS) {
            if (!isPresent(definition.itemPageId())) {
                continue;
            }
            pages.computeIfAbsent(definition.itemPageId(), id -> new ItemPageDefinition(
                    id,
                    "item_page_" + sanitizeToRegistryPath(id),
                    firstNonBlank(definition.itemPageZhCnName(), id),
                    firstNonBlank(definition.itemPageEnUsName(), definition.itemPageZhCnName(), id),
                    firstNonBlank(definition.itemPageIconSourceId(), definition.sourceConfigId(), definition.registryName())
            ));
        }
        return List.copyOf(pages.values());
    }

    public static boolean hasDefinitions() {
        bootstrap();
        return !DEFINITIONS.isEmpty();
    }

    public static Path getGeneratedPackRoot() {
        bootstrap();
        return GENERATED_PACK_ROOT;
    }

    public static String resolveDisplayName(Definition definition) {
        String languageCode = firstNonBlank(readLanguageCodeFromOptionsFile(), readLanguageCodeFromClientOptions());
        if (languageCode != null && languageCode.toLowerCase(Locale.ROOT).startsWith("zh")) {
            return firstNonBlank(definition.zhCnName(), definition.enUsName(), definition.registryName());
        }
        return firstNonBlank(definition.enUsName(), definition.zhCnName(), definition.registryName());
    }

    private static void ensureConfigRoot() throws IOException {
        Files.createDirectories(CONFIG_NAMESPACE_ROOT);
        Files.createDirectories(CONFIG_ROOT);
        if (Files.notExists(CONFIG_PLACEHOLDER_FILE)) {
            Files.writeString(CONFIG_PLACEHOLDER_FILE, "", StandardCharsets.UTF_8);
        }
    }

    private static void rebuildGeneratedPack() throws IOException {
        deleteTree(GENERATED_PACK_ROOT);
        Files.createDirectories(GENERATED_PACK_ROOT);
        writePackMeta();
        try (Stream<Path> stream = Files.list(CONFIG_ROOT)) {
            stream.sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .forEach(ImportedWallDecals::loadEntry);
        }
        if (DEFINITIONS.isEmpty() && Files.isDirectory(SOURCE_WALL_DECAL_ROOT)) {
            try (Stream<Path> stream = Files.list(SOURCE_WALL_DECAL_ROOT)) {
                stream.filter(ImportedWallDecals::isJsonFile)
                        .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                        .forEach(path -> loadConfig(path, SOURCE_WALL_DECAL_ROOT));
            }
        }
        writeLanguageFiles();
    }

    private static void loadEntry(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> stream = Files.list(path)) {
                    stream.filter(Files::isRegularFile)
                            .filter(ImportedWallDecals::isJsonFile)
                            .sorted(Comparator.comparing(file -> file.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                            .forEach(file -> loadConfig(file, path));
                }
                return;
            }
            if (Files.isRegularFile(path) && isJsonFile(path)) {
                loadConfig(path, path.getParent());
            }
        } catch (Exception exception) {
            Moreblock.LOGGER.error("读取墙纸贴画来源失败: {}", path, exception);
        }
    }

    private static void loadConfig(Path configFile, Path baseDirectory) {
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!Boolean.TRUE.equals(getOptionalBoolean(root, "wall_decal", "wallDecal", "wallpaper_decal", "wallpaperDecal"))) {
                return;
            }

            JsonObject namesObject = getObject(root, "name", "names");
            String sourceId = firstNonBlank(getOptionalString(root, "id", "decal_id", "registry_name", "key"), stripExtension(configFile.getFileName().toString()));
            String registryName = allocateRegistryName(sourceId);
            String textureFile = firstNonBlank(getOptionalString(root, "texture", "texture_file"), "texture.png");
            Path textureSource = baseDirectory.resolve(textureFile);
            if (!Files.isRegularFile(textureSource)) {
                Moreblock.LOGGER.warn("跳过墙纸贴画配置 {}，缺少纹理文件 {}", configFile, textureFile);
                return;
            }

            PackItemPageConfig itemPageConfig = resolvePackItemPageConfig(root);
            PackLoreConfig loreConfig = resolvePackLoreConfig(root);
            Definition definition = new Definition(
                    registryName,
                    sourceId,
                    firstNonBlank(getOptionalString(namesObject, "zh_cn", "zhCN", "zh"), getOptionalString(root, "zh_cn", "name_zh_cn"), sourceId),
                    firstNonBlank(getOptionalString(namesObject, "en_us", "enUS", "en"), getOptionalString(root, "en_us", "name_en_us"), sourceId),
                    itemPageConfig == null ? null : itemPageConfig.id(),
                    itemPageConfig == null ? null : itemPageConfig.zhCnName(),
                    itemPageConfig == null ? null : itemPageConfig.enUsName(),
                    itemPageConfig == null ? null : itemPageConfig.iconSourceId(),
                    loreConfig == null ? List.of() : loreConfig.zhCnLines(),
                    loreConfig == null ? List.of() : loreConfig.enUsLines(),
                    configFile,
                    textureSource,
                    ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "textures/wall_decals/" + registryName + ".png"),
                    ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "models/item/" + registryName + ".json")
            );
            writeGeneratedAssets(definition);
            DEFINITIONS.add(definition);
            DEFINITIONS_BY_KEY.put(registryName, definition);
        } catch (Exception exception) {
            Moreblock.LOGGER.error("导入墙纸贴画失败: {}", configFile, exception);
        }
    }

    private static void writeGeneratedAssets(Definition definition) throws IOException {
        Path assetsRoot = GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID);
        copyFile(definition.textureSourceFile(), assetsRoot.resolve("textures").resolve("wall_decals").resolve(definition.registryName() + ".png"));
        writePaddedItemTexture(definition.textureSourceFile(), assetsRoot.resolve("textures").resolve("item").resolve(definition.registryName() + ".png"));

        JsonObject itemModel = new JsonObject();
        itemModel.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", Moreblock.MODID + ":item/" + definition.registryName());
        itemModel.add("textures", textures);
        writeJson(assetsRoot.resolve("models").resolve("item").resolve(definition.registryName() + ".json"), itemModel);
    }

    private static void writeLanguageFiles() throws IOException {
        Path langRoot = GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID).resolve("lang");
        JsonObject zhCn = new JsonObject();
        JsonObject enUs = new JsonObject();
        for (Definition definition : DEFINITIONS) {
            zhCn.addProperty(definition.itemTranslationKey(), definition.zhCnName());
            enUs.addProperty(definition.itemTranslationKey(), definition.enUsName());
            for (int index = 0; index < definition.loreLineCount(); index++) {
                zhCn.addProperty(definition.itemLoreTranslationKey(index + 1), resolveLoreLine(definition.zhCnLoreLines(), definition.enUsLoreLines(), index));
                enUs.addProperty(definition.itemLoreTranslationKey(index + 1), resolveLoreLine(definition.enUsLoreLines(), definition.zhCnLoreLines(), index));
            }
        }
        for (ItemPageDefinition itemPage : buildItemPages()) {
            zhCn.addProperty(itemPage.translationKey(), itemPage.zhCnName());
            enUs.addProperty(itemPage.translationKey(), itemPage.enUsName());
        }
        writeJson(langRoot.resolve("zh_cn.json"), zhCn);
        writeJson(langRoot.resolve("en_us.json"), enUs);
    }

    private static void writePackMeta() throws IOException {
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "MoreBlock imported wall decal runtime resources");
        pack.addProperty("pack_format", 15);
        JsonObject root = new JsonObject();
        root.add("pack", pack);
        writeJson(GENERATED_PACK_ROOT.resolve("pack.mcmeta"), root);
    }

    private static void copyFile(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void writePaddedItemTexture(Path source, Path target) throws IOException {
        BufferedImage original = ImageIO.read(source.toFile());
        if (original == null || original.getWidth() <= 0 || original.getHeight() <= 0) {
            copyFile(source, target);
            return;
        }

        int canvasSize = Math.max(original.getWidth(), original.getHeight());
        BufferedImage canvas = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            int offsetX = (canvasSize - original.getWidth()) / 2;
            int offsetY = (canvasSize - original.getHeight()) / 2;
            graphics.drawImage(original, offsetX, offsetY, null);
        } finally {
            graphics.dispose();
        }

        Files.createDirectories(target.getParent());
        if (!ImageIO.write(canvas, "png", target.toFile())) {
            throw new IOException("无法写入墙纸物品贴图: " + target);
        }
    }

    private static void writeJson(Path target, JsonObject jsonObject) throws IOException {
        Files.createDirectories(target.getParent());
        try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            GSON.toJson(jsonObject, writer);
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
    }

    private static String allocateRegistryName(String source) {
        String base = sanitizeToRegistryPath(source);
        if (base.isBlank()) {
            base = "wall_decal";
        }
        String candidate = trimRegistryPath("wall_decal_" + base);
        String unique = candidate;
        int index = 2;
        while (DEFINITIONS_BY_KEY.containsKey(unique)) {
            unique = trimRegistryPath(candidate + "_" + index);
            index++;
        }
        return unique;
    }

    private static String trimRegistryPath(String input) {
        String normalized = input.toLowerCase(Locale.ROOT);
        return normalized.length() <= 48 ? normalized : normalized.substring(0, 48);
    }

    private static String sanitizeToRegistryPath(String source) {
        StringBuilder builder = new StringBuilder();
        source.codePoints().forEach(codePoint -> {
            if (codePoint >= 'A' && codePoint <= 'Z') {
                builder.appendCodePoint(Character.toLowerCase(codePoint));
            } else if ((codePoint >= 'a' && codePoint <= 'z') || (codePoint >= '0' && codePoint <= '9')) {
                builder.appendCodePoint(codePoint);
            } else if (codePoint == '-' || codePoint == '_' || codePoint == ' ') {
                builder.append('_');
            } else {
                builder.append('u').append(Integer.toHexString(codePoint));
            }
        });
        return builder.toString().replaceAll("_+", "_");
    }

    private static boolean isJsonFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    private static String stripExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index > 0 ? fileName.substring(0, index) : fileName;
    }

    private static JsonObject getObject(JsonObject root, String... keys) {
        for (String key : keys) {
            if (root != null && root.has(key) && root.get(key).isJsonObject()) {
                return root.getAsJsonObject(key);
            }
        }
        return null;
    }

    private static JsonElement getElement(JsonObject root, String... keys) {
        if (root == null) {
            return null;
        }
        for (String key : keys) {
            if (root.has(key) && !root.get(key).isJsonNull()) {
                return root.get(key);
            }
        }
        return null;
    }

    private static String getOptionalString(JsonObject root, String... keys) {
        if (root == null) {
            return null;
        }
        for (String key : keys) {
            if (!root.has(key) || root.get(key).isJsonNull()) {
                continue;
            }
            try {
                String value = root.get(key).getAsString();
                if (isPresent(value)) {
                    return value.trim();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Boolean getOptionalBoolean(JsonObject root, String... keys) {
        if (root == null) {
            return null;
        }
        for (String key : keys) {
            if (!root.has(key) || root.get(key).isJsonNull()) {
                continue;
            }
            try {
                return root.get(key).getAsBoolean();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static PackItemPageConfig resolvePackItemPageConfig(JsonObject root) {
        JsonElement itemPageElement = getElement(root, "item_page", "itemPage", "creative_tab", "creativeTab");
        JsonObject itemPageObject = itemPageElement != null && itemPageElement.isJsonObject() ? itemPageElement.getAsJsonObject() : null;
        JsonObject itemPageNameObject = getObject(itemPageObject, "name", "names");
        String itemPageId = null;
        if (itemPageElement != null && itemPageElement.isJsonPrimitive()) {
            try {
                itemPageId = itemPageElement.getAsString();
            } catch (Exception ignored) {
            }
        }
        itemPageId = firstNonBlank(itemPageId, getOptionalString(itemPageObject, "id", "page_id", "pageId", "tab_id", "tabId", "key"));
        if (!isPresent(itemPageId)) {
            return null;
        }
        String zhCnName = firstNonBlank(getOptionalString(itemPageNameObject, "zh_cn", "zhCN", "zh"), getOptionalString(itemPageObject, "zh_cn"));
        String enUsName = firstNonBlank(getOptionalString(itemPageNameObject, "en_us", "enUS", "en"), getOptionalString(itemPageObject, "en_us"));
        String iconSourceId = getOptionalString(itemPageObject, "icon", "icon_id", "iconId", "item", "item_id", "itemId");
        return new PackItemPageConfig(itemPageId.trim(), zhCnName, enUsName, iconSourceId);
    }

    private static PackLoreConfig resolvePackLoreConfig(JsonObject root) {
        JsonElement loreElement = getElement(root, "lore", "description", "desc", "tooltip");
        if (loreElement == null || loreElement.isJsonNull()) {
            return null;
        }
        if (!loreElement.isJsonObject()) {
            List<String> lines = readLoreLines(loreElement);
            return lines.isEmpty() ? null : new PackLoreConfig(lines, lines);
        }
        JsonObject loreObject = loreElement.getAsJsonObject();
        List<String> defaultLines = readLoreLines(getElement(loreObject, "default", "value", "lines"));
        List<String> zhCnLines = firstNonEmpty(readLoreLines(getElement(loreObject, "zh_cn", "zhCN", "zh")), defaultLines);
        List<String> enUsLines = firstNonEmpty(readLoreLines(getElement(loreObject, "en_us", "enUS", "en")), zhCnLines, defaultLines);
        zhCnLines = firstNonEmpty(zhCnLines, enUsLines);
        enUsLines = firstNonEmpty(enUsLines, zhCnLines);
        return zhCnLines.isEmpty() && enUsLines.isEmpty() ? null : new PackLoreConfig(zhCnLines, enUsLines);
    }

    private static List<String> readLoreLines(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                lines.addAll(readLoreLines(child));
            }
            return normalizeLoreLines(lines);
        }
        if (!element.isJsonPrimitive()) {
            return List.of();
        }
        String[] splitLines = element.getAsString().replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        Collections.addAll(lines, splitLines);
        return normalizeLoreLines(lines);
    }

    private static List<String> normalizeLoreLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String line : lines) {
            normalized.add(line == null ? "" : line.strip());
        }
        int start = 0;
        while (start < normalized.size() && normalized.get(start).isEmpty()) {
            start++;
        }
        int end = normalized.size() - 1;
        while (end >= start && normalized.get(end).isEmpty()) {
            end--;
        }
        return start > end ? List.of() : List.copyOf(normalized.subList(start, end + 1));
    }

    @SafeVarargs
    private static <T> List<T> firstNonEmpty(List<T>... candidates) {
        if (candidates == null) {
            return List.of();
        }
        for (List<T> candidate : candidates) {
            if (candidate != null && !candidate.isEmpty()) {
                return List.copyOf(candidate);
            }
        }
        return List.of();
    }

    private static String resolveLoreLine(List<String> preferred, List<String> fallback, int index) {
        if (preferred != null && index >= 0 && index < preferred.size()) {
            return preferred.get(index);
        }
        if (fallback != null && index >= 0 && index < fallback.size()) {
            return fallback.get(index);
        }
        return "";
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (isPresent(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static String readLanguageCodeFromOptionsFile() {
        Path optionsFile = FMLPaths.GAMEDIR.get().resolve("options.txt");
        if (!Files.isRegularFile(optionsFile)) {
            return null;
        }
        try (Stream<String> lines = Files.lines(optionsFile, StandardCharsets.UTF_8)) {
            return lines.filter(line -> line.startsWith("lang:"))
                    .map(line -> line.substring("lang:".length()).trim())
                    .filter(ImportedWallDecals::isPresent)
                    .findFirst()
                    .orElse(null);
        } catch (IOException exception) {
            return null;
        }
    }

    private static String readLanguageCodeFromClientOptions() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            Object options = minecraftClass.getField("options").get(minecraft);
            return (String) options.getClass().getField("languageCode").get(options);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private record PackItemPageConfig(String id, String zhCnName, String enUsName, String iconSourceId) {
    }

    private record PackLoreConfig(List<String> zhCnLines, List<String> enUsLines) {
    }

    public record ItemPageDefinition(String id, String registryName, String zhCnName, String enUsName, String iconSourceId) {
        public String translationKey() {
            return "itemGroup." + Moreblock.MODID + "." + registryName;
        }
    }

    public record Definition(
            String registryName,
            String sourceConfigId,
            String zhCnName,
            String enUsName,
            String itemPageId,
            String itemPageZhCnName,
            String itemPageEnUsName,
            String itemPageIconSourceId,
            List<String> zhCnLoreLines,
            List<String> enUsLoreLines,
            Path configSourceFile,
            Path textureSourceFile,
            ResourceLocation textureLocation,
            ResourceLocation itemModelLocation
    ) {
        public String itemTranslationKey() {
            return "item." + Moreblock.MODID + "." + registryName;
        }

        public String itemLoreTranslationKey(int lineNumber) {
            return itemTranslationKey() + ".lore." + lineNumber;
        }

        public int loreLineCount() {
            return Math.max(zhCnLoreLines.size(), enUsLoreLines.size());
        }

        public boolean hasLore() {
            return loreLineCount() > 0;
        }
    }
}
