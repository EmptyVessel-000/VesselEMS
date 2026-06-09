<template>
  <div class="nl-query">
    <el-card shadow="never" class="query-card">
      <el-form :inline="true">
        <el-form-item label="数据源">
          <el-select v-model="dsId" placeholder="选择数据源" style="width:200px">
            <el-option v-for="d in dsList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型">
          <el-select v-model="modelId" placeholder="选择模型" style="width:200px">
            <el-option v-for="m in modelList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-input v-model="question" type="textarea" :rows="3" placeholder="输入自然语言查询，如：查询所有用户信息" />
      <div style="margin-top:12px;display:flex;gap:8px">
        <el-button type="primary" :icon="Search" :loading="querying" @click="handleQuery">查询</el-button>
        <el-button :icon="Download" :disabled="!resultRows.length" @click="handleDownload">下载JSON</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="result-card" v-if="sqlText">
      <h4 style="margin:0 0 8px">生成SQL</h4>
      <el-input v-model="sqlText" type="textarea" :rows="2" readonly />
    </el-card>

    <el-card shadow="never" class="result-card" v-if="resultRows.length">
      <h4 style="margin:0 0 8px">查询结果（{{ resultRows.length }} 行）</h4>
      <el-table :data="resultRows" stripe border size="small" style="width:100%">
        <el-table-column v-for="col in resultCols" :key="col" :prop="col" :label="col" min-width="120" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const dsList = ref([]), modelList = ref([])
const dsId = ref(null), modelId = ref(null), question = ref(''), querying = ref(false)
const sqlText = ref(''), resultRows = ref([]), resultCols = ref([])

async function fetchDs() { try { const data = (await request.get('/api/ds')) || []; dsList.value = data.filter(d => d.status === 1) } catch { dsList.value = [] } }
async function fetchModel() { try { const data = (await request.get('/api/model')) || []; modelList.value = data.filter(m => m.status === 1) } catch { modelList.value = [] } }

async function handleQuery() {
  if (!dsId.value || !modelId.value || !question.value.trim()) {
    ElMessage.warning('请选择数据源、模型并输入问题'); return
  }
  querying.value = true
  try {
    const d = await request.post('/api/dialog', { dsId: dsId.value, modelId: modelId.value, question: question.value })
    sqlText.value = d.sql || ''
    const rows = d.result || []
    resultRows.value = rows
    if (rows.length > 0) {
      resultCols.value = Object.keys(rows[0])
    } else {
      resultCols.value = []
    }
  } catch { resultRows.value = []; resultCols.value = [] }
  finally { querying.value = false }
}

function handleDownload() {
  const blob = new Blob([JSON.stringify(resultRows.value, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = 'result.json'; a.click()
  URL.revokeObjectURL(url)
}

onMounted(() => { fetchDs(); fetchModel() })
</script>

<style scoped>
.nl-query { height: 100%; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
.query-card { flex-shrink: 0; }
.result-card { flex-shrink: 0; }
</style>