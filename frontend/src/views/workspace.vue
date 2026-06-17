<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '56px' : '200px'" class="main-aside">
      <!-- Logo 区域 — 点击返回首页 -->
      <div class="aside-logo" @click="goHome">
        <div class="logo-icon">
          <el-icon :size="22"><Ship /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">VesselEMS</span>
      </div>

      <!-- 菜单（可滚动） -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="aside-menu"
      >
        <template v-for="node in visibleMenus" :key="node.id">
          <!-- 页面 -->
          <el-menu-item v-if="node.menuType === 1" :index="resolvePath(node)">
            <el-icon v-if="node.menuIcon">
              <component :is="resolveIcon(node.menuIcon)" />
            </el-icon>
            <template #title>{{ node.menuName }}</template>
          </el-menu-item>

          <!-- 分组 -->
          <el-sub-menu v-else-if="node.menuType === 2 && hasVisibleChildren(node)" :index="String(node.id)">
            <template #title>
              <el-icon v-if="node.menuIcon">
                <component :is="resolveIcon(node.menuIcon)" />
              </el-icon>
              <span>{{ node.menuName }}</span>
            </template>
            <template v-for="child in node.children" :key="child.id">
              <!-- 二级：页面 -->
              <el-menu-item v-if="child.menuType === 1" :index="resolvePath(child)">
                {{ child.menuName }}
              </el-menu-item>
              <!-- 二级：分组（三级） -->
              <el-sub-menu v-else-if="child.menuType === 2 && hasVisibleChildren(child)" :index="String(child.id)">
                <template #title>{{ child.menuName }}</template>
                <el-menu-item v-for="sub in child.children" :key="sub.id"
                  v-show="sub.visible === 1 && hasMenu(sub.id)"
                  :index="resolvePath(sub)">
                  {{ sub.menuName }}
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-sub-menu>
        </template>
      </el-menu>

      <!-- 底部折叠按钮 -->
      <div class="aside-footer">
        <el-icon class="collapse-btn" :size="18" @click="isCollapse = !isCollapse">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
    </el-aside>

    <!-- 右侧区域 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="main-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="dashboardPath">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="hover">
            <div class="user-avatar-area">
              <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
              <span class="user-name">{{ displayName }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Ship, DataBoard, Setting, Cpu, User, Fold, Expand,
  UserFilled, ArrowDown, SwitchButton, ChatDotRound
} from '@element-plus/icons-vue'
import { userStore, logout } from '../stores/user.js'
import { hasMenu, loadPermissions, permissionStore, getDashboardPath } from '../stores/permissions.js'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const breadcrumbTitle = computed(() => {
  return route.meta?.title || ''
})

const displayName = computed(() => {
  return userStore.user?.username || ''
})

const dashboardPath = computed(() => getDashboardPath())

// 图标名到组件的映射（仅常用图标，可扩展）
const iconMap = {
  'Ship': Ship,
  'DataBoard': DataBoard,
  'Setting': Setting,
  'Cpu': Cpu,
  'User': User,
  'ChatDotRound': ChatDotRound
}

function resolveIcon(iconName) {
  return iconMap[iconName] || null
}

// 可见的 workspace 子菜单（已过滤 visible + 权限）
const visibleMenus = computed(() => {
  const wsNode = permissionStore.menuTree.find(n => n.menuComponent === 'workspace')
  const children = wsNode?.children || []
  if (!children.length) return []

  function filterVisible(list) {
    if (!list) return []
    return list
      .filter(n => n.visible === 1 && hasMenu(n.id))
      .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
      .map(n => ({
        ...n,
        children: n.children ? filterVisible(n.children) : []
      }))
      .filter(n => {
        if (n.menuType === 1) return true
        if (n.menuType === 2) return n.children && n.children.length > 0
        return false
      })
  }

  return filterVisible(children)
})

function hasVisibleChildren(node) {
  if (!node.children) return false
  return node.children.some(c => c.visible === 1 && hasMenu(c.id))
}

function resolvePath(node) {
  if (!node.menuPath) return '/workspace'
  return `/workspace/${node.menuPath}`
}

function goHome() {
  router.push('/')
}

function goProfile() {
  router.push('/workspace/profile')
}

function handleLogout() {
  logout()
  router.push('/login')
}

onMounted(() => {
  // 权限和路由已在 App.vue 启动时加载，此处不再重复加载
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

/* ===== 侧边栏 ===== */
.main-aside {
  background: #fcfcfb;
  border-right: 1px solid #f0efed;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.aside-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity 0.2s;
}
.aside-logo:hover {
  opacity: 0.9;
}

.logo-icon {
  color: #ffffff;
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
  white-space: nowrap;
}

.aside-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: transparent;
  padding: 4px 0;
  border-right: none;
}

.aside-menu .el-menu-item {
  margin: 1px 6px;
  border-radius: 6px;
  color: #57534e;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
}

.aside-menu .el-menu-item:hover {
  background-color: #f0efed;
  color: #1c1917;
}

.aside-menu .el-menu-item.is-active {
  background-color: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}

.aside-menu .el-sub-menu__title {
  margin: 1px 6px;
  border-radius: 6px;
  color: #57534e;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
}

.aside-menu .el-sub-menu__title:hover {
  background-color: #f0efed;
  color: #1c1917;
}

.aside-menu .el-menu--inline .el-menu-item {
  margin: 1px 6px 1px 20px;
  font-size: 12px;
  height: 32px;
  line-height: 32px;
}

.aside-footer {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid #f0efed;
  flex-shrink: 0;
}

.collapse-btn {
  cursor: pointer;
  color: #a8a29e;
  transition: color 0.2s;
  padding: 4px;
  border-radius: 4px;
}

.collapse-btn:hover {
  color: #2563eb;
  background-color: #f0efed;
}

/* ===== 顶栏 ===== */
.main-header {
  height: 56px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0efed;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-avatar-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-avatar-area:hover {
  background: #f5f5f4;
}

.user-avatar {
  background: #eff6ff;
  color: #2563eb;
}

.user-name {
  font-size: 13px;
  color: #1c1917;
  font-weight: 500;
}

.arrow-icon {
  color: #a8a29e;
  font-size: 12px;
}

/* ===== 内容区 ===== */
.main-content {
  background: #f5f5f4;
  padding: 20px;
  overflow-y: auto;
}
</style>