import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src') // 配置 @ 指向 src 目录
    }
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      // 当你请求 /api/xxx 时，Vite 会自动帮你代理到 http://localhost:8080/api/xxx
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 这里不需要 rewrite，因为我们后端的接口确实是带有 /api 的
      }
    }
  }
})