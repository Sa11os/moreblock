# MoreBlock

MoreBlock 是一个面向 Minecraft 1.20.1 Forge 的自定义方块导入模组。

它会在运行时读取 `config/moreblock/block` 目录中的方块包，并将对应的方块、物品、方块实体与客户端资源加载进游戏。方块包既可以是普通文件夹，也可以是 zip 压缩包，适合用于整合包扩展、服务器内容分发和私有项目中的自定义装饰方块管理。

## 特性

- 从 `config/moreblock/block` 自动扫描并导入自定义方块包
- 同时支持文件夹与 zip 压缩包两种载入方式
- 支持在外层包中继续包含多个子文件夹或子 zip
- 自动创建配置目录、示例配置和参数说明文件
- 根据导入内容在运行时生成并挂载客户端资源
- 提供客户端与服务端导入内容一致性校验
- 为导入方块提供独立创造模式页签
- 支持通过配置为导入方块启用右键坐下功能

## 环境要求

- Minecraft `1.20.1`
- Forge `47.4.20` 或兼容的 `47.x`
- GeckoLib `4.4.2+`

## 安装

1. 安装 Minecraft `1.20.1`
2. 安装 Forge `47.x`
3. 安装 GeckoLib 4
4. 将 `MoreBlock` 放入 `mods` 文件夹
5. 首次启动游戏，让模组自动创建 `config/moreblock/block`

## 快速开始

首次启动后，模组会生成以下目录：

```text
config/moreblock/block
```

将你的方块包放入该目录后，重新进入游戏即可完成扫描与导入。支持以下两种形式：

- 一个完整的方块包文件夹
- 一个完整的方块包 zip 压缩包

示例结构：

```text
config/moreblock/block/
├─ ExampleBlock/
│  ├─ example.json
│  ├─ example.geo.json
│  ├─ texture.png
│  └─ example-display.json
└─ more_blocks.zip
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

当前版本的配置已支持基础导入参数、亮度设置，以及导入方块的坐下交互相关参数。

## 联机说明

- 客户端与服务端都需要安装 MoreBlock
- 双方使用的导入方块包内容需要保持一致
- 如果检测到内容不一致，进入服务器时会收到提示

## 命令

- `/moreblock block list`：列出当前已识别的导入包
- `/moreblock block check`：检查手中物品对应的导入来源

## Blockbench 插件

仓库中包含配套的 Blockbench 工具脚本，位于：

```text
tools/blockbench/moreblock_blockbench_tools.js
```

该工具可用于：

- 生成 MoreBlock 配置 JSON
- 根据当前模型生成 MoreBlock 可识别的 hitbox 骨骼

## 适用场景

- 为整合包补充额外装饰方块
- 为服务器统一分发自定义建筑内容
- 为私有项目维护可复用的方块资源包

## 许可

`All Rights Reserved`
