# EpsilonBC 浜ゆ帴鏂囨。锛?026-08-25 浼氳瘽锛?
> GitHub@NDBlockConnect | BlockConnect@StarsailsClover

## 鏈細璇濆畬鎴?
### 1. 涓婃父鍚堝苟锛圢ekoyaHouse/Epsilon 寮烘帹鍚庣殑 26.1.x锛?- **宸插悎鍏?*锛歀ua 鑴氭湰绯荤粺锛坈cece482锛孭R #420锛夆啋 commit `1817430d`
  - 16 涓?Lua 鏍稿績鏂囦欢 + ChoiceSetting 涓変欢濂?GUI + Addon 闈㈡澘鎶借薄
  - 娓叉煋闈紙LuaRender2DService/LuaUiContext/LuaEventListener锛夊凡閫傞厤鍐呭缓 gui.lib 鏍?  - 鏂板鍐呭缓 shim锛歚BuiltInTextMetrics`銆乣UiTree.nodeCount()/text(String瀛椾綋鍚?/texture(Identifier,5鍙?`銆乣EpsilonUiTheme.lumin()` 妗?- **鏈悎鍏ワ紙鏈夋剰锛?*锛?  - d3baeed8 Lumin Graphics 澶栭儴搴撹縼绉伙紙閲囨贩鍚堟柟妗堬紝寰?OpenLumin 绋冲畾鍚庡叏閲忓垏鎹級
  - 1abf752e/6a31908d/38071a78/a59a0769 浠樿垂閿佸畾鍥涜繛锛堢粷涓嶅悎鍏ワ級

### 2. 绱ф€ヤ慨澶嶏紙commit ff7b772c + 1817430d 鍐咃級
| 淇 | 鏍瑰洜 |
|------|------|
| MovementFix 鍏柟鍚戞灇涓惧尮閰?| 绂绘暎璞￠檺鏄犲皠鍦ㄨ竟鐣?璐熻澶辨晥锛涗繚鐣欏弻 yaw 闈欓粯鏃嬭浆鍚堝悓 |
| Jesus 璁╀綅 HoleSnap | 鍚屼紭鍏堢骇鍨傜洿鍐欏叆绔炰簤 |
| Speed 闆惰緭鍏ラ槻寰?| 26.2 杈撳叆閾捐矾璇绘暟涓嶅悓姝ユ椂 setHorizontal(0,0) 閽夋绉诲姩 |
| Scaffold motionAim NaN 瀹堝崼 | deltaMovement 闈炴湁闄愬€兼薄鏌撴悳绱㈠熀鐐?|
| ESP2D getPosition+AABB 鎶曞奖 | 鎵嬪姩 xOld lerp 鍦?26.2 婕傜Щ |
| epsilon-core 7 宸ュ叿绫绘仮澶?| 宸ヤ綔鏍戣鏃犳浛浠ｅ垹闄わ紙Rot2f/Priority/Animation/Easing/ColorUtils/MathUtils/TimerUtils锛?|

### 3. 瀹炴祴缁撹
- 绉诲姩锛氬噣閰嶇疆 5.34 鏍?3s锛涘叏閰嶇疆锛?5 妯″潡锛?4.3 鏍?3s 鉁?- **"26.2 鏃犳硶绉诲姩"鐪熷洜 = 涓栫晫鍒濇鍔犺浇鏈熸湇鍔″櫒 tick 鎷ュ**锛堥潪浠ｇ爜 bug锛夛紝
  鍙犲姞骞惰浼氳瘽 8G 娓告垙鍐呭瓨绔炰簤浼氭樉钁楀欢闀挎嫢濉炵獥鍙?- ESP 妗嗙兢姝ｇ‘杩借釜浼犻€佸疄浣撳爢鍙?鉁?
### 4. 宸ョ▼鐜鏁欒锛堣娉級
- `~/.m2` 娈嬬己 neoform-runtime POM锛堟棤 .module锛? 鍏ㄥ眬 init 鑴氭湰娉ㄥ叆 mavenLocal()
  鈫?capability 瑙ｆ瀽澶辫触銆?*宸叉竻** net/neoforged 鏍戯紱鍕垮啀寰€ m2 濉?net.neoforged
- 骞惰 Agent 浼氳瘽鐨?mdl 鍚姩浼氳Е鍙?OOM 淇濇姢娓呮壂"璇潃鎴戜滑鐨勬父鎴?鏋勫缓杩涚▼锛?  澶у唴瀛樻父鎴忥紙Xmx8G锛夎繍琛屾湡闂?FreeVirt<3GB锛岀姝㈠惎鍔ㄦ柊娓告垙
- 娓告垙楠岃瘉鐢ㄥ垎绂?cmd + `cmd /k` 鍓嶅彴 holder锛涙祴瀹岀珛鍗?`Stop-Process` 鏉€娓告垙锛堥槻绯荤粺 OOM锛?- PowerShell 5.1 `Set-Content -Encoding UTF8` 甯?BOM 鈫?javac 鎶ラ潪娉曞瓧绗︼紱鐢?  `[IO.File]::WriteAllText(path, content, UTF8Encoding($false))`
- 灞忓箷鐐瑰嚮鐢ㄧ湡瀹?OS 浜嬩欢锛圫etCursorPos+mouse_event锛屾瘮渚嬪潗鏍囷級锛?  Despotes 娉ㄥ叆鐐瑰嚮瀵硅嚜缁?Screen 鏃犳晥锛涢敭鐩?Tab/Enter 鍙湪鍘熺敓 Screen 鏈夋晥

## 寰呭姙锛堜笅涓€浼氳瘽锛?1. **瀹炴満楠岃瘉 Lua**锛歸atcher 鑴氭湰鍦?`C:\Users\Sails\AppData\Local\Temp\opencode\verify_watcher.ps1`
   锛堢瓑 FreeVirt>10GB 鑷姩璺戯細绉诲姩+ESP 鎴浘+Lua 鏃ュ織鍙栬瘉+鑷姩鏉€娓告垙锛夛紱
2. ✅ **Alpha 3 已发布**: https://github.com/NDBlockConnect/EpsilonBC/releases/tag/v26.0.0-alpha.3 （cfdfb48f + 标签 v26.0.0-alpha.3 + Fabric/NeoForge 双 jar）
   （Release Note: releases/v26.0-alpha.3/RELEASE_NOTES.md；实机 Lua 运行验证仍待内存窗口，见待办 1）

## 重写战役状态（REWRITE_PLAN.md）
- R1 ✅ EpsilonShot UiRuntime 抽象（platform/UiRuntime+Registry+InternalUiRuntime，commit 4bb644e8）
- R3 ✅(部分) ESP 隐形实体过滤（ESP2D/Tracers/EnemyView/Hitboxes，Show Invisible 默认关）；相机快照数学经与上游对照为等价，非根因
- R2 ⏳ upstream 新 GUI 移植（84 文件清单：git ls-tree upstream/26.1.x -- .../gui/）：
  1. 批量 checkout + 导入重映射（slmpc→gui.lib / graphics.text.IconChars，同 Lua 移植脚本模式）
  2. MinecraftUiRuntime2612.current()→UiRuntime.current()；runtime.*→UiRuntime 抽象调用
  3. 编译器驱动修残；FontPathResolver 需从 upstream d3baeed8 补移植
  4. 陪伴系统再集成：ReisaDropdownCompanion.react 调用点在 DropdownScreen/AbstractDropdownPanel/
     ModuleButton/widgets（grep react\(Reisa 即全清单），逐文件回插
  5. 三方合并警告：勿整文件覆盖我们已含陪伴集成的版本
- R5 ⏳ Aprism spike：mdl launch --aprism + AprismRefract 桥调研
4. **Jesus 姘撮潰瀹炴祴**锛氶渶姘村煙鍦烘櫙锛堝綋鍓嶆祴璇曚笘鐣屼负闆師鍐版箹锛?5. **Despotes 鍏虫満鎸傝捣涓婃姤**锛欻TTP-Dispatcher 闈?daemon 绾跨▼瀵艰嚧 post-main watchdog
   宕╂簝鎶ュ憡锛圖espotes 渚?bug锛岄潪鏈粨搴擄級
6. **OpenLumin 娣峰悎鏂规鎺ㄨ繘**锛氬弬鑰冩枃妗ｅ凡鎻愪氦 OpenLumin 浠撳簱
   `docs/references/epsilon-lumin-consumption/`锛坋63f32f锛?
## 鍏抽敭鍛戒护
```powershell
# 鏋勫缓锛堝垎绂昏繘绋嬮槻瓒呮椂鏉€锛?$env:JAVA_HOME='C:\Users\Sails\Java\jdk-25.0.3+9'
Start-Process cmd '/k set JAVA_HOME=C:\Users\Sails\Java\jdk-25.0.3+9&& .\gradlew.bat :fabric:jar :neoforge:jar -x test --no-daemon > build.log 2>&1'
# 閮ㄧ讲
Copy-Item fabric\build\libs\epsilon-fabric-*.jar $env:APPDATA\mdl\instances\epsilon-test-26.2-fabric\mods\ -Force
# 鍚姩锛坅gent 妯″紡锛?mdl launch epsilon-test-26.2-fabric --detach --agent --no-idle-timeout -m 2G --username Tester
```
