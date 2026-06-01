---
name: "moreblock-pack-id-item-page-normalizer"
description: "Normalizes MoreBlock block-pack ids and item_page config one file at a time. Invoke when user asks to unify pack prefixes, shared item pages, or preserve per-file progress."
---

# MoreBlock Pack Id And Item Page Normalizer

这个技能用于处理 MoreBlock 的方块包配置 JSON，重点是把一个家具包里的多个配置文件按统一规则整理为：

- 统一的方块 `id` 前缀
- 统一的 `item_page`
- 保留每个文件已经写好的其他差异字段
- 按“一个一个文件处理”的方式落地，而不是粗暴整包覆盖

适用场景：

- 用户要求给某个 MoreBlock 家具包统一 `id` 前缀
- 用户要求给整包统一一个物品页
- 用户明确说“一个一个文件处理”“不要批量处理”
- 用户强调每个配置文件编辑进度不一致，希望尽量少动现有字段

不适用场景：

- 用户要重做整包结构，允许批量重写
- 用户只问某一个单独 JSON 的写法，而不是整包统一
- 用户的目标不是 MoreBlock 方块包 JSON

## 目标

把目标家具包整理成下面这种结构：

1. 每个配置文件的 `id` 统一使用指定前缀
2. 整包共用一个 `item_page.id`
3. 选定一个基准文件保留完整 `item_page` 对象
4. 其余文件只写字符串形式的 `item_page`
5. 尽量不碰与本次任务无关的字段，比如：
   - `supports_sitting`
   - `supports_lying`
   - `seat_height`
   - `light_level`
   - 已经手工微调过的其他内容

## 工作原则

### 1. 必须逐文件检查

先逐个读取目标目录下的每个配置 JSON，不要直接整包批量替换。

检查项：

- 当前 `id` 是否已经带前缀
- 当前是否已有 `item_page`
- 当前 `item_page` 是字符串还是对象
- 是否已有某个文件被用户当作基准页
- 是否存在个别文件独有字段，不应该误改

### 2. 先确定统一规则

在动手前，先从用户指令中提炼出：

- 目标前缀，例如 `lddgjj_`、`jwjxxs_`
- 统一物品页中文名
- 统一物品页英文名
- 统一物品页 id，例如 `lddgjjjjb`
- 哪个文件作为基准文件
- 基准图标应该使用哪个方块 id

如果用户已经点名某个文件或某段 `item_page`，优先以此为准。

### 3. 基准文件与普通文件分开处理

推荐做法：

- 基准文件：
  - 修正方块 `id`
  - 写完整 `item_page` 对象
  - `item_page.icon` 使用修正后的方块 `id`
- 其他文件：
  - 修正方块 `id`
  - 只写 `"item_page": "<统一物品页id>"`

示例：

```json
"item_page": {
  "id": "jwjxxsjjb",
  "name": {
    "zh_cn": "近卫局休息室家具包",
    "en_us": "Guard Bureau Lounge Furniture Pack"
  },
  "icon": "jwjxxs_heisedujiaozhuo"
}
```

普通文件：

```json
"item_page": "jwjxxsjjb"
```

### 4. 不要顺手大改别的内容

除非用户明确要求，否则不要额外修改：

- `name.zh_cn`
- `name.en_us`
- `geo`
- `texture`
- 交互参数
- 发光参数
- 床、椅、沙发等特殊交互逻辑

尤其当用户强调“每个配置文件我撰写的进度不太一样”时，这条必须严格遵守。

## 推荐流程

### 步骤 1：列目录并逐个读取

对目标家具包目录下的每个配置 JSON 逐个读取。

注意：

- 只读真正的配置文件
- 排除 `*.geo.json`
- 排除 `*-display.json`

### 步骤 2：汇总现状

汇总时重点说明：

- 哪些文件还没前缀
- 哪些文件已经有统一物品页
- 哪些文件还没有 `item_page`
- 当前整包是否已经部分处理过

### 步骤 3：逐文件编辑

编辑时优先使用单文件补丁，按文件逐个改。

每次修改只做本文件所需最小变更：

- 只改 `id`
- 必要时补 `item_page`
- 基准文件改完整对象

### 步骤 4：批量核对结果，但不要批量重写

修改完成后可以用搜索核对：

- 所有文件的 `id` 是否都带统一前缀
- 所有文件的 `item_page` 是否都统一
- 基准文件是否仍保留完整对象

这里允许“批量检查”，但不建议“批量覆盖”。

### 步骤 5：抽查代表性文件

至少抽查三类：

- 基准页文件
- 原本没有 `item_page` 的文件
- 拥有特殊字段的文件

例如：

- 发光灯具
- 可坐下家具
- 可躺下家具

确认这些特殊字段没有被误伤。

### 步骤 6：做诊断或格式确认

如果环境支持，对代表性 JSON 做诊断检查，确保：

- JSON 语法正确
- 末尾逗号没有问题
- 文件仍是合法配置

## 输出建议

向用户汇报时应包含：

1. 已处理完成
2. 统一后的前缀
3. 统一后的物品页 id
4. 基准文件路径
5. 物品页英文名
6. 列出最终统一后的方块 id
7. 说明哪些原有特殊字段被保留未动

## 常见规则模板

### 模板：统一前缀

把：

```json
"id": "heisedujiaozhuo"
```

改成：

```json
"id": "jwjxxs_heisedujiaozhuo"
```

### 模板：基准文件

把：

```json
"item_page": {
  "id": "old_page",
  "name": {
    "zh_cn": "旧名字",
    "en_us": "old name"
  },
  "icon": "old_icon"
}
```

改成新的统一页对象。

### 模板：普通文件

把缺失的：

```json
}
```

补成：

```json
  "item_page": "jwjxxsjjb"
}
```

或把旧字符串：

```json
"item_page": "old_page"
```

改成：

```json
"item_page": "jwjxxsjjb"
```

## 英文名拟定建议

如果用户说“英文译名你自己拟就行”，优先使用自然、稳定、易懂的包名，不要直接乱拼音。

推荐模式：

- `Guard Bureau Lounge Furniture Pack`
- `Black Steel Safehouse Furniture Pack`
- `Rhodes Island Workshop Furniture Pack`

原则：

- 保持包级语义
- 用 `Furniture Pack` 结尾
- 不要过度缩写，除非用户明确要求

## 成功标准

任务完成后，应满足：

- 所有目标配置文件都带统一前缀
- 整包只使用一个物品页 id
- 只有一个基准文件保留完整 `item_page` 对象
- 其他文件使用字符串引用
- 用户原有差异字段未被误改
- 抽查与诊断通过
