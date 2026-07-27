# EpsilonBC 架构重构计划

## 概览

从 v26.0 Alpha 3 开始，EpsilonBC 将从单体架构重构为三层模块化架构：

```
┌─────────────────────────────────────────────────────────┐
│                  EpsilonBC Modules                      │
│  (HvH-specific implementations: KillAura, Scaffold...)  │
└─────────────────────────────────────────────────────────┘
                          ↓ depends on
┌─────────────────────────────────────────────────────────┐
│                    Epsilon Core                         │
│  (Events, Managers, Module API, Settings, Utils...)    │
└─────────────────────────────────────────────────────────┘
                          ↓ depends on
┌─────────────────────────────────────────────────────────┐
│                 EpsilonShot Platform                    │
│  (Fabric/NeoForge adapters, multi-version support...)   │
└─────────────────────────────────────────────────────────┘
```

## 模块职责

### Epsilon-core
核心框架与抽象层，所有客户端共享：

- **事件系统**: EventBus、50+ Event 类型
- **管理器抽象**: RotationManager、TargetManager、FriendManager 等基类
- **模块框架**: Module 基类、SettingHost、Category、键绑定
- **工具类**: 数学、渲染、世界交互、库存操作
- **图形抽象接口**: IGraphicsAdapter（统一 OriginalLumin 和 OpenLumin）
- **资源系统**: 国际化、配置迁移、资源定位

### EpsilonBC-modules
HvH 专用模块实现（104 个模块）：

- **Combat** (25): KillAura, CrystalAura, ZealotCrystalPlus, Surround, AutoTotem...
- **Movement** (22): Scaffold, Flight, ElytraFly, Speed, NoSlow, Bhop, Burrow...
- **Render** (30): ESP2D, BlockESP, Tracers, NameTags, Shaders, Chams...
- **Player** (27): InvManager, AutoArmor, AutoEat, Timer, FastPlace...
- **HUD Elements**: Watermark, ModuleList, TargetHUD, Notifications...

### EpsilonShot Platform
多版本/加载器适配层：

- **Fabric 适配器**: ClientModInitializer、Mixin 注入、Sodium 兼容
- **NeoForge 适配器**: Mod 构造函数、事件总线桥接
- **版本抽象**: 1.20.x/1.21.x/26.x 统一接口
- **Addon 系统**: EpsilonAddon、AddonBootstrap

## 图形库策略

### 当前状态 (Alpha 2)
- **OriginalLumin**: 自研图形库，包含完整渲染管线
  - Render2D/3DScheduler、RoundRectRenderer、TextureRenderer
  - Vulkan 后端、TTF 字体、着色器效果系统
  - 与 OpenLumin API 接口高度相似（80%+）

### 迁移路径

#### Alpha 3-4: 图形抽象接口
```java
interface IGraphicsAdapter {
    IRender2DScheduler getRender2DScheduler();
    IRender3DScheduler getRender3DScheduler();
    IRoundRectRenderer getRoundRectRenderer();
    IWorldToScreen getWorldToScreen();
    // ...
}

class OriginalLuminAdapter implements IGraphicsAdapter {
    // 包装当前 OriginalLumin 实现
}

class OpenLuminAdapter implements IGraphicsAdapter {
    // 预留，Alpha 3-4 暂不实现
}
```

#### Alpha 5+: OpenLumin 迁移
- 实现 OpenLuminAdapter（适配 https://github.com/NDBlockConnect/OpenLumin）
- 增加运行时图形后端切换配置
- Alpha 5 后弃用 OriginalLumin

## 实施计划

### 阶段一：图形抽象接口定义 (Alpha 3, 第 1-2 周)

**目标**: 定义 IGraphicsAdapter 接口族，不破坏现有代码

1. ✅ 创建 `common/src/main/java/com/github/epsilon/graphics/abstraction/`:
   - ✅ `IGraphicsAdapter.java` (主适配器接口)
   - ✅ `IRender2DScheduler.java`, `IRender3DScheduler.java`
   - ✅ `IRoundRectRenderer.java`, `ITextureRenderer.java`
   - ✅ `IWorldToScreen.java`, `IShaderEffectManager.java`

2. ✅ 实现 `OriginalLuminAdapter`:
   - ✅ 包装现有 `graphics/*` 包中的所有类
   - ✅ 保持现有功能完全不变

3. ⚙️ 重构调用点（进行中）：
   - ⏳ 32 个渲染模块改为通过 `IGraphicsAdapter` 获取渲染器
   - ✅ Managers 增加 `GRAPHICS`（持有当前适配器实例）

**当前状态**: 接口定义和适配器实现已完成并提交（commit 56b87546），编译验证通过。下一步：重构渲染模块调用点。

**验收**: 所有模块编译通过，实机渲染无退化

### 阶段二：Core/Modules 垂直切分 (Alpha 3, 第 3-4 周)

**目标**: 分离核心框架与模块实现

1. 创建 `epsilon-core` 子项目：
   ```
   epsilon-core/
   ├── events/          (EventBus + 50+ Event 类型)
   ├── managers/        (10 个管理器基类)
   ├── modules/         (Module 抽象 + Category)
   ├── settings/        (SettingHost + 所有 Setting 类型)
   ├── utils/           (工具类，不含模块特定逻辑)
   └── graphics/abstraction/  (图形接口定义)
   ```

2. 创建 `epsilonbc-modules` 子项目：
   ```
   epsilonbc-modules/
   ├── combat/          (25 个战斗模块)
   ├── movement/        (22 个移动模块)
   ├── render/          (30 个渲染模块)
   ├── player/          (27 个玩家模块)
   └── elements/        (HUD 元素)
   ```

3. 调整 Gradle 配置：
   ```kotlin
   // settings.gradle.kts
   include("epsilon-core")
   include("epsilonbc-modules")
   include("common")  // 临时聚合层
   include("fabric")
   include("neoforge")
   
   // common/build.gradle.kts
   dependencies {
       api(project(":epsilon-core"))
       implementation(project(":epsilonbc-modules"))
   }
   ```

**验收**: 模块间依赖清晰，无循环引用，构建成功

### 阶段三：Platform 层隔离 (Alpha 4)

**目标**: 提取平台特定代码，支持多版本

1. 创建 `epsilonshot-platform` 子项目
2. 定义 PlatformBridge 接口（事件适配、资源加载）
3. 实现 Fabric/NeoForge 平台桥接器
4. 设计多版本抽象层（为 1.20.x/1.21.x 回溯做准备）

### 阶段四：OpenLumin 集成 (Alpha 5+)

1. 实现 OpenLuminAdapter（基于 OpenLumin v26.0+）
2. 增加图形后端配置项（OriginalLumin/OpenLumin 切换）
3. 性能对比测试，完成迁移
4. 弃用 OriginalLumin

## 版本规划

- **v26.0 Alpha 2**: 当前单体架构，OriginalLumin
- **v26.0 Alpha 3**: 图形抽象接口 + Core/Modules 垂直切分
- **v26.0 Alpha 4**: Platform 层隔离 + 多版本框架
- **v26.0 Alpha 5**: OpenLumin 集成 + OriginalLumin 弃用公告
- **v26.0 Beta 1**: 完全迁移到 OpenLumin，移除 OriginalLumin 代码

## 风险与缓解

### 风险 1: 图形抽象开销
- **缓解**: 接口方法内联、零开销抽象（Java 25 JIT 优化）
- **验证**: 帧率基准测试（Alpha 2 vs Alpha 3）

### 风险 2: 模块间依赖混乱
- **缓解**: 严格的包可见性规则、编译时依赖检查
- **验证**: Gradle 依赖分析、架构测试用例

### 风险 3: OpenLumin 不稳定
- **缓解**: Alpha 3-4 仅预留接口，不实际接入
- **验证**: 持续跟踪 OpenLumin 仓库稳定性

### 风险 4: 破坏 Addon 兼容性
- **缓解**: 保持 common 作为临时聚合层，旧 API 桥接
- **验证**: 已知 Addon（如有）回归测试

## 参考资料

- OpenLumin API 文档: https://github.com/NDBlockConnect/OpenLumin/blob/v26.0-alpha.1/docs/API_REFERENCE.md
- 当前架构分析: [AI 研究报告 ab08f050b693847b1]
- Alpha 2 问题清单: `memory/FACT.md`
