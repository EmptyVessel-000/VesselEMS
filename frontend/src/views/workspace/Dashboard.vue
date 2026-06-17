<template>
  <div class="dashboard">
    <!-- 欢迎区 -->
    <div class="welcome-section">
      <div class="welcome-card">
        <div class="welcome-text">
          <h2 class="welcome-title">欢迎回来，{{ displayName }}</h2>
          <p class="welcome-subtitle">VesselEMS 管理系统总览</p>
        </div>
        <div class="welcome-icon">
          <el-icon :size="64"><Ship /></el-icon>
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-inner">
          <div class="stat-icon stat-icon-blue">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.userCount }}</p>
            <p class="stat-label">用户总数</p>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card-inner">
          <div class="stat-icon stat-icon-green">
            <el-icon :size="24"><Menu /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.menuCount }}</p>
            <p class="stat-label">页面总数</p>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card-inner">
          <div class="stat-icon stat-icon-amber">
            <el-icon :size="24"><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.dsCount }}</p>
            <p class="stat-label">数据源总数</p>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card-inner">
          <div class="stat-icon stat-icon-cyan">
            <el-icon :size="24"><DataBoard /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.modelCount }}</p>
            <p class="stat-label">模型总数</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 下栏：快捷入口 + 最近活动 -->
    <div class="bottom-section">
      <div class="quick-actions">
        <h3 class="section-title">
          <el-icon :size="18"><Opportunity /></el-icon>
          快捷操作
        </h3>
        <div class="action-list">
          <div class="action-card">
            <div class="action-icon action-icon-blue">
              <el-icon :size="18"><User /></el-icon>
            </div>
            <div class="action-text">
              <span class="action-label">用户管理</span>
              <span class="action-desc">管理系统用户</span>
            </div>
          </div>
          <div class="action-card">
            <div class="action-icon action-icon-amber">
              <el-icon :size="18"><Setting /></el-icon>
            </div>
            <div class="action-text">
              <span class="action-label">系统设置</span>
              <span class="action-desc">配置系统参数</span>
            </div>
          </div>
          <div class="action-card">
            <div class="action-icon action-icon-green">
              <el-icon :size="18"><ChatDotRound /></el-icon>
            </div>
            <div class="action-text">
              <span class="action-label">智能问答</span>
              <span class="action-desc">NL2SQL 查询</span>
            </div>
          </div>
          <div class="action-card">
            <div class="action-icon action-icon-cyan">
              <el-icon :size="18"><DataBoard /></el-icon>
            </div>
            <div class="action-text">
              <span class="action-label">数据看板</span>
              <span class="action-desc">数据可视化</span>
            </div>
          </div>
        </div>
      </div>

      <div class="recent-activities">
        <h3 class="section-title">
          <el-icon :size="18"><Timer /></el-icon>
          最近活动
        </h3>
        <div class="activity-list" v-loading="activityLoading">
          <div v-for="item in activities" :key="item.id" class="activity-item">
            <div class="activity-dot" :class="item.status === 1 ? 'dot-success' : 'dot-error'"></div>
            <span class="activity-user">{{ item.username }}</span>
            <span class="activity-module-tag">{{ item.module }}</span>
            <span class="activity-action">{{ item.operation }}</span>
            <el-tag v-if="item.status === 0" type="danger" size="small" class="activity-status">失败</el-tag>
            <span v-if="item.ip" class="activity-ip">{{ item.ip }}</span>
            <span class="activity-spacer"></span>
            <span v-if="item.duration != null" class="activity-duration">{{ item.duration }}ms</span>
            <span class="activity-time">{{ formatTime(item.createTime) }}</span>
          </div>
          <el-empty v-if="activities.length === 0 && !activityLoading" description="暂无活动记录" :image-size="60" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { UserFilled, Menu, Connection, DataBoard, User, Setting, ChatDotRound, Ship, Opportunity, Timer } from '@element-plus/icons-vue'
import { userStore } from '../../stores/user.js'
import request from '../../api/request.js'

const displayName = computed(() => userStore.user?.username || '')

const stats = reactive({
  userCount: 0,
  menuCount: 0,
  dsCount: 0,
  modelCount: 0
})

const activities = ref([])
const activityLoading = ref(false)

async function fetchStats() {
  try {
    const data = await request.get('/api/dashboard/stats')
    if (data) {
      stats.userCount = data.userCount || 0
      stats.menuCount = data.menuCount || 0
      stats.dsCount = data.dsCount || 0
      stats.modelCount = data.modelCount || 0
    }
  } catch {
    // 后端未启动时静默失败，保留默认值
  }
}

async function fetchActivities() {
  activityLoading.value = true
  try {
    const data = await request.get('/api/dashboard/recent-activities')
    activities.value = data || []
  } catch {
    activities.value = []
  } finally {
    activityLoading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  try {
    const d = new Date(t)
    return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
  } catch {
    return t
  }
}

onMounted(() => {
  fetchStats()
  fetchActivities()
})
</script>

<style scoped>
.dashboard {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ===== 欢迎区 ===== */
.welcome-section {
  margin-bottom: 0;
}

.welcome-card {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
  border-radius: 16px;
  padding: 32px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #ffffff;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.15);
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
  color: #ffffff;
  line-height: 1.4;
}

.welcome-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.5;
}

.welcome-icon {
  color: rgba(255, 255, 255, 0.12);
  flex-shrink: 0;
}

/* ===== 统计卡片 ===== */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease-out, transform 0.2s ease-out;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.stat-card-inner {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-blue {
  background: #eff6ff;
  color: #2563eb;
}

.stat-icon-green {
  background: #ecfdf5;
  color: #10b981;
}

.stat-icon-amber {
  background: #fffbeb;
  color: #f59e0b;
}

.stat-icon-cyan {
  background: #f0fdfa;
  color: #06b6d4;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1c1917;
  line-height: 1.2;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 13px;
  color: #a8a29e;
  line-height: 1.5;
}

/* ===== 下栏：快捷入口 + 最近活动 ===== */
.bottom-section {
  flex: 1;
  display: flex;
  gap: 20px;
  min-height: 0;
}

.quick-actions {
  flex: 0 0 240px;
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1c1917;
  margin-bottom: 16px;
  line-height: 1.5;
  display: flex;
  align-items: center;
  gap: 6px;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #fafaf9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease-out;
}

.action-card:hover {
  background: #eff6ff;
}

.action-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-icon-blue { background: #eff6ff; color: #2563eb; }
.action-icon-green { background: #ecfdf5; color: #10b981; }
.action-icon-amber { background: #fffbeb; color: #f59e0b; }
.action-icon-cyan { background: #f0fdfa; color: #06b6d4; }

.action-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.action-label {
  font-size: 13px;
  font-weight: 600;
  color: #44403c;
  line-height: 1.4;
}

.action-desc {
  font-size: 11px;
  color: #a8a29e;
  line-height: 1.3;
}

.action-card:hover .action-label {
  color: #2563eb;
}

.action-card:hover .action-icon-blue { background: #dbeafe; }
.action-card:hover .action-icon-green { background: #d1fae5; }
.action-card:hover .action-icon-amber { background: #fef3c7; }
.action-card:hover .action-icon-cyan { background: #ccfbf1; }

/* ===== 最近活动 ===== */
.recent-activities {
  flex: 1;
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.activity-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f4;
  font-size: 13px;
  line-height: 1.4;
  white-space: nowrap;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-success {
  background: #10b981;
}

.dot-error {
  background: #ef4444;
}

.activity-user {
  font-weight: 600;
  color: #2563eb;
  flex-shrink: 0;
}

.activity-module-tag {
  font-size: 11px;
  color: #78716c;
  background: #f5f5f4;
  padding: 1px 6px;
  border-radius: 4px;
  flex-shrink: 0;
}

.activity-action {
  color: #44403c;
  flex-shrink: 0;
}

.activity-time {
  color: #a8a29e;
  font-size: 12px;
  flex-shrink: 0;
}

.activity-duration {
  color: #10b981;
  font-size: 11px;
  flex-shrink: 0;
}

.activity-status {
  flex-shrink: 0;
}

.activity-spacer {
  flex: 1;
  min-width: 0;
}

.activity-ip {
  color: #d6d3d1;
  font-size: 11px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .bottom-section {
    flex-direction: column;
  }
  .action-list {
    flex-direction: row;
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }
  .action-list {
    flex-direction: column;
  }
}
</style>