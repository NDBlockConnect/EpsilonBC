export interface FeatureItem {
  index: string;
  title: string;
  description: string;
  icon: "layers" | "sparkles" | "grid" | "puzzle";
}

export interface GraphicsCapability {
  name: string;
  description: string;
}

export interface ReleaseChannel {
  branch: string;
  version: string;
  status: "stable" | "alpha" | "dev";
  mcVersion: string;
  loaders: string[];
  description: string;
}

export interface CommunityLink {
  name: string;
  description: string;
  url: string;
  icon: "message" | "github" | "book";
}

export const FEATURES: FeatureItem[] = [
  {
    index: "01",
    title: "多加载器架构",
    description:
      "统一代码库同时支持 NeoForge、Forge 与 Fabric，通过 common 公共模块隔离加载器差异，一次编写多处运行。",
    icon: "layers",
  },
  {
    index: "02",
    title: "Lumin 渲染系统",
    description:
      "自研渲染管线提供矩形、圆角矩形、阴影、模糊、TTF 字体与纹理的统一抽象，现已由 OpenLumin 承载底层能力。",
    icon: "sparkles",
  },
  {
    index: "03",
    title: "模块化设计",
    description:
      "声明式 UiTree 与 UiScene 调度器将界面、布局与渲染解耦，模块按语义层合批，支持自适应 QuadTree 优化。",
    icon: "grid",
  },
  {
    index: "04",
    title: "插件生态",
    description:
      "通过 EpsilonAddon 基类与跨加载器注册入口，第三方可在 Fabric 与 NeoForge 上以同一套 API 扩展客户端。",
    icon: "puzzle",
  },
];

export const GRAPHICS_CAPABILITIES: GraphicsCapability[] = [
  { name: "Rectangles & Round Rectangles", description: "基础图元与圆角几何的批量化绘制" },
  { name: "Shadows & Blur", description: "高质量阴影与模糊后处理" },
  { name: "TTF Font Rendering", description: "矢量字体的字形缓存与按需扩容" },
  { name: "Texture Rendering", description: "按纹理分桶的纹理批次管理" },
  { name: "Custom Vertex Formats", description: "自定义顶点格式与 LuminRingBuffer" },
  { name: "Layered Render Pipeline", description: "BACKGROUND 到 OVERLAY 的六层语义调度" },
];

export const RELEASE_CHANNELS: ReleaseChannel[] = [
  {
    branch: "26.1.x",
    version: "v26.0 Alpha 2",
    status: "alpha",
    mcVersion: "Minecraft 26.1.2",
    loaders: ["NeoForge", "Fabric", "Forge"],
    description: "当前主线稳定分支，承载 Lumin 图形系统迁移与 GUI 重构成果。",
  },
  {
    branch: "26.2.x",
    version: "开发中",
    status: "dev",
    mcVersion: "Minecraft 26.2.x",
    loaders: ["NeoForge", "Fabric"],
    description: "面向下一版本的开发分支，包含 prism-rhi 等实验性渲染接口探索。",
  },
];

export const COMMUNITY_LINKS: CommunityLink[] = [
  {
    name: "BlockConnect 社区群",
    description: "加入 QQ 群参与 EpsilonBC 的社区维护与反馈",
    url: "https://qm.qq.com/q/71qUqMO87e",
    icon: "message",
  },
  {
    name: "GitHub 仓库",
    description: "查看源码、提交 Issue 与 Pull Request",
    url: "https://github.com/NDBlockConnect/EpsilonBC",
    icon: "github",
  },
  {
    name: "插件开发指南",
    description: "EpsilonAddon 跨加载器插件开发文档",
    url: "https://github.com/NDBlockConnect/EpsilonBC/blob/26.1.x/docs/addon-development.md",
    icon: "book",
  },
];

export const SITE_META = {
  name: "EpsilonBC",
  symbol: "ε",
  tagline: "Lumin Rendering Client",
  description:
    "由 BlockConnect 社区维护的现代多加载器 Minecraft 实用工具客户端，搭载 Lumin 渲染系统与模块化架构。",
  repoUrl: "https://github.com/NDBlockConnect/EpsilonBC",
  releasesUrl: "https://github.com/NDBlockConnect/EpsilonBC/releases",
  upstreamName: "Epsilon",
  upstreamAuthor: "NekoyaHouse",
  upstreamUrl: "https://github.com/NekoyaHouse/Epsilon",
  openLuminUrl: "https://github.com/NDBlockConnect/OpenLumin",
  addonTemplateUrl: "https://github.com/slmpc/Epsilon-Addon-Template",
  license: "GNU General Public License v3.0",
  copyright: "Copyright © 2026 NekoyaHouse. Forked by BlockConnect@StarsailsClover",
};
