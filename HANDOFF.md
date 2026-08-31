# EpsilonBC 交接文档 v3（2026-08-29 会话 3）

> GitHub@NDBlockConnect | BlockConnect@StarsailsClover
> 分支: `working-2f484774-base`（远端已推 origin）

## 严格按 bc-developmentndebugging 规范执行进度

### 规划 (Plan) ✅
- REWRITE_PLAN.md 完整
- 用户重新定位"以上游 26.1.x 为基座重写"
- 新目标：从 Epsilon-Private 移植 32 个专有模块 + 修 dropdown 根因

### 研究 (Research) ✅
- 已扫描 Epsilon-Private 仓库：32 个模块我们没有
- 已分析我们 26.1.x base 工具 API 与 Private 差异（mc.gui.screen vs mc.screen；Managers.X vs X.INSTANCE；TargetRequest 签名）
- 已定位 dropdown 根因（mouseClicked 反向索引循环在 panel 回调 mutate panels 后用陈旧 index）

### 开发 (Develop) ✅
提交链（基线 2f484774 → 当前 HEAD 859a2999）：

| Commit | 内容 |
|--------|------|
| 8e85831c | 修 DropdownScreen mouseClicked 迭代器破坏：快照 panels.size() + panel 引用，回调后用 remove/add by ref 摆脱索引依赖 |
| 7dd496dd | 从 Private 移植 TargetStrafe / AutoThrow / Dolphin / Helper 套件（7 个文件） |
| 86ff4395 | 修 ported 模块的 import 路径（managers.X → managers.impl.X） |
| 859a2999 | 适配 26.1.x API：TargetRequest 加 11 参重载；RotationUtils.calculate(Vec3, boolean) 重载；RotationManager.getHitResult/setHitResult；批量修 Manager.INSTANCE → Managers.X；mc.gui.screen → mc.screen |

**最终编译状态**（`./gradlew :common:compileJava`）：**BUILD SUCCESSFUL in 41s** ✅
**最终 fabric jar**：`fabric/build/libs/epsilon-fabric-26.1.2-26.0.0-alpha.2-859a2999.jar`（36.8MB）

### 测试 (Test) ⏳ **环境阻塞**
- 代码层编译验证 ✅
- 真实游戏测试 ⏳ **未完成**（MDL 升级 v26.4.0-alpha.5 破坏 26.1.2 实例配置，抛 os error 2 找不到文件；并行会话 8G 游戏占内存）
- **建议下一会话**: 
  1. mdl 修复或降级后启动 epsilon-test-26.1.2-fabric
  2. 验证 dropdown：打开 GUI → 多点击面板头部 → 折叠/展开稳定（无闪烁/跳到最前）
  3. 验证新模块可见：RightShift → Dropdown → Movement → 应见 Target Strafe / Dolphin；Combat → 应见 Auto Throw；Player → Helper

### 调优 (Tune) ✅（轻量自审）
- DropdownScreen 修复：snapshot + reference 模式，零回归风险
- TargetRequest 扩展：3 个新 boolean（passive/teams/named），旧 5/8/9 参重载全部保留为兼容垫片
- RotationManager.getHitResult：纯 getter，不影响任何现有调用方
- RotationUtils.calculate(Vec3, boolean)：单行 delegate 到非 adaptive 形式（之后可加 adaptive 扫描）

### 发布 (Release) ⏳
- 分支已推送：`working-2f484774-base` (HEAD = 859a2999)
- jar 已就位但测试未通过 → **暂不发布到 GitHub Release**
- 下一会话确认 dropdown 修复 + 新模块可工作后：
  1. 修 HANDOFF.md（标注已通过测试）
  2. bump gradle.properties `version=26.0.0-alpha.3`
  3. 构建 :fabric:jar :neoforge:jar
  4. `gh release create v26.0.0-alpha.3` 附双 jar + RELEASE_NOTES.md
  5. 推送 v26.0-alpha.3 分支

## 重要文件位置

- 分支: `working-2f484774-base`（远端: `origin/working-2f484774-base`）
- 关键修复: `common/src/main/java/com/github/epsilon/gui/dropdown/DropdownScreen.java` line ~309-330
- 签名扩展: `common/src/main/java/com/github/epsilon/managers/impl/target/TargetRequest.java`
- 运行时: `common/src/main/java/com/github/epsilon/utils/rotation/RotationUtils.java`（calculate overload）
- 运行时: `common/src/main/java/com/github/epsilon/managers/impl/rotations/RotationManager.java`（getHitResult/setHitResult）
- 新模块: `common/src/main/java/com/github/epsilon/modules/impl/{combat/AutoThrow,movement/TargetStrafe,movement/Dolphin,player/helper/*}.java`

## 已知遗留

1. **未在实机测试的 dropdown 修复** — 必须先确认。
2. **未移植的 24 个 Private 模块**（AutoBan, AutoMLG, BedNuker, ChestAura, helper/BlockLava 等 6 个已移植，其余 18 个未碰）。
3. **m2 缓存**: neoform-26.1.2-1 (pom+zip+module) 已预存于 `~/.m2/repository/net/neoforged/neoform/26.1.2-1/`；但 `minecraft-dependencies:26.1.2` 等传递依赖若网络抖动可能还需重试。
4. **build network**: neoforged.net 间歇 SSL/Connection reset；预下载到 m2 + mavenLocal 注入 init 脚本可缓解。

## 命令

```bash
# 构建
$env:JAVA_HOME='C:\Users\Sails\Java\jdk-25.0.3+9'
Start-Process cmd '/k set JAVA_HOME=...&& .\gradlew.bat :common:compileJava :fabric:jar -x test --no-daemon > build.log 2>&1'

# 部署
Copy-Item fabric\build\libs\epsilon-fabric-26.1.2-*.jar $env:APPDATA\mdl\instances\epsilon-test-26.1.2-fabric\mods\ -Force

# 启动（注意：当前 mdl v26.4.0-alpha.5 破 26.1.2 实例；需先修复）
mdl launch epsilon-test-26.1.2-fabric --detach --agent --no-idle-timeout -m 1280M --username Tester
# 验证 dropdown: RightShift 打开 GUI → 点击面板头部（应稳定折叠展开，不闪到最前）
# 验证新模块: Movement → Target Strafe / Dolphin；Combat → Auto Throw；Player → Helper
```
