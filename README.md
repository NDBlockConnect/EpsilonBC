<h1 align="center">EpsilonBC</h1>
<h4 align="center">
    <p>
        <b>English</b> |
        <a href="./README_zh.md">中文</a>
    </p>
</h4>

<p align="center">
  <a href="https://github.com/NDBlockConnect/EpsilonBC/actions"><img alt="Build" src="https://img.shields.io/badge/build-gradle-4c1?style=flat-square"></a>
  <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/license-GPLv3-blue?style=flat-square"></a>
  <img alt="MC Version" src="https://img.shields.io/badge/minecraft-26.1.2-62b47a?style=flat-square">
  <img alt="Loaders" src="https://img.shields.io/badge/loaders-NeoForge%20%26%20Fabric-6a5acd?style=flat-square">
  <a href="https://github.com/NDBlockConnect/EpsilonBC/releases"><img alt="Release" src="https://img.shields.io/github/v/release/NDBlockConnect/EpsilonBC?include_prereleases&style=flat-square&label=latest"></a>
</p>

> [!IMPORTANT]
> **EpsilonBC is a closed-source HvH client for FishWish PvP server.**
> Distributed exclusively as an obfuscated binary to registered HvH players.
> This repository contains the source from which EpsilonBC is built — it is **not** a general-purpose public client.

> [!NOTE]
> ## About EpsilonBC
> EpsilonBC is a fork of the original [Epsilon](https://github.com/NekoyaHouse/Epsilon) project, developed and maintained by **BlockConnect / StarsailsClover** for FishWish PvP's HvH game mode.
>
> The original Epsilon repository has been archived. EpsilonBC continues development with new HvH-focused modules, multi-language support, and ongoing compatibility updates.
>
> **Engine:** Epsilon Kernel 2026.8.1 · **Base:** Epsilon 2026.8.2 · **Rendering:** OpenLumin v26.0 Alpha 0 (Lumin Graphics+)

## 📌 Overview

A modern Minecraft HvH utility client for FishWish PvP, built on NeoForge & Fabric with advanced rendering (Lumin Graphics+), full CJK language support, and a growing suite of HvH-specific modules.

**Current release:** v26.0 Alpha 2 · Minecraft 26.1.2

## 🆕 What's New in Alpha 2

See the full [Release Notes](https://github.com/NDBlockConnect/EpsilonBC/releases/tag/v26.0.0-alpha.2) for the complete changelog.

Highlights:
- **30+ new modules** across Combat, Movement, Render, and Player categories
- **Complete HvH identification system** (AllyMarker, AllyManager, MatchDetector, EnemyView)
- **Full i18n** — Chinese, Korean, Japanese, Russian translations
- **In-game account switching** and GitHub update checker
- **GUI & stability fixes** — dropdown flicker, waterfall panel layout, singleplayer disconnect, and more

## 🚀 Addon System

[Epsilon Addon Template](https://github.com/slmpc/Epsilon-Addon-Template)

[Addon Development Guide](docs/addon-development.md)

## 🎨 Graphics System

The **Lumin Graphics+** rendering engine provides:
- Rectangles & Round Rectangles with shadows
- TTF Font rendering (with CJK support)
- Texture, blur, and custom vertex formats
- Declarative UI layer (UiTree / UiScene)

See [Lumin Graphics README](common/src/main/java/com/github/epsilon/graphics/README.md) and [GUI Library Guide](docs/gui-library.md).

## ⚙️ Build

> Requires **JDK 25** (full JDK, not JRE).

```bash
# Build all loaders
./gradlew :fabric:jar :neoforge:jar -x test

# Run dev client (Fabric)
./gradlew :fabric:runClient
```

## 🙏 Credits

Special thanks to the following projects. See [NOTICE](NOTICE.md) for third-party attributions.

- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)
- Orbit · LeavesHack · TrollHack
- [Epsilon](https://github.com/NekoyaHouse/Epsilon) — original project by NekoyaHouse

## 📝 License

This project, including Lumin Graphics, is licensed under the **GNU General Public License v3.0**.

Copyright © 2026 NekoyaHouse. Fork maintained by BlockConnect @ StarsailsClover.
