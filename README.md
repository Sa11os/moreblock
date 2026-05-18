# MoreBlock

MoreBlock 是一个面向 Minecraft 1.20.1 Forge 的实用模组。

它做的事情很直接：从 `config/moreblock/block` 目录读取你准备好的方块包，把它们注册进游戏，并在客户端挂载对应资源。你可以把内容做成文件夹，也可以直接打成 zip 扔进去。

这个模组适合两种用法：

- 给整合包补充自定义装饰方块
- 给服务器或私有包做统一的扩展方块内容

## 基本信息

- 模组名：MoreBlock
- 模组 ID：`moreblock`
- 作者：Sallos
- 适用版本：Minecraft `1.20.1`
- Forge：`47.x`
- 依赖：`GeckoLib 4`
- 授权：`All Rights Reserved`

## 功能概览

- 从 `config/moreblock/block` 自动读取导入方块包
- 支持直接读取文件夹
- 支持直接读取 zip 压缩包
- 支持在一个外层包里继续嵌套多个子文件夹或子 zip
- 自动创建配置目录和说明文件
- 自动生成运行时资源包并挂载到客户端
- 提供客户端与服务端导入包一致性校验
- 在创造模式中提供 `MoreBlock-更多物品` 页签

## 安装说明

1. 安装 Minecraft `1.20.1`
2. 安装 Forge `47.x`
3. 安装 GeckoLib 4
4. 把 `MoreBlock` 放进 `mods` 文件夹
5. 启动一次游戏，让模组自动生成 `config/moreblock/block`

## 使用方法

启动游戏后，模组会自动创建这个目录：

```text
config/moreblock/block
```

你可以把导入内容按下面两种方式放进去：

- 直接放一个文件夹
- 直接放一个 zip 压缩包

常见示例：

```text
config/moreblock/block/
├─ 示例方块/
│  ├─ safebox.json
│  ├─ safebox2.geo.json
│  ├─ texture.png
│  └─ safebox2-display.json
└─ 更多方块.zip
```

模组会在启动时扫描这个目录，识别其中的有效内容并导入。

## 导入包说明

一个最基本的导入包通常至少需要：

- 一个方块配置文件
- 一个 `.geo.json` 模型文件
- 一张贴图

可选文件：

- `*-display.json` 用于物品显示变换

如果你已经有现成资源包，建议先用一个小方块做测试，确认命名和结构无误后再批量导入。

## 命令

- `/moreblock block list`：列出当前已识别的导入包
- `/moreblock block check`：检查手中物品对应的导入来源

## 联机说明

- 客户端和服务端都需要安装 MoreBlock
- 双方的导入方块包需要保持一致
- 如果配置不一致，进入服务器时会直接给出提示

## 发布页可用简介

下面这段可以直接作为发布页简介使用：

> MoreBlock 是一个支持从配置目录导入自定义方块内容的 Forge 模组。它可以读取文件夹或 zip 形式的方块包，在运行时注册方块、物品、方块实体与资源，并提供客户端和服务端的一致性校验，适合整合包、私服和自定义内容扩展。

## 说明

- 当前版本主要围绕“导入更多方块”这件事
- 配置目录里的 `README.txt` 和 `.keep` 只是说明与占位文件，不参与导入
- 如果导入后没显示，先检查目录位置、文件命名和 zip 内部结构
