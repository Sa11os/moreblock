# MoreBlock

MoreBlock 是一个面向 Minecraft 1.20.1 Forge 的自定义内容导入模组。

它会在运行时读取 `config/moreblock/block` 和 `config/moreblock/entity` 目录中的导入包，并将对应的方块、实体、物品与客户端资源加载进游戏。导入包既可以是普通文件夹，也可以是 zip 压缩包，适合用于整合包扩展、服务器内容分发和私有项目中的自定义内容管理。

## 特性

- 从 `config/moreblock/block` 自动扫描并导入自定义方块包
- 从 `config/moreblock/entity` 自动扫描并导入自定义实体包
- 同时支持文件夹与 zip 压缩包两种载入方式
- 支持在外层包中继续包含多个子文件夹或子 zip
- 自动创建配置目录、示例配置和参数说明文件
- 根据导入内容在运行时生成并挂载客户端资源
- 为导入实体自动生成刷怪蛋
- 提供客户端与服务端导入内容一致性校验
- 为导入方块提供独立创造模式页签
- 支持通过配置为导入方块启用右键坐下、躺下功能
- 提供外部 Mod 开发 API，可复用 MoreBlock 动态方块、资源绑定和交互回调能力

## 环境要求

- Minecraft `1.20.1`
- Forge `47.4.20` 或兼容的 `47.x`
- 前置模组：GeckoLib `4.4.2+`

## 安装

1. 安装 Minecraft `1.20.1`
2. 安装 Forge `47.x`
3. 安装 GeckoLib 4
4. 将 `MoreBlock` 放入 `mods` 文件夹
5. 首次启动游戏，让模组自动创建 `config/moreblock/block` 和 `config/moreblock/entity`

## 快速开始

首次启动后，模组会生成以下目录：

```text
config/moreblock/block
config/moreblock/entity
```

将你的方块包或实体包放入对应目录后，重新进入游戏即可完成扫描与导入。支持以下两种形式：

- 一个完整的方块包文件夹
- 一个完整的方块包 zip 压缩包
- 一个完整的实体包文件夹
- 一个完整的实体包 zip 压缩包

示例结构：

```text
config/moreblock/block/
├─ ExampleBlock/
│  ├─ example.json
│  ├─ example.geo.json
│  ├─ texture.png
│  └─ example-display.json
└─ more_blocks.zip

config/moreblock/entity/
├─ BlueSlime/
│  ├─ blue_slime.json
│  ├─ blue_slime.geo.json
│  ├─ blue_slime.animation.json
│  └─ texture.png
└─ more_entities.zip
```

## 方块包结构

一个最基础的方块包通常包含以下文件：

- 一个方块配置文件，例如 `example.json`
- 一个 GeckoLib 模型文件，例如 `example.geo.json`
- 一张贴图，例如 `texture.png`

可选文件：

- `*-display.json`：用于物品显示变换

模组启动时会自动扫描目录内容，识别其中有效的配置与资源并完成注册。

## 配置说明

模组会在配置目录中自动生成示例文件，用于说明当前版本支持的参数。

- `example.json`：标准 JSON 示例配置
- `example.md`：对应参数说明文档

当前版本的配置已支持：

- 方块导入参数、亮度设置，以及导入方块的坐下交互相关参数
- 实体导入参数、碰撞尺寸、同步范围和刷怪蛋颜色相关参数

## 联机说明

- 客户端与服务端都需要安装 MoreBlock
- 双方使用的导入方块包内容需要保持一致
- 如果检测到内容不一致，进入服务器时会收到提示

## 命令

- `/moreblock block list`：列出当前已识别的导入包
- `/moreblock block check`：检查手中物品对应的导入来源
- `/moreblock entity list`：列出当前已识别的导入实体
- `/moreblock entity check`：检查手中刷怪蛋对应的导入来源

## 实体导入

实体导入当前已经提供基础能力，目录为：

```text
config/moreblock/entity
```

基础版会自动完成：

- 扫描实体配置目录和 zip
- 解析 GeckoLib `geo`、贴图和可选动画文件
- 生成实体运行时资源包
- 注册动态实体类型
- 为每个导入实体自动生成刷怪蛋

完整说明见：

```text
docs/实体导入说明.md
```

## API 文档

MoreBlock 也提供给其他 Mod 调用的开发 API。外部 Mod 可以通过 `me.sallos.moreblock.api.MoreBlockApi` 注册使用 MoreBlock 动态模型渲染的方块，绑定 GeckoLib 模型、贴图、物品 display 资源，并按需启用亮度、半透明渲染、hitbox 骨骼、坐下、躺下等参数。

API 方块会注册到 MoreBlock 的动态方块与动态物品流程中，实际方块 id 使用 `moreblock:<ownerModId>_<id>` 形式；是否显示在 MoreBlock 创造模式页签中，可通过 `showInMoreBlockTab(...)` 控制。外部 Mod 如果需要自己的创造页签、语言文件或额外物品逻辑，仍然应该在自己的 Mod 内维护。

完整用法见：

```text
docs/api.md
```

API 文档包含以下内容：

- `MoreBlockApi.builder(...)` 与 `registerBlock(...)` 的使用方式
- `resourceBase(...)`、`geo(...)`、`texture(...)`、`display(...)` 的资源路径规则
- `hitboxBoneName(...)`、`translucent(...)`、`lightLevel(...)`、`sitting(...)`、`lying(...)` 等构造参数
- `MoreBlockEvents` 的右键、放置、移除回调
- `RegisteredMoreBlock` 获取注册名、方块对象和物品对象的方法
- 外部 Mod 的 `mods.toml` 依赖声明示例

## Blockbench 插件

制作模型时需要在 Blockbench 中安装 GeckoLib 插件，并将项目转换为 GeckoLib 模型后再导出 `geo.json`。MoreBlock 读取的是 GeckoLib 的 `*.geo.json` 模型文件，普通方块模型或未转换的 Blockbench 项目不能直接作为导入模型使用。

仓库中包含配套的 Blockbench 工具脚本，位于：

```text
tools/blockbench/moreblock_blockbench_tools.js
```

该工具可用于：

- 生成 MoreBlock 配置 JSON
- 根据当前模型生成 MoreBlock 可识别的 hitbox 骨骼
- 推荐使用高质量和 Greedy 模式

## 适用场景

- 为整合包补充额外装饰方块
- 为服务器统一分发自定义建筑内容
- 为私有项目维护可复用的方块资源包

## 许可

`All Rights Reserved`
