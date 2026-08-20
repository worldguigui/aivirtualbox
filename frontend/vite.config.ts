import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // 开发环境下将后端接口代理到 Spring Boot(:8080),避免跨域
    proxy: {
      '/api': 'http://localhost:8080',
      '/step': 'http://localhost:8080',
      '/state': 'http://localhost:8080',
      '/test': 'http://localhost:8080',
    },
  },
})
