import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

const apiProxyTarget = process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')  // @ 代表 src 目录，可以使用 @/xxx 导入
    }
  },
  server: {
    port: 5173,              // 前端开发服务器端口
    open: true,              // 启动时自动打开浏览器
    proxy: {
      '/api': {              // 以 /api 开头的请求会被代理
        target: apiProxyTarget,  // 后端服务地址
        changeOrigin: true,  // 改变请求头中的 origin，解决跨域
        secure: false        // 如果是 https 接口，需要配置这个参数
      }
    }
  }
})
