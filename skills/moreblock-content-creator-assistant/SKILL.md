---
name: "moreblock-content-creator-assistant"
description: "Assists MoreBlock creators with block/entity JSON drafting, asset organization, validation, and zip packaging. Invoke when user wants to create, repair, or package MoreBlock import content."
---

# MoreBlock Content Creator Assistant

这个技能用于辅助 MoreBlock 资源制作者处理“导入内容制作”这一整条链路，尤其适合下面几类任务：

- 为 MoreBlock 撰写或修复方块导入 JSON
- 为 MoreBlock 撰写或修复实体导入 JSON
- 整理 `geo`、`texture`、`animation`、`display` 等素材文件
- 按 MoreBlock 规则把文件夹整理成可直接导入的目录结构
- 把素材打成可投放到 `config/moreblock/block` 或 `config/moreblock/entity` 的 `zip`
- 在用户只给了模型、贴图、动画、截图、命名草稿时，补齐缺失配置

适用触发场景：

- 用户说“帮我写 MoreBlock 实体 JSON”
- 用户说“帮我整理这个方块包/实体包”
- 用户说“把这些素材收拾成能导入的 zip”
- 用户说“检查一下为什么导不进去”
- 用户给出 Blockbench 导出的素材，要求补配置、补命名、补页签或补打包

不适用场景：

- 普通 Minecraft 数据包、资源包，不是 MoreBlock 导入内容
- 纯 Java 代码开发任务，例如实体 AI 逻辑、注册器代码、渲染器代码重构
- 用户只想改某一个完全独立的 JSON 语法细节，且不涉及 MoreBlock 内容生产流程

## 你要先知道的项目事实

处理任务时，默认基于当前仓库已经存在的导入机制，不要凭空假设：

- MoreBlock 会在运行时读取 `config/moreblock/block`
- MoreBlock 也会在运行时读取 `config/moreblock/entity`
- 方块包和实体包都支持“直接放文件夹”与“直接放 zip”
- 实体至少需要 GeckoLib 的 `*.geo.json` 和一张 `png`
- 方块通常至少需要 `*.geo.json` 和 `png`，`*-display.json` 可选
- 实体导入成功后会自动注册实体类型和刷怪蛋
- Blockbench 工具脚本已经内置于仓库：`tools/blockbench/moreblock_blockbench_tools.js`
- 该工具脚本已经支持导出 MoreBlock 方块 JSON、实体 JSON，以及导出为可直接导入的 ZIP

如需校对依据，优先参考这些现有内容：

- `docs/中文-更多实体导入教程.md`
- `docs/中文-更多方块导入教程.md`
- `docs/中文使用教程.md`
- `src/main/java/me/sallos/moreblock/config/ImportedEntityPacks.java`
- `tools/blockbench/moreblock_blockbench_tools.js`

## 工作目标

当你完成任务时，交付物应尽量满足：

1. JSON 可以直接被 MoreBlock 识别
2. 文件名与 JSON 中引用的文件名能一一对应
3. 目录结构清晰，适合后续继续维护
4. 能直接作为文件夹投放，或进一步打成 `zip`
5. 用户没有明确要求的字段尽量不乱改
6. JSON 文件统一使用 `UTF-8` 无 BOM

## 总流程

### 1. 先判断内容类型

先确认当前任务属于哪一类：

- 方块导入包
- 实体导入包
- 混合整理任务
- 仅打包和校验任务

如果用户没说清楚，要先从文件名和素材特征判断：

- 有 `*.animation.json`、实体尺寸、AI、刷怪蛋颜色，通常是实体
- 有 `item_page`、`display`、`light_level`、坐下/躺下参数，通常是方块

### 2. 先看现有素材，再决定“补写”还是“修复”

不要一上来整包重写。先检查：

- 已有哪些 JSON
- 是否已有 `geo`
- 是否已有 `texture.png`
- 是否已有 `*.animation.json`
- 是否已有 `*-display.json`
- 文件名是否已经统一
- 用户是否已经手工写过一部分字段

如果用户已经写过配置，优先做最小改动。

### 3. 缺信息时先追问，不要硬编

以下信息缺失时，应优先向用户确认：

- `id`
- 中文名和英文名
- 是方块还是实体
- 目标投放目录是 `block` 还是 `entity`
- 是否需要 `item_page`
- 实体是否需要 `ai_template`
- 是否有动画文件
- 是否需要打包成 `zip`

如果只是可安全推导的内容，可以自行补：

- `geo` 通常取同目录 `*.geo.json`
- `texture` 通常可用 `texture.png`
- 有实体动画文件时再写 `animation`
- 没有实体动画文件时，不要硬写一个不存在的 `animation`

### 4. 写完后做结构核对

至少核对这些点：

- JSON 语法合法
- JSON 文件编码为 `UTF-8` 无 BOM
- 每个引用的文件都真实存在
- 文件夹或压缩包结构符合 MoreBlock 的读取规则
- 没把示例目录、说明文件误当成实际导入内容

## 实体任务规则

### 最小实体包结构

推荐结构：

```text
blue_slime/
├─ blue_slime.json
├─ blue_slime.geo.json
├─ blue_slime.animation.json
└─ texture.png
```

最少要求：

- 一个 `*.geo.json`
- 一张 `png`

可选内容：

- `*.animation.json`
- 一个实体配置 `*.json`

虽然仓库支持“只放模型和贴图时按目录推导基础定义”，但长期维护时，默认仍应建议用户保留显式配置 JSON。

### 实体 JSON 的核心字段

优先使用标准字段名：

- `id`
- `name.zh_cn`
- `name.en_us`
- `geo`
- `texture`
- `animation`
- `width`
- `height`
- `eye_height`
- `max_health`
- `movement_speed`
- `follow_range`
- `attack_damage`
- `armor`
- `knockback_resistance`
- `tracking_range`
- `update_interval`
- `ai_enabled`
- `ai_template`
- `animation_transition`
- `animation_states`
- `disable_vanilla_death_animation`
- `spawn_egg_primary_color`
- `spawn_egg_secondary_color`
- `show_in_moreblock_tab`
- `translucent`

除非是兼容旧配置，否则不要优先写别名字段。

### 实体动画处理规则

- 有 `*.animation.json` 时，再考虑写 `animation`
- 没有动画文件时，允许省略 `animation`
- `animation_states` 优先使用 `spawn`、`idle`、`walk`、`run`、`attack`、`hurt`、`die`
- 如果动画命名里已经有 `idle_1`、`attack_1` 这类后缀，允许继续按前缀分组写权重

### 实体 AI 处理规则

- 用户没有说明时，不要乱选复杂 AI
- 若用户只是想先跑通测试，优先给出稳定、常见的原版模板建议，如 `minecraft:zombie` 或其他用户指定模板
- 如果用户明确不要 AI，可写 `ai_enabled: false`

### 实体颜色与默认值

- 刷怪蛋颜色只有在用户需要时再认真补
- 不确定颜色时，先向用户确认，不要为了“完整”乱填
- 数值类字段没有可靠依据时，优先使用保守、接近示例的参数，不要写夸张数值

## 方块任务规则

### 常见方块包结构

推荐结构：

```text
blue_chair/
├─ blue_chair.json
├─ blue_chair.geo.json
├─ texture.png
└─ blue_chair-display.json
```

常见核心字段：

- `id`
- `name.zh_cn`
- `name.en_us`
- `geo`
- `texture`
- `display`
- `item_page`
- `light_level`
- `supports_sitting`
- `seat_height`
- `supports_lying`
- `lying_height`

### 方块整理原则

- `display` 缺失时，不要说一定导入失败，因为仓库允许缺省显示参数
- `supports_sitting` 和 `supports_lying` 不要同时设为 `true`
- `item_page` 既可以是字符串，也可以是对象
- 如果用户只要求挂到某个已有页签，优先用字符串
- 如果用户要求新建页签，才补完整对象，包括名称和图标

## 素材整理规则

### 文件名对齐

优先把下面几类内容整理为“内容 ID 对齐”的命名方式：

- 配置 JSON
- `geo`
- `animation`
- `display`
- 贴图

但要注意：

- 如果用户目录里已经稳定使用了 `texture.png`，不要为了追求整齐强行改成别的名字
- 改文件名时必须同步更新 JSON 引用
- 若用户已有大量外部引用，先提醒再改

### 素材缺失时的策略

- 缺 `geo`：不能假装可导入，直接指出缺少核心模型
- 缺贴图：不能打成可用内容，直接指出缺图
- 缺实体动画：可继续做基础实体包，但不要捏造动画文件
- 缺方块 `display`：可先继续，必要时说明会走默认显示参数

## 打包规则

### 目录与 zip

MoreBlock 支持：

- 直接放一个文件夹
- 直接放一个 `zip`
- `zip` 里再套一层同名文件夹
- 外层容器包里混合多个子文件夹或子 `zip`

但实际交付时，优先推荐：

1. 一个内容包对应一个清晰目录
2. 发布前再打成单独 `zip`
3. `zip` 文件名尽量使用 `UTF-8`

### 打包前检查

打包前至少核对：

- JSON 引用的 `geo`、`texture`、`display`、`animation` 是否都能找到
- 没把无关缓存文件、工程临时文件塞进包里
- 没把 `example`、说明文档误打成实际资源的一部分

## Blockbench 配合规则

如果用户的素材来源于 Blockbench，优先提醒或帮助其利用仓库内置脚本：

- `tools/blockbench/moreblock_blockbench_tools.js`

该脚本已支持：

- 导出 MoreBlock 方块配置 JSON
- 导出 MoreBlock 实体配置 JSON
- 导出 MoreBlock 可直接导入的 ZIP

因此当用户已经有 Blockbench 工程时，优先路线通常是：

1. 在 Blockbench 导出 `geo`
2. 用内置脚本导出 MoreBlock 配置 JSON
3. 让脚本或当前任务继续整理缺失资源
4. 最后整理目录或打成 `zip`

## 编辑原则

### 尽量少动无关内容

- 只修当前任务需要的字段
- 不要顺手统一所有命名风格，除非用户明确要求
- 不要顺手替用户“优化”数值平衡
- 不要把已有中文名、英文名、AI 模板全改掉

### 注释与说明

给用户汇报时，应明确说明：

- 处理的是方块包还是实体包
- 新建或修改了哪些文件
- 哪些字段是根据现有素材推导的
- 哪些字段仍需用户确认
- 最终应放进哪个目录
- 是否已经可直接打包或投放测试

## 推荐输出格式

完成任务后，建议向用户汇报：

1. 已整理好的目录或文件列表
2. 核心 JSON 字段摘要
3. 缺失素材或待确认项
4. 投放路径，例如 `config/moreblock/entity` 或 `config/moreblock/block`
5. 是否已整理为 `zip`

## 成功标准

任务完成后，应满足：

- MoreBlock 目标内容的 JSON 已补齐或修复
- 资源文件与 JSON 引用一致
- 目录结构清晰
- 文件编码正确
- 可直接作为导入文件夹使用，或可直接打包为 `zip`
