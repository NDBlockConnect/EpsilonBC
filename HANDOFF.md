# EpsilonBC 交接文档 v2（2026-08-25 会话 2）

> GitHub@NDBlockConnect | BlockConnect@StarsailsClover

## 当前状态

- **新基座**: `working-2f484774-base` 分支（基线 `26.1.x` 2f484774，已推送 origin）
- **已应用**:
  1. 修复 DropdownScreen 鼠标点击迭代器破坏（8e85831c，~ 200 行 DropdownScreen）—— **用户报告的"多级下拉展开/折叠异常"根因已修**。机制：line 309 反向索引循环调用 `panel.mouseClicked()`，该回调会内部 mutate `panels` 列表（popup open/close 等），后续 `i < panels.size() - 1` 用陈旧 index 配合新 size 产生错位 reorder，表现为面板突然闪到最前。修法：在回调前快照 `panels.size()` 和 panel 引用，回调后用引用做 `remove/add` 脱离索引依赖。
  2. 从 Epsilon-Private 拷贝 TargetStrafe / AutoThrow / Dolphin / Helper 套件（7dd496dd）—— 8 个新模块已注册到 ModuleHolder，import 路径已修（`managers.X` → `managers.impl.X`）

## 未完成（需要下一会话）

- **8 个新模块编译错**: `TargetRequest.of` 等签名不匹配（Private 签名更丰富，需要扩展我们的 `TargetRequest` + `TargetManager` + 可能 `EntityTypeFilter` 类型）。需逐个查 javac 报错改。
- **build 网络问题**: neoforged.net 间歇 SSL/Connection reset。**已**将 neoform-26.1.2-1 的 pom+zip+module 放入 `~/.m2/repository/net/neoforged/neoform/26.1.2-1/` 让 mavenLocal 优先解析。但 `minecraft-dependencies:26.1.2` 等传递依赖仍需网络下载。**已部分缓解**。
- **32 个 Epsilon-Private 专有模块** vs 我们 26.1.x base：批量 port 需多会话。每模块签名差异需个案适配。

## 关键诊断

- **Epsilon-Private vs 26.1.x**: 我们的 `26.1.x` base (2f484774) 与 Private repo 的 DropdownScreen/模块**实质内容相同**（仅 CRLF/LF 差异）。Private 的"新"工作主要在 **Epsilon-Private 闭源仓库中的额外 commit**（未通过公开 PR 同步），其中 32 个模块我们没有。
- **dropdown 根因** ≠ `layoutPanels` 字段同步（我们之前在 v26.0-alpha.3 修过），而是 `mouseClicked` 反向索引循环在面板回调可能 mutate `panels` 时的 iterator-state-collision。
- **基础架构已就位**: 26.1.x base 含 EpsilonShot（platform/UiRuntime 抽象）、OpenLumin 26.2 集成、模块化 epsilon-core 分层。后续 Epsilon-Private 移植不需要重做架构。

## 下一会话切入点

1. **最高优先级**: 逐个修编译错，TargetRequest 签名扩展（增加 passive/teams 等 filter booleans → 8 个 boolean 参数），然后 `gradlew :common:compileJava` 验证。
2. **次优先级**: AutoThrow 之外的其他 7 个新模块编译错扫描修复。
3. **再次**: 评估是否需要 EpsilonShot 抽象（platform/UiRuntime）真正接线（已建立但未在所有渲染代码中路由），或简化为直接调用 gui.lib。
4. **最后**: 编译 :fabric:jar 出 jar，部署到 mdl instance 实机测试 dropdown 修复 + 新模块。

## 关键文件位置

- 分支: `working-2f484774-base`（远端: `origin/working-2f484774-base`）
- 关键修复: `common/src/main/java/com/github/epsilon/gui/dropdown/DropdownScreen.java` line ~309-330
- 新模块: `common/src/main/java/com/github/epsilon/modules/impl/{combat/AutoThrow,movement/TargetStrafe,movement/Dolphin,player/helper/*}.java`
- 注册: `common/src/main/java/com/github/epsilon/holders/ModuleHolder.java` line 44-150（已有通配 import，新模块自动注册）

## 注意事项

- **网络**: neoforged.net 间歇失败。如果模块编译已通但 build 失败，先 `mvn dependency:go-offline` 或等待重试。
- **OpenLumin 切换**: 26.2 Vulkan 基底已就位。`EpsilonShot` (LuminShot 模式) 抽象了图形后端。未来 Lumin Graphics 26.2 上游发布后可平滑切换。
- **避免直接 cherry-pick 整个 26.1.x 或混用上游 PR**: 它们的 runtime 抽象依赖外部 Lumin Graphics 库（仅 26.1.2 提供），26.2 上不可用。Epsilon-Private 的同步工作要逐 commit 看依赖。

## 命令

```bash
# 构建
$env:JAVA_HOME='C:\Users\Sails\Java\jdk-25.0.3+9'
Start-Process cmd '/k set JAVA_HOME=...&& .\gradlew.bat :common:compileJava -x test --no-daemon > build.log 2>&1'

# 实机
mdl launch epsilon-test-26.2-fabric --detach --agent --no-idle-timeout -m 3G --username Tester
# 验证 dropdown: 打开 GUI 后多点击面板头部，关闭/打开应稳定（无闪烁/跳到最前）
```
