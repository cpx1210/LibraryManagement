import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'  // 暗色主题支持
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'  // 中文语言包
import router from './router'
import App from './App.vue'
import './style.css'

const app = createApp(App)

// 注册 Pinia 状态管理
app.use(createPinia())

// 注册 Vue Router 路由管理
app.use(router)

// 注册 Element Plus UI 组件库（配置中文）
app.use(ElementPlus, {
  locale: zhCn,  // 设置语言为简体中文
})

app.mount('#app')
