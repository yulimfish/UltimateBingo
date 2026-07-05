# UltimateBingo — Agent 开发记录

> Paper 1.21.1 Minecraft 宾果插件。基于 MegaBingo，深度中文本地化 + 功能增强 + 性能优化。
> 
> 仓库: `yulimfish/UltimateBingo` | 编译: `mvn clean package -DskipTests` | JAR: `target/UltimateBingo-1.2.1.jar`

---

## 项目概况

| 项目 | 值 |
|------|-----|
| 语言 | Java 17+ |
| 构建 | Maven |
| 编译目标 | spigot-api 1.20.4（Paper 专用 API 通过反射调用） |
| 运行环境 | Paper 1.21.1 + Multiverse（多世界） |
| 源文件 | 36 个 |
| 作者 | @yulimfish |

---

## 架构概览

```
io.shantek/
├── UltimateBingo.java          ← 插件主类，初始化所有 Manager/Listener
├── BingoCommand.java           ← /bingo 命令入口 (start, stop, random, records...)
├── BingoCompleter.java         ← Tab 补全
├── BingoPlaceholderExpansion.java ← PlaceholderAPI 占位符
├── ConfigFile.java             ← config.yml 读写
├── SettingsManager.java        ← 游戏设置 GUI 后端
├── HubConfig.java              ← 大厅传送配置
├── InGameConfigManager.java    ← 游戏中配置读写
├── Leaderboard.java            ← 玩家胜率统计 (leaderboard.yml)
├── CardTypes.java              ← 宾果判定 (行/列/对角线/满卡)
├── Metrics.java                ← bStats
├── managers/
│   ├── BingoManager.java       ← 卡片创建、任务生成、完成标记
│   ├── BingoMapManager.java    ← 地图视图管理、卡片渲染调度
│   ├── BingoCardMapRenderer.java ← 128×128 地图像素渲染（材质着色）
│   ├── BingoGameGUIManager.java  ← 游戏设置 GUI（难度/大小/模式）
│   ├── BingoPlayerGUIManager.java ← 队伍选择 GUI（三色羊毛）
│   ├── BingoScoreboardManager.java ← 实时排行 + 历史纪录侧边栏
│   ├── RecordBoard.java        ← 历史纪录持久化 (records.yml)
│   └── CardTypes.java
├── listeners/
│   ├── BingoPickupListener.java     ← 捡起物品 → 标记完成
│   ├── BingoAdvancementListener.java ← 完成成就 → 标记完成
│   ├── BingoInteractListener.java   ← 右键地图打开 GUI
│   ├── BingoMapInteractListener.java ← 地图交互
│   ├── BingoInventoryCloseListener.java ← GUI 关闭保存
│   ├── BingoPlayerJoinListener.java  ← 进世界/重连状态恢复
│   ├── BingoGUIListener.java        ← 设置 GUI 交互
│   ├── BingoPlayerGUIListener.java   ← 队伍选择 GUI 交互
│   ├── BingoSignListener.java        ← 告示牌交互
│   ├── EntityDamageListener.java     ← 死亡随机重生
│   ├── SettingsListener.java         ← 物品增删设置
│   └── HubRegionListener.java        ← 大厅区域进入检测
└── tools/
    ├── BingoFunctions.java    ← 核心工具（传送、材料名翻译、队伍、成就翻译）
    ├── MaterialList.java      ← 物品池（5 难度梯队，bingoitems.yml）
    ├── AdvancementList.java   ← 成就池（5 难度梯队，运行时解析 key）
    ├── ItemBuilder.java       ← ItemStack 构建器
    └── WorldGuardHelper.java  ← WorldGuard 区域检测
```

---

## 功能清单

### 1. 完整中文本地化
- **所有玩家可见文本汉化**：GUI 标题、物品名、lore、聊天消息、告示牌文本
- **控制台日志汉化**
- **config.yml / plugin.yml 注释汉化**
- **规则**：内部 key（gameMode、difficulty）不可汉化；权限节点、类名不可汉化
- **材料翻译**：`BingoFunctions.MATERIAL_CN` 映射表，168 条目覆盖全部 MaterialList 物品
- **药水翻译**：`BingoFunctions.POTION_CN`，~30 条目
- **成就翻译**：`BingoFunctions.ADVANCEMENT_CN`，75 条目，覆盖全部 5 个难度梯队，使用官方 Minecraft 中文成就名

### 2. 成就任务（成就格子）
- **核心设计**：`KNOWLEDGE_BOOK` + `PersistentDataContainer` 存储成就 key
- **比例控制**：`slotCount / 5` 取 floor，`random.nextInt(maxAdv + 1)`，严格 <20%
- **生成**：`BingoManager.generateTasks()` 替代 `generateMaterials()`，自动混入成就 ItemStack
- **GUI 显示**：§d 紫色名称 "成就：xxx"，灰色 lore 提示
- **地图渲染**：亮紫色 (hue=280) 区分于棕色/灰色物品格子
- **检测**：`BingoAdvancementListener` 监听 `PlayerAdvancementDoneEvent`，PDC 精确匹配 slot
- **完成**：`markSlotAsComplete()` 按槽位索引标记，避免多重 KNOWLEDGE_BOOK 冲突
- **成就分级**：
  - T1 简单：石器时代、获得升级、整装上阵、怪物猎人…
  - T2 普通：勇往直下、附魔师、本地酿造厂、冰桶挑战…
  - T3 困难：僵尸科医生、阴森的要塞、解放末地、超越生死…
  - T4 极限：天空即为极限、凋零山庄、信标工程师…
  - T5 不可能：资深怪物猎人、探索的时光、为什么会变成这样？…
- **团队公平**：`createTeamBingoCards()` 预抽 `sharedAdvCount`，三队同数量不同内容

### 3. 随机传送系统
- **圆形均匀分布**：`radius * sqrt(random)` 保证面积均匀
- **半径配置**：`config.yml` → `teleport-radius`，默认 5000，最大 8000
- **渐进缩半径**：5000→2500→1250→625→312→出生点，不轻易放弃
- **Paper 异步 chunk 加载**：反射调用 `getChunkAtAsync`，避免主线程卡死
- **安全检测**：跳过 WATER/LAVA，`isSafeLocation()` 检查固体地面
- **冷却时间**：公开 API 5 秒冷却（防死亡循环），内部重试走 `doTeleportToRandomGround()` 跳过冷却
- **竞态保护**：`AtomicBoolean.compareAndSet` 保证异步回调和超时回退只有一个执行
- **60 tick 超时回退**：异步未完成则同步加载 + 递归重试
- **retryDepth 收敛**：递归 ≥5 次后强制 loaded-chunk 模式，保证到达出生点
- **开局错开**：`staggerTicks * 10`，每人隔 0.5 秒启动传送，避免 7 人同时轰炸 Paper 异步管道
- **调用点**：开局自动、`/bingo random` 手动、中途加入、死亡重生

### 4. GUI 系统
- **游戏设置 GUI**：`BingoGameGUIManager` — 难度/大小/模式/满卡/唯一卡/公开/装备
- **配置项悬浮说明**：各选项有详细 lore 描述
- **队伍选择 GUI**：`BingoPlayerGUIManager` — 三列羊毛 + 玩家名 + 点击切换，赛前预选 + 游戏中换队
- **宾果卡片 GUI**：54 格库存，9/16/25 格布局，已标记格子变 LIME_CONCRETE

### 5. 计分板
- **实时排行**：个人/队伍/团队模式，每秒刷新时间 + 已完成数
- **队伍模式队友显示**：侧边栏显示队友列表
- **历史纪录**：非游戏期间在宾果世界显示历史排行榜（前 10）
- **`/bingo records`**：分页查看全部纪录 + OP 删除指定 ID
- **持久化**：`records.yml` 存储每场比赛（玩家名、完成项数、用时、卡片大小、日期）

### 6. 游戏机制
- **保持物品栏**：游戏开始时自动 `keepInventory = true`
- **成就清空**：游戏开始时撤销所有活跃玩家全部成就进度
- **玩家状态恢复**：掉线重连/跨世界返回时恢复卡片 GUI、地图、计分板
- **死亡随机重生**：`respawnTeleport = true` 时死亡自动随机传送
- **多种游戏模式**：传统、极速、酿造冲刺、团队、分组

---

## 关键技术决策

| 决策 | 原因 |
|------|------|
| 反射调用 Paper API | spigot-api 编译时不提供 `getChunkAtAsync`，运行时 Paper 可用 |
| `MaterialList` 硬编码翻译表 | Spigot 无 `ItemStack.getI18NDisplayName()`，Adventure API 编译不可用 |
| `isTaskCompleted` 改槽位索引 | KNOWLEDGE_BOOK 多格子同 Material，按 Material 匹配会误判 |
| 传送冷却只拦公开 API | 内部递归重试需要快速收敛，不能每次重试等 5 秒 |
| `retryDepth` 强制收敛 | 每轮递归从最大半径重启 → async 永远先返回 → 出生点 fallback 死代码 |
| 开局错开传送 | Paper 异步管道承受不住 7+ 并发 `getChunkAtAsync` |

---

## 已知问题和注意事项

1. **非 Paper 服务器**：反射 `getChunkAtAsync` 失败时回退同步 `getChunkAt`，会卡主线程
2. **海洋世界**：随机位置大量落在水中，需要多次重试，最终收敛到出生点
3. **MaterialList 用户自定义**：管理员可通过 `bingoitems.yml` 增删物品，但翻译表只涵盖默认物品
4. **成就兼容性**：`AdvancementList` 运行时 `Bukkit.getAdvancement()` 解析，MC 版本差异的成就自动跳过

---

## 编译和部署

```bash
# 编译
mvn clean package -DskipTests

# 输出
target/UltimateBingo-1.2.1.jar

# 部署
cp target/UltimateBingo-1.2.1.jar /path/to/server/plugins/
```

### 依赖
- Paper 1.21.1（或兼容版本）
- Multiverse（可选，多世界模式）
- PlaceholderAPI（可选，占位符支持）
- WorldGuard（可选，大厅区域保护）

---

## 近期提交记录

```
257b61f fix: infinite retry loop when all positions are ocean/unsafe
c0f3ca8 fix: cooldown was silently blocking all retry teleports
b2ab12a fix: two race conditions in loadChunkAsync causing 40% teleport failure
09001e5 fix: stagger teleports on game start to avoid concurrent chunk-gen overload
38f89e9 i18n: complete Chinese translations for materials & advancements
20e21c5 fix: ensure all teams get the same advancement count in unique team mode
146c8ac feat: add advancement tasks to bingo cards (<20% of cells)
df0838e fix: narrow exception handling in clearAllAdvancements
```

---

## 开发规范

- **代码/注释/commit 信息**：技术向内容保持英文
- **UI/UX 改动**：用 ASCII UI 示意
- **用户意图不清时**：主动询问细节
- **如无必要，勿增实体**
- **中文回复**
