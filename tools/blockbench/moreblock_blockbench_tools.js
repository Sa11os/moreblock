let moreblock_export_action;
let moreblock_export_entity_action;
let moreblock_generate_hitbox_action;

(function() {
    const PLUGIN_ID = 'moreblock_blockbench_tools';
    const TEXT = {
        zh: {
            dialogTitle: '导出 MoreBlock 配置 JSON',
            entityDialogTitle: '导出 MoreBlock 实体配置 JSON',
            zipExportType: 'MoreBlock ZIP 导入包',
            blockId: '方块 ID',
            entityId: '实体 ID',
            chineseName: '中文名称',
            englishName: '英文名称',
            geoFile: '模型文件',
            textureFile: '贴图文件',
            animationFile: '动画文件',
            displayFile: '显示参数文件',
            lightLevel: '亮度',
            canSit: '可坐下',
            seatHeight: '坐下高度',
            canLie: '可躺下',
            lyingHeight: '躺下高度',
            entityWidth: '实体宽度',
            entityHeight: '实体高度',
            eyeHeight: '视线高度',
            maxHealth: '最大生命值',
            movementSpeed: '移动速度',
            followRange: '跟随距离',
            attackDamage: '攻击伤害',
            armor: '护甲',
            knockbackResistance: '击退抗性',
            trackingRange: '追踪距离',
            updateInterval: '更新间隔',
            aiEnabled: '启用 AI',
            aiTemplate: '原版 AI 模板',
            animationTransition: '动作切换过渡',
            disableVanillaDeathAnimation: '禁用原版死亡翻转',
            translucent: '半透明渲染',
            showInMoreBlockTab: '显示在 MoreBlock 页签',
            spawnEggPrimaryColor: '刷怪蛋主色',
            spawnEggSecondaryColor: '刷怪蛋副色',
            includeAnimationStates: '附带动画状态模板',
            includeAnimationStatesDescription: '勾选后会写入 `animation_states` 示例，方便直接按 idle、walk、attack、die 命名继续改。',
            exportZipPackage: '导出 ZIP 导入包',
            exportZipPackageDescription: '勾选后会把配置文件和当前项目目录中能找到的 geo、贴图、display、animation 一起打包成 MoreBlock 可直接导入的 ZIP。',
            exportType: 'MoreBlock 配置 JSON',
            entityExportType: 'MoreBlock 实体配置 JSON',
            exported: 'MoreBlock 配置已导出',
            entityExported: 'MoreBlock 实体配置已导出',
            zipExported: 'MoreBlock ZIP 导入包已导出',
            zipExportedWithMissing: missing => `MoreBlock ZIP 导入包已导出，但以下文件未找到：${missing}`,
            zipExportDesktopOnly: 'ZIP 打包导出仅支持 Blockbench 桌面版',
            noTextureAvailable: '当前项目没有可导出的贴图',
            noGeoExporterAvailable: '当前项目不是 GeckoLib 模型，无法自动导出 geo.json',
            pluginTitle: 'MoreBlock Blockbench 工具',
            pluginDescription: '从 Blockbench 导出 MoreBlock 方块/实体配置 JSON，并自动生成 MoreBlock 可识别的 hitbox。',
            actionName: '导出 MoreBlock 配置 JSON',
            actionDescription: '创建 MoreBlock 方块配置 JSON 文件',
            entityActionName: '导出 MoreBlock 实体配置 JSON',
            entityActionDescription: '创建 MoreBlock 实体配置 JSON 文件',
            hitboxActionName: '生成 MoreBlock Hitbox',
            simpleHitboxActionName: '生成简单 Hitbox',
            complexHitboxActionName: '生成复杂 Hitbox',
            qualityHitboxActionName: '生成高质量 Hitbox',
            greedyHitboxActionName: '生成 Greedy Hitbox',
            hitboxActionDescription: '根据当前模型自动创建 MoreBlock 可识别的 hitbox 骨骼',
            hitboxDialogTitle: '生成 MoreBlock Hitbox',
            hitboxMode: '生成模式',
            simpleMode: '简单模式：整体大盒子',
            complexMode: '复杂模式：快速体素合并',
            qualityMode: '高质量模式：全局最大盒覆盖',
            greedyMode: 'Greedy 模式：切片矩形合并',
            simplification: '简化程度',
            simplificationInfo: '数值越小越精细，生成盒子可能更多；数值越大越简单。建议 1。高质量和 Greedy 模式会更慢。',
            maxBoxes: '最大盒子数量',
            maxBoxesInfo: '超过数量后会保留体积最大的盒子。高质量模式会按最大体积逐个提取，Greedy 模式会按三轴切片候选选择。',
            hitboxGenerated: 'MoreBlock hitbox 已生成',
            hitboxGeneratedWithCount: count => `MoreBlock hitbox 已生成：${count} 个盒子`,
            noModelCubes: '当前模型没有可用于计算 hitbox 的方块',
            defaultChineseName: '自定义方块',
            defaultEnglishName: 'Custom Block',
            defaultEntityChineseName: '自定义实体',
            defaultEntityEnglishName: 'Custom Entity'
        },
        en: {
            dialogTitle: 'Export MoreBlock Config JSON',
            entityDialogTitle: 'Export MoreBlock Entity Config JSON',
            zipExportType: 'MoreBlock ZIP Package',
            blockId: 'Block ID',
            entityId: 'Entity ID',
            chineseName: 'Chinese Name',
            englishName: 'English Name',
            geoFile: 'Geo File',
            textureFile: 'Texture File',
            animationFile: 'Animation File',
            displayFile: 'Display File',
            lightLevel: 'Light Level',
            canSit: 'Can Sit',
            seatHeight: 'Seat Height',
            canLie: 'Can Lie',
            lyingHeight: 'Lying Height',
            entityWidth: 'Entity Width',
            entityHeight: 'Entity Height',
            eyeHeight: 'Eye Height',
            maxHealth: 'Max Health',
            movementSpeed: 'Movement Speed',
            followRange: 'Follow Range',
            attackDamage: 'Attack Damage',
            armor: 'Armor',
            knockbackResistance: 'Knockback Resistance',
            trackingRange: 'Tracking Range',
            updateInterval: 'Update Interval',
            aiEnabled: 'Enable AI',
            aiTemplate: 'Vanilla AI Template',
            animationTransition: 'Animation Transition',
            disableVanillaDeathAnimation: 'Disable Vanilla Death Flip',
            translucent: 'Translucent Render',
            showInMoreBlockTab: 'Show In MoreBlock Tab',
            spawnEggPrimaryColor: 'Spawn Egg Primary Color',
            spawnEggSecondaryColor: 'Spawn Egg Secondary Color',
            includeAnimationStates: 'Include Animation State Template',
            includeAnimationStatesDescription: 'When enabled, the exported JSON will include an `animation_states` template for idle, walk, attack and die style naming.',
            exportZipPackage: 'Export ZIP Package',
            exportZipPackageDescription: 'Bundle the config file together with any geo, texture, display and animation files found in the current project folder into an import-ready MoreBlock ZIP.',
            exportType: 'MoreBlock Config JSON',
            entityExportType: 'MoreBlock Entity Config JSON',
            exported: 'MoreBlock config exported',
            entityExported: 'MoreBlock entity config exported',
            zipExported: 'MoreBlock ZIP package exported',
            zipExportedWithMissing: missing => `MoreBlock ZIP package exported, but these files were not found: ${missing}`,
            zipExportDesktopOnly: 'ZIP packaging is only available in Blockbench desktop',
            noTextureAvailable: 'No exportable texture is available in the current project',
            noGeoExporterAvailable: 'The current project is not a GeckoLib model, so geo.json cannot be exported automatically',
            pluginTitle: 'MoreBlock Blockbench Tools',
            pluginDescription: 'Export MoreBlock block/entity config JSON from Blockbench and generate a MoreBlock hitbox bone.',
            actionName: 'Export MoreBlock Config JSON',
            actionDescription: 'Create a MoreBlock block config JSON file',
            entityActionName: 'Export MoreBlock Entity Config JSON',
            entityActionDescription: 'Create a MoreBlock entity config JSON file',
            hitboxActionName: 'Generate MoreBlock Hitbox',
            simpleHitboxActionName: 'Generate Simple Hitbox',
            complexHitboxActionName: 'Generate Complex Hitbox',
            qualityHitboxActionName: 'Generate High Quality Hitbox',
            greedyHitboxActionName: 'Generate GitHub Greedy Hitbox',
            hitboxActionDescription: 'Create a MoreBlock-compatible hitbox bone from the current model',
            hitboxDialogTitle: 'Generate MoreBlock Hitbox',
            hitboxMode: 'Generation Mode',
            simpleMode: 'Simple: One Bounding Box',
            complexMode: 'Complex: Fast Voxel Merge',
            qualityMode: 'High Quality: Global Largest-Box Cover',
            greedyMode: 'GitHub Greedy: Sliced Rectangle Merge',
            simplification: 'Simplification',
            simplificationInfo: 'Lower values are more accurate and may create more boxes. Higher values are simpler. 1 is recommended. High quality and GitHub Greedy modes are slower.',
            maxBoxes: 'Max Boxes',
            maxBoxesInfo: 'If the result exceeds this number, only the largest boxes are kept. High quality extracts the largest box each round, and GitHub Greedy chooses candidates from three-axis slices.',
            hitboxGenerated: 'MoreBlock hitbox generated',
            hitboxGeneratedWithCount: count => `MoreBlock hitbox generated: ${count} boxes`,
            noModelCubes: 'No model cubes are available for hitbox calculation',
            defaultChineseName: '自定义方块',
            defaultEnglishName: 'Custom Block',
            defaultEntityChineseName: 'Custom Entity',
            defaultEntityEnglishName: 'Custom Entity'
        }
    };

    function getLanguageCode() {
        const candidates = [];
        if (typeof Settings !== 'undefined' && Settings && Settings.get) {
            candidates.push(Settings.get('language'));
        }
        if (typeof StateMemory !== 'undefined' && StateMemory) {
            candidates.push(StateMemory.language);
        }
        if (typeof Blockbench !== 'undefined' && Blockbench) {
            candidates.push(Blockbench.language);
        }
        if (typeof navigator !== 'undefined' && navigator) {
            candidates.push(navigator.language);
        }
        const value = candidates.find(candidate => typeof candidate === 'string' && candidate.trim());
        return String(value || 'en').toLowerCase();
    }

    function getText() {
        return getLanguageCode().startsWith('zh') ? TEXT.zh : TEXT.en;
    }

    function sanitizeId(value, fallback = 'custom_block') {
        return String(value || '')
            .trim()
            .toLowerCase()
            .replace(/[^a-z0-9_\-]+/g, '_')
            .replace(/_+/g, '_')
            .replace(/^_+|_+$/g, '') || fallback;
    }

    function getProjectBaseName() {
        if (typeof Project !== 'undefined' && Project && Project.name) {
            return String(Project.name).replace(/\.geo\.json$/i, '').replace(/\.json$/i, '');
        }
        if (typeof Format !== 'undefined' && Format && Format.id) {
            return Format.id;
        }
        return 'custom_block';
    }

    function getDefaultGeoFile() {
        const baseName = getProjectBaseName();
        return baseName.toLowerCase().endsWith('.geo') ? `${baseName}.json` : `${baseName}.geo.json`;
    }

    function getDefaultAnimationFile() {
        return `${getProjectBaseName()}.animation.json`;
    }

    function getNativeModule(moduleName) {
        if (typeof requireNativeModule === 'function') {
            try {
                return requireNativeModule(moduleName);
            } catch (error) {
                // Fall back to CommonJS require if the desktop helper is unavailable.
            }
        }
        if (typeof require === 'function') {
            try {
                return require(moduleName);
            } catch (error) {
                return null;
            }
        }
        return null;
    }

    function getProjectDirectory(nativePath) {
        if (!nativePath || typeof Project === 'undefined' || !Project) {
            return '';
        }
        const sourcePath = typeof Project.export_path === 'string' && Project.export_path.trim()
            ? Project.export_path
            : (typeof Project.save_path === 'string' ? Project.save_path : '');
        if (!sourcePath || !sourcePath.trim()) {
            return '';
        }
        try {
            return nativePath.dirname(sourcePath);
        } catch (error) {
            return '';
        }
    }

    function getProjectFilePath() {
        if (typeof Project === 'undefined' || !Project) {
            return '';
        }
        if (typeof Project.export_path === 'string' && Project.export_path.trim()) {
            return Project.export_path;
        }
        if (typeof Project.save_path === 'string' && Project.save_path.trim()) {
            return Project.save_path;
        }
        return '';
    }

    function encodeUtf8(value) {
        const input = String(value || '');
        if (typeof TextEncoder !== 'undefined') {
            return new TextEncoder().encode(input);
        }
        const encoded = unescape(encodeURIComponent(input));
        const bytes = new Uint8Array(encoded.length);
        for (let index = 0; index < encoded.length; index++) {
            bytes[index] = encoded.charCodeAt(index);
        }
        return bytes;
    }

    function toUint8Array(value) {
        if (value instanceof Uint8Array) {
            return value;
        }
        if (value instanceof ArrayBuffer) {
            return new Uint8Array(value);
        }
        if (ArrayBuffer.isView(value)) {
            return new Uint8Array(value.buffer, value.byteOffset, value.byteLength);
        }
        return encodeUtf8(String(value || ''));
    }

    function toArrayBuffer(bytes) {
        return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength);
    }

    function writeUInt16LE(target, offset, value) {
        target[offset] = value & 0xff;
        target[offset + 1] = (value >>> 8) & 0xff;
    }

    function writeUInt32LE(target, offset, value) {
        target[offset] = value & 0xff;
        target[offset + 1] = (value >>> 8) & 0xff;
        target[offset + 2] = (value >>> 16) & 0xff;
        target[offset + 3] = (value >>> 24) & 0xff;
    }

    let crc32Table;
    function getCrc32Table() {
        if (crc32Table) {
            return crc32Table;
        }
        crc32Table = new Uint32Array(256);
        for (let index = 0; index < 256; index++) {
            let current = index;
            for (let bit = 0; bit < 8; bit++) {
                current = (current & 1) ? (0xedb88320 ^ (current >>> 1)) : (current >>> 1);
            }
            crc32Table[index] = current >>> 0;
        }
        return crc32Table;
    }

    function calculateCrc32(bytes) {
        const table = getCrc32Table();
        let crc = 0xffffffff;
        for (let index = 0; index < bytes.length; index++) {
            crc = table[(crc ^ bytes[index]) & 0xff] ^ (crc >>> 8);
        }
        return (crc ^ 0xffffffff) >>> 0;
    }

    function getDosDateTime(date = new Date()) {
        const year = Math.max(1980, date.getFullYear());
        return {
            time: ((date.getHours() & 0x1f) << 11) | ((date.getMinutes() & 0x3f) << 5) | ((Math.floor(date.getSeconds() / 2)) & 0x1f),
            date: (((year - 1980) & 0x7f) << 9) | (((date.getMonth() + 1) & 0x0f) << 5) | (date.getDate() & 0x1f)
        };
    }

    function concatUint8Arrays(parts) {
        const totalLength = parts.reduce((sum, part) => sum + part.length, 0);
        const merged = new Uint8Array(totalLength);
        let offset = 0;
        parts.forEach(part => {
            merged.set(part, offset);
            offset += part.length;
        });
        return merged;
    }

    function createZipArchive(entries, zlibModule) {
        const locals = [];
        const central = [];
        let offset = 0;
        const dosDateTime = getDosDateTime();

        entries.forEach(entry => {
            const entryName = String(entry.name || '').replace(/\\/g, '/');
            const nameBytes = encodeUtf8(entryName);
            const rawContent = entry.directory ? new Uint8Array(0) : toUint8Array(entry.content);
            const compressedContent = (!entry.directory && rawContent.length)
                ? toUint8Array(zlibModule.deflateRawSync(rawContent))
                : rawContent;
            const compressionMethod = (!entry.directory && rawContent.length) ? 8 : 0;
            const crc32 = entry.directory ? 0 : calculateCrc32(rawContent);
            const localHeader = new Uint8Array(30 + nameBytes.length);
            writeUInt32LE(localHeader, 0, 0x04034b50);
            writeUInt16LE(localHeader, 4, 20);
            writeUInt16LE(localHeader, 6, 0x0800);
            writeUInt16LE(localHeader, 8, compressionMethod);
            writeUInt16LE(localHeader, 10, dosDateTime.time);
            writeUInt16LE(localHeader, 12, dosDateTime.date);
            writeUInt32LE(localHeader, 14, crc32);
            writeUInt32LE(localHeader, 18, compressedContent.length);
            writeUInt32LE(localHeader, 22, rawContent.length);
            writeUInt16LE(localHeader, 26, nameBytes.length);
            writeUInt16LE(localHeader, 28, 0);
            localHeader.set(nameBytes, 30);
            locals.push(localHeader, compressedContent);

            const centralHeader = new Uint8Array(46 + nameBytes.length);
            writeUInt32LE(centralHeader, 0, 0x02014b50);
            writeUInt16LE(centralHeader, 4, 20);
            writeUInt16LE(centralHeader, 6, 20);
            writeUInt16LE(centralHeader, 8, 0x0800);
            writeUInt16LE(centralHeader, 10, compressionMethod);
            writeUInt16LE(centralHeader, 12, dosDateTime.time);
            writeUInt16LE(centralHeader, 14, dosDateTime.date);
            writeUInt32LE(centralHeader, 16, crc32);
            writeUInt32LE(centralHeader, 20, compressedContent.length);
            writeUInt32LE(centralHeader, 24, rawContent.length);
            writeUInt16LE(centralHeader, 28, nameBytes.length);
            writeUInt16LE(centralHeader, 30, 0);
            writeUInt16LE(centralHeader, 32, 0);
            writeUInt16LE(centralHeader, 34, 0);
            writeUInt16LE(centralHeader, 36, 0);
            writeUInt32LE(centralHeader, 38, entry.directory ? (0x10 << 16) : 0);
            writeUInt32LE(centralHeader, 42, offset);
            centralHeader.set(nameBytes, 46);
            central.push(centralHeader);

            offset += localHeader.length + compressedContent.length;
        });

        const centralDirectory = concatUint8Arrays(central);
        const endRecord = new Uint8Array(22);
        writeUInt32LE(endRecord, 0, 0x06054b50);
        writeUInt16LE(endRecord, 4, 0);
        writeUInt16LE(endRecord, 6, 0);
        writeUInt16LE(endRecord, 8, entries.length);
        writeUInt16LE(endRecord, 10, entries.length);
        writeUInt32LE(endRecord, 12, centralDirectory.length);
        writeUInt32LE(endRecord, 16, offset);
        writeUInt16LE(endRecord, 20, 0);

        return concatUint8Arrays([...locals, centralDirectory, endRecord]);
    }

    function decodeBase64(base64) {
        const value = String(base64 || '');
        if (typeof atob === 'function') {
            const decoded = atob(value);
            const bytes = new Uint8Array(decoded.length);
            for (let index = 0; index < decoded.length; index++) {
                bytes[index] = decoded.charCodeAt(index);
            }
            return bytes;
        }
        const buffer = getNativeModule('buffer');
        if (buffer && buffer.Buffer) {
            return new Uint8Array(buffer.Buffer.from(value, 'base64'));
        }
        return new Uint8Array(0);
    }

    function dataUrlToBytes(dataUrl) {
        const value = String(dataUrl || '');
        const commaIndex = value.indexOf(',');
        if (commaIndex === -1) {
            return null;
        }
        return decodeBase64(value.slice(commaIndex + 1));
    }

    function getTextureEntry(fileName) {
        if (typeof Texture === 'undefined' || !Texture || !Array.isArray(Texture.all) || !Texture.all.length) {
            return null;
        }
        const preferred = Texture.getDefault ? Texture.getDefault() : null;
        const texture = preferred || Texture.selected || Texture.all[0];
        if (!texture) {
            return null;
        }

        if (texture.canvas && typeof texture.canvas.toDataURL === 'function') {
            const bytes = dataUrlToBytes(texture.canvas.toDataURL('image/png'));
            if (bytes && bytes.length) {
                return {
                    name: String(fileName || texture.name || 'texture.png').trim() || 'texture.png',
                    content: bytes
                };
            }
        }

        if (typeof texture.source === 'string' && texture.source.startsWith('data:')) {
            const bytes = dataUrlToBytes(texture.source);
            if (bytes && bytes.length) {
                return {
                    name: String(fileName || texture.name || 'texture.png').trim() || 'texture.png',
                    content: bytes
                };
            }
        }
        return null;
    }

    function isGeckoLibProject() {
        if (typeof Format === 'undefined' || !Format || !Format.id) {
            return false;
        }
        return Format.id === 'animated_entity_model' || Format.id === 'geckolib_model';
    }

    function parseJsonSafe(value) {
        if (!value) {
            return null;
        }
        if (typeof value === 'object') {
            return value;
        }
        if (typeof value !== 'string') {
            return null;
        }
        try {
            return JSON.parse(value);
        } catch (error) {
            return null;
        }
    }

    function isProjectModelJson(value) {
        const json = parseJsonSafe(value);
        return Boolean(json && json.meta && json.meta.model_format);
    }

    function isLegacyGeoJson(value) {
        const json = parseJsonSafe(value);
        return Boolean(
            json &&
            json.format_version &&
            Array.isArray(json['minecraft:geometry']) &&
            json['minecraft:geometry'].length
        );
    }

    function getGeoCompiler() {
        if (isGeckoLibProject()) {
            if (typeof Codecs !== 'undefined' && Codecs && Codecs.bedrock && typeof Codecs.bedrock.compile === 'function') {
                return () => Codecs.bedrock.compile();
            }
            return null;
        }
        if (typeof Format !== 'undefined' && Format && Format.codec && typeof Format.codec.compile === 'function') {
            return () => Format.codec.compile();
        }
        if (typeof Codecs !== 'undefined' && Codecs && Codecs.bedrock && typeof Codecs.bedrock.compile === 'function') {
            return () => Codecs.bedrock.compile();
        }
        return null;
    }

    function getCompiledGeoContent() {
        const compilers = [];
        const preferredCompiler = getGeoCompiler();
        if (preferredCompiler) {
            compilers.push(preferredCompiler);
        }
        if (typeof Codecs !== 'undefined' && Codecs && Codecs.bedrock && typeof Codecs.bedrock.compile === 'function') {
            const bedrockCompiler = () => Codecs.bedrock.compile();
            if (!compilers.includes(bedrockCompiler)) {
                compilers.push(bedrockCompiler);
            }
        }
        if (typeof Format !== 'undefined' && Format && Format.codec && typeof Format.codec.compile === 'function') {
            const formatCompiler = () => Format.codec.compile();
            compilers.push(formatCompiler);
        }

        for (const compileGeo of compilers) {
            try {
                const compiled = compileGeo();
                if (isProjectModelJson(compiled)) {
                    continue;
                }
                if (typeof compiled === 'string' && isLegacyGeoJson(compiled)) {
                    return compiled.endsWith('\n') ? compiled : `${compiled}\n`;
                }
                if (compiled && typeof compiled === 'object' && isLegacyGeoJson(compiled)) {
                    return `${JSON.stringify(compiled, null, 2)}\n`;
                }
            } catch (error) {
                // Try the next compiler candidate.
            }
        }
        try {
            if (typeof Codecs !== 'undefined' && Codecs && Codecs.bedrock && typeof Codecs.bedrock.export === 'function') {
                let captured = null;
                Blockbench.export({
                    resource_id: 'model',
                    type: Codecs.bedrock.name || 'Bedrock Model',
                    extensions: ['json'],
                    name: getDefaultGeoFile().replace(/\.json$/i, ''),
                    custom_writer(content, filePath, callback) {
                        captured = typeof content === 'string' ? content : JSON.stringify(content, null, 2);
                        if (typeof callback === 'function') {
                            callback(filePath);
                        }
                    }
                }, () => {});
                if (captured && isLegacyGeoJson(captured)) {
                    return captured.endsWith('\n') ? captured : `${captured}\n`;
                }
            }
        } catch (error) {
            return null;
        }
        return null;
    }

    function collectZipResource(rootName, relativePath, projectDirectory, fsModule, nativePath) {
        const normalized = String(relativePath || '').trim().replace(/\\/g, '/').replace(/^\/+/, '');
        if (!normalized || normalized.includes('..')) {
            return null;
        }
        if (!projectDirectory || !fsModule || !nativePath) {
            return {missing: normalized};
        }

        const baseDirectory = nativePath.resolve(projectDirectory);
        const absolutePath = nativePath.resolve(baseDirectory, normalized);
        const safePrefix = `${baseDirectory.toLowerCase()}${nativePath.sep}`;
        const normalizedAbsolute = absolutePath.toLowerCase();
        if (normalizedAbsolute !== baseDirectory.toLowerCase() && !normalizedAbsolute.startsWith(safePrefix)) {
            return {missing: normalized};
        }
        if (!fsModule.existsSync(absolutePath)) {
            return {missing: normalized};
        }

        let stats;
        try {
            stats = fsModule.statSync(absolutePath);
        } catch (error) {
            return {missing: normalized};
        }
        if (!stats.isFile()) {
            return {missing: normalized};
        }
        return {
            entry: {
                name: `${rootName}/${normalized}`,
                content: toUint8Array(fsModule.readFileSync(absolutePath))
            }
        };
    }

    function exportJsonContent(type, name, content, successMessage) {
        Blockbench.export({
            type,
            extensions: ['json'],
            name,
            content
        }, filePath => {
            if (filePath) {
                Blockbench.showQuickMessage(successMessage);
            }
        });
    }

    function exportZipPackage(config, resourceKeys) {
        const text = getText();
        const fsModule = getNativeModule('fs');
        const nativePath = getNativeModule('path');
        const zlibModule = getNativeModule('zlib');
        if (!fsModule || !nativePath || !zlibModule) {
            Blockbench.showQuickMessage(text.zipExportDesktopOnly);
            return;
        }

        const configContent = JSON.stringify(config, null, 2) + '\n';
        const rootName = sanitizeId(config.id, 'moreblock_package');
        const entries = [
            {name: `${rootName}/`, directory: true},
            {name: `${rootName}/${config.id}.json`, content: configContent}
        ];
        const missingFiles = [];
        const projectDirectory = getProjectDirectory(nativePath);
        const embeddedResourceKeys = new Set();

        if (config.geo) {
            const geoContent = getCompiledGeoContent();
            if (geoContent) {
                entries.push({
                    name: `${rootName}/${String(config.geo).trim().replace(/\\/g, '/').replace(/^\/+/, '')}`,
                    content: geoContent
                });
                embeddedResourceKeys.add('geo');
            } else if (isGeckoLibProject()) {
                missingFiles.push(text.noGeoExporterAvailable);
            }
        }

        if (config.texture) {
            const textureEntry = getTextureEntry(config.texture);
            if (textureEntry) {
                entries.push({
                    name: `${rootName}/${textureEntry.name.replace(/\\/g, '/').replace(/^\/+/, '')}`,
                    content: textureEntry.content
                });
                embeddedResourceKeys.add('texture');
            } else {
                const projectPath = getProjectFilePath();
                if (!projectPath) {
                    missingFiles.push(text.noTextureAvailable);
                }
            }
        }

        resourceKeys.forEach(key => {
            if (embeddedResourceKeys.has(key)) {
                return;
            }
            if (!config[key]) {
                return;
            }
            const result = collectZipResource(rootName, config[key], projectDirectory, fsModule, nativePath);
            if (!result) {
                return;
            }
            if (result.entry) {
                entries.push(result.entry);
            } else if (result.missing) {
                missingFiles.push(result.missing);
            }
        });

        const zipBytes = createZipArchive(entries, zlibModule);
        Blockbench.export({
            type: text.zipExportType,
            extensions: ['zip'],
            name: `${rootName}.zip`,
            content: toArrayBuffer(zipBytes)
        }, filePath => {
            if (filePath) {
                Blockbench.showQuickMessage(
                    missingFiles.length
                        ? text.zipExportedWithMissing(missingFiles.join(', '))
                        : text.zipExported
                );
            }
        });
    }

    function roundTo(value, decimals = 3) {
        const factor = Math.pow(10, decimals);
        return Math.round(Number(value) * factor) / factor;
    }

    function clampNumber(value, fallback, min, max) {
        const numeric = Number.parseFloat(value);
        if (!Number.isFinite(numeric)) {
            return fallback;
        }
        return Math.min(max, Math.max(min, numeric));
    }

    function getSuggestedEntityMetrics() {
        const sourceBoxes = getSourceCubes().map(getCubeBounds).filter(box => getBoxVolume(box) > 0);
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        if (!bounds) {
            return {
                width: 0.6,
                height: 1.8,
                eyeHeight: 1.62
            };
        }

        const width = Math.max(bounds.maxX - bounds.minX, bounds.maxZ - bounds.minZ) / 16;
        const height = (bounds.maxY - bounds.minY) / 16;
        return {
            width: roundTo(Math.max(0.1, width)),
            height: roundTo(Math.max(0.1, height)),
            eyeHeight: roundTo(Math.max(0.05, height * 0.85))
        };
    }

    function buildConfig(form) {
        const text = getText();
        const config = {
            id: sanitizeId(form.id, 'custom_block'),
            name: {
                zh_cn: String(form.zh_cn || '').trim() || text.defaultChineseName,
                en_us: String(form.en_us || '').trim() || text.defaultEnglishName
            },
            geo: String(form.geo || '').trim() || getDefaultGeoFile(),
            texture: String(form.texture || '').trim() || 'texture.png',
            light_level: Math.max(0, Math.min(15, Number.parseInt(form.light_level, 10) || 0)),
            supports_sitting: Boolean(form.supports_sitting) && !Boolean(form.supports_lying),
            seat_height: Number.isFinite(Number.parseFloat(form.seat_height)) ? Number.parseFloat(form.seat_height) : 0.5,
            supports_lying: Boolean(form.supports_lying) && !Boolean(form.supports_sitting),
            lying_height: Number.isFinite(Number.parseFloat(form.lying_height)) ? Number.parseFloat(form.lying_height) : 0.5
        };

        const display = String(form.display || '').trim();
        if (display) {
            config.display = display;
        }
        return config;
    }

    function buildEntityConfig(form) {
        const text = getText();
        const metrics = getSuggestedEntityMetrics();
        const config = {
            id: sanitizeId(form.id, 'custom_entity'),
            name: {
                zh_cn: String(form.zh_cn || '').trim() || text.defaultEntityChineseName,
                en_us: String(form.en_us || '').trim() || text.defaultEntityEnglishName
            },
            geo: String(form.geo || '').trim() || getDefaultGeoFile(),
            texture: String(form.texture || '').trim() || 'texture.png',
            width: clampNumber(form.width, metrics.width, 0.1, 64),
            height: clampNumber(form.height, metrics.height, 0.1, 64),
            eye_height: clampNumber(form.eye_height, metrics.eyeHeight, 0.05, 64),
            max_health: clampNumber(form.max_health, 20, 1, 2048),
            movement_speed: clampNumber(form.movement_speed, 0.2, 0, 10),
            follow_range: clampNumber(form.follow_range, 16, 0, 256),
            attack_damage: clampNumber(form.attack_damage, 2, 0, 2048),
            armor: clampNumber(form.armor, 0, 0, 2048),
            knockback_resistance: clampNumber(form.knockback_resistance, 0.2, 0, 1),
            tracking_range: Math.round(clampNumber(form.tracking_range, 8, 1, 256)),
            update_interval: Math.round(clampNumber(form.update_interval, 3, 1, 60)),
            ai_enabled: Boolean(form.ai_enabled),
            ai_template: String(form.ai_template || '').trim() || 'minecraft:zombie',
            animation_transition: Boolean(form.animation_transition),
            disable_vanilla_death_animation: Boolean(form.disable_vanilla_death_animation),
            spawn_egg_primary_color: String(form.spawn_egg_primary_color || '').trim() || '#4b7cf0',
            spawn_egg_secondary_color: String(form.spawn_egg_secondary_color || '').trim() || '#d8e4ff',
            show_in_moreblock_tab: Boolean(form.show_in_moreblock_tab),
            translucent: Boolean(form.translucent)
        };

        const animation = String(form.animation || '').trim();
        if (animation) {
            config.animation = animation;
        }

        if (Boolean(form.include_animation_states)) {
            config.animation_states = {
                idle: {
                    idle: 1.0,
                    idle_1: 0.6,
                    idle_2: 0.4
                },
                walk: {
                    walk: 1.0,
                    walk_1: 0.5
                },
                run: {
                    run: 1.0
                },
                attack: {
                    attack: 1.0,
                    attack_1: 0.8
                },
                hurt: {
                    hurt: 1.0
                },
                spawn: {
                    spawn: 1.0
                },
                die: {
                    die: 1.0,
                    die_1: 0.6
                }
            };
        }

        return config;
    }

    function readVector(value, fallback) {
        if (Array.isArray(value) && value.length >= 3) {
            return [Number(value[0]) || 0, Number(value[1]) || 0, Number(value[2]) || 0];
        }
        return fallback.slice();
    }

    function getCubeBounds(cube) {
        const from = readVector(cube.from, [0, 0, 0]);
        const to = readVector(cube.to, [0, 0, 0]);
        const inflate = Number(cube.inflate) || 0;
        return {
            minX: Math.min(from[0], to[0]) - inflate,
            minY: Math.min(from[1], to[1]) - inflate,
            minZ: Math.min(from[2], to[2]) - inflate,
            maxX: Math.max(from[0], to[0]) + inflate,
            maxY: Math.max(from[1], to[1]) + inflate,
            maxZ: Math.max(from[2], to[2]) + inflate
        };
    }

    function getSourceCubes() {
        if (typeof Cube === 'undefined' || !Cube.all || !Cube.all.length) {
            return [];
        }
        return Cube.all.filter(cube => !(cube.parent && cube.parent.name === 'hitbox'));
    }

    function calculateBoundsFromBoxes(boxes) {
        if (!boxes.length) {
            return null;
        }
        return boxes.reduce((bounds, box) => {
            if (!bounds) {
                return Object.assign({}, box);
            }
            return {
                minX: Math.min(bounds.minX, box.minX),
                minY: Math.min(bounds.minY, box.minY),
                minZ: Math.min(bounds.minZ, box.minZ),
                maxX: Math.max(bounds.maxX, box.maxX),
                maxY: Math.max(bounds.maxY, box.maxY),
                maxZ: Math.max(bounds.maxZ, box.maxZ)
            };
        }, null);
    }

    function getBoxVolume(box) {
        return Math.max(0, box.maxX - box.minX) * Math.max(0, box.maxY - box.minY) * Math.max(0, box.maxZ - box.minZ);
    }

    function intersects(a, b) {
        return a.minX < b.maxX && a.maxX > b.minX
            && a.minY < b.maxY && a.maxY > b.minY
            && a.minZ < b.maxZ && a.maxZ > b.minZ;
    }

    function createVoxelGrid(sourceBoxes, bounds, cellSize) {
        let sizeX = Math.max(1, Math.ceil((bounds.maxX - bounds.minX) / cellSize));
        let sizeY = Math.max(1, Math.ceil((bounds.maxY - bounds.minY) / cellSize));
        let sizeZ = Math.max(1, Math.ceil((bounds.maxZ - bounds.minZ) / cellSize));
        let adjustedCellSize = cellSize;
        while (sizeX * sizeY * sizeZ > 32768) {
            adjustedCellSize *= 2;
            sizeX = Math.max(1, Math.ceil((bounds.maxX - bounds.minX) / adjustedCellSize));
            sizeY = Math.max(1, Math.ceil((bounds.maxY - bounds.minY) / adjustedCellSize));
            sizeZ = Math.max(1, Math.ceil((bounds.maxZ - bounds.minZ) / adjustedCellSize));
        }
        const data = new Array(sizeX * sizeY * sizeZ).fill(false);
        const index = (x, y, z) => x + sizeX * (y + sizeY * z);
        for (let z = 0; z < sizeZ; z++) {
            for (let y = 0; y < sizeY; y++) {
                for (let x = 0; x < sizeX; x++) {
                    const voxelBox = {
                        minX: bounds.minX + x * adjustedCellSize,
                        minY: bounds.minY + y * adjustedCellSize,
                        minZ: bounds.minZ + z * adjustedCellSize,
                        maxX: Math.min(bounds.maxX, bounds.minX + (x + 1) * adjustedCellSize),
                        maxY: Math.min(bounds.maxY, bounds.minY + (y + 1) * adjustedCellSize),
                        maxZ: Math.min(bounds.maxZ, bounds.minZ + (z + 1) * adjustedCellSize)
                    };
                    data[index(x, y, z)] = sourceBoxes.some(box => intersects(box, voxelBox));
                }
            }
        }
        return {bounds, cellSize: adjustedCellSize, sizeX, sizeY, sizeZ, data, index};
    }

    function isVoxelFilled(grid, visited, x, y, z) {
        if (x < 0 || y < 0 || z < 0 || x >= grid.sizeX || y >= grid.sizeY || z >= grid.sizeZ) {
            return false;
        }
        const index = grid.index(x, y, z);
        return grid.data[index] && !visited[index];
    }

    function canFillBox(grid, visited, startX, startY, startZ, width, height, depth) {
        if (startX + width > grid.sizeX || startY + height > grid.sizeY || startZ + depth > grid.sizeZ) {
            return false;
        }
        for (let z = startZ; z < startZ + depth; z++) {
            for (let y = startY; y < startY + height; y++) {
                for (let x = startX; x < startX + width; x++) {
                    if (!isVoxelFilled(grid, visited, x, y, z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    function tryGrowBox(grid, visited, startX, startY, startZ, order) {
        const box = {width: 1, height: 1, depth: 1};
        let changed = true;
        while (changed) {
            changed = false;
            for (const axis of order) {
                const next = Object.assign({}, box);
                if (axis === 'x') {
                    next.width++;
                } else if (axis === 'y') {
                    next.height++;
                } else {
                    next.depth++;
                }
                if (canFillBox(grid, visited, startX, startY, startZ, next.width, next.height, next.depth)) {
                    box.width = next.width;
                    box.height = next.height;
                    box.depth = next.depth;
                    changed = true;
                }
            }
        }
        return box;
    }

    function chooseBestBox(grid, visited, startX, startY, startZ) {
        const orders = [
            ['x', 'z', 'y'],
            ['x', 'y', 'z'],
            ['z', 'x', 'y'],
            ['z', 'y', 'x'],
            ['y', 'x', 'z'],
            ['y', 'z', 'x']
        ];
        return orders
            .map(order => tryGrowBox(grid, visited, startX, startY, startZ, order))
            .sort((a, b) => (b.width * b.height * b.depth) - (a.width * a.height * a.depth))[0];
    }

    function markVisited(grid, visited, startX, startY, startZ, width, height, depth) {
        for (let z = startZ; z < startZ + depth; z++) {
            for (let y = startY; y < startY + height; y++) {
                for (let x = startX; x < startX + width; x++) {
                    visited[grid.index(x, y, z)] = true;
                }
            }
        }
    }

    function voxelBoxToModelBox(grid, startX, startY, startZ, width, height, depth) {
        return {
            minX: grid.bounds.minX + startX * grid.cellSize,
            minY: grid.bounds.minY + startY * grid.cellSize,
            minZ: grid.bounds.minZ + startZ * grid.cellSize,
            maxX: Math.min(grid.bounds.maxX, grid.bounds.minX + (startX + width) * grid.cellSize),
            maxY: Math.min(grid.bounds.maxY, grid.bounds.minY + (startY + height) * grid.cellSize),
            maxZ: Math.min(grid.bounds.maxZ, grid.bounds.minZ + (startZ + depth) * grid.cellSize)
        };
    }

    function greedyMergeVoxels(grid) {
        const visited = new Array(grid.data.length).fill(false);
        const boxes = [];
        for (let y = 0; y < grid.sizeY; y++) {
            for (let z = 0; z < grid.sizeZ; z++) {
                for (let x = 0; x < grid.sizeX; x++) {
                    if (!isVoxelFilled(grid, visited, x, y, z)) {
                        continue;
                    }
                    const box = chooseBestBox(grid, visited, x, y, z);
                    markVisited(grid, visited, x, y, z, box.width, box.height, box.depth);
                    boxes.push(voxelBoxToModelBox(grid, x, y, z, box.width, box.height, box.depth));
                }
            }
        }
        return boxes;
    }

    function boxesTouchOrOverlap(a, b, axis) {
        const epsilon = 0.000001;
        if (axis === 'x') {
            return Math.abs(a.maxX - b.minX) < epsilon || Math.abs(b.maxX - a.minX) < epsilon;
        }
        if (axis === 'y') {
            return Math.abs(a.maxY - b.minY) < epsilon || Math.abs(b.maxY - a.minY) < epsilon;
        }
        return Math.abs(a.maxZ - b.minZ) < epsilon || Math.abs(b.maxZ - a.minZ) < epsilon;
    }

    function sameRange(a, b, minKey, maxKey) {
        const epsilon = 0.000001;
        return Math.abs(a[minKey] - b[minKey]) < epsilon && Math.abs(a[maxKey] - b[maxKey]) < epsilon;
    }

    function canMergeBoxes(a, b) {
        return (boxesTouchOrOverlap(a, b, 'x') && sameRange(a, b, 'minY', 'maxY') && sameRange(a, b, 'minZ', 'maxZ'))
            || (boxesTouchOrOverlap(a, b, 'y') && sameRange(a, b, 'minX', 'maxX') && sameRange(a, b, 'minZ', 'maxZ'))
            || (boxesTouchOrOverlap(a, b, 'z') && sameRange(a, b, 'minX', 'maxX') && sameRange(a, b, 'minY', 'maxY'));
    }

    function mergeTwoBoxes(a, b) {
        return {
            minX: Math.min(a.minX, b.minX),
            minY: Math.min(a.minY, b.minY),
            minZ: Math.min(a.minZ, b.minZ),
            maxX: Math.max(a.maxX, b.maxX),
            maxY: Math.max(a.maxY, b.maxY),
            maxZ: Math.max(a.maxZ, b.maxZ)
        };
    }

    function mergeAdjacentBoxes(boxes) {
        let merged = boxes.slice();
        let changed = true;
        while (changed) {
            changed = false;
            outer: for (let i = 0; i < merged.length; i++) {
                for (let j = i + 1; j < merged.length; j++) {
                    if (!canMergeBoxes(merged[i], merged[j])) {
                        continue;
                    }
                    merged[i] = mergeTwoBoxes(merged[i], merged[j]);
                    merged.splice(j, 1);
                    changed = true;
                    break outer;
                }
            }
        }
        return merged;
    }

    function generateSimpleBoxes(sourceBoxes) {
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        return bounds ? [bounds] : [];
    }

    function generateComplexBoxes(sourceBoxes, simplification, maxBoxes) {
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        if (!bounds) {
            return [];
        }
        const cellSize = Math.max(0.25, Math.min(8, Number(simplification) || 1));
        const grid = createVoxelGrid(sourceBoxes, bounds, cellSize);
        return mergeAdjacentBoxes(greedyMergeVoxels(grid))
            .sort((a, b) => getBoxVolume(b) - getBoxVolume(a))
            .slice(0, Math.max(1, Math.min(128, Number.parseInt(maxBoxes, 10) || 32)));
    }

    function getUnvisitedFilledCount(grid, visited) {
        let count = 0;
        for (let index = 0; index < grid.data.length; index++) {
            if (grid.data[index] && !visited[index]) {
                count++;
            }
        }
        return count;
    }

    function findLargestCuboidAt(grid, visited, startX, startY, startZ) {
        if (!isVoxelFilled(grid, visited, startX, startY, startZ)) {
            return null;
        }
        let best = {x: startX, y: startY, z: startZ, width: 1, height: 1, depth: 1, volume: 1};
        let maxWidth = 0;
        while (isVoxelFilled(grid, visited, startX + maxWidth, startY, startZ)) {
            maxWidth++;
        }
        for (let width = 1; width <= maxWidth; width++) {
            let maxDepth = 0;
            while (true) {
                const z = startZ + maxDepth;
                if (z >= grid.sizeZ) {
                    break;
                }
                let valid = true;
                for (let x = startX; x < startX + width; x++) {
                    if (!isVoxelFilled(grid, visited, x, startY, z)) {
                        valid = false;
                        break;
                    }
                }
                if (!valid) {
                    break;
                }
                maxDepth++;
            }
            for (let depth = 1; depth <= maxDepth; depth++) {
                let height = 1;
                while (canFillBox(grid, visited, startX, startY, startZ, width, height + 1, depth)) {
                    height++;
                }
                const volume = width * height * depth;
                if (volume > best.volume) {
                    best = {x: startX, y: startY, z: startZ, width, height, depth, volume};
                }
            }
        }
        return best;
    }

    function findGlobalLargestCuboid(grid, visited) {
        let best = null;
        for (let y = 0; y < grid.sizeY; y++) {
            for (let z = 0; z < grid.sizeZ; z++) {
                for (let x = 0; x < grid.sizeX; x++) {
                    if (!isVoxelFilled(grid, visited, x, y, z)) {
                        continue;
                    }
                    const cuboid = findLargestCuboidAt(grid, visited, x, y, z);
                    if (cuboid && (!best || cuboid.volume > best.volume)) {
                        best = cuboid;
                    }
                }
            }
        }
        return best;
    }

    function globalLargestCuboidCover(grid, maxBoxes) {
        const limit = Math.max(1, Math.min(128, Number.parseInt(maxBoxes, 10) || 32));
        const visited = new Array(grid.data.length).fill(false);
        const boxes = [];
        while (boxes.length < limit && getUnvisitedFilledCount(grid, visited) > 0) {
            const cuboid = findGlobalLargestCuboid(grid, visited);
            if (!cuboid) {
                break;
            }
            markVisited(grid, visited, cuboid.x, cuboid.y, cuboid.z, cuboid.width, cuboid.height, cuboid.depth);
            boxes.push(voxelBoxToModelBox(grid, cuboid.x, cuboid.y, cuboid.z, cuboid.width, cuboid.height, cuboid.depth));
        }
        return mergeAdjacentBoxes(boxes).sort((a, b) => getBoxVolume(b) - getBoxVolume(a));
    }

    function generateQualityBoxes(sourceBoxes, simplification, maxBoxes) {
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        if (!bounds) {
            return [];
        }
        const cellSize = Math.max(0.25, Math.min(8, Number(simplification) || 1));
        const grid = createVoxelGrid(sourceBoxes, bounds, cellSize);
        return globalLargestCuboidCover(grid, maxBoxes);
    }

    function greedyRectangles2D(mask, width, height) {
        const rectangles = [];
        const data = mask.slice();
        let n = 0;
        for (let y = 0; y < height; y++) {
            for (let x = 0; x < width;) {
                if (!data[n]) {
                    x++;
                    n++;
                    continue;
                }
                let rectWidth = 1;
                while (x + rectWidth < width && data[n + rectWidth]) {
                    rectWidth++;
                }
                let rectHeight = 1;
                while (y + rectHeight < height) {
                    let k = 0;
                    while (k < rectWidth && data[n + k + rectHeight * width]) {
                        k++;
                    }
                    if (k < rectWidth) {
                        break;
                    }
                    rectHeight++;
                }
                for (let dy = 0; dy < rectHeight; dy++) {
                    for (let dx = 0; dx < rectWidth; dx++) {
                        data[n + dx + dy * width] = false;
                    }
                }
                rectangles.push({x, y, width: rectWidth, height: rectHeight, area: rectWidth * rectHeight});
                x += rectWidth;
                n += rectWidth;
            }
        }
        return rectangles;
    }

    function buildSliceMask(grid, visited, axis, layer) {
        const u = (axis + 1) % 3;
        const v = (axis + 2) % 3;
        const dims = [grid.sizeX, grid.sizeY, grid.sizeZ];
        const width = dims[u];
        const height = dims[v];
        const mask = new Array(width * height).fill(false);
        for (let vv = 0; vv < height; vv++) {
            for (let uu = 0; uu < width; uu++) {
                const coord = [0, 0, 0];
                coord[axis] = layer;
                coord[u] = uu;
                coord[v] = vv;
                mask[uu + vv * width] = isVoxelFilled(grid, visited, coord[0], coord[1], coord[2]);
            }
        }
        return {mask, width, height, u, v};
    }

    function rectangleCanExtrude(grid, visited, axis, startLayer, rect, u, v, depth) {
        const layer = startLayer + depth;
        const dims = [grid.sizeX, grid.sizeY, grid.sizeZ];
        if (layer >= dims[axis]) {
            return false;
        }
        for (let vv = rect.y; vv < rect.y + rect.height; vv++) {
            for (let uu = rect.x; uu < rect.x + rect.width; uu++) {
                const coord = [0, 0, 0];
                coord[axis] = layer;
                coord[u] = uu;
                coord[v] = vv;
                if (!isVoxelFilled(grid, visited, coord[0], coord[1], coord[2])) {
                    return false;
                }
            }
        }
        return true;
    }

    function markAxisRectVisited(grid, visited, axis, startLayer, rect, u, v, depth) {
        for (let dd = 0; dd < depth; dd++) {
            for (let vv = rect.y; vv < rect.y + rect.height; vv++) {
                for (let uu = rect.x; uu < rect.x + rect.width; uu++) {
                    const coord = [0, 0, 0];
                    coord[axis] = startLayer + dd;
                    coord[u] = uu;
                    coord[v] = vv;
                    visited[grid.index(coord[0], coord[1], coord[2])] = true;
                }
            }
        }
    }

    function axisRectToModelBox(grid, axis, startLayer, rect, u, v, depth) {
        const mins = [0, 0, 0];
        const maxs = [0, 0, 0];
        mins[axis] = startLayer;
        maxs[axis] = startLayer + depth;
        mins[u] = rect.x;
        maxs[u] = rect.x + rect.width;
        mins[v] = rect.y;
        maxs[v] = rect.y + rect.height;
        return voxelBoxToModelBox(grid, mins[0], mins[1], mins[2], maxs[0] - mins[0], maxs[1] - mins[1], maxs[2] - mins[2]);
    }

    function getGreedySliceCandidates(grid, visited) {
        const candidates = [];
        const dims = [grid.sizeX, grid.sizeY, grid.sizeZ];
        for (let axis = 0; axis < 3; axis++) {
            for (let layer = 0; layer < dims[axis]; layer++) {
                const slice = buildSliceMask(grid, visited, axis, layer);
                greedyRectangles2D(slice.mask, slice.width, slice.height).forEach(rect => {
                    let depth = 1;
                    while (rectangleCanExtrude(grid, visited, axis, layer, rect, slice.u, slice.v, depth)) {
                        depth++;
                    }
                    candidates.push({axis, layer, rect, u: slice.u, v: slice.v, depth, volume: rect.area * depth});
                });
            }
        }
        return candidates;
    }

    function greedySliceCover(grid, maxBoxes) {
        const limit = Math.max(1, Math.min(128, Number.parseInt(maxBoxes, 10) || 32));
        const visited = new Array(grid.data.length).fill(false);
        const boxes = [];
        while (boxes.length < limit && getUnvisitedFilledCount(grid, visited) > 0) {
            const candidates = getGreedySliceCandidates(grid, visited);
            if (!candidates.length) {
                break;
            }
            const best = candidates.sort((a, b) => b.volume - a.volume)[0];
            markAxisRectVisited(grid, visited, best.axis, best.layer, best.rect, best.u, best.v, best.depth);
            boxes.push(axisRectToModelBox(grid, best.axis, best.layer, best.rect, best.u, best.v, best.depth));
        }
        return mergeAdjacentBoxes(boxes).sort((a, b) => getBoxVolume(b) - getBoxVolume(a));
    }

    function generateGreedySliceBoxes(sourceBoxes, simplification, maxBoxes) {
        const bounds = calculateBoundsFromBoxes(sourceBoxes);
        if (!bounds) {
            return [];
        }
        const cellSize = Math.max(0.25, Math.min(8, Number(simplification) || 1));
        const grid = createVoxelGrid(sourceBoxes, bounds, cellSize);
        return greedySliceCover(grid, maxBoxes);
    }

    function removeExistingHitboxGroup() {
        if (typeof Group === 'undefined' || !Group.all) {
            return;
        }
        const existing = Group.all.find(group => group.name === 'hitbox');
        if (existing && existing.remove) {
            existing.remove();
        }
    }

    function clearCubeFaces(cube) {
        if (!cube || !cube.faces) {
            return;
        }
        Object.keys(cube.faces).forEach(key => {
            const face = cube.faces[key];
            if (!face) {
                return;
            }
            face.texture = null;
            face.enabled = false;
            if (face.uv) {
                face.uv = [0, 0, 0, 0];
            }
        });
    }

    function createHitboxCubes(boxes) {
        Undo.initEdit({elements: Cube.all.slice(), outliner: true});
        removeExistingHitboxGroup();
        const hitboxGroup = new Group({
            name: 'hitbox',
            origin: [0, 0, 0]
        }).init();
        hitboxGroup.addTo('root');
        boxes.forEach((box, index) => {
            const hitboxCube = new Cube({
                name: boxes.length === 1 ? 'hitbox' : `hitbox_${index + 1}`,
                from: [box.minX, box.minY, box.minZ],
                to: [box.maxX, box.maxY, box.maxZ],
                origin: [0, 0, 0],
                autouv: 0
            }).init();
            clearCubeFaces(hitboxCube);
            hitboxCube.addTo(hitboxGroup);
        });
        hitboxGroup.openUp();
        Canvas.updateAll();
        Undo.finishEdit('Generate MoreBlock hitbox');
    }

    function resolveHitboxMode(value) {
        if (value === 'complex' || value === 'quality' || value === 'greedy') {
            return value;
        }
        return 'simple';
    }

    function generateHitbox(mode, options) {
        const text = getText();
        const sourceBoxes = getSourceCubes().map(getCubeBounds).filter(box => getBoxVolume(box) > 0);
        if (!sourceBoxes.length) {
            Blockbench.showQuickMessage(text.noModelCubes);
            return;
        }
        const simplification = options && options.simplification !== undefined ? options.simplification : 1;
        const maxBoxes = options && options.max_boxes !== undefined ? options.max_boxes : 32;
        let boxes;
        if (mode === 'greedy') {
            boxes = generateGreedySliceBoxes(sourceBoxes, simplification, maxBoxes);
        } else if (mode === 'quality') {
            boxes = generateQualityBoxes(sourceBoxes, simplification, maxBoxes);
        } else if (mode === 'complex') {
            boxes = generateComplexBoxes(sourceBoxes, simplification, maxBoxes);
        } else {
            boxes = generateSimpleBoxes(sourceBoxes);
        }
        if (!boxes.length) {
            Blockbench.showQuickMessage(text.noModelCubes);
            return;
        }
        createHitboxCubes(boxes);
        Blockbench.showQuickMessage(text.hitboxGeneratedWithCount(boxes.length));
    }

    function showHitboxDialog() {
        const text = getText();
        const dialog = new Dialog({
            id: 'moreblock_hitbox_dialog',
            title: text.hitboxDialogTitle,
            width: 520,
            form: {
                mode: {
                    label: text.hitboxMode,
                    type: 'select',
                    options: {
                        simple: text.simpleMode,
                        complex: text.complexMode,
                        quality: text.qualityMode,
                        greedy: text.greedyMode
                    },
                    value: 'simple'
                },
                simplification: {
                    label: text.simplification,
                    type: 'number',
                    value: 1,
                    min: 0.25,
                    max: 8,
                    step: 0.25,
                    description: text.simplificationInfo
                },
                max_boxes: {
                    label: text.maxBoxes,
                    type: 'number',
                    value: 32,
                    min: 1,
                    max: 128,
                    step: 1,
                    description: text.maxBoxesInfo
                }
            },
            onConfirm(form) {
                generateHitbox(resolveHitboxMode(form.mode), form);
            }
        });
        dialog.show();
    }

    function showExportDialog() {
        const text = getText();
        const baseName = getProjectBaseName();
        const defaultId = sanitizeId(baseName);
        const dialog = new Dialog({
            id: 'moreblock_config_export_dialog',
            title: text.dialogTitle,
            width: 560,
            form: {
                id: {
                    label: text.blockId,
                    type: 'text',
                    value: defaultId
                },
                zh_cn: {
                    label: text.chineseName,
                    type: 'text',
                    value: baseName === 'custom_block' ? text.defaultChineseName : baseName
                },
                en_us: {
                    label: text.englishName,
                    type: 'text',
                    value: text.defaultEnglishName
                },
                geo: {
                    label: text.geoFile,
                    type: 'text',
                    value: getDefaultGeoFile()
                },
                texture: {
                    label: text.textureFile,
                    type: 'text',
                    value: 'texture.png'
                },
                display: {
                    label: text.displayFile,
                    type: 'text',
                    value: ''
                },
                export_zip_package: {
                    label: text.exportZipPackage,
                    type: 'checkbox',
                    value: false,
                    description: text.exportZipPackageDescription
                },
                light_level: {
                    label: text.lightLevel,
                    type: 'number',
                    value: 0,
                    min: 0,
                    max: 15
                },
                supports_sitting: {
                    label: text.canSit,
                    type: 'checkbox',
                    value: false
                },
                seat_height: {
                    label: text.seatHeight,
                    type: 'number',
                    value: 0.5,
                    min: 0,
                    max: 2,
                    step: 0.05
                },
                supports_lying: {
                    label: text.canLie,
                    type: 'checkbox',
                    value: false
                },
                lying_height: {
                    label: text.lyingHeight,
                    type: 'number',
                    value: 0.5,
                    min: 0,
                    max: 2,
                    step: 0.05
                }
            },
            onConfirm(form) {
                const latestText = getText();
                const config = buildConfig(form);
                const content = JSON.stringify(config, null, 2) + '\n';
                if (Boolean(form.export_zip_package)) {
                    exportZipPackage(config, ['geo', 'texture', 'display']);
                    return;
                }
                exportJsonContent(latestText.exportType, `${config.id}.json`, content, latestText.exported);
            }
        });
        dialog.show();
    }

    function showEntityExportDialog() {
        const text = getText();
        const baseName = getProjectBaseName();
        const defaultId = sanitizeId(baseName, 'custom_entity');
        const metrics = getSuggestedEntityMetrics();
        const dialog = new Dialog({
            id: 'moreblock_entity_config_export_dialog',
            title: text.entityDialogTitle,
            width: 640,
            form: {
                id: {
                    label: text.entityId,
                    type: 'text',
                    value: defaultId
                },
                zh_cn: {
                    label: text.chineseName,
                    type: 'text',
                    value: baseName === 'custom_block' ? text.defaultEntityChineseName : baseName
                },
                en_us: {
                    label: text.englishName,
                    type: 'text',
                    value: text.defaultEntityEnglishName
                },
                geo: {
                    label: text.geoFile,
                    type: 'text',
                    value: getDefaultGeoFile()
                },
                texture: {
                    label: text.textureFile,
                    type: 'text',
                    value: 'texture.png'
                },
                animation: {
                    label: text.animationFile,
                    type: 'text',
                    value: getDefaultAnimationFile()
                },
                width: {
                    label: text.entityWidth,
                    type: 'number',
                    value: metrics.width,
                    min: 0.1,
                    max: 64,
                    step: 0.01
                },
                height: {
                    label: text.entityHeight,
                    type: 'number',
                    value: metrics.height,
                    min: 0.1,
                    max: 64,
                    step: 0.01
                },
                eye_height: {
                    label: text.eyeHeight,
                    type: 'number',
                    value: metrics.eyeHeight,
                    min: 0.05,
                    max: 64,
                    step: 0.01
                },
                max_health: {
                    label: text.maxHealth,
                    type: 'number',
                    value: 20,
                    min: 1,
                    max: 2048,
                    step: 1
                },
                movement_speed: {
                    label: text.movementSpeed,
                    type: 'number',
                    value: 0.2,
                    min: 0,
                    max: 10,
                    step: 0.01
                },
                follow_range: {
                    label: text.followRange,
                    type: 'number',
                    value: 16,
                    min: 0,
                    max: 256,
                    step: 1
                },
                attack_damage: {
                    label: text.attackDamage,
                    type: 'number',
                    value: 2,
                    min: 0,
                    max: 2048,
                    step: 0.5
                },
                armor: {
                    label: text.armor,
                    type: 'number',
                    value: 0,
                    min: 0,
                    max: 2048,
                    step: 0.5
                },
                knockback_resistance: {
                    label: text.knockbackResistance,
                    type: 'number',
                    value: 0.2,
                    min: 0,
                    max: 1,
                    step: 0.05
                },
                tracking_range: {
                    label: text.trackingRange,
                    type: 'number',
                    value: 8,
                    min: 1,
                    max: 256,
                    step: 1
                },
                update_interval: {
                    label: text.updateInterval,
                    type: 'number',
                    value: 3,
                    min: 1,
                    max: 60,
                    step: 1
                },
                ai_enabled: {
                    label: text.aiEnabled,
                    type: 'checkbox',
                    value: true
                },
                ai_template: {
                    label: text.aiTemplate,
                    type: 'text',
                    value: 'minecraft:zombie'
                },
                animation_transition: {
                    label: text.animationTransition,
                    type: 'checkbox',
                    value: true
                },
                disable_vanilla_death_animation: {
                    label: text.disableVanillaDeathAnimation,
                    type: 'checkbox',
                    value: true
                },
                translucent: {
                    label: text.translucent,
                    type: 'checkbox',
                    value: true
                },
                show_in_moreblock_tab: {
                    label: text.showInMoreBlockTab,
                    type: 'checkbox',
                    value: true
                },
                spawn_egg_primary_color: {
                    label: text.spawnEggPrimaryColor,
                    type: 'color',
                    value: '#4b7cf0'
                },
                spawn_egg_secondary_color: {
                    label: text.spawnEggSecondaryColor,
                    type: 'color',
                    value: '#d8e4ff'
                },
                include_animation_states: {
                    label: text.includeAnimationStates,
                    type: 'checkbox',
                    value: true,
                    description: text.includeAnimationStatesDescription
                },
                export_zip_package: {
                    label: text.exportZipPackage,
                    type: 'checkbox',
                    value: false,
                    description: text.exportZipPackageDescription
                }
            },
            onConfirm(form) {
                const latestText = getText();
                const config = buildEntityConfig(form);
                const content = JSON.stringify(config, null, 2) + '\n';
                if (Boolean(form.export_zip_package)) {
                    exportZipPackage(config, ['geo', 'texture', 'animation']);
                    return;
                }
                exportJsonContent(latestText.entityExportType, `${config.id}.json`, content, latestText.entityExported);
            }
        });
        dialog.show();
    }

    Plugin.register(PLUGIN_ID, {
        title: getText().pluginTitle,
        author: 'Sallos',
        icon: 'fa-cube',
        description: getText().pluginDescription,
        version: '2.0.0',
        variant: 'both',
        min_version: '4.0.0',
        tags: ['Minecraft: Java Edition'],
        onload() {
            const text = getText();
            moreblock_export_action = new Action('export_moreblock_config_json', {
                name: text.actionName,
                description: text.actionDescription,
                icon: 'fa-cube',
                category: 'file',
                click: showExportDialog
            });
            moreblock_export_entity_action = new Action('export_moreblock_entity_config_json', {
                name: text.entityActionName,
                description: text.entityActionDescription,
                icon: 'fa-user',
                category: 'file',
                click: showEntityExportDialog
            });
            moreblock_generate_hitbox_action = new Action('generate_moreblock_hitbox', {
                name: text.hitboxActionName,
                description: text.hitboxActionDescription,
                icon: 'select_all',
                category: 'edit',
                click: showHitboxDialog
            });
            MenuBar.addAction(moreblock_export_action, 'file.export');
            MenuBar.addAction(moreblock_export_entity_action, 'file.export');
            MenuBar.addAction(moreblock_generate_hitbox_action, 'filter');
        },
        onunload() {
            if (moreblock_export_action) {
                moreblock_export_action.delete();
                moreblock_export_action = null;
            }
            if (moreblock_export_entity_action) {
                moreblock_export_entity_action.delete();
                moreblock_export_entity_action = null;
            }
            if (moreblock_generate_hitbox_action) {
                moreblock_generate_hitbox_action.delete();
                moreblock_generate_hitbox_action = null;
            }
        }
    });
})();
