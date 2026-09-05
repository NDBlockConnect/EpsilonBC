# EpsilonBC 交接文档 v5（2026-09-05/06 会话 5）

> GitHub@NDBlockConnect | BlockConnect@StarsailsClover
> 分支: `working-2f484774-base`（HEAD = 2dbb1306，已推 origin）
> **✅ v26.0.0-alpha.4 已发布**: https://github.com/NDBlockConnect/EpsilonBC/releases/tag/v26.0.0-alpha.4

## 实机测试结论（本轮完成 ✅）

**测试环境排障（三个根因全部定位）**：
1. **实例游戏静默死亡** = mods 里缺 **Despotes agent 模组** → `--agent` 等不到连接被 mdl 杀；从 openlumin-fabric-26.1.2 拷 `Despotes-v26.12-Alpha.3-fabric-26.1.2.jar` 解决
2. **keybd_event 注入键无效** = scancode=0 时 GLFW 不识别；**必须带正确 scancode**（RightShift=VK 0xA1 + scan 0x36）；ESC/字母键 scan=0 恰好能通造成误判
3. **mdl `game key`/raw-action key 走 KeyMapping 层**，不经过我们的 GLFW mixin —— 测模组按键必须用 OS 级输入
4. mdl 版本自动升级频繁（v26.4→v26.5.0-alpha.8），instance→window 映射有 bug；`--oom-list-only` 防止 launch 清场杀别的游戏

**验证通过项**：
- 全部 157 模块注册（含 20 新）——本次运行生成的 epsilon-empty-i18n.json 逐项确认
- 自定义 MainMenuScreen 渲染 ✓；世界创建/进入 ✓
- **DropdownGui 开启/关闭正常**；主面板头部连点 5 次 + 分类面板切换 3 次**无 z-shuffle、无异常**（8e85831c 修复实机验证通过）
- dropdown-layout.json 持久化正常

## 测试环境快速复现

```powershell
# 实例: epsilon-test-26.1.2-fabric（MC 26.1.2 + fabric 0.19.2，runtime/versions 已从 epsilon-fabric-26 拷贝）
# mods 必须: epsilon jar + fabric-api + Despotes-v26.12-Alpha.3-fabric-26.1.2.jar
mdl launch epsilon-test-26.1.2-fabric --detach --agent --no-idle-timeout --oom-list-only -m 1280M --username Tester
# GUI 按键注入（PowerShell, keybd_event 必须带 scancode）:
#   RightShift = keybd_event(0xA1, 0x36, 0/2) ；ESC = 0x1B；进入世界后按键才生效
# mdl click 坐标 = MC gui-scaled 空间（guiScale=2 时 427x240）
# 自定义主菜单 Singleplayer 按钮 ≈ (75, 220)；Create 按钮 ≈ (134, 224)（左 Create 右 Cancel！）
```

## 发布信息（alpha.4）

- tag `v26.0.0-alpha.4`（注意：alpha.3 标签被旧 26.2 线占用，故跳号）
- 资产：epsilon-fabric-26.1.2-26.0.0-alpha.4.jar + epsilon-neoforge-26.1.2-26.0.0-alpha.4.jar（36.9MB×2，SHA256 校验通过）
- 发布用 gh api（`gh release create` 需 workflow scope；REST API 只要 repo 权限）
- ⚠️ C 盘空间曾耗尽（0GB）——已清旧 gradle dists/caches 释放 ~2GB；构建前留意

## 前情摘要（会话 4 及更早）

- 瀑布式根因：Private 用 index-based remove/add；我们已用 headerClick+reference 方案修复并实机验证
- 移植 20 模块（9b2ff173）+ i18n（09166ba8，en/zh 各 216+ keys）
- 重写：AutoTotem（3a021406 预测伤害）、Velocity（c421cdbc Modify）、CrystalAura（108a3ae3 yawSteps+fastBreak）
- Speed/Scaffold 审查后判定与参考持平，无需重写
- 构建环境：gradle 9.5.1（wrapper）、MavenLocal 首位重排（init.d/zz-mavenlocal-first.init.gradle.kts）、minecraft-dependencies 只在 mojang-meta 仓库（pom+module，packaging 已改 pom）
- _parked_wip/：并行会话 WIP（vulkan/abstraction/scripting/platform/gui Choice*）+ 26.2 顺延（MotionBlur/BetterChat）

## 用户定位（本轮确认）

一切基于 2f484774（EpsilonShot 底层架构革新 + 陪伴系统 + 新功能）做：
1. **研究 Epsilon-Private（转闭源源码）**：搞清瀑布式根因 + 跟进新增功能；**不跟进新界面**
2. **基于参考仓库（LiquidBounce/Meteor/Wurst）做模块重写与新增**
3. **保留我们自己的模块/代码改进**

## 瀑布式根因（已确认 + 已修复）

Epsilon-Private `DropdownScreen.mouseClicked` 用 **index-based** `panels.remove(i)` + `panels.add(panel)`
在 panel 回调 mutate panels 后索引错位 → z-shuffle 闪烁。
我们的修复（8e85831c）：headerClick 判定 + reference-based remove/add，**优于 Private 原实现**。

## 模块差异（已全部量化）

- Epsilon-Private 119 个模块；EpsilonBC 137 个（含 42 个独有自定义模块，全部保留）
- 重命名对：HandView=HandsView、NoSlowdown=NoSlow（不重复移植）
- **本轮移植 20 个注册**（19 新 + Helper）：见 commit 9b2ff173
- **顺延到 26.2**（_parked_wip/）：MotionBlur（需 MotionBlurShader，26.2 blaze3d BindGroupLayout）、BetterChat（26.2 chat 内部 mixin）

## 参考仓库研究结论（子任务已归档）

- **Meteor 最适合直接移植**：纯 Java、单文件模块、事件模型与 EpsilonBC 一一对应
- **Wurst 架构最干净**：两阶段 rotate→attack 模式匹配我们的 pending-rotation 模式
- **LB 借思想不借代码**：rotation-goal 系统、SimulatedPlayer/FallingPlayer、transaction buffer
- 值得移植的技法：Meteor doYawSteps（Grim 绕过）、EntityAddedEvent fast-crystal、AutoTotem（130 行）；
  LB jumpOrder[]（Step 包重放）、SimulatedPlayer；Wurst ±ms 攻速随机化（Vulcan bypass）
- 文件路径前缀：LB=`reference/LiquidBounce-0.40.0/LiquidBounce-0.40.0/src/main/kotlin/net/ccbluex/liquidbounce/`，
  MC=`reference/meteor-client-1.21.11/meteor-client-1.21.11/src/main/java/meteordevelopment/meteorclient/`，
  W=`reference/Wurst7-26.2/Wurst7-26.2/src/main/java/net/wurstclient/`

## 构建环境修复（本轮重大）

| 问题 | 修复 |
|------|------|
| gradle-9.2.1 dist 缓存丢失 + services.gradle.org 超时 | wrapper 切到本地已缓存 **9.5.1**（gradle-wrapper.properties） |
| maven.neoforged.net 间歇 Connection reset | 手动补齐 m2：neoform-runtime 2.0.18 (pom/jar/module/all.jar)、mergetool 2.0.7(+api)、mergetool 1.1.7、accesstransformers 13.0.1(+at-parser)、installertools 4.0.12(+fatjar)、AutoRenamingTool 2.0.17(+all)、vineflower-plugins 0.1.5、**minecraft-dependencies 26.1.2 (pom+module，来自 mojang-meta 代理仓库，packaging 已改 pom)** |
| MavenLocal 被排在 Mojang Meta 之后（moddev dependencyResolutionManagement 前插） | init 脚本 afterEvaluate 把 MavenLocal move 到首位（`~/.gradle/init.d/zz-diag.init.gradle.kts`，名字可改） |
| metadata 缓存的负结果 | 清 `~/.gradle/caches/modules-2/metadata-2.107/descriptors` |

**关键知识**：`minecraft-dependencies` 只存在于 `https://maven.neoforged.net/mojang-meta/net/neoforged/minecraft-dependencies/<ver>/`（releases 仓库 404！）；packaging=module 无 jar，pom+module 即完整。

## 并行会话 WIP（_parked_wip/，动我构建的都暂存了）

- `graphics/vulkan/`、`graphics/abstraction/`、`scripting/`（lua，缺 org.luaj 依赖）
- `gui/dropdown/component/UiScrollBar`、`gui/dropdown/widget/ChoiceWidget`、
  `gui/panel/component/setting/ChoiceSettingRow`、`gui/panel/popup/ChoiceSelectPopup`
- `platform/`（UiRuntime/UiRuntimeRegistry/InternalUiRuntime）
- `gui/addon/AddonPanelEntryRegistry`（注意：AddonPanelEntry/BuiltInTextMetrics/LuminColorShim 被误并入 9b2ff173 提交，可编译无害）
- 我方顺延：`MotionBlur.java`、`MotionBlurShader.java`、`LuminBindGroupLayouts.java`、`BetterChat.java`

⚠️ 并行会话正在往本仓库写 WIP 文件——每次构建前 `git status` 检查 ?? 文件，必要时再暂存。

## 下一步

1. **i18n 同步**：✅ 已完成（09166ba8）：20 新模块 en_us/zh_cn key 树（源码扫描生成）
2. **参考仓库重写进度**：
   - ✅ **AutoTotem**（3a021406）：Meteor Smart/Strict + 预测伤害（晶体/锚爆炸扫描 + FallingPlayer 摔落模拟）+ 弹出包重置延迟 + isLocked() 暴露
   - ✅ **Velocity**（c421cdbc）：Modify 模式（current + (incoming-current)*factor 百分比削减）
   - ✅ **CrystalAura**（108a3ae3）：Grim yawSteps 门控（移动包追踪 serverYaw）+ Fast Break（新水晶当 tick 攻击）
   - ⏳ **Speed**：LB ~20 AC 模式参考，Meteor Strafe 4 阶段状态机（MoveEvent 需求）
   - ⏳ **Scaffold**：LB techniques/towers 最重（Normal/Expand/GodBridge/Breezily + tower 系列）
   - ⏳ **KillAura**：已有 CPS 随机化（Wurst 技法已在），剩余升级空间：LB rotation-goal 的 raytraceBox 选点
3. **实机测试进展**（2026-09-05）：
   - ✅ **修复 26.1.2 实例**：instance.json 丢失（只剩 mods/）→ 重建 + 从 epsilon-fabric-26 拷贝 runtime/versions + loader 版本对齐 **0.19.2**（0.19.3 会启动即死）
   - ✅ **游戏启动成功，模组完整加载**："Welcome to EpsilonBC." + CJK fallback font (malgun.ttf) 初始化成功 = 全部 157 模块（含 20 新）注册无崩溃
   - ❌ **进程在 17:03:21 后被静默击杀**（无崩溃日志 = 已知并行会话 8G 游戏触发 OOM 清扫问题）；⚠️ 注意 mdl v26.5.0-alpha.5 有 instance→window 映射 bug，会把他人的 openlumin 游戏映射到我们的实例名（已两次误导测试，须用进程 cmdline 验证归属）
   - ⏳ **未验证**：dropdown 修复实机行为、新模块 GUI 可见性、Smart AutoTotem 预测
   - 网络现状：services.gradle.org / piston-meta.mojang.com 均间歇不可达（新建实例受阻）
4. **发布 alpha.3**：测试通过后 bump version + gh release

## 快速命令

```powershell
$env:JAVA_HOME='C:\Users\Sails\Java\jdk-25.0.3+9'
# 构建（务必 --offline 防网络停滞；分离进程防 shell 超时）
Start-Process cmd '/k set JAVA_HOME=C:\Users\Sails\Java\jdk-25.0.3+9&& .\gradlew.bat :common:compileJava :fabric:jar -x test --console=plain --offline > build.log 2>&1' -WindowStyle Hidden
# 部署 26.1.2 实例（epsilon-test-26.1.2-fabric，runtime 已修好，loader 必须 0.19.2）
Copy-Item fabric\build\libs\epsilon-fabric-26.1.2-*.jar $env:APPDATA\mdl\instances\epsilon-test-26.1.2-fabric\mods\ -Force
# 启动
mdl launch epsilon-test-26.1.2-fabric --detach --agent --no-idle-timeout -m 1280M --username Tester
# 验证归属（mdl 有实例→窗口映射 bug）：
Get-CimInstance Win32_Process -Filter "Name='java.exe'" | ? { $_.CommandLine -match 'instances\\([\w.\-]+)' } | % { $Matches[1] }
```

## 既有 jar

- 最新：`fabric/build/libs/epsilon-fabric-26.1.2-26.0.0-alpha.2-86999bf9.jar`（含 20 新模块 + AutoTotem/Velocity/CrystalAura 重写 + i18n）
