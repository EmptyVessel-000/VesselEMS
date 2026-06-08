<template>
  <div class="system-config">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="配置键"><el-input v-model="searchForm.configKey" placeholder="请输入配置键" clearable /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button v-if="hasMenu(26) && hasPermission('config:manage')" type="primary" :icon="Plus" @click="handleAdd">新增配置</el-button>
          <el-button v-if="hasMenu(26) && hasPermission('config:manage')" type="danger" :icon="Delete" :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</el-button>
        </div>
      </div>
      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width:100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="configKey" label="配置键" min-width="180" />
        <el-table-column prop="configValue" label="配置值" min-width="250" show-overflow-tooltip />
        <el-table-column prop="configType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ configTypeMap[row.configType] || '字符串' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="status" label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasMenu(26) && hasPermission('config:manage')" type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm v-if="hasMenu(26) && hasPermission('config:manage')" title="确定删除?" @confirm="handleDelete(row)"><template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="filteredData.length" layout="total,sizes,prev,pager,next,jumper" background @size-change="handleSizeChange" /></div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" class="dialog-form">
        <el-form-item label="配置键" prop="configKey"><el-input v-model="formData.configKey" :disabled="isEdit" /></el-form-item>
        <el-form-item label="配置值" prop="configValue"><el-input v-model="formData.configValue" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="类型" prop="configType"><el-select v-model="formData.configType"><el-option v-for="(label, val) in configTypeMap" :key="val" :label="label" :value="Number(val)" /></el-select></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="formData.description" /></el-form-item>
        <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="formData.sortOrder" :min="0" :max="999" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="formData.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 项配置吗？</p>
      <template #footer><el-button @click="batchDeleteVisible=false">取消</el-button><el-button type="danger" :loading="batchLoading" @click="confirmBatchDelete">确定删除</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission, hasMenu } from '../../stores/permissions.js'

const configTypeMap = { 0: '字符串', 1: '数字', 2: '布尔', 3: 'JSON' }

const allConfigs = ref([]), tableLoading = ref(false)

async function fetchConfigs() {
  tableLoading.value = true
  try { const d = await request.get('/api/configs'); allConfigs.value = (d || []).map(c => ({ ...c, configKey: c.configKey || c.config_key || '', configValue: c.configValue || c.config_value || '', configType: c.configType !== undefined ? c.configType : (c.config_type !== undefined ? c.config_type : 0), createTime: c.createTime || (c.createdAt ? new Date(c.createdAt).toLocaleString('zh-CN',{hour12:false}) : '') })) }
  catch { allConfigs.value = [] }
  finally { tableLoading.value = false }
}

const searchForm = reactive({ configKey: '' })
const filteredData = computed(() => allConfigs.value.filter(i => !searchForm.configKey || (i.configKey||'').includes(searchForm.configKey)))
function handleSearch() { currentPage.value = 1 }
function handleReset() { searchForm.configKey = ''; currentPage.value = 1 }

const currentPage = ref(1), pageSize = ref(10)
const pagedData = computed(() => { const s = (currentPage.value-1)*pageSize.value; return filteredData.value.slice(s,s+pageSize.value) })
function handleSizeChange() { currentPage.value = 1 }
const selectedIds = ref([])
function handleSelectionChange(rows) { selectedIds.value = rows.map(r=>r.id) }

const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null), submitLoading = ref(false), formRef = ref(null)
const formData = reactive({ configKey: '', configValue: '', configType: 0, description: '', sortOrder: 0, status: 1 })
const formRules = { configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }], configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }] }
const dialogTitle = computed(() => isEdit.value ? '编辑配置' : '新增配置')

function handleAdd() { isEdit.value=false; editId.value=null; resetForm(); dialogVisible.value=true }
function handleEdit(row) { isEdit.value=true; editId.value=row.id; formData.configKey=row.configKey; formData.configValue=row.configValue; formData.configType=row.configType; formData.description=row.description||''; formData.sortOrder=row.sortOrder||0; formData.status=row.status||1; dialogVisible.value=true }
function resetForm() { formData.configKey=''; formData.configValue=''; formData.configType=0; formData.description=''; formData.sortOrder=0; formData.status=1 }
function handleDialogClose() { resetForm(); formRef.value?.resetFields() }

async function handleSubmit() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    const payload = { configKey: formData.configKey, configValue: formData.configValue, configType: formData.configType, description: formData.description, sortOrder: formData.sortOrder, status: formData.status }
    if (isEdit.value) { await request.put(`/api/configs/${editId.value}`, payload); ElMessage.success('配置修改成功') }
    else { await request.post('/api/configs', payload); ElMessage.success('配置创建成功') }
    dialogVisible.value = false; resetForm(); await fetchConfigs()
  } catch {} finally { submitLoading.value = false }
}

async function handleDelete(row) { try { await request.delete(`/api/configs/${row.id}`); ElMessage.success(`配置「${row.configKey}」已删除`); await fetchConfigs() } catch {} }

async function handleStatusChange(row) {
  try {
    await request.put(`/api/configs/${row.id}`, { configKey: row.configKey, configValue: row.configValue, configType: row.configType, description: row.description, sortOrder: row.sortOrder, status: row.status })
    ElMessage.success(`配置「${row.configKey}」已${row.status === 1 ? '启用' : '禁用'}`)
  } catch { row.status = row.status === 1 ? 0 : 1; ElMessage.error('状态更新失败') }
}

const batchDeleteVisible = ref(false), batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length===0) { ElMessage.warning('请先选择'); return }; batchDeleteVisible.value = true }
async function confirmBatchDelete() { batchLoading.value=true; try { for (const id of selectedIds.value) await request.delete(`/api/configs/${id}`); ElMessage.success(`已删除 ${selectedIds.value.length} 项配置`); selectedIds.value=[]; batchDeleteVisible.value=false; await fetchConfigs() } catch { ElMessage.error('批量删除失败') } finally { batchLoading.value=false } }

onMounted(() => { fetchConfigs() })
</script>

<style scoped>
.system-config { height:100%; display:flex; flex-direction:column; gap:16px; }
.search-card { flex-shrink:0; } .search-card .el-form { margin-bottom:0; }
.table-card { flex:1; display:flex; flex-direction:column; overflow:hidden; }
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
.toolbar-left { display:flex; gap:8px; }
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; flex-shrink:0; }
.dialog-form { padding-right:20px; }
.batch-delete-text { font-size:15px; color:#374151; text-align:center; padding:16px 0; }
.batch-delete-text strong { color:#ef4444; font-size:18px; }
</style>