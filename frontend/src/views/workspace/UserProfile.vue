<template>
  <div class="user-profile">
    <el-card shadow="never" class="profile-card">
      <template #header>
        <div class="card-header">
          <span>用户个人中心</span>
        </div>
      </template>

      <div class="profile-content" v-if="user.user">
        <!-- 头像和基本信息 -->
        <div class="profile-top">
          <el-avatar :size="80" :src="user.user.avatar">
            {{ (user.user.nickname || user.user.username || '?')[0] }}
          </el-avatar>
          <div class="profile-basic">
            <div class="profile-name">
              {{ user.user.nickname || user.user.username }}
              <el-tag v-if="user.user.status === 1" size="small" type="success">正常</el-tag>
              <el-tag v-else size="small" type="danger">禁用</el-tag>
            </div>
            <div class="profile-roles">
              <el-tag v-for="r in user.user.roles" :key="r" size="small" effect="plain" style="margin-right:4px">
                {{ r }}
              </el-tag>
              <span v-if="!user.user.roles?.length" style="color:#9ca3af;font-size:13px">无角色</span>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 详细信息 -->
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="用户ID">{{ user.user.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ user.user.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ user.user.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ user.user.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            <template v-if="user.user.gender === 1">男</template>
            <template v-else-if="user.user.gender === 0">女</template>
            <template v-else>-</template>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ user.user.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.user.telephone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="部门ID">{{ user.user.departmentId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="user.user.status === 1 ? 'success' : 'danger'" size="small">
              {{ user.user.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ user.user.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最后登录IP">{{ user.user.lastLoginIp || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最后登录时间" :span="2">
            {{ formatTime(user.user.lastLoginTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(user.user.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="修改时间">{{ formatTime(user.user.modifyTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-empty v-else description="加载中..." />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { checkAuth, userStore } from '../../stores/user.js'

const user = userStore

function formatTime(t) {
  if (!t) return '-'
  try {
    return new Date(t).toLocaleString('zh-CN', { hour12: false })
  } catch {
    return t
  }
}

onMounted(async () => {
  await checkAuth()
})
</script>

<style scoped>
.user-profile {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.profile-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.profile-content {
  flex: 1;
  overflow-y: auto;
}

.profile-top {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 0;
}

.profile-basic {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-name {
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-roles {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
</style>