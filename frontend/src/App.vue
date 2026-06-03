<template>
  <div class="app">
    <header class="header">
      <h1>🚢 VesselEMS</h1>
      <p>Vue 3 + Vite 前端启动成功</p>
    </header>

    <main class="main">
      <div class="card">
        <h2>前后端连通性测试</h2>
        <p v-if="loading" class="info">正在请求后端...</p>
        <p v-else-if="result" class="success">{{ result }}</p>
        <p v-else-if="error" class="error">连接失败：{{ error }}</p>
        <p v-else class="hint">点击下方按钮测试后端连接</p>
        <button :disabled="loading" @click="testConnection">
          {{ loading ? '请求中...' : '测试后端连接' }}
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from './api/request.js'

const loading = ref(false)
const result = ref('')
const error = ref('')

async function testConnection() {
  loading.value = true
  result.value = ''
  error.value = ''
  try {
    const data = await request.get('/api/tesk')
    result.value = '后端连接成功！返回 ' + JSON.stringify(data).length + ' 字节数据'
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f0f2f5;
  min-height: 100vh;
}

.app {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 80px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 2rem;
  color: #1a1a2e;
}

.header p {
  color: #6b7280;
  margin-top: 8px;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 420px;
  max-width: 90vw;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.card h2 {
  font-size: 1.2rem;
  color: #1f2937;
  margin-bottom: 20px;
}

.info {
  color: #3b82f6;
  margin-bottom: 16px;
}

.success {
  color: #10b981;
  margin-bottom: 16px;
  word-break: break-all;
}

.error {
  color: #ef4444;
  margin-bottom: 16px;
}

.hint {
  color: #9ca3af;
  margin-bottom: 16px;
}

button {
  padding: 10px 28px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}

button:hover {
  background: #2563eb;
}

button:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}
</style>