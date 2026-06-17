import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { checkAuth, userStore } from './stores/user'
import { loadPermissions, permissionStore } from './stores/permissions'
import { initDynamicRoutes } from './router/dynamicRoutes'

async function bootstrap() {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      await checkAuth()
      await loadPermissions()
      // 在 Vue 应用创建之前注册动态路由
      initDynamicRoutes(router, permissionStore.menuTree)
    } catch {
      // token 无效，清除
      localStorage.removeItem('token')
      userStore.user = null
      userStore.isAuthenticated = false
    }
  }

  const app = createApp(App)
  app.use(ElementPlus)
  app.use(router)

  // 全局注册所有 Element Plus 图标
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

  app.mount('#app')
}

bootstrap()