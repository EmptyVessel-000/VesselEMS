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

  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { UserFilled, Document, School, Collection } from '@element-plus/icons-vue'
import { userStore } from '../../stores/user.js'
import request from '../../api/request.js'

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

</style>
