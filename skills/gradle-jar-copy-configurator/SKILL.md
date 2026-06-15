---
name: "gradle-jar-copy-configurator"
description: "Sets up Gradle jar copy tasks driven by a root-level path config file. Invoke when user wants build.gradle to copy built jars to configurable target directories."
---

# Gradle Jar Copy Configurator

这个技能用于把“构建后复制 jar 到多个目标目录”的固定写死逻辑，改造成“由根目录配置文件驱动”的可维护方案。

适用触发场景：

- 用户希望把 `build.gradle` 里写死的 jar 复制路径提取到单独配置文件
- 用户希望在项目根目录维护一个“每行一个路径”的复制目标列表
- 用户希望 `build` 任务结束后，自动把产物复制到配置文件列出的多个目录
- 用户希望后续只改配置文件，不再频繁改 `build.gradle`
- 用户要求把这套逻辑落地到任意 Gradle Java 项目里

不适用场景：

- 用户只是想临时复制一次 jar，而不是改造构建流程
- 用户要做的是发布到 Maven、本地仓库或 CI 制品上传
- 用户项目不是 Gradle，或不存在类似 `build.gradle` 的构建入口

## 目标

完成任务后，应满足：

1. 项目根目录存在一个专门保存复制目标路径的配置文件
2. `build.gradle` 从该配置文件读取目标目录，而不是写死多个路径变量
3. 配置文件支持“一行一个路径”
4. 空行与注释行会被忽略
5. `build` 完成后会把 jar 复制到所有有效目标目录
6. 缺失目录时会跳过并打印提示，而不是让整个构建失败
7. 修改完成后执行构建验证

## 推荐文件约定

默认优先使用根目录文件：

```text
mod-copy-targets.txt
```

推荐格式：

```text
# 需要复制 jar 的目标目录
# 每行一个路径
F:/ProjectA/run/mods
F:/ProjectB/run/mods
```

规则：

- 一行一个绝对路径
- 去掉首尾空白后再处理
- 空行忽略
- 以 `#` 开头的行忽略

## 实施步骤

### 1. 先检查现有 `build.gradle`

先定位是否已经存在类似逻辑，例如：

- 写死的 `copyTargets`
- 手写多个 `copyJarToXxx` 任务
- `build.finalizedBy(...)`
- 复制 jar 的 `copy { from ... into ... }`

如果项目里已经有旧逻辑，优先做“最小改造”，不要顺手重写整个构建脚本。

### 2. 新建根目录配置文件

如果项目根目录还没有复制目标配置文件，就新建：

```text
mod-copy-targets.txt
```

写入当前已有的目标目录，保证用户原先的使用方式不丢失。

### 3. 把写死路径改为读配置文件

推荐在 `build.gradle` 中使用类似思路：

```groovy
def copyTargetsFile = rootProject.file('mod-copy-targets.txt')
def copyTargets = copyTargetsFile.exists()
        ? copyTargetsFile.readLines('UTF-8').collect { it.trim() }.findAll { it && !it.startsWith('#') }
        : []
```

要求：

- 从根目录读取配置文件
- 使用 `UTF-8`
- 读取后 `trim`
- 过滤空行和注释行

### 4. 动态注册复制任务

不要继续为每个路径手写一个固定任务名。应改为遍历路径集合，动态生成任务。

推荐结构：

```groovy
def copyJarTasks = copyTargets.withIndex().collect { targetPath, index ->
    registerCopyTask("copyJarToTarget${index + 1}", targetPath)
}

tasks.named('build') {
    finalizedBy(copyJarTasks)
}
```

这样后续新增或删除目标路径时，只需要改 `mod-copy-targets.txt`。

### 5. 保留安全判断

复制任务里应继续保留这些防护：

- 仅在目标平台允许时执行目标路径解析
- 目标目录不存在时跳过
- 目标不是目录时跳过
- 构建产物 jar 不存在时跳过
- 输出清晰日志，说明复制到了哪里，或为什么跳过

示例判断重点：

```groovy
onlyIf {
    def target = targetProvider.getOrNull()
    if (target == null || !target.exists() || !target.isDirectory()) {
        println "Skipping jar copy because target directory does not exist: ${targetPath}"
        return false
    }
    return true
}
```

### 6. 构建验证

修改完成后，必须运行项目对应的构建命令，例如：

```powershell
./gradlew.bat build
```

或：

```bash
./gradlew build
```

检查点：

- 构建是否成功
- 是否出现动态生成的复制任务
- jar 是否成功复制到配置文件列出的目标目录
- 不存在的目录是否被安全跳过

## 编辑原则

- 优先复用项目里现有的注册复制任务结构
- 不要顺手改无关的 Gradle 配置
- 不要把用户已有路径 silently 丢掉
- 不要引入复杂格式，除非用户明确要求改成 `properties`、`json` 或 `yaml`
- 默认维持 `txt` 方案，简单直接，方便用户手改

## 汇报建议

完成后向用户汇报时，建议说明：

1. 新建了哪个配置文件
2. `build.gradle` 改成了如何读取它
3. 配置文件的写法规则
4. 是否已经执行构建验证
5. 构建和复制是否成功

## 成功标准

任务完成后，应满足：

- 根目录已有复制目标配置文件
- `build.gradle` 不再写死多个目标目录常量
- `build` 会根据配置文件动态注册并执行复制任务
- 无效目录不会导致整个构建失败
- 已完成一次实际构建验证
