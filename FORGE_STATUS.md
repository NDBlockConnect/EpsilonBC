# Forge 支持状态 (26.2.x-forge)

## 当前状态

⏳ **等待 ForgeGradle 工具链更新**

## 问题说明

Forge 配置已完成，但由于 ForgeGradle 工具链尚未完全支持 Minecraft 26.2 的新 JSON 格式，构建过程会失败。

### 技术细节

- **Minecraft 版本**: 26.2
- **Forge 版本**: 65.0.4
- **ForgeGradle 版本**: 6.0.30
- **Parchment 版本**: 2026.07.13-26.2

### 失败原因

ForgeGradle 在 `extractSrg` 任务中尝试从 Mojang 的版本清单中查找 `client_mappings` 字段，但 Minecraft 26.x 系列的 JSON 格式已经改变，不再包含该字段。

## 配置文件

已完成的配置：
- ✅ `build.gradle.kts` - 已添加 ForgeGradle 和 Parchment 插件
- ✅ `settings.gradle.kts` - 已添加 MinecraftForge 和 ParchmentMC 仓库
- ✅ `gradle/libs.versions.toml` - 已配置 Forge 和 Parchment 版本
- ✅ `forge/build.gradle.kts` - Forge 子模块构建配置
- ✅ `forge/src/main/resources/META-INF/mods.toml` - Mod 元数据配置

## 预期构建命令

等工具链就绪后，可以使用：

```bash
# 构建 Forge 版本
./gradlew :forge:build

# 运行 Forge 客户端
./gradlew :forge:runClient
```

## 监控更新

请关注：
- [ForgeGradle GitHub](https://github.com/MinecraftForge/ForgeGradle)
- [MinecraftForge 论坛](https://forums.minecraftforge.net/)

等待 ForgeGradle 7.x 或更新版本发布，以支持 Minecraft 26.x 系列。

## 分支信息

- **主分支**: 26.2.x (NeoForge)
- **Forge 分支**: 26.2.x-forge (当前分支)

---

*最后更新: 2026-07-21*
