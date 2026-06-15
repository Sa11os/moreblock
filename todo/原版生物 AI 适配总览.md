# 原版生物 AI 适配总览（Minecraft Java 1.20.1）

本文件对照 **Minecraft Java Edition 1.20.1** 全部原版生物，标注 MoreBlock 导入实体 `ai_template` 当前**已适配**与**待适配**的情况，作为后续开发的 TODO 清单。

- **图例**：✅ 已支持 ｜ ⬜ 待支持
- **数据来源**：
  - 已支持列表：[ImportedEntityAiTemplates.java](../src/main/java/me/sallos/moreblock/entity/ImportedEntityAiTemplates.java)
  - 原版生物清单：[Minecraft Wiki - Mob](https://minecraft.wiki/w/Mob)

## 进度总览

| 分类 | 已支持 | 总数 |
| --- | --- | --- |
| 被动型生物 Passive | 5 | 33 |
| 中立型生物 Neutral | 2 | 14 |
| 敌对型生物 Hostile | 4 | 28 |
| Boss 生物 Boss | 1 | 2 |
| **合计** | **12** | **77** |

> 已适配 5 套行为模板：被动型动物、僵尸型（近战亡灵）、蜘蛛型（扑跃近战）、骷髅型（远程）、末影龙型（Boss）。
> 「建议模板」列给出待支持生物可复用的现有模板，或建议新增的模板类型。

---

## 一、被动型生物 / Passive Mobs

| 状态 | 中文名 | 英文名 | 实体 id | 建议模板 |
| --- | --- | --- | --- | --- |
| ✅ | 鸡 | Chicken | `minecraft:chicken` | 被动型动物 |
| ✅ | 牛 | Cow | `minecraft:cow` | 被动型动物 |
| ✅ | 哞菇（蘑菇牛） | Mooshroom | `minecraft:mooshroom` | 被动型动物 |
| ✅ | 猪 | Pig | `minecraft:pig` | 被动型动物 |
| ✅ | 绵羊 | Sheep | `minecraft:sheep` | 被动型动物 |
| ⬜ | 悦灵 | Allay | `minecraft:allay` | 需新增（飞行/拾取） |
| ⬜ | 美西螈 | Axolotl | `minecraft:axolotl` | 需新增（水生） |
| ⬜ | 蝙蝠 | Bat | `minecraft:bat` | 需新增（飞行） |
| ⬜ | 骆驼 | Camel | `minecraft:camel` | 可复用 被动型动物 |
| ⬜ | 猫 | Cat | `minecraft:cat` | 可复用 被动型动物（可驯服扩展） |
| ⬜ | 鳕鱼 | Cod | `minecraft:cod` | 需新增（水生） |
| ⬜ | 驴 | Donkey | `minecraft:donkey` | 可复用 被动型动物（可骑乘扩展） |
| ⬜ | 狐狸 | Fox | `minecraft:fox` | 可复用 被动型动物 |
| ⬜ | 青蛙 | Frog | `minecraft:frog` | 可复用 被动型动物 |
| ⬜ | 发光鱿鱼 | Glow Squid | `minecraft:glow_squid` | 需新增（水生） |
| ⬜ | 马 | Horse | `minecraft:horse` | 可复用 被动型动物（可骑乘扩展） |
| ⬜ | 骡 | Mule | `minecraft:mule` | 可复用 被动型动物（可骑乘扩展） |
| ⬜ | 豹猫 | Ocelot | `minecraft:ocelot` | 可复用 被动型动物 |
| ⬜ | 鹦鹉 | Parrot | `minecraft:parrot` | 需新增（飞行/跟随） |
| ⬜ | 河豚 | Pufferfish | `minecraft:pufferfish` | 需新增（水生/膨胀） |
| ⬜ | 兔子 | Rabbit | `minecraft:rabbit` | 可复用 被动型动物 |
| ⬜ | 鲑鱼 | Salmon | `minecraft:salmon` | 需新增（水生） |
| ⬜ | 嗅探兽 | Sniffer | `minecraft:sniffer` | 可复用 被动型动物 |
| ⬜ | 骷髅马 | Skeleton Horse | `minecraft:skeleton_horse` | 可复用 被动型动物（可骑乘扩展） |
| ⬜ | 僵尸马 | Zombie Horse | `minecraft:zombie_horse` | 可复用 被动型动物（可骑乘扩展） |
| ⬜ | 鱿鱼 | Squid | `minecraft:squid` | 需新增（水生） |
| ⬜ | 炽足兽 | Strider | `minecraft:strider` | 可复用 被动型动物（熔岩行走） |
| ⬜ | 蝌蚪 | Tadpole | `minecraft:tadpole` | 需新增（水生） |
| ⬜ | 热带鱼 | Tropical Fish | `minecraft:tropical_fish` | 需新增（水生） |
| ⬜ | 海龟 | Turtle | `minecraft:turtle` | 需新增（两栖） |
| ⬜ | 村民 | Villager | `minecraft:villager` | 需新增（交易/作息） |
| ⬜ | 流浪商人 | Wandering Trader | `minecraft:wandering_trader` | 需新增（交易/游荡） |
| ⬜ | 雪傀儡 | Snow Golem | `minecraft:snow_golem` | 需新增（远程/投掷雪球） |

---

## 二、中立型生物 / Neutral Mobs

| 状态 | 中文名 | 英文名 | 实体 id | 建议模板 |
| --- | --- | --- | --- | --- |
| ✅ | 蜘蛛 | Spider | `minecraft:spider` | 蜘蛛型 |
| ✅ | 洞穴蜘蛛 | Cave Spider | `minecraft:cave_spider` | 蜘蛛型 |
| ⬜ | 蜜蜂 | Bee | `minecraft:bee` | 需新增（飞行/受激惹反击） |
| ⬜ | 海豚 | Dolphin | `minecraft:dolphin` | 需新增（水生） |
| ⬜ | 末影人 | Enderman | `minecraft:enderman` | 需新增（瞬移/凝视激怒） |
| ⬜ | 山羊 | Goat | `minecraft:goat` | 可复用 被动型动物（撞击扩展） |
| ⬜ | 铁傀儡 | Iron Golem | `minecraft:iron_golem` | 可复用 僵尸型（近战，目标改敌对） |
| ⬜ | 羊驼 | Llama | `minecraft:llama` | 需新增（远程吐口水/受激惹） |
| ⬜ | 行商羊驼 | Trader Llama | `minecraft:trader_llama` | 需新增（远程吐口水/受激惹） |
| ⬜ | 熊猫 | Panda | `minecraft:panda` | 可复用 被动型动物（受激惹反击） |
| ⬜ | 猪灵 | Piglin | `minecraft:piglin` | 可复用 僵尸型（近战/远程混合） |
| ⬜ | 北极熊 | Polar Bear | `minecraft:polar_bear` | 可复用 僵尸型（近战，受激惹） |
| ⬜ | 狼 | Wolf | `minecraft:wolf` | 可复用 僵尸型（近战，可驯服扩展） |
| ⬜ | 僵尸猪灵 | Zombified Piglin | `minecraft:zombified_piglin` | 可复用 僵尸型（受激惹群体反击） |

---

## 三、敌对型生物 / Hostile Mobs

| 状态 | 中文名 | 英文名 | 实体 id | 建议模板 |
| --- | --- | --- | --- | --- |
| ✅ | 僵尸 | Zombie | `minecraft:zombie` | 僵尸型 |
| ✅ | 尸壳 | Husk | `minecraft:husk` | 僵尸型 |
| ✅ | 骷髅 | Skeleton | `minecraft:skeleton` | 骷髅型（远程） |
| ✅ | 流浪者 | Stray | `minecraft:stray` | 骷髅型（远程） |
| ⬜ | 烈焰人 | Blaze | `minecraft:blaze` | 可复用 骷髅型（远程，火球） |
| ⬜ | 苦力怕 | Creeper | `minecraft:creeper` | 需新增（靠近自爆） |
| ⬜ | 溺尸 | Drowned | `minecraft:drowned` | 可复用 僵尸型（含远程三叉戟） |
| ⬜ | 末影螨 | Endermite | `minecraft:endermite` | 可复用 僵尸型（小型近战） |
| ⬜ | 唤魔者 | Evoker | `minecraft:evoker` | 需新增（施法/召唤恼鬼） |
| ⬜ | 恶魂 | Ghast | `minecraft:ghast` | 需新增（飞行/远程火球） |
| ⬜ | 守卫者 | Guardian | `minecraft:guardian` | 需新增（水生/激光） |
| ⬜ | 疣猪兽 | Hoglin | `minecraft:hoglin` | 可复用 僵尸型（近战冲撞） |
| ⬜ | 岩浆怪 | Magma Cube | `minecraft:magma_cube` | 需新增（跳跃/分裂） |
| ⬜ | 幻翼 | Phantom | `minecraft:phantom` | 需新增（飞行/俯冲） |
| ⬜ | 猪灵蛮兵 | Piglin Brute | `minecraft:piglin_brute` | 可复用 僵尸型（近战） |
| ⬜ | 掠夺者 | Pillager | `minecraft:pillager` | 可复用 骷髅型（远程弩） |
| ⬜ | 劫掠兽 | Ravager | `minecraft:ravager` | 可复用 僵尸型（近战冲撞） |
| ⬜ | 潜影贝 | Shulker | `minecraft:shulker` | 需新增（固定/远程导弹） |
| ⬜ | 蠹虫 | Silverfish | `minecraft:silverfish` | 可复用 僵尸型（小型近战） |
| ⬜ | 史莱姆 | Slime | `minecraft:slime` | 需新增（跳跃/分裂） |
| ⬜ | 恼鬼 | Vex | `minecraft:vex` | 需新增（飞行/穿墙近战） |
| ⬜ | 卫道士 | Vindicator | `minecraft:vindicator` | 可复用 僵尸型（近战斧） |
| ⬜ | 监守者 | Warden | `minecraft:warden` | 需新增（震动感知/音爆） |
| ⬜ | 女巫 | Witch | `minecraft:witch` | 可复用 骷髅型（远程药水） |
| ⬜ | 凋灵骷髅 | Wither Skeleton | `minecraft:wither_skeleton` | 可复用 僵尸型（近战剑） |
| ⬜ | 僵尸疣猪兽 | Zoglin | `minecraft:zoglin` | 可复用 僵尸型（近战冲撞） |
| ⬜ | 僵尸村民 | Zombie Villager | `minecraft:zombie_villager` | 可复用 僵尸型 |
| ⬜ | 远古守卫者 | Elder Guardian | `minecraft:elder_guardian` | 需新增（水生/激光，迷你 Boss） |

---

## 四、Boss 生物 / Boss Mobs

| 状态 | 中文名 | 英文名 | 实体 id | 建议模板 |
| --- | --- | --- | --- | --- |
| ✅ | 末影龙 | Ender Dragon | `minecraft:ender_dragon` | 末影龙型（专用 Boss 控制器） |
| ⬜ | 凋灵 | Wither | `minecraft:wither` | 需新增（飞行 Boss/远程头颅） |

---

## 五、优先级建议 / TODO 优先级

按「可复用现有模板、改动小、覆盖面广」优先：

1. **直接复用 被动型动物**：骆驼、猫、狐狸、青蛙、豹猫、兔子、嗅探兽、马/驴/骡/骷髅马/僵尸马、山羊、熊猫、炽足兽。
2. **直接复用 僵尸型（近战亡灵）**：僵尸村民、溺尸、凋灵骷髅、卫道士、猪灵蛮兵、蠹虫、末影螨、僵尸猪灵、铁傀儡、北极熊、狼、疣猪兽/僵尸疣猪兽、劫掠兽。
3. **直接复用 骷髅型（远程）**：掠夺者、女巫、烈焰人。
4. **需新增模板（建议按下列分组实现）**：
   - 水生型（鱼类 / 鱿鱼 / 海豚 / 守卫者）
   - 飞行型（蝙蝠 / 鹦鹉 / 蜜蜂 / 幻翼 / 恶魂 / 恼鬼 / 悦灵）
   - 跳跃分裂型（史莱姆 / 岩浆怪）
   - 自爆型（苦力怕）
   - 施法/召唤型（唤魔者）
   - 固定炮台型（潜影贝）
   - Boss 型扩展（凋灵 / 远古守卫者）
   - 交易/作息型（村民 / 流浪商人）

> 注：表中实体 id 均为 1.20.1 原版注册名，可直接填入实体配置的 `ai_template` 字段；当前仅 ✅ 项会真正生效，⬜ 项会回退为「空闲游荡」逻辑。
