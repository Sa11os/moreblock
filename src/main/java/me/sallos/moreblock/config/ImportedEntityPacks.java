package me.sallos.moreblock.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.entity.ImportedEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;

@SuppressWarnings("null")
public final class ImportedEntityPacks {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String DYNAMIC_ANIMATION_NAME = "config_entity_idle.animation.json";
    private static final Path CONFIG_NAMESPACE_ROOT = FMLPaths.CONFIGDIR.get().resolve(Moreblock.MODID);
    private static final Path CONFIG_ROOT = CONFIG_NAMESPACE_ROOT.resolve("entity");
    private static final Path CONFIG_PLACEHOLDER_FILE = CONFIG_ROOT.resolve(".keep");
    private static final Path CONFIG_GUIDE_FILE = CONFIG_ROOT.resolve("README.txt");
    private static final String EXAMPLE_DIRECTORY_NAME = "example";
    private static final Path EXAMPLE_CONFIG_DIR = CONFIG_ROOT.resolve(EXAMPLE_DIRECTORY_NAME);
    private static final Path EXAMPLE_CONFIG_FILE = EXAMPLE_CONFIG_DIR.resolve("example.json");
    private static final Path EXAMPLE_MARKDOWN_FILE = EXAMPLE_CONFIG_DIR.resolve("example.md");
    private static final Path GENERATED_PACK_ROOT = CONFIG_NAMESPACE_ROOT.resolve(".generated_entity_pack");
    private static final Path GENERATED_SOURCE_ROOT = GENERATED_PACK_ROOT.resolve("_sources");

    private static final List<Definition> DEFINITIONS = new ArrayList<>();
    private static final Map<String, Definition> DEFINITIONS_BY_KEY = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<EntityType<?>>> DYNAMIC_ENTITY_TYPES = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<Item>> DYNAMIC_EGG_ITEMS = new LinkedHashMap<>();
    private static final Map<EntityType<?>, Definition> DEFINITIONS_BY_ENTITY_TYPE = new IdentityHashMap<>();
    private static final Map<Item, Definition> DEFINITIONS_BY_ITEM = new IdentityHashMap<>();
    private static List<PackManifestEntry> PACK_MANIFEST = List.of();
    private static boolean bootstrapped = false;

    private ImportedEntityPacks() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }

        DEFINITIONS.clear();
        DEFINITIONS_BY_KEY.clear();
        DEFINITIONS_BY_ENTITY_TYPE.clear();
        DEFINITIONS_BY_ITEM.clear();
        PACK_MANIFEST = List.of();

        try {
            ensureConfigRoot();
            rebuildGeneratedPack();
            PACK_MANIFEST = buildPackManifest();
        } catch (Exception exception) {
            Moreblock.LOGGER.error("初始化配置实体目录失败: {}", CONFIG_ROOT, exception);
        }

        bootstrapped = true;
        Moreblock.LOGGER.info("配置实体扫描完成，共读取 {} 个配置包", DEFINITIONS.size());
    }

    public static synchronized Collection<Definition> getDefinitions() {
        bootstrap();
        return List.copyOf(DEFINITIONS);
    }

    public static synchronized boolean hasDefinitions() {
        bootstrap();
        return !DEFINITIONS.isEmpty();
    }

    public static synchronized Map<String, RegistryObject<EntityType<?>>> registerEntityTypes(DeferredRegister<EntityType<?>> registry) {
        bootstrap();
        for (Definition definition : DEFINITIONS) {
            if (DYNAMIC_ENTITY_TYPES.containsKey(definition.registryName())) {
                continue;
            }

            RegistryObject<EntityType<?>> registryObject = registry.register(definition.registryName(), () -> EntityType.Builder
                    .<ImportedEntity>of(ImportedEntity::new, MobCategory.CREATURE)
                    .sized(definition.width(), definition.height())
                    .clientTrackingRange(definition.trackingRange())
                    .updateInterval(definition.updateInterval())
                    .build(definition.registryName()));
            DYNAMIC_ENTITY_TYPES.put(definition.registryName(), registryObject);
        }
        return Collections.unmodifiableMap(DYNAMIC_ENTITY_TYPES);
    }

    public static synchronized Map<String, RegistryObject<Item>> registerEggItems(DeferredRegister<Item> registry) {
        bootstrap();
        for (Definition definition : DEFINITIONS) {
            if (DYNAMIC_EGG_ITEMS.containsKey(definition.spawnEggRegistryName())) {
                continue;
            }

            RegistryObject<EntityType<?>> entityType = DYNAMIC_ENTITY_TYPES.get(definition.registryName());
            if (entityType == null) {
                continue;
            }

            RegistryObject<Item> registryObject = registry.register(definition.spawnEggRegistryName(),
                    () -> new ForgeSpawnEggItem(() -> (EntityType<? extends net.minecraft.world.entity.Mob>) entityType.get(),
                            definition.eggPrimaryColor(),
                            definition.eggSecondaryColor(),
                            new Item.Properties()));
            DYNAMIC_EGG_ITEMS.put(definition.spawnEggRegistryName(), registryObject);
        }
        return Collections.unmodifiableMap(DYNAMIC_EGG_ITEMS);
    }

    @SuppressWarnings("unchecked")
    public static synchronized Map<String, RegistryObject<EntityType<ImportedEntity>>> getDynamicEntityTypeRegistryObjects() {
        bootstrap();
        Map<String, RegistryObject<EntityType<ImportedEntity>>> converted = new LinkedHashMap<>();
        for (Map.Entry<String, RegistryObject<EntityType<?>>> entry : DYNAMIC_ENTITY_TYPES.entrySet()) {
            converted.put(entry.getKey(), (RegistryObject<EntityType<ImportedEntity>>) (RegistryObject<?>) entry.getValue());
        }
        return Collections.unmodifiableMap(converted);
    }

    public static synchronized List<RegistryObject<Item>> getDynamicEggRegistryObjects() {
        bootstrap();
        return List.copyOf(DYNAMIC_EGG_ITEMS.values());
    }

    public static synchronized Definition getDefinition(String registryName) {
        bootstrap();
        return DEFINITIONS_BY_KEY.get(registryName);
    }

    public static synchronized Definition getDefinition(EntityType<?> entityType) {
        bootstrap();
        Definition cached = DEFINITIONS_BY_ENTITY_TYPE.get(entityType);
        if (cached != null) {
            return cached;
        }

        for (Map.Entry<String, RegistryObject<EntityType<?>>> entry : DYNAMIC_ENTITY_TYPES.entrySet()) {
            if (entry.getValue().isPresent() && entry.getValue().get() == entityType) {
                Definition definition = DEFINITIONS_BY_KEY.get(entry.getKey());
                if (definition != null) {
                    DEFINITIONS_BY_ENTITY_TYPE.put(entityType, definition);
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

        for (Map.Entry<String, RegistryObject<Item>> entry : DYNAMIC_EGG_ITEMS.entrySet()) {
            if (entry.getValue().isPresent() && entry.getValue().get() == item) {
                String definitionKey = stripSpawnEggSuffix(entry.getKey());
                Definition definition = DEFINITIONS_BY_KEY.get(definitionKey);
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
        return PACK_MANIFEST;
    }

    public static synchronized Path getGeneratedPackRoot() {
        bootstrap();
        return GENERATED_PACK_ROOT;
    }

    private static void ensureConfigRoot() throws IOException {
        Files.createDirectories(CONFIG_NAMESPACE_ROOT);
        Files.createDirectories(CONFIG_ROOT);

        if (Files.notExists(CONFIG_PLACEHOLDER_FILE)) {
            Files.writeString(CONFIG_PLACEHOLDER_FILE, "", StandardCharsets.UTF_8);
        }

        writeExampleConfig();

        if (Files.notExists(CONFIG_GUIDE_FILE)) {
            Files.writeString(CONFIG_GUIDE_FILE, """
                    MoreBlock 导入实体目录

                    用法：
                    1. 在当前目录下创建一个子文件夹，例如：
                       config/moreblock/entity/示例实体/
                    2. 子文件夹内放入一个配置文件，例如：
                       - blue_slime.json
                    3. 配置文件中填写标准 id、中英文名称、模型和贴图文件名
                    4. 子文件夹内至少放入：
                       - blue_slime.geo.json
                       - texture.png
                    5. 可选放入：
                       - blue_slime.animation.json
                    6. 也可以直接放 zip 压缩包，压缩包内支持：
                       - 直接放文件
                       - 再套一层同名文件夹
                       - 外层容器包里混合多个子文件夹和子 zip

                    说明：
                    - 模组启动时会自动创建 `config/moreblock/entity` 目录
                    - `id` 会作为实体注册路径和语言键后缀使用
                    - 客户端启动时会读取目录并挂载运行时资源
                    - 模组会为每个导入实体自动注册刷怪蛋
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
        JsonObject name = new JsonObject();
        name.addProperty("zh_cn", "示例实体");
        name.addProperty("en_us", "Example Entity");
        root.addProperty("id", "example_entity");
        root.add("name", name);
        root.addProperty("geo", "example_entity.geo.json");
        root.addProperty("texture", "texture.png");
        root.addProperty("animation", "example_entity.animation.json");
        root.addProperty("width", 0.8d);
        root.addProperty("height", 1.8d);
        root.addProperty("eye_height", 1.55d);
        root.addProperty("max_health", 30.0d);
        root.addProperty("movement_speed", 0.28d);
        root.addProperty("follow_range", 24.0d);
        root.addProperty("attack_damage", 4.0d);
        root.addProperty("armor", 2.0d);
        root.addProperty("knockback_resistance", 0.1d);
        root.addProperty("tracking_range", 8);
        root.addProperty("update_interval", 3);
        root.addProperty("ai_enabled", true);
        root.addProperty("ai_template", "minecraft:zombie");
        root.addProperty("spawn_egg_primary_color", "#4b7cf0");
        root.addProperty("spawn_egg_secondary_color", "#d8e4ff");
        root.addProperty("show_in_moreblock_tab", true);
        root.addProperty("translucent", true);
        return root;
    }

    private static String buildExampleMarkdownText() {
        return """
                # MoreBlock 实体示例配置说明

                这份文件由 MoreBlock 自动生成，用来说明 example.json 里每个参数的用法。example 文件夹只作参考，不会被加载为自定义实体。

                旁边的 example.json 是标准 JSON，可以直接复制成新的实体配置再修改。

                ## `id`

                实体 ID，会作为注册名的一部分使用。建议只使用小写英文、数字和下划线。

                示例：`example_entity`

                ## `name`

                实体显示名称。`zh_cn` 是中文名，`en_us` 是英文名。

                示例：`{ "zh_cn": "示例实体", "en_us": "Example Entity" }`

                ## `geo`

                GeckoLib 实体模型文件名，需要对应同一实体文件夹内的 `.geo.json` 文件。

                示例：`example_entity.geo.json`

                ## `texture`

                贴图文件名，通常使用 `texture.png`。

                示例：`texture.png`

                ## `animation`

                动画文件名，可选。不填写时会使用内置默认 idle 动画。

                示例：`example_entity.animation.json`

                ## `width`

                实体碰撞宽度，单位为方块。

                示例：`0.8`

                ## `height`

                实体碰撞高度，单位为方块。

                示例：`1.8`

                ## `eye_height`

                实体视线高度，可选。不填写时会按高度自动推导。

                示例：`1.55`

                ## `max_health`

                实体最大生命值，默认 `20`。

                示例：`30`

                ## `movement_speed`

                实体基础移动速度，默认 `0.2`。

                示例：`0.28`

                ## `follow_range`

                实体仇恨或观察目标时的基础跟随距离，默认 `16`。

                示例：`24`

                ## `attack_damage`

                实体近战或远程攻击基础伤害，默认 `2`。

                示例：`4`

                ## `armor`

                实体基础护甲值，默认 `0`。

                示例：`2`

                ## `knockback_resistance`

                实体击退抗性，默认 `0.2`。

                示例：`0.1`

                ## `tracking_range`

                客户端追踪距离，默认 `8`。

                示例：`8`

                ## `update_interval`

                实体同步更新间隔，默认 `3`。

                示例：`3`

                ## `ai_enabled`

                是否启用实体 AI。默认 `true`。关闭后实体只保留基础生存行为，不会主动寻路、攻击或游荡。

                示例：`true`

                ## `ai_template`

                原版生物 AI 模板，直接填写原版实体 id。当前基础版已适配一批常见的 Goal 型生物，如 `minecraft:cow`、`minecraft:pig`、`minecraft:sheep`、`minecraft:chicken`、`minecraft:mooshroom`、`minecraft:zombie`、`minecraft:husk`、`minecraft:spider`、`minecraft:cave_spider`、`minecraft:skeleton`、`minecraft:stray`。

                示例：`minecraft:zombie`

                ## `spawn_egg_primary_color`

                刷怪蛋主色，支持 `#RRGGBB`、`0xRRGGBB` 或十进制颜色值。

                示例：`#4b7cf0`

                ## `spawn_egg_secondary_color`

                刷怪蛋副色，支持 `#RRGGBB`、`0xRRGGBB` 或十进制颜色值。

                示例：`#d8e4ff`

                ## `show_in_moreblock_tab`

                是否把自动生成的刷怪蛋显示到 MoreBlock 创造模式页签中，默认 `true`。

                示例：`true`

                ## `translucent`

                是否按半透明材质渲染，默认 `true`。

                示例：`true`
                """;
    }

    private static void rebuildGeneratedPack() throws IOException {
        deleteTree(GENERATED_PACK_ROOT);
        Files.createDirectories(GENERATED_PACK_ROOT);
        Files.createDirectories(GENERATED_SOURCE_ROOT);

        writePackMeta();
        writeSharedAnimation();

        try (Stream<Path> stream = Files.list(CONFIG_ROOT)) {
            stream.sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .forEach(ImportedEntityPacks::loadPackEntry);
        }

        writeLanguageFiles();
        writeSpawnEggModels();
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
            Moreblock.LOGGER.error("读取配置实体来源失败: {}", packEntry, exception);
        }
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

    private static void loadSinglePack(Path packDirectory, String sourceZipName) {
        String folderName = packDirectory.getFileName().toString();
        try {
            PackConfig packConfig = readPackConfig(packDirectory, folderName);
            Path geoSource = resolveGeoSource(packDirectory, folderName, packConfig);
            Path animationSource = resolveAnimationSource(packDirectory, folderName, packConfig);
            Path textureSource = resolveTextureSource(packDirectory, packConfig);
            if (!Files.isRegularFile(geoSource) || !Files.isRegularFile(textureSource)) {
                Moreblock.LOGGER.warn("跳过配置实体目录 {}，缺少模型文件 {} 或贴图文件 {}",
                        packDirectory,
                        geoSource.getFileName(),
                        textureSource.getFileName());
                return;
            }

            String registryName = allocateRegistryName(firstNonBlank(packConfig.id(), folderName));
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
                    animationSource,
                    textureSource,
                    resolveWidth(packConfig.width()),
                    resolveHeight(packConfig.height()),
                    resolveEyeHeight(packConfig.eyeHeight(), packConfig.height()),
                    resolveAttributeValue(packConfig.maxHealth(), 1.0d, 2048.0d, 20.0d),
                    resolveAttributeValue(packConfig.movementSpeed(), 0.0d, 4.0d, 0.2d),
                    resolveAttributeValue(packConfig.followRange(), 1.0d, 256.0d, 16.0d),
                    resolveAttributeValue(packConfig.attackDamage(), 0.0d, 2048.0d, 2.0d),
                    resolveAttributeValue(packConfig.armor(), 0.0d, 30.0d, 0.0d),
                    resolveAttributeValue(packConfig.knockbackResistance(), 0.0d, 1.0d, 0.2d),
                    resolveTrackingRange(packConfig.trackingRange()),
                    resolveUpdateInterval(packConfig.updateInterval()),
                    resolveAiEnabled(packConfig.aiEnabled()),
                    resolveAiTemplate(packConfig.aiTemplate()),
                    resolveColor(packConfig.spawnEggPrimaryColor(), 0x4b7cf0),
                    resolveColor(packConfig.spawnEggSecondaryColor(), 0xd8e4ff),
                    resolveShowInMoreBlockTab(packConfig.showInMoreBlockTab()),
                    resolveTranslucent(packConfig.translucent()),
                    ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "geo/entity/" + registryName + ".geo.json"),
                    animationSource == null
                            ? ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "animations/entity/" + DYNAMIC_ANIMATION_NAME)
                            : ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "animations/entity/" + registryName + ".animation.json"),
                    ResourceLocation.fromNamespaceAndPath(Moreblock.MODID, "textures/entity/" + registryName + "/texture.png")
            );
            DEFINITIONS.add(definition);
            DEFINITIONS_BY_KEY.put(registryName, definition);
            writeGeneratedAssets(definition);
        } catch (Exception exception) {
            Moreblock.LOGGER.error("导入配置实体失败: {}", packDirectory, exception);
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
                    getOptionalString(root, "id", "entity_id", "registry_name", "key"),
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
                    getOptionalString(root, "geo", "geo_file", "model", "model_file"),
                    getOptionalString(root, "animation", "animation_file"),
                    firstNonBlank(getOptionalString(root, "texture", "texture_file"), "texture.png"),
                    getOptionalDouble(root, "width", "hitbox_width", "collision_width"),
                    getOptionalDouble(root, "height", "hitbox_height", "collision_height"),
                    getOptionalDouble(root, "eye_height", "eyeHeight"),
                    getOptionalDouble(root, "max_health", "maxHealth", "health"),
                    getOptionalDouble(root, "movement_speed", "movementSpeed", "speed"),
                    getOptionalDouble(root, "follow_range", "followRange"),
                    getOptionalDouble(root, "attack_damage", "attackDamage"),
                    getOptionalDouble(root, "armor"),
                    getOptionalDouble(root, "knockback_resistance", "knockbackResistance"),
                    getOptionalInt(root, "tracking_range", "trackingRange"),
                    getOptionalInt(root, "update_interval", "updateInterval"),
                    getOptionalBoolean(root, "ai_enabled", "aiEnabled"),
                    getOptionalString(root, "ai_template", "aiTemplate", "vanilla_ai", "vanillaAi", "brain_template", "brainTemplate"),
                    getOptionalString(root, "spawn_egg_primary_color", "egg_primary_color", "spawnEggPrimaryColor"),
                    getOptionalString(root, "spawn_egg_secondary_color", "egg_secondary_color", "spawnEggSecondaryColor"),
                    getOptionalBoolean(root, "show_in_moreblock_tab", "showInMoreBlockTab"),
                    getOptionalBoolean(root, "translucent")
            );
        }
    }

    private static Path findPackConfigFile(Path packDirectory) throws IOException {
        try (Stream<Path> stream = Files.list(packDirectory)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .filter(path -> !looksLikeGeoModel(path))
                    .filter(path -> !looksLikeAnimationFile(path))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .findFirst()
                    .orElse(null);
        }
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

    private static boolean looksLikeAnimationFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".animation.json")) {
            return true;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            return element.isJsonObject() && element.getAsJsonObject().has("animations");
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

    private static Path resolveAnimationSource(Path packDirectory, String folderName, PackConfig packConfig) {
        if (isPresent(packConfig.animationFile())) {
            Path configuredAnimation = packDirectory.resolve(packConfig.animationFile());
            return Files.isRegularFile(configuredAnimation) ? configuredAnimation : null;
        }

        Path legacyAnimation = packDirectory.resolve(folderName + ".animation.json");
        return Files.isRegularFile(legacyAnimation) ? legacyAnimation : null;
    }

    private static Path resolveTextureSource(Path packDirectory, PackConfig packConfig) {
        return packDirectory.resolve(firstNonBlank(packConfig.textureFile(), "texture.png"));
    }

    private static boolean isZipPackFile(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip");
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
        return fileName.equalsIgnoreCase(".DS_Store") || fileName.equalsIgnoreCase("__MACOSX");
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
                    throw new IOException("解压配置实体压缩包失败: " + zipFile, exception);
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

    private static void writeGeneratedAssets(Definition definition) throws IOException {
        Path assetsRoot = GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID);
        copyFile(definition.geoSourceFile(), assetsRoot.resolve("geo").resolve("entity").resolve(definition.registryName() + ".geo.json"));
        copyFile(definition.textureSourceFile(), assetsRoot.resolve("textures").resolve("entity").resolve(definition.registryName()).resolve("texture.png"));
        if (definition.animationSourceFile() != null) {
            copyFile(definition.animationSourceFile(), assetsRoot.resolve("animations").resolve("entity").resolve(definition.registryName() + ".animation.json"));
        }
    }

    private static void writeLanguageFiles() throws IOException {
        Path langRoot = GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID).resolve("lang");
        JsonObject zhCn = new JsonObject();
        JsonObject enUs = new JsonObject();

        for (Definition definition : DEFINITIONS) {
            zhCn.addProperty(definition.translationKey(), definition.zhCnName());
            enUs.addProperty(definition.translationKey(), definition.enUsName());
        }

        writeJson(langRoot.resolve("zh_cn.json"), zhCn);
        writeJson(langRoot.resolve("en_us.json"), enUs);
    }

    private static void writeSpawnEggModels() throws IOException {
        Path modelRoot = GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID).resolve("models").resolve("item");
        for (Definition definition : DEFINITIONS) {
            JsonObject root = new JsonObject();
            root.addProperty("parent", "minecraft:item/template_spawn_egg");
            writeJson(modelRoot.resolve(definition.spawnEggRegistryName() + ".json"), root);
        }
    }

    private static void writePackMeta() throws IOException {
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "MoreBlock imported entity runtime resources");
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
        animations.add("animation.imported_entity.idle", idle);
        root.add("animations", animations);
        writeJson(GENERATED_PACK_ROOT.resolve("assets").resolve(Moreblock.MODID).resolve("animations").resolve("entity").resolve(DYNAMIC_ANIMATION_NAME), root);
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

    private static String allocateRegistryName(String sourceName) {
        String base = sanitizeToRegistryPath(sourceName);
        if (base.isBlank()) {
            base = "entity";
        }

        String candidate = trimRegistryPath("config_entity_" + base);
        String unique = candidate;
        int index = 2;
        while (DEFINITIONS_BY_KEY.containsKey(unique)) {
            unique = trimRegistryPath(candidate + "_" + index);
            index++;
        }
        return unique;
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

    private static String trimRegistryPath(String input) {
        String normalized = input.toLowerCase(Locale.ROOT);
        return normalized.length() <= 48 ? normalized : normalized.substring(0, 48);
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private static String stripSpawnEggSuffix(String registryName) {
        return registryName.endsWith("_spawn_egg")
                ? registryName.substring(0, registryName.length() - "_spawn_egg".length())
                : registryName;
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

    private static float resolveWidth(Double configuredWidth) {
        double width = configuredWidth == null ? 0.6d : configuredWidth;
        return (float) Math.max(0.1d, Math.min(4.0d, width));
    }

    private static float resolveHeight(Double configuredHeight) {
        double height = configuredHeight == null ? 1.8d : configuredHeight;
        return (float) Math.max(0.1d, Math.min(8.0d, height));
    }

    private static float resolveEyeHeight(Double configuredEyeHeight, Double configuredHeight) {
        if (configuredEyeHeight != null) {
            return (float) Math.max(0.0d, Math.min(8.0d, configuredEyeHeight));
        }
        return (float) (resolveHeight(configuredHeight) * 0.85d);
    }

    private static int resolveTrackingRange(Integer configuredTrackingRange) {
        if (configuredTrackingRange != null) {
            return Math.max(4, Math.min(64, configuredTrackingRange));
        }
        return 8;
    }

    private static int resolveUpdateInterval(Integer configuredUpdateInterval) {
        if (configuredUpdateInterval != null) {
            return Math.max(1, Math.min(20, configuredUpdateInterval));
        }
        return 3;
    }

    private static double resolveAttributeValue(Double configuredValue, double min, double max, double fallback) {
        if (configuredValue == null) {
            return fallback;
        }
        return Math.max(min, Math.min(max, configuredValue));
    }

    private static boolean resolveAiEnabled(Boolean configuredValue) {
        return configuredValue == null || configuredValue;
    }

    private static ResourceLocation resolveAiTemplate(String configuredValue) {
        if (!isPresent(configuredValue)) {
            return null;
        }

        ResourceLocation resourceLocation = ResourceLocation.tryParse(configuredValue.trim());
        if (resourceLocation != null) {
            return resourceLocation;
        }

        Moreblock.LOGGER.warn("实体 AI 模板 id 解析失败，已忽略该配置: {}", configuredValue);
        return null;
    }

    private static int resolveColor(String configuredColor, int fallbackColor) {
        if (!isPresent(configuredColor)) {
            return fallbackColor;
        }

        String value = configuredColor.trim().toLowerCase(Locale.ROOT);
        try {
            if (value.startsWith("#")) {
                return Integer.parseInt(value.substring(1), 16);
            }
            if (value.startsWith("0x")) {
                return Integer.parseInt(value.substring(2), 16);
            }
            return Integer.decode(value);
        } catch (NumberFormatException exception) {
            Moreblock.LOGGER.warn("刷怪蛋颜色解析失败，已回退默认值: {}", configuredColor);
            return fallbackColor;
        }
    }

    private static boolean resolveShowInMoreBlockTab(Boolean configuredValue) {
        return configuredValue == null || configuredValue;
    }

    private static boolean resolveTranslucent(Boolean configuredValue) {
        return configuredValue == null || configuredValue;
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
            updateDigestWithFile(digest, definition.animationSourceFile());
            updateDigestWithFile(digest, definition.textureSourceFile());
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException exception) {
            throw new IllegalStateException("生成配置实体清单指纹失败: " + definition.registryName(), exception);
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
            Path animationSourceFile,
            Path textureSourceFile,
            float width,
            float height,
            float eyeHeight,
            double maxHealth,
            double movementSpeed,
            double followRange,
            double attackDamage,
            double armor,
            double knockbackResistance,
            int trackingRange,
            int updateInterval,
            boolean aiEnabled,
            ResourceLocation aiTemplate,
            int eggPrimaryColor,
            int eggSecondaryColor,
            boolean showInMoreBlockTab,
            boolean translucent,
            ResourceLocation geoLocation,
            ResourceLocation animationLocation,
            ResourceLocation textureLocation
    ) {
        public String displayName() {
            return firstNonBlank(zhCnName, enUsName, sourceFolderName, registryName);
        }

        public String translationKey() {
            return "entity." + ownerModId + "." + registryName;
        }

        public String spawnEggRegistryName() {
            return registryName + "_spawn_egg";
        }
    }

    private record PackConfig(
            Path configSourceFile,
            String id,
            String zhCnName,
            String enUsName,
            String geoFile,
            String animationFile,
            String textureFile,
            Double width,
            Double height,
            Double eyeHeight,
            Double maxHealth,
            Double movementSpeed,
            Double followRange,
            Double attackDamage,
            Double armor,
            Double knockbackResistance,
            Integer trackingRange,
            Integer updateInterval,
            Boolean aiEnabled,
            String aiTemplate,
            String spawnEggPrimaryColor,
            String spawnEggSecondaryColor,
            Boolean showInMoreBlockTab,
            Boolean translucent
    ) {
        private static PackConfig legacy() {
            return new PackConfig(null, null, null, null, null, null, "texture.png", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
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
