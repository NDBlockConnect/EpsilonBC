# Forge 版本状态

## 当前状态：⏳ 等待 ForgeGradle 更新

### 问题说明

Minecraft 26.1.2 使用了新的版本号系统（年份.游戏更新.补丁），Forge 官方已经发布了对应的版本 `26.1.2-64.0.12`，但 **ForgeGradle 构建工具链还不支持新版本的 JSON 格式**。

### 技术细节

- Minecraft 26.1.2 的版本清单 JSON 中**没有 `client_mappings` 字段**
- ForgeGradle 6.0.30 在 `extractSrg` 任务中硬编码查找这个字段
- 错误信息：`26.1.2.json missing download for client_mappings`

### 已尝试的解决方案

- ✅ 更新到最新的 Forge 版本 (64.0.12)
- ✅ 更新 ForgeGradle 到 6.0.30
- ✅ 尝试使用 Parchment mappings 代替官方 mappings
- ❌ 所有方案均失败，问题出在 ForgeGradle 底层

### 当前配置

```kotlin
// forge/build.gradle.kts
plugins {
    id("multiloader-loader")
    id("net.minecraftforge.gradle") version "6.0.30"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}

dependencies {
    minecraft("net.minecraftforge:forge:26.1.2-64.0.12")
    // ...
}

minecraft {
    mappings("parchment", "2026.04.13-26.1.2")
    // ...
}
```

### 解决方案

需要等待以下之一：
1. ForgeGradle 7.x 或更新版本发布，支持 26.x 版本系列
2. Forge 官方发布针对 26.x 的特殊构建说明
3. Mojang 在后续版本中恢复 `client_mappings` 字段

### 替代方案

目前项目的 **NeoForge 版本正常工作**，可以使用 NeoForge 版本进行开发和测试。

---

**最后更新**：2026-07-21  
**等待组件**：ForgeGradle 7.x 或官方指导
