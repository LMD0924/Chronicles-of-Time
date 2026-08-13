/**
 * 文件说明：拾光记后台管理系统拾光记项目脚本模块，封装拾光记项目相关的配置、状态、路由或工具逻辑。
 */
import { fileURLToPath, URL } from 'node:url'
import { Agent } from 'node:http'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools({
      launchEditor:'idea',
    }),
  ],
  server: {
    port: 5174,
    open: true,
    proxy: {
      '/api/auth': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        agent: new Agent({ keepAlive: false }),
      },
      '/api/admin': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        agent: new Agent({ keepAlive: false }),
      },
      '/api': {
        target: 'http://127.0.0.1:8500',
        changeOrigin: true,
        agent: new Agent({ keepAlive: false }),
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
