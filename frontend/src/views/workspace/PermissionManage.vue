<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">权限管理</h2>
      <p class="page-subtitle">管理系统中的权限标识与编码</p>
    </div>
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="权限标识">
          <el-input v-model="searchForm.keyword" placeholder="请输入权限标识" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="table-section">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" :icon="Plus" @click="handleAdd" v-if="hasPermission('perm:create')">新增权限</el-button>
          <el-button type="danger" :icon="Delete" :disabled="selectedIds.length===0" @click="handleBatchDelete" v-if="hasPermission('perm:delete')">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <span class="selected-tip" v-if="selectedIds.length > 0">已选择 <strong>{{ selectedIds.length }}</strong> 项</span>
        </div>
      </div>
      <el-table :data="pagedData" v-loading="loading" stripe style="width:100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="permissionCode" label="权限标识" min-width="180">
          <template #default="{ row }"><el-tag size="small">{{ row.permissionCode }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="description" label="权限描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermission('perm:edit')" type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm v-if="hasPermission('perm:delete')" title="确定删除？" @confirm="handleDel(row)">
              <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10,20,50]" :total="filteredList.length" layout="total,sizes,prev,pager,next" background />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑权限' : '新增权限'" width="520px" @close="resetForm">
      <el-form ref="fRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="权限标识" prop="permissionCode"><el-input v-model="form.permissionCode" placeholder="例如：user:create" /></el-form-item>
        <el-form-item label="权限描述" prop="description"><el-input v-model="form.description" placeholder="例如：创建用户" /></el-form-item>
        <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个权限吗？</p>
      <template #footer><el-button @click="batchDeleteVisible=false">取消</el-button><el-button type="danger" :loading="batchLoading" @click="confirmBatchDelete">确定删除</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission } from '../../stores/permissions.js'

const list = ref([]), loading = ref(false)
const page = ref(1), size = ref(10)
const selectedIds = ref([])
const searchForm = reactive({ keyword: '' })

const filteredList = computed(() => {
  if (!searchForm.keyword) return list.value
  return list.value.filter(i => (i.permissionCode || '').includes(searchForm.keyword) || (i.description || '').includes(searchForm.keyword))
})

const pagedData = computed(() => {
  const s = (page.value - 1) * size.value
  return filteredList.value.slice(s, s + size.value)
})

function handleSearch() { page.value = 1 }
function handleReset() { searchForm.keyword = ''; page.value = 1 }
function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

async function fetch() {
  loading.value = true
  try {
    const d = await request.get('/api/permissions')
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

const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null), submitting = ref(false), fRef = ref(null)
const form = reactive({ permissionCode: '', description: '', sortOrder: 0 })
const rules = {
  permissionCode: [{ required: true, message: '请输入权限标识' }],
  description: [{ required: true, message: '请输入权限描述' }]
}

function handleAdd() { isEdit.value = false; editId.value = null; resetRaw(); dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; form.permissionCode = row.permissionCode; form.description = row.description; form.sortOrder = row.sortOrder; dialogVisible.value = true }
function resetRaw() { form.permissionCode = ''; form.description = ''; form.sortOrder = 0 }
function resetForm() { resetRaw(); fRef.value?.resetFields() }

async function handleSubmit() {
  if (!fRef.value) return
  try { await fRef.value.validate() } catch { return }
  submitting.value = true
  try {
    if (isEdit.value) { await request.put(`/api/permissions/${editId.value}`, { ...form }); ElMessage.success('已修改') }
    else { await request.post('/api/permissions', { ...form }); ElMessage.success('已新增') }
    dialogVisible.value = false; resetRaw(); await fetch()
  } catch {} finally { submitting.value = false }
}

async function handleDel(row) {
  try { await request.delete(`/api/permissions/${row.id}`); ElMessage.success('已删除'); await fetch() } catch {}
}

const batchDeleteVisible = ref(false), batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length === 0) { ElMessage.warning('请先选择'); return }; batchDeleteVisible.value = true }
async function confirmBatchDelete() {
  batchLoading.value = true
  try {
    for (const id of selectedIds.value) await request.delete(`/api/permissions/${id}`)
    ElMessage.success(`已删除 ${selectedIds.value.length} 个权限`); selectedIds.value = []; batchDeleteVisible.value = false; await fetch()
  } catch { ElMessage.error('批量删除失败') }
  finally { batchLoading.value = false }
}

onMounted(fetch)
</script>

<style scoped>
/* PermissionManage 使用全局 .page-container / .page-header / .search-section / .table-section 样式 */
</style>