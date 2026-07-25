<h1 align="center">EpsilonBC</h1>
<h4 align="center">
    <p>
        <a href="./README.md">English</a> |
        <b>中文</b>
    </p>
</h4>

<p align="center">
  <a href="https://github.com/NDBlockConnect/EpsilonBC/actions"><img alt="构建状态" src="https://img.shields.io/badge/build-gradle-4c1?style=flat-square"></a>
  <a href="LICENSE"><img alt="开源协议" src="https://img.shields.io/badge/license-GPLv3-blue?style=flat-square"></a>
  <img alt="MC 版本" src="https://img.shields.io/badge/minecraft-26.1.2-62b47a?style=flat-square">
  <img alt="加载器" src="https://img.shields.io/badge/loaders-NeoForge%20%26%20Fabric-6a5acd?style=flat-square">
  <a href="https://github.com/NDBlockConnect/EpsilonBC/releases"><img alt="最新发布" src="https://img.shields.io/github/v/release/NDBlockConnect/EpsilonBC?include_prereleases&style=flat-square&label=latest"></a>
</p>

> [!IMPORTANT]
> **EpsilonBC 是 FishWish PvP 服务器专属的闭源 HvH 客户端。**
> 仅以混淆二进制形式分发给已注册的 HvH 玩家。
> 本仓库包含用于构建 EpsilonBC 的源代码 — 这**不是**面向公众的通用客户端。

> [!NOTE]
> ## 关于 EpsilonBC
> EpsilonBC 是原版 [Epsilon](https://github.com/NekoyaHouse/Epsilon) 项目的分支，由 **BlockConnect / StarsailsClover** 为 FishWish PvP 的 HvH 游戏模式开发和维护。
>
> 原 Epsilon 仓库已归档。EpsilonBC 在此基础上持续开发，新增 HvH 专属模块、多语言支持以及持续的兼容性更新。
>
> **内核版本：** Epsilon Kernel 2026.8.1 · **基础版本：** Epsilon 2026.8.2 · **渲染引擎：** OpenLumin v26.0 Alpha 0 (Lumin Graphics+)

## 📌 概览

一款现代化 Minecraft HvH 功能性客户端，专为 FishWish PvP 打造，基于 NeoForge & Fabric 双加载器，搭载先进渲染引擎（Lumin Graphics+）、完整 CJK 语言支持，以及持续扩展的 HvH 专属模块套件。

**当前版本：** v26.0 Alpha 2 · Minecraft 26.1.2

## 🆕 Alpha 2 更新亮点

完整变更日志请查看 [Release Notes](https://github.com/NDBlockConnect/EpsilonBC/releases/tag/v26.0.0-alpha.2)。

主要更新：
- **30+ 个全新模块**，涵盖战斗、移动、渲染、玩家四大类别
- **完整 HvH 识别系统**（AllyMarker 友军标记、AllyManager 友军管理、MatchDetector 对局检测、EnemyView 敌视渲染）
- **完整国际化支持** — 中文、韩语、日语、俄语全面翻译
- **游戏内账号切换**与 GitHub 自动更新检查
- **GUI 与稳定性修复** — 下拉菜单闪烁、瀑布式面板布局、单人模式断线等问题

## 🚀 插件系统

[Epsilon Addon 模板](https://github.com/slmpc/Epsilon-Addon-Template)

[插件开发指南](docs/addon-development.md)

## 🎨 图形系统

**Lumin Graphics+** 渲染引擎提供：
- 矩形与圆角矩形（含阴影）
- TTF 字体渲染（含 CJK 支持）
- 贴图、模糊效果与自定义顶点格式
- 声明式 UI 层（UiTree / UiScene）

详见 [Lumin Graphics README](common/src/main/java/com/github/epsilon/graphics/README.md) 与 [GUI 库使用指南](docs/gui-library.md)。

## ⚙️ 构建

> 需要 **完整 JDK 25**（非 JRE）。

```bash
# 构建所有加载器
./gradlew :fabric:jar :neoforge:jar -x test

# 运行开发客户端（Fabric）
./gradlew :fabric:runClient
```

## 🙏 致谢

特别感谢以下项目，第三方归属详见 [NOTICE](NOTICE.md)。

- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)
- Orbit · LeavesHack · TrollHack
- [Epsilon](https://github.com/NekoyaHouse/Epsilon) — NekoyaHouse 的原始项目

## 📝 许可证

本项目（含 Lumin Graphics）以 **GNU 通用公共许可证 v3.0** 授权。

Copyright © 2026 NekoyaHouse. 分支由 BlockConnect @ StarsailsClover 维护。
