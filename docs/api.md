# MoreBlock API 使用说明

这份文档写给想在自己 mod 里复用 MoreBlock 自定义方块渲染、模型、贴图和交互底层能力的开发者。

MoreBlock API 不负责替你的 mod 决定物品怎么注册、放到哪个创造模式页签，也不替你管理自己的内容分类。API 只提供一个底层方块注册与资源绑定工具：你的 mod 准备资源，调用 API 注册 MoreBlock 动态方块，然后由你的 mod 自己注册对应 `BlockItem`、语言文件和创造页签。

## 适用版本

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- MoreBlock 当前项目版本

使用方需要在自己的 `mods.toml` 里依赖 MoreBlock，资源文件仍然放在自己 mod 的 `resources` 目录下。

## API 负责什么

- 注册一个使用 MoreBlock 动态模型渲染的方块
- 绑定 GeckoLib `geo.json` 模型资源
- 绑定贴图资源
- 绑定可选的物品 display 模型资源
- 提供坐下、躺下等基础交互参数
- 提供右键、放置、移除事件回调

## API 不负责什么

- 不自动帮外部 mod 注册 `BlockItem`
- 不自动把外部 mod 的物品放入 MoreBlock 创造模式页签
- 不替外部 mod 维护语言文件
- 不替外部 mod 决定物品属性、堆叠数量、稀有度、创造栏位置

如果需要物品，请在你的 mod 里用自己的 `DeferredRegister<Item>` 注册。要放哪个创造页签，也在你的 mod 里自己处理。

## 基本流程

1. 在自己的 mod 资源目录准备 GeckoLib 模型、贴图和可选的物品 display 文件。
2. 在自己的 mod 构造阶段调用 `MoreBlockApi.builder(...).register()` 注册动态方块。
3. 在自己的 mod 里注册 `BlockItem`，绑定上一步拿到的方块。
4. 在自己的 mod 里决定要不要把物品加入某个创造模式页签。
5. 如需交互逻辑，通过 `MoreBlockEvents` 注册回调。

## 资源目录建议

假设你的 modid 是 `examplemod`，要注册一个 `blue_chair`：

```text
src/main/resources/assets/examplemod/
├─ geo/block/blue_chair.geo.json
├─ textures/block/blue_chair.png
├─ models/item/blue_chair.json
└─ lang/zh_cn.json
```

其中：

- `geo/block/blue_chair.geo.json` 是 GeckoLib 模型。
- `textures/block/blue_chair.png` 是贴图。
- `models/item/blue_chair.json` 是物品 display 模型，可选但建议准备。
- 语言文件由你的 mod 自己维护。

`resourceBase("geo/block/blue_chair")` 会自动推导：

- geo：`examplemod:geo/block/blue_chair.geo.json`
- texture：`examplemod:textures/block/blue_chair.png`
- display：`examplemod:models/item/blue_chair.json`

如果你的贴图不放在这个自动路径，建议显式调用 `geo(...)`、`texture(...)` 和 `display(...)`。

## 注册动态方块

在你的主类构造阶段或静态初始化阶段准备 `RegisteredMoreBlock`：

```java
package com.example.examplemod;

import me.sallos.moreblock.api.MoreBlockApi;
import me.sallos.moreblock.api.RegisteredMoreBlock;
import net.minecraftforge.fml.common.Mod;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";

    public static final RegisteredMoreBlock BLUE_CHAIR_BLOCK = MoreBlockApi.builder(MODID, "blue_chair")
            .name("蓝色椅子", "Blue Chair")
            .resourceBase("geo/block/blue_chair")
            .sitting(0.45d)
            .lightLevel(0)
            .register();

    public ExampleMod() {
    }
}
```

注册后实际方块 id 类似：

```text
moreblock:examplemod_blue_chair
```

如果同名已存在，MoreBlock 会自动追加序号，避免重复注册名直接冲突。

## 在你的 mod 里注册物品

MoreBlock 不会替 API 方块注册物品。你可以用自己的 `DeferredRegister<Item>` 注册：

```java
package com.example.examplemod;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ExampleItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExampleMod.MODID);

    public static final RegistryObject<Item> BLUE_CHAIR = REGISTRY.register("blue_chair", () ->
            new BlockItem(ExampleMod.BLUE_CHAIR_BLOCK.block().orElseThrow().get(), new Item.Properties())
    );

    private ExampleItems() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
```

然后在你的 mod 主类构造方法里注册你自己的物品表：

```java
public ExampleMod(FMLJavaModLoadingContext context) {
    IEventBus modBus = context.getModEventBus();
    ExampleItems.register(modBus);
}
```

这样物品属于你的 mod，由你的 mod 控制属性、语言键、创造页签和后续逻辑。

## 自己决定创造模式页签

如果你想把物品放入自己的创造页签，可以按 Forge 正常方式写，例如：

```java
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ExampleCreativeTabEvents {
    private ExampleCreativeTabEvents() {
    }

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ExampleItems.BLUE_CHAIR.get());
        }
    }
}
```

也可以注册自己的 `CreativeModeTab`。MoreBlock 不会把 API 注册出来的方块物品自动加入 MoreBlock 页签。

## Builder 参数

| 方法 | 说明 |
| --- | --- |
| `builder(ownerModId, id)` | 创建注册构造器。`ownerModId` 通常传你的 modid，`id` 传方块短名。 |
| `name(zhCnName, enUsName)` | 设置中英文显示名。当前 API 会保存名称，后续可用于查询与事件判断。 |
| `geo(ResourceLocation)` | 设置 GeckoLib 模型资源。 |
| `texture(ResourceLocation)` | 设置贴图资源。 |
| `display(ResourceLocation)` | 设置物品 display 模型资源，可选。 |
| `resourceBase(path)` | 按固定规则快速推导资源路径，路径不符合时不建议使用。 |
| `lightLevel(value)` | 设置亮度，范围会被限制在 0 到 15。 |
| `sitting(height)` | 允许右键坐下，并设置座位高度。 |
| `lying(height, rotationCompensation)` | 允许右键躺下，第二个参数按 90 度为单位修正躺下方向。 |
| `register()` | 提交动态方块定义。 |

## 绑定右键事件

```java
import me.sallos.moreblock.api.event.MoreBlockEvents;
import me.sallos.moreblock.api.event.MoreBlockInteractionResult;
import net.minecraft.network.chat.Component;

MoreBlockEvents.onUseBlock((definition, state, level, pos, player, hand, hit) -> {
    if (!definition.registryName().equals("examplemod_blue_chair")) {
        return MoreBlockInteractionResult.PASS;
    }

    if (!level.isClientSide) {
        player.displayClientMessage(Component.literal("你点了蓝色椅子"), true);
    }
    return MoreBlockInteractionResult.PASS;
});
```

返回值含义：

| 返回值 | 行为 |
| --- | --- |
| `PASS` | 不拦截，继续执行 MoreBlock 默认逻辑，比如坐下或躺下。 |
| `SUCCESS` | 拦截并返回成功。 |
| `CONSUME` | 拦截并消费交互。 |
| `FAIL` | 拦截并返回失败。 |

如果你只想监听，不想影响坐下、躺下等默认行为，返回 `PASS`。

## 绑定放置和移除事件

```java
MoreBlockEvents.onPlaceBlock((definition, level, pos, state, placer, stack) -> {
    if (!definition.registryName().equals("examplemod_blue_chair")) {
        return;
    }
});

MoreBlockEvents.onRemoveBlock((definition, level, pos, state, newState, movedByPiston) -> {
    if (!definition.registryName().equals("examplemod_blue_chair")) {
        return;
    }
});
```

这两个回调不会改变方块默认行为，适合写计数、同步数据、播放效果、触发自定义逻辑。

## 获取注册对象

`register()` 返回 `RegisteredMoreBlock`，可以拿到注册名和方块 `RegistryObject`：

```java
RegisteredMoreBlock block = MoreBlockApi.builder(MODID, "blue_chair")
        .name("蓝色椅子", "Blue Chair")
        .resourceBase("geo/block/blue_chair")
        .register();

block.block().ifPresent(registryObject -> {
    var minecraftBlock = registryObject.get();
});
```

注意：`RegistryObject#get()` 只能在 Forge 注册完成后安全调用。构造阶段只保存 `RegisteredMoreBlock` 即可，不要太早取实体对象。

## 依赖声明示例

`mods.toml` 中添加：

```toml
[[dependencies."examplemod"]]
modId = "moreblock"
mandatory = true
versionRange = "[当前 MoreBlock 版本,)"
ordering = "AFTER"
side = "BOTH"
```

如果你的方块资源用到了 GeckoLib 模型，客户端也需要有 GeckoLib。

## 常见问题

### 为什么我的模型是紫黑块？

先检查 `geo(...)`、`texture(...)` 的命名空间和路径是否和 `resources/assets/<modid>/...` 对得上。`ResourceLocation` 里的路径不需要写 `assets/<modid>` 这一级。

### 为什么创造栏里看不到物品？

API 不会自动注册物品，也不会自动把物品加入 MoreBlock 页签。请确认你的 mod 已经注册了自己的 `BlockItem`，并把它加入你想要的创造页签。

### 什么时候调用注册？

在你的 mod 主类构造阶段调用。不要等到服务器启动、玩家登录或世界加载后再调用，那个时候 Forge 注册表已经冻结。

### 可以注册多个方块吗？

可以。每个方块调用一次 builder 即可，建议用 `public static final RegisteredMoreBlock` 保存返回值。

### 能不能接管 MoreBlock 默认坐下行为？

可以。在 `onUseBlock` 里返回 `CONSUME`、`SUCCESS` 或 `FAIL` 就会阻止后续默认交互。返回 `PASS` 则继续默认行为。
