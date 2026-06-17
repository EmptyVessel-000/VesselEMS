<template>
  <div class="nl-chat">
    <!-- 左侧会话列表 -->
    <div class="session-panel">
      <div class="session-header">
        <el-button type="primary" :icon="Plus" @click="newSession">新会话</el-button>
      </div>
      <div class="session-list" v-loading="sessionLoading">
        <div v-for="s in sessions" :key="s.sessionId" class="session-item"
             :class="{ active: s.sessionId === currentSessionId }"
             @click="selectSession(s)">
          <div class="session-title">{{ s.firstQuestion || '新会话' }}</div>
          <div class="session-meta">{{ s.count }} 条 · {{ fmtTime(s.firstTime) }}</div>
        </div>
        <el-empty v-if="sessions.length===0" description="暂无会话" :image-size="60" />
      </div>
    </div>

    <!-- 右侧对话区 -->
    <div class="chat-panel">
      <!-- 顶部选择器 -->
      <div class="chat-top">
        <el-select v-model="dsId" placeholder="数据源" size="large" style="width:240px">
          <el-option v-for="d in dsList" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-select v-model="modelId" placeholder="模型" size="large" style="width:240px">
          <el-option v-for="m in modelList" :key="m.id" :label="m.name" :value="m.id" />
        </el-select>
        <el-button v-if="messages.length > 0 && currentSessionId && hasPermission('nl2sql:export')"
          size="small" type="warning" :icon="Document" @click="exportSummary">
          导出报告
        </el-button>
      </div>

      <!-- 消息区域 -->
      <div class="chat-messages" ref="msgBox">
        <div v-if="messages.length === 0" class="chat-empty">
          <div class="empty-icon">
            <el-icon :size="48"><ChatDotSquare /></el-icon>
          </div>
          <h2 class="empty-title">请开始您的任务</h2>
          <p class="empty-desc">选择一个数据源和模型，输入自然语言问题进行查询</p>
        </div>
        <div v-for="(m, idx) in messages" :key="idx" class="msg-block">
          <div class="msg-user">{{ m.question }}</div>
          <div class="msg-sql" v-if="m.sql">
            <strong>SQL：</strong><code>{{ m.sql }}</code>
            <el-tag v-if="m.status==='pending'" size="small" type="warning" style="margin-left:8px">待确认</el-tag>
            <el-tag v-if="m.status==='error'" size="small" type="danger" style="margin-left:8px">失败</el-tag>
          </div>
          <div class="msg-error" v-if="m.error">{{ m.error }}</div>
          <div class="msg-result" v-if="m.result && m.result.length">
            <strong>结果（{{ m.result.length }} 行）：</strong>
            <el-button size="small" :icon="Download" link @click="downloadResult(m)">下载JSON</el-button>
            <el-table :data="m.result" stripe border size="small" style="margin-top:4px">
              <el-table-column v-for="col in getCols(m.result)" :key="col" :prop="col" :label="col" min-width="100" show-overflow-tooltip />
            </el-table>
          </div>
          <div class="msg-actions" v-if="m.status==='pending' && m.dialogId">
            <el-button type="primary" size="small" :icon="Check" @click="confirmExecute(m)">确认执行</el-button>
          </div>
        </div>
        <div v-if="querying" class="msg-loading"><el-icon class="is-loading"><Loading /></el-icon> 思考中...</div>
      </div>

      <!-- 底部输入框 -->
      <div class="chat-input">
        <el-input v-model="question" type="textarea" :rows="2" placeholder="输入问题..." @keydown.enter.exact.prevent="handleQuery" />
        <el-button type="primary" :icon="Promotion" :loading="querying" @click="handleQuery" style="margin-left:8px;height:auto">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Check, Promotion, Loading, Document, ChatDotSquare } from '@element-plus/icons-vue'
import axios from 'axios'
import request from '../../api/request.js'
import { hasPermission } from '../../stores/permissions.js'

const dsList = ref([]), modelList = ref([])
const dsId = ref(null), modelId = ref(null)
const sessions = ref([]), sessionLoading = ref(false), currentSessionId = ref(null)
const messages = ref([]), question = ref(''), querying = ref(false)
const msgBox = ref(null)

async function fetchDs() {
  try { const d = await request.get('/api/ds'); dsList.value = (d || []).filter(x => x.status === 1) } catch { dsList.value = [] }
}
async function fetchModel() {
  try { const d = await request.get('/api/model'); modelList.value = (d || []).filter(x => x.status === 1) } catch { modelList.value = [] }
}
async function fetchSessions() {
  sessionLoading.value = true
  try { sessions.value = (await request.get('/api/dialog/sessions')) || [] } catch { sessions.value = [] }
  finally { sessionLoading.value = false }
}

function fmtTime(t) {
  if (!t) return ''
  try { return new Date(t).toLocaleString('zh-CN', { hour12: false }) } catch { return t }
}

function newSession() {
  currentSessionId.value = null
  dsId.value = null
  modelId.value = null
  messages.value = []
  scrollBottom()
}

async function selectSession(s) {
  currentSessionId.value = s.sessionId
  if (s.datasourceId) dsId.value = s.datasourceId
  if (s.modelId) modelId.value = s.modelId
  messages.value = []
  try {
    const dialogs = (await request.get(`/api/dialog/session/${s.sessionId}`)) || []
    messages.value = dialogs.map(d => {
      let c = {}
      try { c = JSON.parse(d.content || '{}') } catch {}
      return { dialogId: d.id, ...c }
    })
  } catch { messages.value = [] }
  scrollBottom()
}

async function handleQuery() {
  if (!dsId.value || !modelId.value || !question.value.trim()) {
    ElMessage.warning('请选择数据源、模型并输入问题'); return
  }
  querying.value = true
  try {
    const body = { dsId: dsId.value, modelId: modelId.value, question: question.value }
    if (currentSessionId.value) body.sessionId = currentSessionId.value
    const d = await request.post('/api/dialog', body)
    currentSessionId.value = d.sessionId
    const msg = { ...d, dialogId: d.dialogId }
    messages.value.push(msg)
    question.value = ''
    if (d.needConfirm) {
      ElMessage.warning('该SQL包含写操作，请确认后执行')
    } else if (d.error) {
      ElMessage.error(d.error)
    }
    await fetchSessions()
    scrollBottom()
  } catch {} finally { querying.value = false }
}

async function confirmExecute(m) {
  try {
    await ElMessageBox.confirm(`确认执行此SQL？${m.sql}`, '写操作确认', { type: 'warning' })
    const d = await request.post(`/api/dialog/${m.dialogId}/execute`)
    m.result = d.result || []
    m.status = d.error ? 'error' : 'success'
    m.error = d.error || null
    scrollBottom()
  } catch {}
}

function getCols(rows) {
  return rows && rows.length ? Object.keys(rows[0]) : []
}

function downloadResult(m) {
  const blob = new Blob([JSON.stringify(m.result, null, 2)], { type: 'application/json' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob); a.download = 'result.json'; a.click()
}

async function exportSummary() {
  if (!currentSessionId.value || !modelId.value) {
    ElMessage.warning('请先选择一个模型'); return
  }
  const token = localStorage.getItem('token')
  const instance = axios.create({
    timeout: 180000,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    }
  })
  try {
    ElMessage.info('正在生成报告，可能需要较长时间...')
    const resp = await instance.post('/api/dialog/summary', {
      sessionId: currentSessionId.value,
      modelId: modelId.value
    })
    let markdown = resp.data
    if (resp.data && typeof resp.data === 'object' && 'data' in resp.data) {
      markdown = resp.data.data
    }
    const blob = new Blob([markdown], { type: 'text/markdown' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob); a.download = 'analysis-report.md'; a.click()
    ElMessage.success('报告已下载')
  } catch {
    ElMessage.error('导出失败，请重试')
  }
}

function scrollBottom() {
  nextTick(() => {
    const el = msgBox.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

onMounted(() => { fetchDs(); fetchModel(); fetchSessions() })
</script>

<style scoped>
.nl-chat { height: 100%; display: flex; gap: 0; overflow: hidden; background: #ffffff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04); }
.session-panel { width: 220px; background: #fff; border-right: 1px solid #f0efed; display: flex; flex-direction: column; flex-shrink: 0; }
.session-header { padding: 12px; border-bottom: 1px solid #f0efed; }
.session-list { flex: 1; overflow-y: auto; }
.session-item { padding: 10px 12px; cursor: pointer; border-bottom: 1px solid #f5f5f4; }
.session-item:hover, .session-item.active { background: #eef2ff; }
.session-title { font-size: 13px; color: #1c1917; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-meta { font-size: 11px; color: #a8a29e; margin-top: 2px; }

.chat-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: #fafaf9; }
.chat-top { padding: 10px 16px; background: #fff; border-bottom: 1px solid #f0efed; display: flex; gap: 12px; align-items: center; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; }
.chat-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #a8a29e; }
.empty-icon { color: #d6d3d1; margin-bottom: 16px; }
.empty-title { margin: 0; font-size: 22px; font-weight: 600; color: #78716c; }
.empty-desc { margin-top: 8px; font-size: 14px; }
.msg-block { margin-bottom: 16px; }
.msg-user { background: #4f46e5; color: #fff; padding: 8px 12px; border-radius: 10px; display: inline-block; max-width: 80%; font-size: 14px; }
.msg-sql { margin-top: 6px; background: #f5f5f4; color: #1c1917; padding: 8px 12px; border-radius: 8px; font-size: 13px; overflow-x: auto; }
.msg-sql code { font-family: monospace; white-space: pre-wrap; word-break: break-all; }
.msg-error { margin-top: 4px; color: #ef4444; font-size: 13px; padding: 4px 8px; background: #fef2f2; border-radius: 6px; }
.msg-result { margin-top: 6px; font-size: 13px; }
.msg-actions { margin-top: 6px; }
.msg-loading { padding: 8px 16px; color: #78716c; font-size: 14px; }

.chat-input { padding: 10px 16px; background: #fff; border-top: 1px solid #f0efed; display: flex; align-items: flex-end; }
</style>
