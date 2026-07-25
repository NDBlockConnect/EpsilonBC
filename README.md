# EpsilonBC Website

EpsilonBC 官方站点，部署于 GitHub Pages，使用自定义域名 `epsilonbc.n0th1n3ssd0ma1n.top`。

## 技术栈

- React 18 + TypeScript
- Vite 6
- TailwindCSS 3
- three.js + @react-three/fiber + @react-three/drei + @react-three/postprocessing（WebGL）

## 本地开发

```bash
npm install
npm run dev      # 启动开发服务器
npm run build    # 生产构建，输出至 dist/
npm run check    # TypeScript 类型检查
```

## 部署

推送到 `Website` 分支后，GitHub Actions 自动构建并部署到 GitHub Pages。
自定义域名通过 `public/CNAME` 配置，`public/.nojekyll` 禁用 Jekyll 处理。

## 设计文档

需求与技术架构见 `.trae/documents/`。
