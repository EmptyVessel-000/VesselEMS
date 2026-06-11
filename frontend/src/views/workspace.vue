<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="main-aside">
      <!-- Logo 区域 -->
      <div class="aside-logo">
        <el-icon :size="28"><Ship /></el-icon>
        <span v-show="!isCollapse" class="logo-text">VesselEMS</span>
      </div>

      <!-- 菜单（可滚动） -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#1e293b"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
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
    </el-aside>

    <!-- 右侧区域 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="main-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="22"
            @click="isCollapse = !isCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>

          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="dashboardPath">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="hover">
            <div class="user-avatar-area">
              <el-avatar :size="32" :icon="UserFilled" />
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

function goProfile() {
  router.push('/workspace/profile')
}

function handleLogout() {
  logout()
  router.push('/login')
}

onMounted(() => {
  if (!permissionStore.loaded) {
    loadPermissions()
  }
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.main-aside {
  background: #1e293b;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.aside-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #3b82f6;
  border-bottom: 1px solid #334155;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
  white-space: nowrap;
}

.aside-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.aside-menu .el-sub-menu .el-menu {
  background-color: #0f172a;
}

.aside-menu .el-menu-item:hover {
  background-color: #334155;
}

.aside-menu .el-menu-item.is-active {
  background-color: #2563eb;
}

.main-header {
  height: 60px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #6b7280;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #2563eb;
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
  border-radius: 8px;
  transition: background 0.2s;
}

.user-avatar-area:hover {
  background: #f1f5f9;
}

.user-name {
  font-size: 14px;
  color: #374151;
}

.arrow-icon {
  color: #9ca3af;
  font-size: 12px;
}

.main-content {
  background: #f1f5f9;
  padding: 20px;
  overflow-y: auto;
}
</style>