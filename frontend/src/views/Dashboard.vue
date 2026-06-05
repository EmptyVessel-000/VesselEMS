<template>
  <div class="dashboard">
    <!-- 欢迎区 -->
    <div class="welcome-section">
      <h2 class="welcome-title">欢迎回来，{{ displayName }}</h2>
      <p class="welcome-subtitle">这是 VesselEMS 管理系统的总览页面</p>
    </div>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #eff6ff; color: #2563eb">
              <el-icon :size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">系统用户数</p>
              <p class="stat-value">{{ stats.userCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #ecfdf5; color: #10b981">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">文档数量</p>
              <p class="stat-value">{{ stats.taskCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fffbeb; color: #f59e0b">
              <el-icon :size="28"><School /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">学生总数</p>
              <p class="stat-value">0</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0fdfa; color: #06b6d4">
              <el-icon :size="28"><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">知识库条目</p>
              <p class="stat-value">0</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速入口 -->
    <el-card class="quick-entry" shadow="hover">
      <template #header>
        <span>快速入口</span>
      </template>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" v-for="item in quickLinks" :key="item.path">
          <router-link :to="item.path" class="quick-link-item">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { UserFilled, Document, School, Collection } from '@element-plus/icons-vue'
import { userStore } from '../stores/user.js'
import request from '../api/request.js'

const displayName = computed(() => userStore.user?.username || '')

const stats = reactive({
  userCount: 0,
  taskCount: 0
})

async function fetchStats() {
  try {
    const data = await request.get('/api/dashboard/stats')
    if (data) {
      stats.userCount = data.userCount || 0
      stats.taskCount = data.taskCount || 0
    }
  } catch {
    // 后端未启动时静默失败，保留默认值
  }
}

const quickLinks = [
  { path: '/main/system/users', label: '用户管理', icon: 'UserFilled' },
  { path: '/main/rag/documents', label: '文档管理', icon: 'Document' },
  { path: '/main/user/profile', label: '个人中心', icon: 'User' }
]

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard {
  height: 100%;
}

.welcome-section {
  margin-bottom: 24px;
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.welcome-subtitle {
  font-size: 14px;
  color: #6b7280;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

/* 快速入口 */
.quick-entry {
  /* no extra margin */
}

.quick-link-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  color: #374151;
  text-decoration: none;
  transition: all 0.2s;
  font-size: 14px;
}

.quick-link-item:hover {
  background: #f1f5f9;
  color: #2563eb;
}
</style>