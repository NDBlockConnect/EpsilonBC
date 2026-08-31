# EpsilonBC 重写计划（v26.0-alpha.4 起步）— "从上游重写"

> GitHub@NDBlockConnect | BlockConnect@StarsailsClover
> 决策来源：Alpha 3 体验验收不合格；用户指令——从上游重写为解决遗留问题的最优路径。

## 0. 情报结论（2026-08-25）

### ESP 框满天飞 — 根因锁定 [V]
`WorldToScreen` 使用 `gameRenderState().levelRenderState.cameraRenderState` 投影。
该 state 是**上一帧渲染的快照**；相机转动时与"本帧实体位置"错位一帧，
投影结果被旧矩阵甩向天际 → "转动视角时失效/漂移的框未隐藏"。

**修复范式（Wurst 26.2 实证）**：在 `LevelRenderer.render(...)RETURN` 注入点
直接使用方法参数 `positionMatrix` 构建 fresh PoseStack，**零跨帧快照**；
绘制相机相对坐标（`pos - camPos`），当帧即传即绘。

### 参考库分工
| 库 | 用途 |
|----|------|
| Wurst7-26.2 | 26.2 mixin 签名/注入点/Box 绘制/RenderType 用法 |
| LiquidBounce 0.40 | GuiElementRenderState 架构、GPU 缓冲池/延迟回收（长期参考） |
| Meteor 1.21.11 | MeshRenderer 每帧 mesh、事件→渲染生命周期 |
| upstream 26.1.x | 新 GUI 全套、Lua、runtime.render 架构（移植源） |
| OpenLumin | 26.2 Vulkan 基线、LuminShot Platform 抽象（革新模板） |

## 1. 架构决策

### 1.1 基座
**upstream 26.1.x 的 Screen/GUI/架构层** + **EpsilonBC 的 26.2 底层**
（构建系统、mixin、LuminRenderSystem/Vulkan、WorldToScreen、模块增值）。

外部 lumingraphics 无 26.2 构件 → upstream GUI 的 `MinecraftUiRuntime2612.*`
调用点经由 **EpsilonShot UiRuntime 抽象**落地到内建 gui.lib（Lua 适配已验证
此路成本 ≈ 5 类 shim）。未来 OpenLumin 稳定后提供 OpenLuminRuntime 即全量切换。

### 1.2 EpsilonShot Platform（底层革新核心）
```
com.github.epsilon.platform（epsilon-core）
├── UiRuntime            // current()/createScene(theme)/render(scene,layer,tree)/textMetrics()
├── UiRuntimeRegistry    // PlatformRegistry 模式（OpenLumin LuminShot 同构）
├── InternalUiRuntime    // gui.lib 26.2 Vulkan 实现（common 层注册）
├── WorldRenderContext   // positionMatrix + cameraPos（当帧直取，杜绝快照）
└── (预留) OpenLuminRuntime / LuminGraphicsRuntime
```
所有 Screen/模块渲染**只依赖抽象** → 解决：runtime 切换崩溃、跨帧快照、
未来图形后端切换、Addon 兼容。

### 1.3 保持不变的 EpsilonBC 图形学设计
- Render3DScheduler（命令累积→当帧 flush→clear，immediate 管线，Vulkan compute blur）
- Render2DScheduler / LuminImmediateRenderer / TTF 字体栈
- ESP 视觉设计（2D 框+血条、3D 盒、Tracers、HoleESP 等全部视觉参数）
- gui.lib 声明式树（作为 UiRuntime 的内建实现）

## 2. 阶段

| 阶段 | 内容 | 验收 |
|------|------|------|
| R1 | EpsilonShot UiRuntime 抽象 + InternalUiRuntime + WorldRenderContext | 编译绿；现有屏幕经抽象运行 |
| R2 | upstream 新 GUI 全量移植（Dropdown/Panel/HudEditor/MainMenu/全部 widget/popup/view/theme/UiCoordinateMapper/DropdownLayoutState），runtime 调用点改抽象；移除旧 Screen 层 | 实机 GUI 全功能 |
| R3 | ESP 根因修复：WorldToScreen positionMatrix 直取；Render3DEvent/2DEvent 注入点与相机来源对齐 | 转动视角无甩框 |
| R4 | 性能：ESP2D 入 HUD UiTree；冗余计算清理；profiler 驱动 | 卡顿可感知改善 |
| R5 | Aprism 支持：Refract 桥调研 → .aje 打包或桥接加载 | `mdl launch --aprism` 可载 |
| R6 | 陪伴系统增量 + 增值模块回归清单核对 | 功能无回退 |

## 3. 风险
- R2 移植面 ~6k 行：以文件为单位搬运+shim 改写，逐屏编译验证
- upstream GUI 依赖 UiCoordinateMapper（投影缩放）→ 内建实现需对齐 26.2 guiScale
- Aprism 对 Fabric mod 的桥接成熟度未知 → R5 早期做可行性 spike
