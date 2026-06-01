let moreblock_export_action;
let moreblock_export_entity_action;
let moreblock_generate_hitbox_action;
let moreblock_ai_settings_action;

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
            itemPageId: '物品页 ID',
            itemPageChineseName: '物品页中文名',
            itemPageEnglishName: '物品页英文名',
            itemPageIcon: '物品页图标方块 ID',
            itemPageDescription: '只填物品页 ID 时会导出为字符串；如果补了名称或图标，就会导出为对象。',
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
            exportZipPackage: '导出为 ZIP 压缩包',
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
            hitboxTextureMissing: '当前项目没有可用贴图，hitbox 无法自动绑定透明像素，导出后仍可能显示异常',
            hitboxTextureReserved: '已为 hitbox 预留透明像素，避免导出后贴图覆盖',
            defaultChineseName: '自定义方块',
            defaultEnglishName: 'Custom Block',
            defaultEntityChineseName: '自定义实体',
            defaultEntityEnglishName: 'Custom Entity',
            aiSettingsActionName: 'MoreBlock AI 设置',
            aiSettingsActionDescription: '配置 MoreBlock 导出时使用的 AI 接口',
            aiSettingsDialogTitle: 'MoreBlock AI 设置',
            aiBaseUrl: 'AI 接口 URL',
            aiApiKey: 'AI API Key',
            aiModel: 'AI 模型',
            aiTimeoutSeconds: 'AI 超时秒数',
            aiTimeoutSecondsDescription: '请求超过该时长后会自动中止当前 AI 填写。默认 30 秒，范围 5-300 秒。',
            aiPlaintextWarning: '提示：API Key 会以明文保存在当前 Blockbench 的本地配置中，请妥善保管密钥，不要在共享设备上保存生产密钥。',
            aiSettingsHelp: '使用 OpenAI 兼容接口。DeepSeek 常见填写示例：URL 可填 `https://api.deepseek.com/v1`，模型可填 `deepseek-chat`。',
            aiSettingsSaved: 'MoreBlock AI 设置已保存',
            aiSettingsMissingTitle: '请先配置 MoreBlock AI',
            aiSettingsMissingMessage: 'AI 填写需要先在插件设置里填写接口 URL、API Key，以及可选模型名。',
            aiFillButton: 'AI 填写',
            aiSettingsButton: 'AI 设置',
            aiFillHint: 'AI 只会优先补全空值或默认值；明确填写过的字段会尽量保持不变。',
            aiFilling: 'AI 正在分析已填写字段并补全缺省项，请稍候...',
            aiFillSuccess: fields => `AI 已补全字段：${fields}`,
            aiFillNoChangesTitle: 'AI 未补全任何字段',
            aiFillNoChangesMessage: 'AI 没有找到可以可靠补全的字段。可能是当前表单已经完整，或现有信息不足以安全推断。',
            aiFillErrorTitle: 'AI 填写失败',
            aiFillErrorMessage: detail => `AI 请求失败：${detail}`,
            aiTimeoutMessage: seconds => `AI 请求超时（${seconds} 秒）`,
            aiResponseInvalid: 'AI 返回内容无法解析为有效 JSON',
            aiNoChoices: 'AI 没有返回可用结果',
            aiProgressTitle: 'AI 填写中',
            aiProgressMessage: 'AI 正在分析当前表单并填写缺省参数，请稍候...',
            aiProgressTimeout: seconds => `当前超时设置：${seconds} 秒`,
            aiProgressLockedHint: '填写进行中，当前导出表单已临时锁定，完成或异常结束前无法修改参数。'
        },
        en: {
            dialogTitle: 'Export MoreBlock Config JSON',
            entityDialogTitle: 'Export MoreBlock Entity Config JSON',
            zipExportType: 'MoreBlock ZIP Package',
            blockId: 'Block ID',
            entityId: 'Entity ID',
            chineseName: 'Chinese Name',
            englishName: 'English Name',
            itemPageId: 'Item Page ID',
            itemPageChineseName: 'Item Page Zh Name',
            itemPageEnglishName: 'Item Page En Name',
            itemPageIcon: 'Item Page Icon Block ID',
            itemPageDescription: 'If only the item page id is filled, it exports as a string. If a name or icon is provided, it exports as an object.',
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
            hitboxTextureMissing: 'No project texture is available. Hitbox cubes cannot bind to a transparent pixel automatically, so exported rendering may still be incorrect.',
            hitboxTextureReserved: 'Reserved a transparent texture pixel for hitbox cubes to prevent texture bleed after export',
            defaultChineseName: '自定义方块',
            defaultEnglishName: 'Custom Block',
            defaultEntityChineseName: 'Custom Entity',
            defaultEntityEnglishName: 'Custom Entity',
            aiSettingsActionName: 'MoreBlock AI Settings',
            aiSettingsActionDescription: 'Configure the AI endpoint used by MoreBlock export',
            aiSettingsDialogTitle: 'MoreBlock AI Settings',
            aiBaseUrl: 'AI Endpoint URL',
            aiApiKey: 'AI API Key',
            aiModel: 'AI Model',
            aiTimeoutSeconds: 'AI Timeout Seconds',
            aiTimeoutSecondsDescription: 'Automatically stops the current AI fill request after this duration. Default 30 seconds, range 5-300.',
            aiPlaintextWarning: 'Warning: The API key is stored in plain text in the current Blockbench local configuration. Keep it safe and avoid saving production keys on shared devices.',
            aiSettingsHelp: 'Uses an OpenAI-compatible API. Common DeepSeek example: URL `https://api.deepseek.com/v1`, model `deepseek-chat`.',
            aiSettingsSaved: 'MoreBlock AI settings saved',
            aiSettingsMissingTitle: 'Configure MoreBlock AI First',
            aiSettingsMissingMessage: 'AI fill requires an endpoint URL, API key, and optionally a model name in the plugin settings.',
            aiFillButton: 'AI Fill',
            aiSettingsButton: 'AI Settings',
            aiFillHint: 'AI mainly fills empty or default values and tries to keep explicitly entered fields unchanged.',
            aiFilling: 'AI is analyzing filled fields and completing missing values...',
            aiFillSuccess: fields => `AI filled: ${fields}`,
            aiFillNoChangesTitle: 'AI Did Not Fill Anything',
            aiFillNoChangesMessage: 'AI could not find any fields it could fill reliably. The form may already be complete, or the available context is insufficient.',
            aiFillErrorTitle: 'AI Fill Failed',
            aiFillErrorMessage: detail => `AI request failed: ${detail}`,
            aiTimeoutMessage: seconds => `AI request timed out (${seconds}s)`,
            aiResponseInvalid: 'AI response is not valid JSON',
            aiNoChoices: 'AI returned no usable result',
            aiProgressTitle: 'AI Filling',
            aiProgressMessage: 'AI is analyzing the current form and filling missing values...',
            aiProgressTimeout: seconds => `Current timeout: ${seconds}s`,
            aiProgressLockedHint: 'The export form is temporarily locked while AI fill is running. Parameters cannot be edited until completion or failure.'
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

    const AI_SETTINGS_STORAGE_KEY = `${PLUGIN_ID}:ai_settings`;
    const AI_DEFAULT_SETTINGS = {
        base_url: 'https://api.deepseek.com/v1',
        api_key: '',
        model: 'deepseek-chat',
        timeout_seconds: 30
    };

    function getLocalStorage() {
        if (typeof localStorage !== 'undefined' && localStorage) {
            return localStorage;
        }
        return null;
    }

    function loadAiSettings() {
        const storage = getLocalStorage();
        if (!storage) {
            return Object.assign({}, AI_DEFAULT_SETTINGS);
        }
        const saved = parseJsonSafe(storage.getItem(AI_SETTINGS_STORAGE_KEY));
        if (!saved || typeof saved !== 'object') {
            return Object.assign({}, AI_DEFAULT_SETTINGS);
        }
        return {
            base_url: String(saved.base_url || AI_DEFAULT_SETTINGS.base_url).trim() || AI_DEFAULT_SETTINGS.base_url,
            api_key: String(saved.api_key || '').trim(),
            model: String(saved.model || AI_DEFAULT_SETTINGS.model).trim() || AI_DEFAULT_SETTINGS.model,
            timeout_seconds: clampAiTimeoutSeconds(saved.timeout_seconds)
        };
    }

    function saveAiSettings(settings) {
        const storage = getLocalStorage();
        if (!storage) {
            return;
        }
        const payload = {
            base_url: String(settings.base_url || '').trim(),
            api_key: String(settings.api_key || '').trim(),
            model: String(settings.model || '').trim() || AI_DEFAULT_SETTINGS.model,
            timeout_seconds: clampAiTimeoutSeconds(settings.timeout_seconds)
        };
        storage.setItem(AI_SETTINGS_STORAGE_KEY, JSON.stringify(payload));
    }

    function clampAiTimeoutSeconds(value) {
        const numeric = Number.parseInt(value, 10);
        if (!Number.isFinite(numeric)) {
            return AI_DEFAULT_SETTINGS.timeout_seconds;
        }
        return Math.max(5, Math.min(300, numeric));
    }

    function showMessage(title, message, icon = 'info') {
        if (typeof Blockbench !== 'undefined' && Blockbench && typeof Blockbench.showMessageBox === 'function') {
            Blockbench.showMessageBox({
                title,
                message,
                icon
            });
            return;
        }
        if (typeof Blockbench !== 'undefined' && Blockbench && typeof Blockbench.showQuickMessage === 'function') {
            Blockbench.showQuickMessage(message, 4000);
        }
    }

    function normalizeOpenAiEndpoint(baseUrl) {
        const clean = String(baseUrl || '').trim().replace(/\/+$/, '');
        if (!clean) {
            return '';
        }
        return /\/chat\/completions$/i.test(clean) ? clean : `${clean}/chat/completions`;
    }

    function getDialogElement(dialogId) {
        if (!dialogId || typeof document === 'undefined' || !document) {
            return null;
        }
        return document.querySelector(`dialog#${dialogId}`);
    }

    function setDialogLocked(dialogId, locked) {
        const element = getDialogElement(dialogId);
        if (!element) {
            return;
        }
        element.querySelectorAll('input, select, textarea, button').forEach(control => {
            if (locked) {
                control.dataset.moreblockAiPrevDisabled = control.disabled ? '1' : '0';
                control.disabled = true;
            } else {
                const previous = control.dataset.moreblockAiPrevDisabled;
                if (previous === '0') {
                    control.disabled = false;
                }
                delete control.dataset.moreblockAiPrevDisabled;
            }
        });
        if (locked) {
            element.classList.add('moreblock_ai_locked');
        } else {
            element.classList.remove('moreblock_ai_locked');
        }
    }

    function createAiProgressDialog(timeoutSeconds) {
        const text = getText();
        return new Dialog({
            id: 'moreblock_ai_progress_dialog',
            title: text.aiProgressTitle,
            width: 420,
            buttons: [],
            lines: [`
                <style>
                    dialog#moreblock_ai_progress_dialog .dialog_content {
                        text-align: left;
                    }
                    dialog#moreblock_ai_progress_dialog .moreblock_ai_progress_box {
                        display: flex;
                        flex-direction: column;
                        gap: 10px;
                        padding: 6px 0;
                        line-height: 1.6;
                    }
                    dialog#moreblock_ai_progress_dialog .moreblock_ai_progress_title {
                        font-weight: 600;
                    }
                    dialog#moreblock_ai_progress_dialog .moreblock_ai_progress_hint {
                        opacity: 0.85;
                        font-size: 0.95em;
                    }
                </style>
                <div class="moreblock_ai_progress_box">
                    <div class="moreblock_ai_progress_title">${text.aiProgressMessage}</div>
                    <div>${text.aiProgressTimeout(timeoutSeconds)}</div>
                    <div class="moreblock_ai_progress_hint">${text.aiProgressLockedHint}</div>
                </div>
            `],
            onCancel() {
                return false;
            }
        });
    }

    async function withTimeout(promise, timeoutMs, timeoutMessage) {
        let timer = null;
        try {
            return await Promise.race([
                promise,
                new Promise((_, reject) => {
                    timer = setTimeout(() => reject(new Error(timeoutMessage)), timeoutMs);
                })
            ]);
        } finally {
            if (timer) {
                clearTimeout(timer);
            }
        }
    }

    function getDialogDefaultValues(type) {
        const text = getText();
        const baseName = getProjectBaseName();
        if (type === 'entity') {
            const metrics = getSuggestedEntityMetrics();
            return {
                id: sanitizeId(baseName, 'custom_entity'),
                zh_cn: baseName === 'custom_block' ? text.defaultEntityChineseName : baseName,
                en_us: text.defaultEntityEnglishName,
                geo: getDefaultGeoFile(),
                texture: 'texture.png',
                animation: getDefaultAnimationFile(),
                width: metrics.width,
                height: metrics.height,
                eye_height: metrics.eyeHeight,
                max_health: 20,
                movement_speed: 0.2,
                follow_range: 16,
                attack_damage: 2,
                armor: 0,
                knockback_resistance: 0.2,
                tracking_range: 8,
                update_interval: 3,
                ai_enabled: true,
                ai_template: 'minecraft:zombie',
                animation_transition: true,
                disable_vanilla_death_animation: true,
                translucent: true,
                show_in_moreblock_tab: true,
                spawn_egg_primary_color: '#4b7cf0',
                spawn_egg_secondary_color: '#d8e4ff',
                include_animation_states: true,
                export_zip_package: true
            };
        }
        return {
            id: sanitizeId(baseName),
            zh_cn: baseName === 'custom_block' ? text.defaultChineseName : baseName,
            en_us: text.defaultEnglishName,
            item_page_id: '',
            item_page_zh_cn: '',
            item_page_en_us: '',
            item_page_icon: '',
            geo: getDefaultGeoFile(),
            texture: 'texture.png',
            display: '',
            export_zip_package: true,
            light_level: 0,
            supports_sitting: false,
            seat_height: 0.5,
            supports_lying: false,
            lying_height: 0.5
        };
    }

    function getAiFieldMeta(type) {
        const text = getText();
        if (type === 'entity') {
            return {
                id: {label: text.entityId, type: 'id'},
                zh_cn: {label: text.chineseName, type: 'string'},
                en_us: {label: text.englishName, type: 'string'},
                geo: {label: text.geoFile, type: 'string'},
                texture: {label: text.textureFile, type: 'string'},
                animation: {label: text.animationFile, type: 'string'},
                width: {label: text.entityWidth, type: 'number', min: 0.1, max: 64},
                height: {label: text.entityHeight, type: 'number', min: 0.1, max: 64},
                eye_height: {label: text.eyeHeight, type: 'number', min: 0.05, max: 64},
                max_health: {label: text.maxHealth, type: 'number', min: 1, max: 2048},
                movement_speed: {label: text.movementSpeed, type: 'number', min: 0, max: 10},
                follow_range: {label: text.followRange, type: 'number', min: 0, max: 256},
                attack_damage: {label: text.attackDamage, type: 'number', min: 0, max: 2048},
                armor: {label: text.armor, type: 'number', min: 0, max: 2048},
                knockback_resistance: {label: text.knockbackResistance, type: 'number', min: 0, max: 1},
                tracking_range: {label: text.trackingRange, type: 'integer', min: 1, max: 256},
                update_interval: {label: text.updateInterval, type: 'integer', min: 1, max: 60},
                ai_enabled: {label: text.aiEnabled, type: 'boolean'},
                ai_template: {label: text.aiTemplate, type: 'string'},
                animation_transition: {label: text.animationTransition, type: 'boolean'},
                disable_vanilla_death_animation: {label: text.disableVanillaDeathAnimation, type: 'boolean'},
                translucent: {label: text.translucent, type: 'boolean'},
                show_in_moreblock_tab: {label: text.showInMoreBlockTab, type: 'boolean'},
                spawn_egg_primary_color: {label: text.spawnEggPrimaryColor, type: 'color'},
                spawn_egg_secondary_color: {label: text.spawnEggSecondaryColor, type: 'color'}
            };
        }
        return {
            id: {label: text.blockId, type: 'id'},
            zh_cn: {label: text.chineseName, type: 'string'},
            en_us: {label: text.englishName, type: 'string'},
            item_page_id: {label: text.itemPageId, type: 'id'},
            item_page_zh_cn: {label: text.itemPageChineseName, type: 'string'},
            item_page_en_us: {label: text.itemPageEnglishName, type: 'string'},
            item_page_icon: {label: text.itemPageIcon, type: 'id'},
            geo: {label: text.geoFile, type: 'string'},
            texture: {label: text.textureFile, type: 'string'},
            display: {label: text.displayFile, type: 'string'},
            light_level: {label: text.lightLevel, type: 'integer', min: 0, max: 15},
            supports_sitting: {label: text.canSit, type: 'boolean'},
            seat_height: {label: text.seatHeight, type: 'number', min: 0, max: 2},
            supports_lying: {label: text.canLie, type: 'boolean'},
            lying_height: {label: text.lyingHeight, type: 'number', min: 0, max: 2}
        };
    }

    function normalizeComparableValue(value, type) {
        if (type === 'boolean') {
            return Boolean(value);
        }
        if (type === 'number' || type === 'integer') {
            const numeric = Number(value);
            return Number.isFinite(numeric) ? numeric : null;
        }
        return String(value || '').trim();
    }

    function isAiReplaceablePlaceholder(type, key, value, fieldType) {
        if (fieldType !== 'string' && fieldType !== 'id' && fieldType !== 'color') {
            return false;
        }
        const normalizedValue = String(value || '').trim();
        if (!normalizedValue) {
            return false;
        }
        const text = getText();
        const placeholders = type === 'entity'
            ? {
                id: ['custom_entity'],
                zh_cn: [text.defaultEntityChineseName, 'Custom Entity'],
                en_us: [text.defaultEntityEnglishName, 'Custom Entity']
            }
            : {
                id: ['custom_block'],
                zh_cn: [text.defaultChineseName, '自定义方块'],
                en_us: [text.defaultEnglishName, 'Custom Block']
            };
        const candidates = placeholders[key];
        return Array.isArray(candidates) && candidates.includes(normalizedValue);
    }

    function getAiPromptFieldSummary(type, form) {
        const defaults = getDialogDefaultValues(type);
        const meta = getAiFieldMeta(type);
        return Object.keys(meta).map(key => {
            const field = meta[key];
            const currentValue = normalizeComparableValue(form[key], field.type);
            const defaultValue = normalizeComparableValue(defaults[key], field.type);
            const looksDefault = currentValue === defaultValue || isAiReplaceablePlaceholder(type, key, currentValue, field.type);
            const isEmpty = field.type === 'string' || field.type === 'id' || field.type === 'color'
                ? !String(currentValue || '').trim()
                : currentValue === null;
            return {
                key,
                label: field.label,
                type: field.type,
                current_value: currentValue,
                default_value: defaultValue,
                fillable: isEmpty || looksDefault,
                user_provided: !(isEmpty || looksDefault)
            };
        });
    }

    function extractJsonText(value) {
        const content = Array.isArray(value)
            ? value.map(item => (item && typeof item.text === 'string' ? item.text : '')).join('\n')
            : String(value || '');
        const fenced = content.match(/```(?:json)?\s*([\s\S]*?)```/i);
        if (fenced) {
            return fenced[1].trim();
        }
        const firstBrace = content.indexOf('{');
        const lastBrace = content.lastIndexOf('}');
        if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
            return content.slice(firstBrace, lastBrace + 1).trim();
        }
        return content.trim();
    }

    async function postJson(url, payload, headers) {
        if (typeof fetch === 'function') {
            const response = await fetch(url, {
                method: 'POST',
                headers: Object.assign({'Content-Type': 'application/json'}, headers || {}),
                body: JSON.stringify(payload)
            });
            const raw = await response.text();
            const data = parseJsonSafe(raw) || {raw};
            if (!response.ok) {
                const message = data && data.error && data.error.message
                    ? data.error.message
                    : `${response.status} ${response.statusText}`.trim();
                throw new Error(message || 'HTTP request failed');
            }
            return data;
        }
        if (typeof $ !== 'undefined' && $ && typeof $.ajax === 'function') {
            return $.ajax({
                url,
                type: 'POST',
                data: JSON.stringify(payload),
                contentType: 'application/json',
                headers: headers || {}
            });
        }
        throw new Error('Current Blockbench environment does not support network requests');
    }

    function sanitizeAiValue(value, field) {
        if (!field) {
            return undefined;
        }
        if (field.type === 'id') {
            const normalized = String(value || '').trim();
            return normalized ? sanitizeId(normalized, normalized) : undefined;
        }
        if (field.type === 'boolean') {
            return Boolean(value);
        }
        if (field.type === 'number' || field.type === 'integer') {
            const numeric = Number(value);
            if (!Number.isFinite(numeric)) {
                return undefined;
            }
            const clamped = Math.min(field.max, Math.max(field.min, numeric));
            return field.type === 'integer' ? Math.round(clamped) : roundTo(clamped, 3);
        }
        if (field.type === 'color') {
            const normalized = String(value || '').trim();
            return /^#([0-9a-f]{6})$/i.test(normalized) ? normalized.toLowerCase() : undefined;
        }
        const normalized = String(value || '').trim();
        return normalized || undefined;
    }

    async function requestAiCompletion(type, form) {
        const settings = loadAiSettings();
        const url = normalizeOpenAiEndpoint(settings.base_url);
        const fields = getAiPromptFieldSummary(type, form);
        const fillableFields = fields.filter(field => field.fillable).map(field => field.key);
        const payload = {
            model: settings.model || AI_DEFAULT_SETTINGS.model,
            temperature: 0.2,
            messages: [
                {
                    role: 'system',
                    content: [
                        'You fill Blockbench MoreBlock export forms.',
                        'Return JSON only.',
                        'Only fill keys from fillable_fields.',
                        'Never overwrite user_provided fields.',
                        'If a value cannot be inferred reliably, omit it.',
                        'For id use lowercase snake_case and only [a-z0-9_-].',
                        'Never set both supports_sitting and supports_lying to true.',
                        'Boolean or numeric suggestions must be conservative and based on strong evidence.'
                    ].join(' ')
                },
                {
                    role: 'user',
                    content: JSON.stringify({
                        task: 'Infer missing MoreBlock export form values from the already filled fields and project context.',
                        form_type: type,
                        project_context: {
                            project_name: getProjectBaseName(),
                            default_geo: getDefaultGeoFile(),
                            default_animation: type === 'entity' ? getDefaultAnimationFile() : null,
                            suggested_entity_metrics: type === 'entity' ? getSuggestedEntityMetrics() : null
                        },
                        fillable_fields: fillableFields,
                        fields,
                        response_schema: {
                            suggestions: {
                                id: 'optional',
                                zh_cn: 'optional',
                                en_us: 'optional'
                            },
                            reason: 'brief string'
                        }
                    })
                }
            ]
        };
        return postJson(url, payload, {
            Authorization: `Bearer ${settings.api_key}`
        });
    }

    async function applyAiFill(dialog, type) {
        const text = getText();
        const settings = loadAiSettings();
        if (!String(settings.base_url || '').trim() || !String(settings.api_key || '').trim()) {
            showMessage(text.aiSettingsMissingTitle, text.aiSettingsMissingMessage, 'warning');
            showAiSettingsDialog();
            return;
        }
        const dialogId = dialog && dialog.id ? dialog.id : null;
        const form = dialog.getFormResult();
        const meta = getAiFieldMeta(type);
        const fields = getAiPromptFieldSummary(type, form);
        const fillableKeys = fields.filter(field => field.fillable).map(field => field.key);
        if (!fillableKeys.length) {
            showMessage(text.aiFillNoChangesTitle, text.aiFillNoChangesMessage, 'info');
            return;
        }
        const timeoutSeconds = clampAiTimeoutSeconds(settings.timeout_seconds);
        const progressDialog = createAiProgressDialog(timeoutSeconds);
        setDialogLocked(dialogId, true);
        progressDialog.show();
        try {
            const result = await withTimeout(
                requestAiCompletion(type, form),
                timeoutSeconds * 1000,
                text.aiTimeoutMessage(timeoutSeconds)
            );
            const choice = result && Array.isArray(result.choices) ? result.choices[0] : null;
            if (!choice || !choice.message) {
                throw new Error(text.aiNoChoices);
            }
            const jsonText = extractJsonText(choice.message.content);
            const parsed = parseJsonSafe(jsonText);
            if (!parsed || typeof parsed !== 'object') {
                throw new Error(text.aiResponseInvalid);
            }
            const suggestions = parsed.suggestions && typeof parsed.suggestions === 'object'
                ? parsed.suggestions
                : parsed;
            const nextValues = {};
            const changedLabels = [];
            fillableKeys.forEach(key => {
                if (!Object.prototype.hasOwnProperty.call(suggestions, key)) {
                    return;
                }
                const sanitized = sanitizeAiValue(suggestions[key], meta[key]);
                if (typeof sanitized === 'undefined') {
                    return;
                }
                const currentValue = normalizeComparableValue(form[key], meta[key].type);
                const nextValue = normalizeComparableValue(sanitized, meta[key].type);
                if (currentValue === nextValue) {
                    return;
                }
                nextValues[key] = sanitized;
                changedLabels.push(meta[key].label);
            });
            if (!changedLabels.length) {
                showMessage(text.aiFillNoChangesTitle, text.aiFillNoChangesMessage, 'info');
                return;
            }
            dialog.setFormValues(nextValues);
            Blockbench.showQuickMessage(text.aiFillSuccess(changedLabels.join('、')), 5000);
        } catch (error) {
            const message = error && error.message ? error.message : String(error || 'Unknown error');
            showMessage(text.aiFillErrorTitle, text.aiFillErrorMessage(message), 'error');
        } finally {
            progressDialog.hide();
            setDialogLocked(dialogId, false);
        }
    }

    function showAiSettingsDialog() {
        const text = getText();
        const settings = loadAiSettings();
        const dialog = new Dialog({
            id: 'moreblock_ai_settings_dialog',
            title: text.aiSettingsDialogTitle,
            width: 620,
            form: {
                info: {
                    label: ' ',
                    nocolon: true,
                    type: 'info',
                    text: text.aiSettingsHelp
                },
                base_url: {
                    label: text.aiBaseUrl,
                    type: 'text',
                    value: settings.base_url
                },
                api_key: {
                    label: text.aiApiKey,
                    type: 'password',
                    value: settings.api_key
                },
                model: {
                    label: text.aiModel,
                    type: 'text',
                    value: settings.model || AI_DEFAULT_SETTINGS.model
                },
                timeout_seconds: {
                    label: text.aiTimeoutSeconds,
                    type: 'number',
                    value: clampAiTimeoutSeconds(settings.timeout_seconds),
                    min: 5,
                    max: 300,
                    step: 1,
                    description: text.aiTimeoutSecondsDescription
                },
                warning: {
                    label: ' ',
                    nocolon: true,
                    type: 'info',
                    text: text.aiPlaintextWarning
                }
            },
            onConfirm(form) {
                saveAiSettings(form);
                Blockbench.showQuickMessage(text.aiSettingsSaved, 3000);
            }
        });
        dialog.show();
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

        const itemPageId = sanitizeId(String(form.item_page_id || '').trim(), '');
        const itemPageZhCn = String(form.item_page_zh_cn || '').trim();
        const itemPageEnUs = String(form.item_page_en_us || '').trim();
        const itemPageIcon = sanitizeId(String(form.item_page_icon || '').trim(), '');
        if (itemPageId) {
            const itemPageHasDetails = itemPageZhCn || itemPageEnUs || itemPageIcon;
            if (itemPageHasDetails) {
                config.item_page = {
                    id: itemPageId
                };
                if (itemPageZhCn || itemPageEnUs) {
                    config.item_page.name = {};
                    if (itemPageZhCn) {
                        config.item_page.name.zh_cn = itemPageZhCn;
                    }
                    if (itemPageEnUs) {
                        config.item_page.name.en_us = itemPageEnUs;
                    }
                }
                if (itemPageIcon) {
                    config.item_page.icon = itemPageIcon;
                }
            } else {
                config.item_page = itemPageId;
            }
        }

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
            face.texture = false;
            face.enabled = false;
            face.material_name = '';
            if (face.uv) {
                face.uv = [0, 0, 0, 0];
            }
        });
    }

    function getPreferredTextureObject() {
        if (typeof Texture === 'undefined' || !Texture || !Array.isArray(Texture.all) || !Texture.all.length) {
            return null;
        }
        return (Texture.getDefault ? Texture.getDefault() : null) || Texture.selected || Texture.all[0] || null;
    }

    function getTextureCanvas(texture) {
        if (!texture) {
            return null;
        }
        if (typeof Painter !== 'undefined' && Painter && typeof Painter.getCanvas === 'function') {
            const painted = Painter.getCanvas(texture);
            if (painted) {
                return painted;
            }
        }
        if (texture.canvas) {
            return texture.canvas;
        }
        if (texture.ctx && texture.ctx.canvas) {
            return texture.ctx.canvas;
        }
        return null;
    }

    function findTransparentPixelInTexture(texture) {
        const canvas = getTextureCanvas(texture);
        if (!canvas) {
            return null;
        }
        const context = canvas.getContext('2d');
        if (!context) {
            return null;
        }
        const width = Math.max(1, canvas.width | 0);
        const height = Math.max(1, canvas.height | 0);
        const image = context.getImageData(0, 0, width, height);
        for (let y = height - 1; y >= 0; y--) {
            for (let x = width - 1; x >= 0; x--) {
                const alphaIndex = ((y * width) + x) * 4 + 3;
                if (image.data[alphaIndex] === 0) {
                    return {x, y, reserved: false};
                }
            }
        }
        return null;
    }

    function ensureTransparentPixelForHitbox(texture) {
        const existing = findTransparentPixelInTexture(texture);
        if (existing) {
            return existing;
        }
        const canvas = getTextureCanvas(texture);
        if (!canvas) {
            return null;
        }
        const x = Math.max(0, (canvas.width | 0) - 1);
        const y = Math.max(0, (canvas.height | 0) - 1);
        if (typeof texture.edit === 'function') {
            texture.edit(editCanvas => {
                const context = editCanvas.getContext('2d');
                if (!context) {
                    return;
                }
                context.clearRect(x, y, 1, 1);
            });
            if (typeof texture.apply === 'function') {
                texture.apply(true);
            }
            return {x, y, reserved: true};
        }
        return null;
    }

    function getTextureReference(texture) {
        if (!texture) {
            return false;
        }
        return texture.uuid || texture.id || texture.name || false;
    }

    function applyTransparentPixelUv(cube, texture, pixel) {
        if (!cube || !cube.faces || !texture || !pixel) {
            clearCubeFaces(cube);
            return;
        }
        const textureReference = getTextureReference(texture);
        Object.keys(cube.faces).forEach(key => {
            const face = cube.faces[key];
            if (!face) {
                return;
            }
            face.texture = textureReference;
            face.enabled = true;
            face.material_name = '';
            face.rotation = 0;
            face.uv = [pixel.x, pixel.y, pixel.x + 1, pixel.y + 1];
        });
        cube.autouv = 0;
        cube.box_uv = false;
        cube.mirror_uv = false;
    }

    function createEmptyHitboxFaces() {
        return {
            north: {texture: false, enabled: false, uv: [0, 0, 0, 0]},
            east: {texture: false, enabled: false, uv: [0, 0, 0, 0]},
            south: {texture: false, enabled: false, uv: [0, 0, 0, 0]},
            west: {texture: false, enabled: false, uv: [0, 0, 0, 0]},
            up: {texture: false, enabled: false, uv: [0, 0, 0, 0]},
            down: {texture: false, enabled: false, uv: [0, 0, 0, 0]}
        };
    }

    function createHitboxCubes(boxes) {
        const text = getText();
        const hitboxTexture = getPreferredTextureObject();
        const transparentPixel = hitboxTexture ? ensureTransparentPixelForHitbox(hitboxTexture) : null;
        Undo.initEdit({
            elements: Cube.all.slice(),
            outliner: true,
            textures: hitboxTexture ? [hitboxTexture] : [],
            bitmap: Boolean(hitboxTexture)
        });
        removeExistingHitboxGroup();
        const hitboxGroup = new Group({
            name: 'hitbox',
            origin: [0, 0, 0]
        }).init();
        hitboxGroup.addTo('root');
        boxes.forEach((box, index) => {
            const hitboxCube = new Cube().extend({
                name: boxes.length === 1 ? 'hitbox' : `hitbox_${index + 1}`,
                from: [box.minX, box.minY, box.minZ],
                to: [box.maxX, box.maxY, box.maxZ],
                origin: [0, 0, 0],
                autouv: 0,
                faces: createEmptyHitboxFaces()
            }).init();
            applyTransparentPixelUv(hitboxCube, hitboxTexture, transparentPixel);
            hitboxCube.addTo(hitboxGroup);
        });
        hitboxGroup.openUp();
        Canvas.updateAll();
        Undo.finishEdit('Generate MoreBlock hitbox');
        if (!hitboxTexture || !transparentPixel) {
            Blockbench.showQuickMessage(text.hitboxTextureMissing, 5000);
        } else if (transparentPixel.reserved) {
            Blockbench.showQuickMessage(text.hitboxTextureReserved, 4000);
        }
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
        const defaults = getDialogDefaultValues('block');
        const dialog = new Dialog({
            id: 'moreblock_config_export_dialog',
            title: text.dialogTitle,
            width: 560,
            form: {
                id: {
                    label: text.blockId,
                    type: 'text',
                    value: defaults.id
                },
                zh_cn: {
                    label: text.chineseName,
                    type: 'text',
                    value: defaults.zh_cn
                },
                en_us: {
                    label: text.englishName,
                    type: 'text',
                    value: defaults.en_us
                },
                item_page_id: {
                    label: text.itemPageId,
                    type: 'text',
                    value: defaults.item_page_id
                },
                item_page_zh_cn: {
                    label: text.itemPageChineseName,
                    type: 'text',
                    value: defaults.item_page_zh_cn
                },
                item_page_en_us: {
                    label: text.itemPageEnglishName,
                    type: 'text',
                    value: defaults.item_page_en_us
                },
                item_page_icon: {
                    label: text.itemPageIcon,
                    type: 'text',
                    value: defaults.item_page_icon,
                    description: text.itemPageDescription
                },
                geo: {
                    label: text.geoFile,
                    type: 'text',
                    value: defaults.geo
                },
                texture: {
                    label: text.textureFile,
                    type: 'text',
                    value: defaults.texture
                },
                display: {
                    label: text.displayFile,
                    type: 'text',
                    value: defaults.display
                },
                ai_hint: {
                    label: ' ',
                    nocolon: true,
                    type: 'info',
                    text: text.aiFillHint
                },
                ai_actions: {
                    label: ' ',
                    nocolon: true,
                    type: 'buttons',
                    buttons: [text.aiFillButton, text.aiSettingsButton],
                    click(index) {
                        if (index === 0) {
                            applyAiFill(dialog, 'block');
                            return;
                        }
                        showAiSettingsDialog();
                    }
                },
                export_zip_package: {
                    label: text.exportZipPackage,
                    type: 'checkbox',
                    value: defaults.export_zip_package,
                    description: text.exportZipPackageDescription
                },
                light_level: {
                    label: text.lightLevel,
                    type: 'number',
                    value: defaults.light_level,
                    min: 0,
                    max: 15
                },
                supports_sitting: {
                    label: text.canSit,
                    type: 'checkbox',
                    value: defaults.supports_sitting
                },
                seat_height: {
                    label: text.seatHeight,
                    type: 'number',
                    value: defaults.seat_height,
                    min: 0,
                    max: 2,
                    step: 0.05
                },
                supports_lying: {
                    label: text.canLie,
                    type: 'checkbox',
                    value: defaults.supports_lying
                },
                lying_height: {
                    label: text.lyingHeight,
                    type: 'number',
                    value: defaults.lying_height,
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
        const defaults = getDialogDefaultValues('entity');
        const dialog = new Dialog({
            id: 'moreblock_entity_config_export_dialog',
            title: text.entityDialogTitle,
            width: 640,
            form: {
                id: {
                    label: text.entityId,
                    type: 'text',
                    value: defaults.id
                },
                zh_cn: {
                    label: text.chineseName,
                    type: 'text',
                    value: defaults.zh_cn
                },
                en_us: {
                    label: text.englishName,
                    type: 'text',
                    value: defaults.en_us
                },
                geo: {
                    label: text.geoFile,
                    type: 'text',
                    value: defaults.geo
                },
                texture: {
                    label: text.textureFile,
                    type: 'text',
                    value: defaults.texture
                },
                animation: {
                    label: text.animationFile,
                    type: 'text',
                    value: defaults.animation
                },
                ai_hint: {
                    label: ' ',
                    nocolon: true,
                    type: 'info',
                    text: text.aiFillHint
                },
                ai_actions: {
                    label: ' ',
                    nocolon: true,
                    type: 'buttons',
                    buttons: [text.aiFillButton, text.aiSettingsButton],
                    click(index) {
                        if (index === 0) {
                            applyAiFill(dialog, 'entity');
                            return;
                        }
                        showAiSettingsDialog();
                    }
                },
                width: {
                    label: text.entityWidth,
                    type: 'number',
                    value: defaults.width,
                    min: 0.1,
                    max: 64,
                    step: 0.01
                },
                height: {
                    label: text.entityHeight,
                    type: 'number',
                    value: defaults.height,
                    min: 0.1,
                    max: 64,
                    step: 0.01
                },
                eye_height: {
                    label: text.eyeHeight,
                    type: 'number',
                    value: defaults.eye_height,
                    min: 0.05,
                    max: 64,
                    step: 0.01
                },
                max_health: {
                    label: text.maxHealth,
                    type: 'number',
                    value: defaults.max_health,
                    min: 1,
                    max: 2048,
                    step: 1
                },
                movement_speed: {
                    label: text.movementSpeed,
                    type: 'number',
                    value: defaults.movement_speed,
                    min: 0,
                    max: 10,
                    step: 0.01
                },
                follow_range: {
                    label: text.followRange,
                    type: 'number',
                    value: defaults.follow_range,
                    min: 0,
                    max: 256,
                    step: 1
                },
                attack_damage: {
                    label: text.attackDamage,
                    type: 'number',
                    value: defaults.attack_damage,
                    min: 0,
                    max: 2048,
                    step: 0.5
                },
                armor: {
                    label: text.armor,
                    type: 'number',
                    value: defaults.armor,
                    min: 0,
                    max: 2048,
                    step: 0.5
                },
                knockback_resistance: {
                    label: text.knockbackResistance,
                    type: 'number',
                    value: defaults.knockback_resistance,
                    min: 0,
                    max: 1,
                    step: 0.05
                },
                tracking_range: {
                    label: text.trackingRange,
                    type: 'number',
                    value: defaults.tracking_range,
                    min: 1,
                    max: 256,
                    step: 1
                },
                update_interval: {
                    label: text.updateInterval,
                    type: 'number',
                    value: defaults.update_interval,
                    min: 1,
                    max: 60,
                    step: 1
                },
                ai_enabled: {
                    label: text.aiEnabled,
                    type: 'checkbox',
                    value: defaults.ai_enabled
                },
                ai_template: {
                    label: text.aiTemplate,
                    type: 'text',
                    value: defaults.ai_template
                },
                animation_transition: {
                    label: text.animationTransition,
                    type: 'checkbox',
                    value: defaults.animation_transition
                },
                disable_vanilla_death_animation: {
                    label: text.disableVanillaDeathAnimation,
                    type: 'checkbox',
                    value: defaults.disable_vanilla_death_animation
                },
                translucent: {
                    label: text.translucent,
                    type: 'checkbox',
                    value: defaults.translucent
                },
                show_in_moreblock_tab: {
                    label: text.showInMoreBlockTab,
                    type: 'checkbox',
                    value: defaults.show_in_moreblock_tab
                },
                spawn_egg_primary_color: {
                    label: text.spawnEggPrimaryColor,
                    type: 'color',
                    value: defaults.spawn_egg_primary_color
                },
                spawn_egg_secondary_color: {
                    label: text.spawnEggSecondaryColor,
                    type: 'color',
                    value: defaults.spawn_egg_secondary_color
                },
                include_animation_states: {
                    label: text.includeAnimationStates,
                    type: 'checkbox',
                    value: defaults.include_animation_states,
                    description: text.includeAnimationStatesDescription
                },
                export_zip_package: {
                    label: text.exportZipPackage,
                    type: 'checkbox',
                    value: defaults.export_zip_package,
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
        version: '2.2.0',
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
            moreblock_ai_settings_action = new Action('moreblock_ai_settings', {
                name: text.aiSettingsActionName,
                description: text.aiSettingsActionDescription,
                icon: 'settings',
                category: 'file',
                click: showAiSettingsDialog
            });
            MenuBar.addAction(moreblock_export_action, 'file.export');
            MenuBar.addAction(moreblock_export_entity_action, 'file.export');
            MenuBar.addAction(moreblock_ai_settings_action, 'file.export');
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
            if (moreblock_ai_settings_action) {
                moreblock_ai_settings_action.delete();
                moreblock_ai_settings_action = null;
            }
        }
    });
})();
