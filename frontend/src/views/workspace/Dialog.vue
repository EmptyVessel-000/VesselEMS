<template>
  <div class="dialog-history">
    <el-card class="table-card" shadow="never">
<<<<<<< HEAD
      <el-table :data="sessions" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="首条问题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.firstQuestion || '-' }}</template>
        </el-table-column>
        <el-table-column prop="count" label="消息数" width="80" align="center" />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.firstTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="View" link @click="handleView(row.sessionId)">详情</el-button>
=======
      <el-table :data="pagedData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="问题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.question }}</template>
        </el-table-column>
        <el-table-column prop="datasourceId" label="数据源ID" width="90" align="center" />
        <el-table-column prop="modelId" label="模型ID" width="90" align="center" />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="View" link @click="handleView(row)">详情</el-button>
>>>>>>> 2609d393650989f717325435186f2346d621f4dc
            <el-popconfirm title="确定删除？" @confirm="handleDel(row)">
              <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
<<<<<<< HEAD
    </el-card>

    <el-dialog v-model="detailVisible" title="会话详情" width="800px">
      <div v-for="(d, idx) in detailDialogs" :key="idx" class="detail-block">
        <div class="detail-q">{{ d.question || '-' }}</div>
        <div class="detail-sql" v-if="d.sql"><code>{{ d.sql }}</code></div>
        <div class="detail-error" v-if="d.error">错误：{{ d.error }}</div>
        <div v-if="d.result && d.result.length" style="margin-top:4px">
          <el-table :data="d.result" stripe border size="small">
            <el-table-column v-for="col in getCols(d.result)" :key="col" :prop="col" :label="col" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <el-empty v-if="detailDialogs.length===0" description="暂无记录" />
=======
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10,20,50]" :total="list.length" layout="total,sizes,prev,pager,next" background />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="对话详情" width="700px">
      <div v-if="detail">
        <p><strong>问题：</strong>{{ detail.question }}</p>
        <p><strong>SQL：</strong></p>
        <el-input v-model="detail.sql" type="textarea" :rows="3" readonly />
        <p style="margin-top:12px"><strong>结果：</strong></p>
        <el-table v-if="detail.result && detail.result.length" :data="detail.result" stripe border size="small" style="width:100%">
          <el-table-column v-for="col in detailCols" :key="col" :prop="col" :label="col" show-overflow-tooltip />
        </el-table>
        <el-empty v-else description="无结果" />
      </div>
>>>>>>> 2609d393650989f717325435186f2346d621f4dc
    </el-dialog>
  </div>
</template>

<script setup>
<<<<<<< HEAD
import { ref, onMounted } from 'vue'
=======
import { ref, computed, onMounted } from 'vue'
>>>>>>> 2609d393650989f717325435186f2346d621f4dc
import { ElMessage } from 'element-plus'
import { View, Delete } from '@element-plus/icons-vue'
import request from '../../api/request.js'

<<<<<<< HEAD
const sessions = ref([]), loading = ref(false)
=======
const list = ref([]), loading = ref(false)
const page = ref(1), size = ref(10)
const pagedData = computed(() => list.value.slice((page.value - 1) * size.value, page.value * size.value))
>>>>>>> 2609d393650989f717325435186f2346d621f4dc

async function fetch() {
  loading.value = true
  try {
<<<<<<< HEAD
    sessions.value = (await request.get('/api/dialog/sessions')) || []
  } catch { sessions.value = [] }
  finally { loading.value = false }
}

function fmtTime(t) {
  if (!t) return ''
  try { return new Date(t).toLocaleString('zh-CN', { hour12: false }) } catch { return t }
}

const detailVisible = ref(false), detailDialogs = ref([])
async function handleView(sessionId) {
  detailVisible.value = true; detailDialogs.value = []
  try {
    const dialogs = (await request.get(`/api/dialog/session/${sessionId}`)) || []
    detailDialogs.value = dialogs.map(d => {
      let c = {}
      try { c = JSON.parse(d.content || '{}') } catch {}
      return c
    })
  } catch { detailDialogs.value = [] }
}

async function handleDel(row) {
  try {
    const dialogs = (await request.get(`/api/dialog/session/${row.sessionId}`)) || []
    for (const d of dialogs) {
      await request.delete(`/api/dialog/${d.id}`)
    }
    ElMessage.success('已删除'); await fetch()
  } catch { ElMessage.error('删除失败') }
}

function getCols(rows) {
  return rows && rows.length ? Object.keys(rows[0]) : []
=======
    const d = await request.get('/api/dialog')
    list.value = (d || []).map(r => {
      let t = ''
      if (r.createTime) {
        if (Array.isArray(r.createTime)) {
          const [y, m, d, h = 0, mm = 0, s = 0] = r.createTime
          t = `${y}/${String(m).padStart(2, '0')}/${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mm).padStart(2, '0')}:${String(s).padStart(2, '0')}`
        } else {
          t = new Date(r.createTime).toLocaleString('zh-CN', { hour12: false })
        }
      }
      let content = {}
      try { content = JSON.parse(r.content || '{}') } catch {}
      return { ...r, createTime: t, ...content }
    })
  } catch { list.value = [] }
  finally { loading.value = false }
}

const detailVisible = ref(false), detail = ref(null), detailCols = ref([])
function handleView(row) {
  detail.value = row
  detailCols.value = row.result && row.result.length ? Object.keys(row.result[0]) : []
  detailVisible.value = true
}

async function handleDel(row) {
  try { await request.delete(`/api/dialog/${row.id}`); ElMessage.success('已删除'); await fetch() } catch {}
>>>>>>> 2609d393650989f717325435186f2346d621f4dc
}

onMounted(fetch)
</script>

<style scoped>
.dialog-history { height: 100%; display: flex; flex-direction: column; }
.table-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
<<<<<<< HEAD
.detail-block { margin-bottom: 12px; padding: 8px; border: 1px solid #e5e7eb; border-radius: 6px; }
.detail-q { font-size: 14px; color: #2563eb; font-weight: 500; }
.detail-sql { margin-top: 4px; background: #1e293b; color: #e2e8f0; padding: 6px 10px; border-radius: 4px; font-size: 12px; overflow-x: auto; }
.detail-sql code { white-space: pre-wrap; word-break: break-all; }
.detail-error { margin-top: 4px; color: #ef4444; font-size: 13px; }
=======
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; flex-shrink: 0; }
>>>>>>> 2609d393650989f717325435186f2346d621f4dc
</style>