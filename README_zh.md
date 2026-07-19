<h1 align="center">EpsilonBC</h1>
<h4 align="center">
    <p>
        <a href="./README.md">English</a> |
        <b>中文</b>
    </p>
</h4>

<p align="center">
  <a href="https://github.com/NekoyaHouse/Epsilon/actions"><img alt="构建状态" src="https://img.shields.io/badge/build-gradle-4c1?style=flat-square"></a>
  <a href="LICENSE"><img alt="开源协议" src="https://img.shields.io/badge/license-GPLv3-blue?style=flat-square"></a>
  <img alt="加载器支持" src="https://img.shields.io/badge/loaders-NeoForge%20%26%20Fabric%20%26%20Forge-6a5acd?style=flat-square">
  <a href="https://discord.gg/vYbaae3X7e"><img alt="Discord社区" src="https://img.shields.io/badge/Discord-加入服务器-5865F2?style=flat-square&logo=discord&logoColor=white"></a>
</p>

<p align="center">
  <b>BlockConnect Community Group</b>
</p>

<p align="center">
  <a href="https://qm.qq.com/q/71qUqMO87e"><img alt="BlockConnect QQ群" src="https://img.shields.io/badge/BlockConnect%20QQ群-加入群聊-12B7F5?style=flat-square&logo=tencentqq&logoColor=white"></a>
</p>

<p align="center">
  <sub>Epsilon 官方群</sub>
</p>

<p align="center">
  <a href="https://qm.qq.com/q/WPvwQZvYci"><img alt="一号QQ群" src="https://img.shields.io/badge/一号QQ群-加入群聊-12B7F5?style=flat-square&logo=tencentqq&logoColor=white"></a>
  <a href="https://qm.qq.com/q/3hhg8ww9ag"><img alt="二号QQ群" src="https://img.shields.io/badge/二号QQ群-加入群聊-12B7F5?style=flat-square&logo=tencentqq&logoColor=white"></a>
</p>

> [!IMPORTANT]
> ## 🔀 BlockConnect 复刻分支公告
> 本仓库是由 **BlockConnect** 社区维护的复刻分支，基于原版项目 [Epsilon](https://github.com/NekoyaHouse/Epsilon) 持续迭代开发。
>
> 原版 Epsilon 仓库已归档，官方公开开发工作停止。BlockConnect 团队复刻本项目，持续为社区进行维护、漏洞修复与新功能开发。
>
> **加入我们的社区：** [BlockConnect Community Group](https://qm.qq.com/q/71qUqMO87e)

> [!NOTE]
> ## 原版归档公告（来自 Epsilon）
> 原仓库已准备归档，源码仍公开留存以供查阅参考。主动公开开发、问题工单处理以及免费公开发版工作均已终止。
>
> 维护一款功能完善的客户端需要长期投入大量时间与资源。第三方私自打包修改构建并牟利，导致原公开开发模式无法持续运营。
>
> 仓库已发布的全部代码仍遵循 [GNU General Public License v3.0](LICENSE) 协议开源。本公告不会修改现有版本的协议条款与使用权限。

## 📌 项目概述
一款同时适配 Forge/NeoForge 与 Fabric 多加载器的现代化 Minecraft 多功能客户端，搭载高性能渲染系统与模块化架构。

## 🚀 拓展插件系统
[Epsilon Addon Template](https://github.com/slmpc/Epsilon-Addon-Template)

[插件开发指南](docs/addon-development.md)

## 🎨 图形渲染系统

Lumin 渲染引擎提供自定义渲染管线，支持以下功能：
- 矩形、圆角矩形绘制
- 阴影与模糊特效
- TTF 字体渲染
- 纹理贴图渲染
- 自定义顶点格式

详情查看 [Lumin Graphics README](common/src/main/java/com/github/epsilon/graphics/README.md)。

基于 Lumin 构建的声明式UI层相关文档见 [Epsilon GUI Library Guide](docs/gui-library.md)。

## ⚙️ 构建与运行

```bash
# 构建模组
./gradlew build

# 启动游戏客户端
./gradlew runClient
```

## 🙏 致谢特别感谢以下项目，第三方代码归属说明详见 NOTICE 文件。

Meteor Client

Orbit

LeavesHack

TrollHack

原项目仓库Epsilon — 由 NekoyaHouse 开发的原版项目，本复刻分支基于此开发。

## 📝 开源协议

本项目（含 Lumin Graphics）采用 GNU General Public License v3.0 协议开源。

版权所有 © 2026 NekoyaHouse。

由 BlockConnect@StarsailsClover 复刻维护
