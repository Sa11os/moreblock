package me.sallos.moreblock.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.api.MoreBlockBlockDefinition;
import me.sallos.moreblock.api.RegisteredMoreBlock;
import me.sallos.moreblock.block.ImportedBlock;
import me.sallos.moreblock.block.entity.ImportedBlockEntity;
import me.sallos.moreblock.init.ImportedBlocks;
import me.sallos.moreblock.init.ImportedItems;
import me.sallos.moreblock.item.ImportedBlockItem;
import me.sallos.moreblock.util.GeoHitboxSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.HexFormat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;

@SuppressWarnings("null")
public final class ImportedBlockPacks {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String DYNAMIC_ANIMATION_NAME = "config_block_idle.animation.json";
    private static final Path CONFIG_NAMESPACE_ROOT = FMLPaths.CONFIGDIR.get().resolve(Moreblock.MODID);
    private static final Path CONFIG_ROOT = CONFIG_NAMESPACE_ROOT.resolve("block");
    private static final String EXAMPLE_DIRECTORY_NAME = "example";
    private static final Path CONFIG_PLACEHOLDER_FILE = CONFIG_ROOT.resolve(".keep");
    private static final Path CONFIG_GUIDE_FILE = CONFIG_ROOT.resolve("README.txt");
    private static final Path EXAMPLE_CONFIG_DIR = CONFIG_ROOT.resolve(EXAMPLE_DIRECTORY_NAME);
    private static final Path EXAMPLE_CONFIG_FILE = EXAMPLE_CONFIG_DIR.resolve("example.json");
    private static final Path EXAMPLE_MARKDOWN_FILE = EXAMPLE_CONFIG_DIR.resolve("example.md");
    private static final Path GENERATED_PACK_ROOT = FMLPaths.CONFIGDIR.get().resolve(Moreblock.MODID).resolve(".generated_block_pack");
    private static final Path GENERATED_SOURCE_ROOT = GENERATED_PACK_ROOT.resolve("_sources");

    private static final List<Definition> DEFINITIONS = new ArrayList<>();
    private static final Map<String, Definition> DEFINITIONS_BY_KEY = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<Block>> DYNAMIC_BLOCKS = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<Item>> DYNAMIC_ITEMS = new LinkedHashMap<>();
    private static final Map<String, GeoHitboxSystem.HorizontalShapes> API_HORIZONTAL_SHAPES = new LinkedHashMap<>();
    private static final Map<Block, Definition> DEFINITIONS_BY_BLOCK = new IdentityHashMap<>();
    private static final Map<Item, Definition> DEFINITIONS_BY_ITEM = new IdentityHashMap<>();
    private static List<PackManifestEntry> PACK_MANIFEST = List.of();
    private static boolean packManifestDirty = false;

    private static boolean bootstrapped = false;

    private ImportedBlockPacks() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }

        DEFINITIONS.clear();
        DEFINITIONS_BY_KEY.clear();
        DEFINITIONS_BY_BLOCK.clear();
        DEFINITIONS_BY_ITEM.clear();
        API_HORIZONTAL_SHAPES.clear();
        PACK_MANIFEST = List.of();
        packManifestDirty = false;

        try {
            ensureConfigRoot();
            rebuildGeneratedPack();
            PACK_MANIFEST = buildPackManifest();
            packManifestDirty = false;
        } catch (Exception exception) {
            Moreblock.LOGGER.error("初始化配置方块目录失败: {}", CONFIG_ROOT, exception);
        }

        bootstrapped = true;
        Moreblock.LOGGER.info("配置方块扫描完成，共读取 {} 个配置包", DEFINITIONS.size());
    }

    public static synchronized Collection<Definition> getDefinitions() {
        bootstrap();
        return List.copyOf(DEFINITIONS);
    }

    public static synchronized boolean hasDefinitions() {
        bootstrap();
        return !DEFINITIONS.isEmpty();
    }

    public static synchronized Map<String, RegistryObject<Block>> registerBlocks(DeferredRegister<Block> registry) {
        bootstrap();
        for (Definition definition : DEFINITIONS) {
            if (DYNAMIC_BLOCKS.containsKey(definition.registryName())) {
                continue;
            }
            RegistryObject<Block> registryObject = registry.register(definition.registryName(), () -> new ImportedBlock(definition.registryName()));
            DYNAMIC_BLOCKS.put(definition.registryName(), registryObject);
        }
        return Collections.unmodifiableMap(DYNAMIC_BLOCKS);
    }

    public static synchronized RegisteredMoreBlock registerApiBlock(MoreBlockBlockDefinition definition) {
        bootstrap();
        String registryName = allocateRegistryName(definition.ownerModId() + '_' + definition.id());
        GeoHitboxSystem.HorizontalShapes shapes = GeoHitboxSystem.HorizontalShapes.ofFullBlock();
        Definition internalDefinition = new Definition(
                definition.ownerModId(),
                registryName,
                firstNonBlank(definition.zhCnName(), definition.id()),
                firstNonBlank(definition.enUsName(), definition.zhCnName(), definition.id()),
                definition.id(),
                null,
                null,
                null,
                null,
                null,
                null,
                shapes,
                firstNonBlank(definition.hitboxBoneName(), "hitbox"),
                definition.showInMoreBlockTab(),
                definition.translucent(),
                Math.max(0, Math.min(15, definition.lightLevel())),
                definition.supportsSitting(),
                definition.seatHeight(),
                definition.supportsLying(),
                definition.lyingHeight(),
                definition.lyingRotationCompensation(),
                definition.geo(),
                definition.texture(),
                definition.display(),
                ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "models/item/" + registryName + ".json")
        );
        DEFINITIONS.add(internalDefinition);
        DEFINITIONS_BY_KEY.put(registryName, internalDefinition);
        packManifestDirty = true;
        ImportedBlocks.registerApiBlocks();
        ImportedItems.registerApiItems();
        return new RegisteredMoreBlock(registryName);
    }

    public static synchronized GeoHitboxSystem.HorizontalShapes getHorizontalShapes(String registryName) {
        bootstrap();
        Definition definition = DEFINITIONS_BY_KEY.get(registryName);
        if (definition == null) {
            return GeoHitboxSystem.HorizontalShapes.ofFullBlock();
        }
        if (definition.geoSourceFile() != null) {
            return definition.horizontalShapes();
        }
        GeoHitboxSystem.HorizontalShapes cachedShapes = API_HORIZONTAL_SHAPES.get(registryName);
        if (cachedShapes != null) {
            return cachedShapes;
        }
        GeoHitboxSystem.HorizontalShapes resolvedShapes = loadApiHorizontalShapes(definition);
        API_HORIZONTAL_SHAPES.put(registryName, resolvedShapes);
        return resolvedShapes;
    }

    public static synchronized Map<String, RegistryObject<Block>> getDynamicBlockRegistryObjects() {
        bootstrap();
        return Collections.unmodifiableMap(DYNAMIC_BLOCKS);
    }

    public static synchronized Optional<RegistryObject<Block>> getDynamicBlockRegistryObject(String registryName) {
        bootstrap();
        return Optional.ofNullable(DYNAMIC_BLOCKS.get(registryName));
    }

    public static synchronized Optional<RegistryObject<Item>> getDynamicItemRegistryObject(String registryName) {
        bootstrap();
        return Optional.ofNullable(DYNAMIC_ITEMS.get(registryName));
    }

    public static synchronized Map<String, RegistryObject<Item>> registerItems(DeferredRegister<Item> registry) {
        bootstrap();
        for (Definition definition : DEFINITIONS) {
            if (DYNAMIC_ITEMS.containsKey(definition.registryName())) {
                continue;
            }
            RegistryObject<Block> blockRegistryObject = DYNAMIC_BLOCKS.get(definition.registryName());
            if (blockRegistryObject == null) {
                continue;
            }
            RegistryObject<Item> registryObject = registry.register(definition.registryName(),
                    () -> new ImportedBlockItem(blockRegistryObject.get(), new Item.Properties(), definition.registryName()));
            DYNAMIC_ITEMS.put(definition.registryName(), registryObject);
        }
        return Collections.unmodifiableMap(DYNAMIC_ITEMS);
    }

    public static synchronized RegistryObject<BlockEntityType<ImportedBlockEntity>> registerDynamicBlockEntity(DeferredRegister<BlockEntityType<?>> registry) {
        bootstrap();
        if (DEFINITIONS.isEmpty()) {
            return null;
        }

        return registry.register("imported_block", () -> {
            Block[] blocks = DYNAMIC_BLOCKS.values().stream().map(RegistryObject::get).toArray(Block[]::new);
            return BlockEntityType.Builder.of(ImportedBlockEntity::new, blocks).build(null);
        });
    }

    public static synchronized List<RegistryObject<Item>> getDynamicItemRegistryObjects() {
        return List.copyOf(DYNAMIC_ITEMS.values());
    }

    public static synchronized Definition getDefinition(String registryName) {
        bootstrap();
        return DEFINITIONS_BY_KEY.get(registryName);
    }

    public static synchronized Definition getDefinition(Block block) {
        bootstrap();
        Definition cached = DEFINITIONS_BY_BLOCK.get(block);
        if (cached != null) {
            return cached;
        }

        for (Map.Entry<String, RegistryObject<Block>> entry : DYNAMIC_BLOCKS.entrySet()) {
            if (entry.getValue().isPresent() && entry.getValue().get() == block) {
                Definition definition = DEFINITIONS_BY_KEY.get(entry.getKey());
                if (definition != null) {
                    DEFINITIONS_BY_BLOCK.put(block, definition);
                }
                return definition;
            }
        }
        return null;
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

    public static synchronized List<PackManifestEntry> getPackManifest() {
        bootstrap();
        flushGeneratedPackIfDirty();
        return PACK_MANIFEST;
    }

    public static String resolveDisplayName(Definition definition) {
        if (definition == null) {
            return "";
        }

        String languageCode = firstNonBlank(readLanguageCodeFromOptionsFile(), readLanguageCodeFromClientOptions());
        if (languageCode != null && languageCode.toLowerCase(Locale.ROOT).startsWith("zh")) {
            return firstNonBlank(definition.zhCnName(), definition.enUsName(), definition.registryName());
        }
        return firstNonBlank(definition.enUsName(), definition.zhCnName(), definition.registryName());
    }

    public static Path getGeneratedPackRoot() {
        bootstrap();
        flushGeneratedPackIfDirty();
        return GENERATED_PACK_ROOT;
    }

    private static void flushGeneratedPackIfDirty() {
        if (!packManifestDirty) {
            return;
        }

        try {
            writeLanguageFiles();
            writeApiGeneratedAssets();
            PACK_MANIFEST = buildPackManifest();
            packManifestDirty = false;
        } catch (IOException exception) {
            Moreblock.LOGGER.error("刷新 API 方块运行时资源失败", exception);
        }
    }

    private static void ensureConfigRoot() throws IOException {
        // 先确保模组自己的配置根目录存在，再自动创建 block 子文件夹
        Files.createDirectories(CONFIG_NAMESPACE_ROOT);
        Files.createDirectories(CONFIG_ROOT);

        // 给空目录留下可见文件，方便首次启动后直接在文件管理器里找到
        if (Files.notExists(CONFIG_PLACEHOLDER_FILE)) {
            Files.writeString(CONFIG_PLACEHOLDER_FILE, "", StandardCharsets.UTF_8);
        }

        writeExampleConfig();

        if (Files.notExists(CONFIG_GUIDE_FILE)) {
            Files.writeString(CONFIG_GUIDE_FILE, """
                    MoreBlock 导入方块目录

                    用法：
                    1. 在当前目录下创建一个子文件夹，例如：
                       config/moreblock/block/示例方块/
                    2. 子文件夹内放入一个配置文件，例如：
                       - safebox.json
                    3. 配置文件中填写标准 id、中英文名称，以及模型和纹理文件名
                    4. 子文件夹内至少放入：
                       - safebox2.geo.json
                       - texture.png
                    5. 可选放入：
                       - safebox2-display.json
                    6. 也可以直接放 zip 压缩包，压缩包内支持：
                       - 直接放文件
                       - 再套一层同名文件夹
                       - 外层容器包里混合多个子文件夹和子 zip

                    说明：
                    - 模组启动时会自动创建 `config/moreblock/block` 目录
                    - `id` 会作为方块注册路径和语言键后缀使用
                    - 客户端启动时会读取目录并挂载运行时资源
                    - README、.keep 和 example 文件夹只是说明内容，会被自动忽略
                    """, StandardCharsets.UTF_8);
        }
    }

    private static void writeExampleConfig() throws IOException {
        Files.createDirectories(EXAMPLE_CONFIG_DIR);
        writeJson(EXAMPLE_CONFIG_FILE, buildExampleConfigJson());
        Files.writeString(EXAMPLE_MARKDOWN_FILE, buildExampleMarkdownText(), StandardCharsets.UTF_8);
    }

    private static JsonObject buildExampleConfigJson() {
        JsonObject root = new JsonObject();
        for (ExampleConfigParameter parameter : exampleConfigParameters(resolveExampleConfigLanguage())) {
            parameter.apply(root);
        }
        return root;
    }

    private static String buildExampleMarkdownText() {
        ExampleConfigLanguage language = resolveExampleConfigLanguage();
        String lineSeparator = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append(language.markdownTitle()).append(lineSeparator).append(lineSeparator);
        builder.append(language.mainComment()).append(lineSeparator).append(lineSeparator);
        builder.append(language.markdownUsage()).append(lineSeparator).append(lineSeparator);
        for (ExampleConfigParameter parameter : exampleConfigParameters(language)) {
            builder.append("## `").append(parameter.key()).append("`").append(lineSeparator).append(lineSeparator);
            builder.append(parameter.description()).append(lineSeparator).append(lineSeparator);
            builder.append(language.markdownExamplePrefix()).append(" `").append(parameter.exampleText().replace(System.lineSeparator(), " ")).append("`").append(lineSeparator).append(lineSeparator);
        }
        return builder.toString();
    }

    private static List<ExampleConfigParameter> exampleConfigParameters(ExampleConfigLanguage language) {
        JsonObject name = new JsonObject();
        name.addProperty("zh_cn", "示例方块");
        name.addProperty("en_us", "Example Block");

        return List.of(
                ExampleConfigParameter.of("id", "example_block", GSON.toJsonTree("example_block"), language.describeId()),
                ExampleConfigParameter.of("name", "{ \"zh_cn\": \"示例方块\", \"en_us\": \"Example Block\" }", name, language.describeName()),
                ExampleConfigParameter.of("geo", "example_block.geo.json", GSON.toJsonTree("example_block.geo.json"), language.describeGeo()),
                ExampleConfigParameter.of("texture", "texture.png", GSON.toJsonTree("texture.png"), language.describeTexture()),
                ExampleConfigParameter.of("display", "example_block-display.json", GSON.toJsonTree("example_block-display.json"), language.describeDisplay()),
                ExampleConfigParameter.of("light_level", "15", GSON.toJsonTree(15), language.describeLightLevel()),
                ExampleConfigParameter.of("supports_sitting", "false", GSON.toJsonTree(false), language.describeSupportsSitting()),
                ExampleConfigParameter.of("seat_height", "0.5", GSON.toJsonTree(0.5d), language.describeSeatHeight()),
                ExampleConfigParameter.of("supports_lying", "false", GSON.toJsonTree(false), language.describeSupportsLying()),
                ExampleConfigParameter.of("lying_height", "0.5", GSON.toJsonTree(0.5d), language.describeLyingHeight()),
                ExampleConfigParameter.of("lying_rotation_compensation", "0", GSON.toJsonTree(0), language.describeLyingRotationCompensation())
        );
    }

    private static ExampleConfigLanguage resolveExampleConfigLanguage() {
        String languageCode = firstNonBlank(readLanguageCodeFromOptionsFile(), readLanguageCodeFromClientOptions());
        if (languageCode != null && languageCode.toLowerCase(Locale.ROOT).startsWith("zh")) {
            return ExampleConfigLanguage.zhCn();
        }
        return ExampleConfigLanguage.enUs();
    }

    private static String readLanguageCodeFromOptionsFile() {
        Path optionsFile = FMLPaths.GAMEDIR.get().resolve("options.txt");
        if (!Files.isRegularFile(optionsFile)) {
            return null;
        }

        try (Stream<String> lines = Files.lines(optionsFile, StandardCharsets.UTF_8)) {
            return lines
                    .filter(line -> line.startsWith("lang:"))
                    .map(line -> line.substring("lang:".length()).trim())
                    .filter(ImportedBlockPacks::isPresent)
                    .findFirst()
                    .orElse(null);
        } catch (IOException exception) {
            Moreblock.LOGGER.warn("读取客户端语言配置失败: {}", optionsFile, exception);
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

    private static void rebuildGeneratedPack() throws IOException {
        deleteTree(GENERATED_PACK_ROOT);
        Files.createDirectories(GENERATED_PACK_ROOT);
        Files.createDirectories(GENERATED_SOURCE_ROOT);

        writePackMeta();
        writeSharedAnimation();

        try (Stream<Path> stream = Files.list(CONFIG_ROOT)) {
            stream
                    .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .forEach(ImportedBlockPacks::loadPackEntry);
        }

        writeLanguageFiles();
    }

    private static void loadPackEntry(Path packEntry) {
        loadPackEntry(packEntry, null);
    }

    private static void loadPackEntry(Path packEntry, String sourceZipName) {
        try {
            if (isIgnoredArchivePath(packEntry) || isExampleConfigPath(packEntry)) {
                return;
            }
            if (Files.isDirectory(packEntry)) {
                loadDirectoryEntry(packEntry, sourceZipName);
                return;
            }
            if (isZipPackFile(packEntry)) {
                loadZipPack(packEntry);
            }
        } catch (Exception exception) {
            Moreblock.LOGGER.error("读取配置方块来源失败: {}", packEntry, exception);
        }
    }

    @SuppressWarnings("unused")
    private static void loadDirectoryEntry(Path packDirectory) throws IOException {
        loadDirectoryEntry(packDirectory, null);
    }

    private static void loadDirectoryEntry(Path packDirectory, String sourceZipName) throws IOException {
        if (isPackDirectory(packDirectory)) {
            loadSinglePack(packDirectory, sourceZipName);
            return;
        }

        try (Stream<Path> stream = Files.list(packDirectory)) {
            stream.sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .forEach(child -> loadPackEntry(child, sourceZipName));
        }
    }

    @SuppressWarnings("unused")
    private static void loadSinglePack(Path packDirectory) {
        loadSinglePack(packDirectory, null);
    }

    private static void loadSinglePack(Path packDirectory, String sourceZipName) {
        String folderName = packDirectory.getFileName().toString();
        try {
            PackConfig packConfig = readPackConfig(packDirectory, folderName);
            Path geoSource = resolveGeoSource(packDirectory, folderName, packConfig);
            Path displaySource = resolveDisplaySource(packDirectory, folderName, packConfig);
            Path textureSource = resolveTextureSource(packDirectory, packConfig);

            if (!Files.isRegularFile(geoSource) || !Files.isRegularFile(textureSource)) {
                Moreblock.LOGGER.warn("跳过配置方块目录 {}，缺少模型文件 {} 或纹理文件 {}",
                        packDirectory,
                        geoSource.getFileName(),
                        textureSource.getFileName());
                return;
            }

            String registryName = allocateRegistryName(firstNonBlank(packConfig.id(), folderName));

            GeoInspection inspection = inspectGeo(geoSource);
            GeoHitboxSystem.HorizontalShapes shapes;
            if (inspection.hasHitboxBone()) {
                GeoHitboxSystem.Profile profile = new GeoHitboxSystem.Profile(
                        "",
                        inspection.hitboxBoneName(),
                        true,
                        // 导入方块的 geo 命中箱需要做一次水平翻转，才能与实际模型朝向对齐
                        true,
                        false,
                        0.0d,
                        0.0d,
                        0.0d
                );
                shapes = GeoHitboxSystem.loadHorizontalShapes(geoSource, profile);
            } else {
                // 没有显式 hitbox 骨骼时，回退为标准整方块碰撞箱
                Moreblock.LOGGER.warn("导入方块 {} 未找到 hitbox 骨骼，已回退为 1x1x1 整方块碰撞箱", geoSource);
                shapes = GeoHitboxSystem.HorizontalShapes.ofFullBlock();
            }
            Definition definition = new Definition(
                    Moreblock.MODID,
                    registryName,
                    firstNonBlank(packConfig.zhCnName(), folderName),
                    firstNonBlank(packConfig.enUsName(), packConfig.zhCnName(), folderName),
                    folderName,
                    sourceZipName,
                    packDirectory,
                    packConfig.configSourceFile(),
                    geoSource,
                    displaySource,
                    textureSource,
                    shapes,
                    inspection.hitboxBoneName(),
                    true,
                    true,
                    resolveLightLevel(packConfig.lightLevel()),
                    resolveSupportsSitting(packConfig.supportsSitting(), packConfig.supportsLying()),
                    resolveSeatHeight(packConfig.seatHeight()),
                    resolveSupportsLying(packConfig.supportsSitting(), packConfig.supportsLying()),
                    resolveLyingHeight(packConfig.lyingHeight()),
                    resolveLyingRotationCompensation(packConfig.lyingRotationCompensation()),
                    ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "geo/block/" + registryName + ".geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "textures/block/" + registryName + "/texture.png"),
                    displaySource == null ? null : ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "models/item/" + registryName + ".json"),
                    ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "models/item/" + registryName + ".json")
            );

            DEFINITIONS.add(definition);
            DEFINITIONS_BY_KEY.put(registryName, definition);
            writeGeneratedAssets(definition);
        } catch (Exception exception) {
            Moreblock.LOGGER.error("导入配置方块失败: {}", packDirectory, exception);
        }
    }

    private static void loadZipPack(Path zipFile) throws IOException {
        String archiveName = stripExtension(zipFile.getFileName().toString());
        String extractFolderName = firstNonBlank(sanitizeToRegistryPath(archiveName), "zip_pack");
        Path extractRoot = GENERATED_SOURCE_ROOT.resolve(extractFolderName);

        deleteTree(extractRoot);
        Files.createDirectories(extractRoot);
        extractZip(zipFile, extractRoot);
        loadDirectoryEntry(resolveExtractedPackDirectory(extractRoot), zipFile.getFileName().toString());
    }

    private static PackConfig readPackConfig(Path packDirectory, String folderName) throws IOException {
        Path configSource = findPackConfigFile(packDirectory);
        if (configSource == null) {
            return PackConfig.legacy();
        }

        try (Reader reader = Files.newBufferedReader(configSource, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject namesObject = getObject(root, "name", "names");
            return new PackConfig(
                    configSource,
                    getOptionalString(root, "id", "block_id", "registry_name", "key"),
                    firstNonBlank(
                            getOptionalString(namesObject, "zh_cn", "zhCN", "zh"),
                            getOptionalString(root, "zh_cn", "name_zh_cn", "display_name_zh_cn"),
                            folderName
                    ),
                    firstNonBlank(
                            getOptionalString(namesObject, "en_us", "enUS", "en"),
                            getOptionalString(root, "en_us", "name_en_us", "display_name_en_us"),
                            folderName
                    ),
                    firstNonBlank(getOptionalString(root, "geo", "geo_file", "model", "model_file"), null),
                    firstNonBlank(getOptionalString(root, "texture", "texture_file"), "texture.png"),
                    firstNonBlank(getOptionalString(root, "display", "display_file", "item_display", "item_display_file"), null),
                    getOptionalInt(root, "light_level", "lightLevel", "emission", "light"),
                    getOptionalBoolean(root, "supports_sitting", "supportsSitting", "sittable", "can_sit", "canSit"),
                    getOptionalDouble(root, "seat_height", "seatHeight", "sitting_height", "sittingHeight"),
                    getOptionalBoolean(root, "supports_lying", "supportsLying", "lieable", "can_lie", "canLie"),
                    getOptionalDouble(root, "lying_height", "lyingHeight", "lie_height", "lieHeight"),
                    getOptionalInt(root, "lying_rotation_compensation", "lyingRotationCompensation", "bed_rotation_compensation", "bedRotationCompensation")
            );
        }
    }

    private static Path findPackConfigFile(Path packDirectory) throws IOException {
        try (Stream<Path> stream = Files.list(packDirectory)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .filter(path -> !isDisplayModelFile(path))
                    .filter(path -> !looksLikeGeoModel(path))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static boolean isDisplayModelFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith("-display.json");
    }

    private static boolean looksLikeGeoModel(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".geo.json")) {
            return true;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            return element.isJsonObject() && element.getAsJsonObject().has("minecraft:geometry");
        } catch (Exception exception) {
            return false;
        }
    }

    private static Path resolveGeoSource(Path packDirectory, String folderName, PackConfig packConfig) {
        if (isPresent(packConfig.geoFile())) {
            return packDirectory.resolve(packConfig.geoFile());
        }

        Path modernGeo = packDirectory.resolve(folderName + ".geo.json");
        if (Files.isRegularFile(modernGeo)) {
            return modernGeo;
        }

        return packDirectory.resolve(folderName + ".json");
    }

    private static Path resolveDisplaySource(Path packDirectory, String folderName, PackConfig packConfig) {
        if (isPresent(packConfig.displayFile())) {
            Path configuredDisplay = packDirectory.resolve(packConfig.displayFile());
            return Files.isRegularFile(configuredDisplay) ? configuredDisplay : null;
        }

        Path legacyDisplay = packDirectory.resolve(folderName + "-display.json");
        return Files.isRegularFile(legacyDisplay) ? legacyDisplay : null;
    }

    private static Path resolveTextureSource(Path packDirectory, PackConfig packConfig) {
        return packDirectory.resolve(firstNonBlank(packConfig.textureFile(), "texture.png"));
    }

    private static boolean isZipPackFile(Path path) {
        return Files.isRegularFile(path)
                && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip");
    }

    private static boolean isPackDirectory(Path packDirectory) throws IOException {
        if (!Files.isDirectory(packDirectory)) {
            return false;
        }

        if (findPackConfigFile(packDirectory) != null) {
            return true;
        }

        boolean hasGeo = false;
        boolean hasTexture = false;
        try (Stream<Path> stream = Files.list(packDirectory)) {
            List<Path> children = stream.filter(Files::isRegularFile).toList();
            for (Path child : children) {
                String fileName = child.getFileName().toString().toLowerCase(Locale.ROOT);
                if (fileName.endsWith(".geo.json")) {
                    hasGeo = true;
                }
                if (fileName.endsWith(".png")) {
                    hasTexture = true;
                }
                if (hasGeo && hasTexture) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private static Path resolveExtractedPackDirectory(Path extractRoot) throws IOException {
        Path current = extractRoot;
        while (true) {
            List<Path> children;
            try (Stream<Path> stream = Files.list(current)) {
                children = stream
                        .filter(path -> !isIgnoredArchivePath(path) && !isExampleConfigPath(path))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                        .toList();
            }
            if (children.size() == 1 && Files.isDirectory(children.get(0))) {
                current = children.get(0);
                continue;
            }
            return current;
        }
    }

    private static boolean isIgnoredArchivePath(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.equalsIgnoreCase(".DS_Store")
                || fileName.equalsIgnoreCase("__MACOSX");
    }

    private static boolean isExampleConfigPath(Path path) {
        Path fileName = path.getFileName();
        return fileName != null && fileName.toString().equalsIgnoreCase(EXAMPLE_DIRECTORY_NAME);
    }

    private static void extractZip(Path zipFile, Path extractRoot) throws IOException {
        Throwable lastException = null;
        for (Charset charset : getZipFallbackCharsets()) {
            try {
                extractZipWithCharset(zipFile, extractRoot, charset);
                if (!StandardCharsets.UTF_8.equals(charset)) {
                    Moreblock.LOGGER.info("压缩包 {} 使用 {} 编码解压成功", zipFile.getFileName(), charset.displayName());
                }
                return;
            } catch (Exception exception) {
                if (!isZipCharsetException(exception)) {
                    if (exception instanceof IOException ioException) {
                        throw ioException;
                    }
                    throw new IOException("解压配置方块压缩包失败: " + zipFile, exception);
                }
                lastException = exception;
                deleteTree(extractRoot);
                Files.createDirectories(extractRoot);
            }
        }

        throw new IOException("无法识别压缩包文件名编码: " + zipFile, lastException);
    }

    private static void extractZipWithCharset(Path zipFile, Path extractRoot, Charset charset) throws IOException {
        try (ZipFile archive = new ZipFile(zipFile.toFile(), charset)) {
            Enumeration<? extends ZipEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (isIgnoredArchiveEntry(entryName) || entryName.isBlank()) {
                    continue;
                }

                // 防止压缩包里的相对路径跳出目标目录
                Path targetPath = extractRoot.resolve(entryName).normalize();
                if (!targetPath.startsWith(extractRoot)) {
                    throw new IOException("压缩包包含非法路径: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                    continue;
                }

                Files.createDirectories(targetPath.getParent());
                try (var inputStream = archive.getInputStream(entry)) {
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static List<Charset> getZipFallbackCharsets() {
        List<Charset> charsets = new ArrayList<>();
        addZipCharset(charsets, StandardCharsets.UTF_8);
        addZipCharset(charsets, Charset.forName("GBK"));
        addZipCharset(charsets, Charset.defaultCharset());
        return charsets;
    }

    private static void addZipCharset(List<Charset> charsets, Charset charset) {
        if (!charsets.contains(charset)) {
            charsets.add(charset);
        }
    }

    private static boolean isZipCharsetException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof MalformedInputException || current instanceof UnmappableCharacterException) {
                return true;
            }
            if (current instanceof ZipException && current.getMessage() != null) {
                String message = current.getMessage().toLowerCase(Locale.ROOT);
                if (message.contains("bad entry name") || message.contains("invalid cen header")) {
                    return true;
                }
            }
            if (current instanceof IllegalArgumentException && current.getMessage() != null
                    && current.getMessage().toLowerCase(Locale.ROOT).contains("malformed")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isIgnoredArchiveEntry(String entryName) {
        String normalized = entryName.replace('\\', '/');
        if (normalized.startsWith("__MACOSX/")) {
            return true;
        }
        int slashIndex = normalized.lastIndexOf('/');
        String fileName = slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
        return fileName.equalsIgnoreCase(".DS_Store");
    }

    private static GeoInspection inspectGeo(Path geoSource) throws IOException {
        try (Reader reader = Files.newBufferedReader(geoSource, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
            if (geometries == null || geometries.size() == 0) {
                return GeoInspection.none();
            }

            JsonObject geometry = geometries.get(0).getAsJsonObject();
            JsonArray bones = geometry.getAsJsonArray("bones");
            if (bones == null || bones.size() == 0) {
                return GeoInspection.none();
            }

            String hitboxBoneName = null;
            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                if (!bone.has("name")) {
                    continue;
                }

                String boneName = bone.get("name").getAsString();
                if (Objects.equals(boneName, "hitbox")) {
                    hitboxBoneName = "hitbox";
                    break;
                }
            }
            return hitboxBoneName == null ? GeoInspection.none() : GeoInspection.hitbox(hitboxBoneName);
        }
    }

    private static GeoHitboxSystem.HorizontalShapes loadApiHorizontalShapes(Definition definition) {
        ResourceLocation geoLocation = definition.geoLocation();
        if (geoLocation == null) {
            return definition.horizontalShapes();
        }
        String geoPath = "assets/" + geoLocation.getNamespace() + "/" + geoLocation.getPath();
        GeoHitboxSystem.Profile profile = new GeoHitboxSystem.Profile(
                geoPath,
                firstNonBlank(definition.hitboxBoneName(), "hitbox"),
                true,
                true,
                false,
                0.0d,
                0.0d,
                0.0d
        );
        Moreblock.LOGGER.info("延迟加载 API 方块 hitbox: {} -> {}", definition.registryName(), geoPath);
        return GeoHitboxSystem.loadHorizontalShapes(profile);
    }

    private static void writeGeneratedAssets(Definition definition) throws IOException {
        Path assetsRoot = GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID);

        copyFile(definition.geoSourceFile(), assetsRoot.resolve("geo").resolve("block").resolve(definition.registryName() + ".geo.json"));
        copyFile(definition.textureSourceFile(), assetsRoot.resolve("textures").resolve("block").resolve(definition.registryName()).resolve("texture.png"));

        JsonObject itemModel = buildItemModelJson(definition.displaySourceFile());
        writeJson(assetsRoot.resolve("models").resolve("item").resolve(definition.registryName() + ".json"), itemModel);

        writeGeneratedBlockModelAssets(definition, assetsRoot);
    }

    private static void writeApiGeneratedAssets() throws IOException {
        Path assetsRoot = GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID);
        for (Definition definition : DEFINITIONS) {
            if (definition.geoSourceFile() != null) {
                continue;
            }
            copyApiTextureToGeneratedPack(definition, assetsRoot);
            writeGeneratedBlockModelAssets(definition, assetsRoot);
        }
    }

    private static void writeGeneratedBlockModelAssets(Definition definition, Path assetsRoot) throws IOException {
        JsonObject emptyBlockModel = new JsonObject();
        emptyBlockModel.addProperty("parent", "builtin/entity");
        JsonObject textures = new JsonObject();
        // 给方块模型补上 particle 贴图，避免破坏和敲击粒子回退成黑紫缺失材质
        textures.addProperty("particle", resolveParticleTexture(definition));
        emptyBlockModel.add("textures", textures);
        writeJson(assetsRoot.resolve("models").resolve("block").resolve(definition.registryName() + "_empty.json"), emptyBlockModel);

        JsonObject blockStates = new JsonObject();
        JsonObject variants = new JsonObject();
        variants.add("facing=north", createModelVariant(definition.registryName()));
        variants.add("facing=east", createModelVariant(definition.registryName()));
        variants.add("facing=south", createModelVariant(definition.registryName()));
        variants.add("facing=west", createModelVariant(definition.registryName()));
        blockStates.add("variants", variants);
        writeJson(assetsRoot.resolve("blockstates").resolve(definition.registryName() + ".json"), blockStates);
    }

    private static String resolveParticleTexture(Definition definition) {
        return Moreblock.MODID + ":block/" + definition.registryName() + "/texture";
    }

    private static void copyApiTextureToGeneratedPack(Definition definition, Path assetsRoot) throws IOException {
        ResourceLocation textureLocation = definition.textureLocation();
        if (textureLocation == null) {
            return;
        }

        String resourcePath = "assets/" + textureLocation.getNamespace() + "/" + textureLocation.getPath();
        try (InputStream inputStream = ImportedBlockPacks.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                Moreblock.LOGGER.warn("API 方块粒子贴图复制失败，找不到资源: {} ({})", textureLocation, definition.registryName());
                return;
            }
            Path target = assetsRoot.resolve("textures").resolve("block").resolve(definition.registryName()).resolve("texture.png");
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void writeLanguageFiles() throws IOException {
        Path langRoot = GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID).resolve("lang");
        JsonObject zhCn = new JsonObject();
        JsonObject enUs = new JsonObject();

        for (Definition definition : DEFINITIONS) {
            zhCn.addProperty(definition.blockTranslationKey(), definition.zhCnName());
            zhCn.addProperty(definition.itemTranslationKey(), definition.zhCnName());
            enUs.addProperty(definition.blockTranslationKey(), definition.enUsName());
            enUs.addProperty(definition.itemTranslationKey(), definition.enUsName());
        }

        writeJson(langRoot.resolve("zh_cn.json"), zhCn);
        writeJson(langRoot.resolve("en_us.json"), enUs);
    }

    private static JsonObject createModelVariant(String registryName) {
        JsonObject object = new JsonObject();
        object.addProperty("model", Moreblock.MODID + ":block/" + registryName + "_empty");
        return object;
    }

    private static JsonObject buildItemModelJson(Path displaySourceFile) throws IOException {
        JsonObject root;
        if (displaySourceFile != null && Files.isRegularFile(displaySourceFile)) {
            try (Reader reader = Files.newBufferedReader(displaySourceFile, StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } else {
            root = new JsonObject();
            root.addProperty("credit", "Generated by MoreBlock");
            root.addProperty("parent", "builtin/entity");
            root.add("display", buildDefaultDisplayJson());
        }

        root.addProperty("parent", "builtin/entity");
        root.addProperty("gui_light", "front");
        return root;
    }

    private static JsonObject buildDefaultDisplayJson() {
        JsonObject display = new JsonObject();

        // 没有单独 display 文件时，默认套用保险箱模板这组展示参数
        display.add("thirdperson_righthand", createDisplayTransform(null, null, vector3(0.5d, 0.5d, 0.5d)));
        display.add("thirdperson_lefthand", createDisplayTransform(null, null, vector3(0.5d, 0.5d, 0.5d)));
        display.add("firstperson_righthand", createDisplayTransform(vector3(0.0d, -180.0d, 0.0d), null, vector3(0.5d, 0.5d, 0.5d)));
        display.add("firstperson_lefthand", createDisplayTransform(vector3(0.0d, -180.0d, 0.0d), null, vector3(0.5d, 0.5d, 0.5d)));
        display.add("ground", createDisplayTransform(vector3(0.0d, -180.0d, 0.0d), null, vector3(0.5d, 0.5d, 0.5d)));
        display.add("gui", createDisplayTransform(vector3(15.0d, -150.0d, 0.0d), vector3(0.0d, -3.75d, 0.0d), vector3(0.5d, 0.5d, 0.5d)));
        display.add("fixed", createDisplayTransform(null, vector3(0.0d, -3.25d, 0.0d), vector3(0.5d, 0.5d, 0.5d)));
        display.add("on_shelf", createDisplayTransform(null, null, vector3(0.5d, 0.5d, 0.5d)));

        return display;
    }

    private static JsonObject createDisplayTransform(JsonArray rotation, JsonArray translation, JsonArray scale) {
        JsonObject transform = new JsonObject();
        if (rotation != null) {
            transform.add("rotation", rotation);
        }
        if (translation != null) {
            transform.add("translation", translation);
        }
        if (scale != null) {
            transform.add("scale", scale);
        }
        return transform;
    }

    private static JsonArray vector3(double x, double y, double z) {
        JsonArray values = new JsonArray();
        values.add(x);
        values.add(y);
        values.add(z);
        return values;
    }

    private static void writePackMeta() throws IOException {
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "MoreBlock imported block runtime resources");
        pack.addProperty("pack_format", 15);

        JsonObject root = new JsonObject();
        root.add("pack", pack);
        writeJson(GENERATED_PACK_ROOT.resolve("pack.mcmeta"), root);
    }

    private static void writeSharedAnimation() throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.8.0");
        JsonObject animations = new JsonObject();
        JsonObject idle = new JsonObject();
        idle.addProperty("loop", true);
        animations.add("idle", idle);
        root.add("animations", animations);

        writeJson(GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID).resolve("animations").resolve("block").resolve(DYNAMIC_ANIMATION_NAME), root);
    }

    private static void copyFile(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
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

    private static String allocateRegistryName(String folderName) {
        String base = sanitizeToRegistryPath(folderName);
        if (base.isBlank()) {
            base = "pack";
        }

        String candidate = trimRegistryPath("config_block_" + base);
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
                return;
            }
            if ((codePoint >= 'a' && codePoint <= 'z') || (codePoint >= '0' && codePoint <= '9')) {
                builder.appendCodePoint(codePoint);
                return;
            }
            if (codePoint == '-' || codePoint == '_' || codePoint == ' ') {
                builder.append('_');
                return;
            }

            builder.append('u').append(Integer.toHexString(codePoint));
        });
        return builder.toString().replaceAll("_+", "_");
    }

    private static JsonObject getObject(JsonObject root, String... keys) {
        for (String key : keys) {
            if (root == null || !root.has(key) || !root.get(key).isJsonObject()) {
                continue;
            }
            return root.getAsJsonObject(key);
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

    private static Integer getOptionalInt(JsonObject root, String... keys) {
        if (root == null) {
            return null;
        }
        for (String key : keys) {
            if (!root.has(key) || root.get(key).isJsonNull()) {
                continue;
            }
            try {
                return root.get(key).getAsInt();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Double getOptionalDouble(JsonObject root, String... keys) {
        if (root == null) {
            return null;
        }
        for (String key : keys) {
            if (!root.has(key) || root.get(key).isJsonNull()) {
                continue;
            }
            try {
                return root.get(key).getAsDouble();
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

    private static String firstNonBlank(String... values) {
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

    private static int resolveLightLevel(Integer configuredLightLevel) {
        if (configuredLightLevel != null) {
            return Math.max(0, Math.min(15, configuredLightLevel));
        }
        return 0;
    }

    private static boolean resolveSupportsSitting(Boolean configuredSupportsSitting, Boolean configuredSupportsLying) {
        return configuredSupportsSitting != null && configuredSupportsSitting && !(configuredSupportsLying != null && configuredSupportsLying);
    }

    private static double resolveSeatHeight(Double configuredSeatHeight) {
        if (configuredSeatHeight != null) {
            return Math.max(0.0d, Math.min(2.0d, configuredSeatHeight));
        }
        return 0.5d;
    }

    private static boolean resolveSupportsLying(Boolean configuredSupportsSitting, Boolean configuredSupportsLying) {
        return configuredSupportsLying != null && configuredSupportsLying && !(configuredSupportsSitting != null && configuredSupportsSitting);
    }

    private static double resolveLyingHeight(Double configuredLyingHeight) {
        if (configuredLyingHeight != null) {
            return Math.max(0.0d, Math.min(2.0d, configuredLyingHeight));
        }
        return 0.5d;
    }

    private static int resolveLyingRotationCompensation(Integer configuredLyingRotationCompensation) {
        if (configuredLyingRotationCompensation != null) {
            return Math.max(-3, Math.min(3, configuredLyingRotationCompensation));
        }
        return 0;
    }

    private static List<PackManifestEntry> buildPackManifest() {
        return DEFINITIONS.stream()
                .map(definition -> new PackManifestEntry(
                        definition.registryName(),
                        definition.displayName(),
                        firstNonBlank(definition.sourceFolderName(), definition.registryName()),
                        buildDefinitionFingerprint(definition)))
                .toList();
    }

    private static String buildDefinitionFingerprint(Definition definition) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            updateDigestWithString(digest, definition.registryName());
            updateDigestWithString(digest, definition.displayName());
            updateDigestWithFile(digest, definition.configSourceFile());
            updateDigestWithFile(digest, definition.geoSourceFile());
            updateDigestWithFile(digest, definition.displaySourceFile());
            updateDigestWithFile(digest, definition.textureSourceFile());
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException exception) {
            throw new IllegalStateException("生成配置方块清单指纹失败: " + definition.registryName(), exception);
        }
    }

    private static void updateDigestWithFile(MessageDigest digest, Path file) throws IOException {
        if (file == null) {
            updateDigestWithString(digest, "<null>");
            return;
        }

        digest.update(Files.readAllBytes(file));
        digest.update((byte) 0);
    }

    private static void updateDigestWithString(MessageDigest digest, String value) {
        String actualValue = value == null ? "<null>" : value;
        digest.update(actualValue.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    public record Definition(
            String ownerModId,
            String registryName,
            String zhCnName,
            String enUsName,
            String sourceFolderName,
            String sourceZipName,
            Path packDirectory,
            Path configSourceFile,
            Path geoSourceFile,
            Path displaySourceFile,
            Path textureSourceFile,
            GeoHitboxSystem.HorizontalShapes horizontalShapes,
            String hitboxBoneName,
            boolean showInMoreBlockTab,
            boolean translucent,
            int lightLevel,
            boolean supportsSitting,
            double seatHeight,
            boolean supportsLying,
            double lyingHeight,
            int lyingRotationCompensation,
            ResourceLocation geoLocation,
            ResourceLocation textureLocation,
            ResourceLocation displayLocation,
            ResourceLocation itemModelLocation
    ) {
        public String displayName() {
            return firstNonBlank(zhCnName, enUsName, sourceFolderName, registryName);
        }

        public String blockTranslationKey() {
            return "block." + ownerModId + "." + registryName;
        }

        public String itemTranslationKey() {
            return "item." + ownerModId + "." + registryName;
        }

        public ResourceLocation modelLocation() {
            return geoLocation != null ? geoLocation : ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "geo/block/" + registryName + ".geo.json");
        }

        public ResourceLocation textureLocation() {
            return textureLocation != null ? textureLocation : ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "textures/block/" + registryName + "/texture.png");
        }

        public ResourceLocation itemModelLocation() {
            return itemModelLocation != null ? itemModelLocation : ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "models/item/" + registryName + ".json");
        }

        public ResourceLocation animationLocation() {
            return ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "animations/block/" + DYNAMIC_ANIMATION_NAME);
        }
    }

    private record GeoInspection(String hitboxBoneName) {
        private static GeoInspection none() {
            return new GeoInspection(null);
        }

        private static GeoInspection hitbox(String hitboxBoneName) {
            return new GeoInspection(hitboxBoneName);
        }

        private boolean hasHitboxBone() {
            return isPresent(hitboxBoneName);
        }
    }

    private record ExampleConfigLanguage(
            String mainComment,
            String markdownTitle,
            String markdownUsage,
            String markdownExamplePrefix,
            String describeId,
            String describeName,
            String describeGeo,
            String describeTexture,
            String describeDisplay,
            String describeLightLevel,
            String describeSupportsSitting,
            String describeSeatHeight,
            String describeSupportsLying,
            String describeLyingHeight,
            String describeLyingRotationCompensation
    ) {
        private static ExampleConfigLanguage zhCn() {
            return new ExampleConfigLanguage(
                    "这份文件由 MoreBlock 自动生成，用来说明 example.json 里每个参数的用法。example 文件夹只作参考，不会被加载为自定义方块。",
                    "# MoreBlock 示例配置说明",
                    "旁边的 example.json 是标准 JSON，可以直接复制成新的方块配置再修改。",
                    "示例：",
                    "方块 ID，会作为注册名的一部分使用。建议只使用小写英文、数字和下划线。",
                    "方块显示名称。zh_cn 是中文名，en_us 是英文名。",
                    "模型文件名，需要对应同一方块文件夹内的 .geo.json 文件。",
                    "贴图文件名，通常使用 texture.png。",
                    "物品展示参数文件名，可选。不填写时会使用内置默认展示。",
                    "方块亮度，范围 0 到 15。0 表示不发光，15 表示最高亮度。",
                    "是否允许玩家右键坐下。默认 false，设为 true 后玩家可以右键坐在这个方块上。和 supports_lying 互斥，同时开启时两者都不会生效。",
                    "玩家坐下时的高度。默认 0.5，可以按模型高度调整。",
                    "是否允许玩家右键躺下。默认 false，设为 true 后玩家可以像床一样在夜晚睡觉；白天也能躺下，但不会跳过时间或触发睡觉效果。和 supports_sitting 互斥，同时开启时两者都不会生效。",
                    "玩家白天躺下时的显示高度。默认 0.5，可以按模型高度调整。",
                    "玩家躺下方向的旋转补偿，单位是 90 度。默认 0。可用 -3 到 3 调整方向，例如 1 表示顺时针转 90 度，-1 表示逆时针转 90 度。"
            );
        }

        private static ExampleConfigLanguage enUs() {
            return new ExampleConfigLanguage(
                    "This file is generated by MoreBlock to explain every parameter in example.json. The example folder is only a reference and is never loaded as a custom block.",
                    "# MoreBlock example config guide",
                    "The nearby example.json is valid JSON. Copy it into a new block folder and edit it when making a block.",
                    "Example:",
                    "Block id. It becomes part of the registry name. Lowercase letters, numbers, and underscores are recommended.",
                    "Block display names. zh_cn is the Chinese name, and en_us is the English name.",
                    "Model file name. It should point to the .geo.json file in the same block folder.",
                    "Texture file name. texture.png is the usual default.",
                    "Item display transform file name. Optional. The built-in default display is used when omitted.",
                    "Block light level from 0 to 15. 0 means no light, and 15 is the brightest.",
                    "Whether players can right-click this block to sit on it. The default is false. It is mutually exclusive with supports_lying. If both are enabled, neither takes effect.",
                    "Player sitting height. The default is 0.5, and you can adjust it to fit the model.",
                    "Whether players can right-click this block to lie down. The default is false. At night it behaves like a bed and starts normal sleeping; during daytime players can still lie down, but it does not skip time or trigger sleeping effects. It is mutually exclusive with supports_sitting. If both are enabled, neither takes effect.",
                    "Player visual lying height during daytime lying. The default is 0.5, and you can adjust it to fit the model.",
                    "Rotation compensation for the lying direction in 90-degree steps. The default is 0. Use values from -3 to 3, where 1 means 90 degrees clockwise and -1 means 90 degrees counterclockwise."
            );
        }
    }

    private record ExampleConfigParameter(String key, String exampleText, JsonElement value, String description) {
        private void apply(JsonObject root) {
            root.add(key, value.deepCopy());
        }

        private static ExampleConfigParameter of(String key, String exampleText, JsonElement value, String description) {
            return new ExampleConfigParameter(key, exampleText, value, description);
        }
    }

    private record PackConfig(
            Path configSourceFile,
            String id,
            String zhCnName,
            String enUsName,
            String geoFile,
            String textureFile,
            String displayFile,
            Integer lightLevel,
            Boolean supportsSitting,
            Double seatHeight,
            Boolean supportsLying,
            Double lyingHeight,
            Integer lyingRotationCompensation
    ) {
        private static PackConfig legacy() {
            return new PackConfig(null, null, null, null, null, "texture.png", null, null, null, null, null, null, null);
        }
    }

    public record PackManifestEntry(
            String registryName,
            String displayName,
            String sourceName,
            String fingerprint
    ) {
        public String describe() {
            return firstNonBlank(displayName, sourceName, registryName) + " [" + registryName + "]";
        }
    }
}
