<template>
  <div class="model-manage">
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd" v-if="hasPermission('model:create')">新增模型</el-button>
      </div>
      <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="模型名称" width="140" />
        <el-table-column prop="apiUrl" label="API地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="modelId" label="模型标识" width="120" />
        <el-table-column label="版本" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.version === 1 ? 'success' : ''" size="small">{{ row.version === 1 ? 'RAG' : 'NL2SQL' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelType" label="模型类型" width="100" align="center">
          <template #default="{ row }">{{ row.modelType || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermission('model:edit')" type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm v-if="hasPermission('model:delete')" title="确定删除？" @confirm="handleDel(row)">
              <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10,20,50]" :total="list.length" layout="total,sizes,prev,pager,next" background />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '新增模型'" width="520px" @close="resetForm">
      <el-form ref="fRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="API地址" prop="apiUrl"><el-input v-model="form.apiUrl" placeholder="https://api.openai.com/v1" /></el-form-item>
        <el-form-item label="API密钥"><el-input v-model="form.apiKey" type="password" show-password /></el-form-item>
        <el-form-item label="模型标识" prop="modelId"><el-input v-model="form.modelId" placeholder="gpt-4o" /></el-form-item>
        <el-form-item label="模块版本" prop="version">
          <el-radio-group v-model="form.version">
            <el-radio :value="0">NL2SQL模块</el-radio>
            <el-radio :value="1">RAG模块</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType" v-if="form.version === 1">
          <el-select v-model="form.modelType" placeholder="选择模型用途" style="width:100%">
            <el-option label="CHAT - 对话模型" value="CHAT" />
            <el-option label="EMBEDDING - 向量模型" value="EMBEDDING" />
            <el-option label="VISION - 视觉模型" value="VISION" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission } from '../../stores/permissions.js'

const list = ref([]), loading = ref(false)
const page = ref(1), size = ref(10)
const pagedData = computed(() => list.value.slice((page.value - 1) * size.value, page.value * size.value))

async function fetch() {
  loading.value = true
  try {
    const d = await request.get('/api/model')
    list.value = (d || []).map(fmt)
  } catch { list.value = [] }
  finally { loading.value = false }
}

function fmt(r) {
  let t = ''
  if (r.createTime) {
    if (Array.isArray(r.createTime)) {
      const [y, m, d, h = 0, mm = 0, s = 0] = r.createTime
      t = `${y}/${String(m).padStart(2, '0')}/${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mm).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    } else {
      t = new Date(r.createTime).toLocaleString('zh-CN', { hour12: false })
    }
  }
  return { ...r, createTime: t }
}

async function handleToggle(row) {
  try { await request.put(`/api/model/${row.id}`, { status: row.status }); ElMessage.success('已更新') } catch { row.status = row.status === 1 ? 0 : 1 }
}

const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null), submitting = ref(false), fRef = ref(null)
const form = reactive({ name: '', apiUrl: '', apiKey: '', modelId: '', version: 0, modelType: '', status: 1 })
const rules = { name: [{ required: true, message: '请输入名称' }], apiUrl: [{ required: true, message: '请输入API地址' }], modelId: [{ required: true, message: '请输入模型标识' }] }

function handleAdd() { isEdit.value = false; editId.value = null; resetRaw(); dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; form.name = row.name; form.apiUrl = row.apiUrl; form.apiKey = row.apiKey; form.modelId = row.modelId; form.version = row.version ?? 0; form.modelType = row.modelType || ''; form.status = row.status; dialogVisible.value = true }
function resetRaw() { form.name = ''; form.apiUrl = ''; form.apiKey = ''; form.modelId = ''; form.version = 0; form.modelType = ''; form.status = 1 }
function resetForm() { resetRaw(); fRef.value?.resetFields() }

async function handleSubmit() {
  if (!fRef.value) return
  try { await fRef.value.validate() } catch { return }
  submitting.value = true
  try {
    if (isEdit.value) { await request.put(`/api/model/${editId.value}`, { ...form }); ElMessage.success('已修改') }
    else { await request.post('/api/model', { ...form }); ElMessage.success('已新增') }
    dialogVisible.value = false; resetRaw(); await fetch()
  } catch {} finally { submitting.value = false }
}

async function handleDel(row) {
  try { await request.delete(`/api/model/${row.id}`); ElMessage.success('已删除'); await fetch() } catch {}
}

onMounted(fetch)
</script>

<style scoped>
.model-manage { height: 100%; display: flex; flex-direction: column; }
.table-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; flex-shrink: 0; }
</style>