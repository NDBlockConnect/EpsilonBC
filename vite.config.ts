import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tsconfigPaths from "vite-tsconfig-paths";

// https://vite.dev/config/
// 使用相对路径，同时兼容项目页 URL (ndblockconnect.github.io/EpsilonBC/)
// 与自定义域名根路径 (epsilonbc.n0th1n3ssd0ma1n.top)
export default defineConfig({
  base: "./",
  build: {
    sourcemap: 'hidden',
    chunkSizeWarningLimit: 1500,
  },
  plugins: [
    react({
      babel: {
        plugins: [
          'react-dev-locator',
        ],
      },
    }),
    tsconfigPaths()
  ],
})
