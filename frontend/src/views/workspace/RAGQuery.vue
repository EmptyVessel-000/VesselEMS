<template>
  <div class="rag-query">
    <div class="config-panel">
      <el-form label-width="70px" size="default">
        <el-form-item label="选择文库">
          <el-select v-model="libraryId" placeholder="选择知识库" style="width:100%">
            <el-option v-for="lib in libraries" :key="lib.id" :label="lib.name" :value="lib.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择模型">
          <el-select v-model="modelId" placeholder="选择模型 (CHAT)" style="width:100%">
            <el-option v-for="m in chatModels" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div class="chat-panel">
      <div class="chat-messages" ref="msgBox">
        <div v-if="messages.length === 0" class="chat-empty">
          <el-icon :size="40"><ChatDotRound /></el-icon>
          <p>请先选择文库和模型，然后输入问题</p>
        </div>
        <div v-for="(m, idx) in messages" :key="idx" class="msg-block">
          <div class="msg-user">{{ m.question }}</div>
          <div class="msg-ai" v-if="m.answer">
            <div class="answer-text">{{ m.answer }}</div>
            <el-collapse v-if="m.references?.length" class="ref-collapse">
              <el-collapse-item :title="`📎 引用来源 (${m.references.length} 条)`">
                <div v-for="ref in m.references" :key="ref.annotationId" class="ref-item">
                  <div class="ref-score">相似度: {{ (ref.score * 100).toFixed(0) }}%</div>
                  <div class="ref-content">{{ ref.content }}</div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
          <div class="msg-error" v-if="m.error">{{ m.error }}</div>
        </div>
        <div v-if="querying" class="msg-loading"><el-icon class="is-loading"><Loading /></el-icon> 思考中...</div>
      </div>

      <div class="chat-input">
        <el-input v-model="question" type="textarea" :rows="2" placeholder="输入问题..."
          @keydown.enter.exact.prevent="handleQuery" />
        <el-button type="primary" :icon="Promotion" :loading="querying"
          @click="handleQuery" style="margin-left:8px;height:auto">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Promotion, Loading } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const libraries = ref([])
const chatModels = ref([])
const libraryId = ref(null)
const modelId = ref(null)
const messages = ref([])
const question = ref('')
const querying = ref(false)
const msgBox = ref(null)

async function fetchLibraries() {
  try { libraries.value = (await request.get('/api/library')) || [] } catch { libraries.value = [] }
}

async function fetchModels() {
  try {
    const all = (await request.get('/api/model')) || []
    // Filter: version=1 and model_type=CHAT
    chatModels.value = all.filter(m => m.version === 1 && m.modelType === 'CHAT' && m.status === 1)
  } catch { chatModels.value = [] }
}

async function handleQuery() {
  if (!libraryId.value || !modelId.value || !question.value.trim()) {
    ElMessage.warning('请选择文库、模型并输入问题')
    return
  }
  querying.value = true
  const q = question.value
  question.value = ''
  messages.value.push({ question: q, answer: null, references: [], error: null })
  try {
    const d = await request.post('/api/rag/query', {
      libraryId: libraryId.value,
      question: q,
      modelId: modelId.value
    })
    const last = messages.value[messages.value.length - 1]
    last.answer = d.answer
    last.references = d.references || []
  } catch (e) {
    const last = messages.value[messages.value.length - 1]
    last.error = e.message || '查询失败'
  } finally {
    querying.value = false
    scrollBottom()
  }
}

function scrollBottom() {
  nextTick(() => {
    const el = msgBox.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

onMounted(() => { fetchLibraries(); fetchModels() })
</script>

<style scoped>
.rag-query { height: 100%; display: flex; gap: 0; overflow: hidden; }
.config-panel { width: 240px; background: #fff; border-right: 1px solid #e5e7eb; padding: 16px 12px; flex-shrink: 0; }
.chat-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: #f8fafc; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; }
.chat-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #9ca3af; gap: 8px; }
.msg-block { margin-bottom: 16px; }
.msg-user { background: #2563eb; color: #fff; padding: 8px 14px; border-radius: 8px; display: inline-block; max-width: 80%; font-size: 14px; }
.msg-ai { margin-top: 8px; background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px 14px; }
.answer-text { font-size: 14px; color: #1e293b; line-height: 1.7; white-space: pre-wrap; }
.msg-error { margin-top: 4px; color: #ef4444; font-size: 13px; padding: 4px 8px; background: #fef2f2; border-radius: 4px; }
.ref-collapse { margin-top: 10px; }
.ref-item { padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
.ref-score { font-size: 12px; color: #2563eb; font-weight: 600; margin-bottom: 4px; }
.ref-content { font-size: 13px; color: #6b7280; line-height: 1.5; white-space: pre-wrap; }
.msg-loading { padding: 8px 16px; color: #6b7280; font-size: 14px; }
.chat-input { padding: 10px 16px; background: #fff; border-top: 1px solid #e5e7eb; display: flex; align-items: flex-end; }
</style>