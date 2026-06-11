<template>
  <div class="error-page">
    <div class="error-code">404</div>
    <div class="error-desc">抱歉，您访问的页面不存在</div>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { getDashboardPath } from '../stores/permissions.js'

const router = useRouter()

function goHome() {
  router.push(getDashboardPath())
}
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
}

.error-code {
  font-size: 72px;
  font-weight: 700;
  color: #cbd5e1;
}

.error-desc {
  font-size: 16px;
  color: #64748b;
}
</style>