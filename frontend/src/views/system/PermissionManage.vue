<template>
  <div class="perm-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="权限标识">
          <el-input v-model="searchForm.permission_code" placeholder="请输入权限标识" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增权限</el-button>
          <el-button type="danger" :icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <span class="selected-tip" v-if="selectedIds.length > 0">已选择 <strong>{{ selectedIds.length }}</strong> 项</span>
        </div>
      </div>

      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="permission_code" label="权限标识" min-width="180">
          <template #default="{ row }">
            <el-tag size="small">{{ row.permission_code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="权限描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sort_order" label="排序" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row)">
              <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]" :total="filteredData.length" layout="total, sizes, prev, pager, next, jumper" background @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" class="dialog-form">
        <el-form-item label="权限标识" prop="permission_code">
          <el-input v-model="formData.permission_code" placeholder="如 user:create" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="权限描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入权限描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sort_order">
          <el-input-number v-model="formData.sort_order" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个权限吗？</p>
      <template #footer>
        <el-button @click="batchDeleteVisible = false">取消</el-button>
        <el-button type="danger" :loading="batchLoading" @click="confirmBatchDelete">确定删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'

const allPerms = ref([
  { id: 1, permission_code: 'user:view', description: '查看用户列表及详情信息', sort_order: 1, createTime: '2026-01-15 10:30:00' },
  { id: 2, permission_code: 'user:create', description: '创建新用户', sort_order: 2, createTime: '2026-01-15 10:30:00' },
  { id: 3, permission_code: 'user:update', description: '编辑修改用户信息', sort_order: 3, createTime: '2026-01-15 10:30:00' },
  { id: 4, permission_code: 'user:delete', description: '删除用户账号', sort_order: 4, createTime: '2026-01-15 10:30:00' },
  { id: 5, permission_code: 'role:manage', description: '管理角色和分配权限', sort_order: 10, createTime: '2026-01-15 10:32:00' },
  { id: 6, permission_code: 'menu:manage', description: '管理系统菜单结构', sort_order: 11, createTime: '2026-01-15 10:32:00' },
  { id: 7, permission_code: 'dept:view', description: '查看部门信息', sort_order: 20, createTime: '2026-02-01 08:00:00' },
  { id: 8, permission_code: 'doc:manage', description: '管理文档和知识库', sort_order: 30, createTime: '2026-02-10 14:00:00' },
  { id: 9, permission_code: 'student:view', description: '查看学生信息', sort_order: 40, createTime: '2026-03-05 09:00:00' },
  { id: 10, permission_code: 'data:export', description: '导出系统数据', sort_order: 50, createTime: '2026-03-20 11:00:00' }
])
let nextId = 11

const searchForm = reactive({ permission_code: '' })
const filteredData = computed(() => allPerms.value.filter(i => !searchForm.permission_code || i.permission_code.includes(searchForm.permission_code)))
function handleSearch() { currentPage.value = 1 }
function handleReset() { searchForm.permission_code = ''; currentPage.value = 1 }

const currentPage = ref(1); const pageSize = ref(10)
const pagedData = computed(() => { const s = (currentPage.value - 1) * pageSize.value; return filteredData.value.slice(s, s + pageSize.value) })
function handleSizeChange() { currentPage.value = 1 }
function handleCurrentChange() {}

const selectedIds = ref([])
function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }
const tableLoading = ref(false)

const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref(null); const submitLoading = ref(false); const formRef = ref(null)
const formData = reactive({ permission_code: '', description: '', sort_order: 0 })
const formRules = {
  permission_code: [{ required: true, message: '请输入权限标识', trigger: 'blur' }, { pattern: /^[a-z]+:[a-z]+$/, message: '格式：模块:操作', trigger: 'blur' }]
}
const dialogTitle = computed(() => isEdit.value ? '编辑权限' : '新增权限')

function handleAdd() { isEdit.value = false; editId.value = null; resetForm(); dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; formData.permission_code = row.permission_code; formData.description = row.description; formData.sort_order = row.sort_order; dialogVisible.value = true }
function resetForm() { formData.permission_code = ''; formData.description = ''; formData.sort_order = 0 }
function handleDialogClose() { resetForm(); formRef.value?.resetFields() }

function handleSubmit() {
  if (!formRef.value) return
  formRef.value.validate().then(() => {
    submitLoading.value = true; setTimeout(() => {
      if (isEdit.value) {
        const t = allPerms.value.find(p => p.id === editId.value)
        if (t) { t.description = formData.description; t.sort_order = formData.sort_order }
        ElMessage.success('权限修改成功')
      } else {
        allPerms.value.unshift({ id: nextId++, permission_code: formData.permission_code, description: formData.description, sort_order: formData.sort_order, createTime: new Date().toLocaleString('zh-CN', { hour12: false }) })
        ElMessage.success('权限创建成功')
      }
      dialogVisible.value = false; resetForm(); submitLoading.value = false
    }, 600)
  }).catch(() => {})
}

function handleDelete(row) { const i = allPerms.value.findIndex(p => p.id === row.id); if (i > -1) { allPerms.value.splice(i, 1); ElMessage.success(`权限「${row.permission_code}」已删除`) } }

const batchDeleteVisible = ref(false); const batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length === 0) { ElMessage.warning('请先选择'); return }; batchDeleteVisible.value = true }
function confirmBatchDelete() { batchLoading.value = true; setTimeout(() => { allPerms.value = allPerms.value.filter(p => !selectedIds.value.includes(p.id)); ElMessage.success(`已删除 ${selectedIds.value.length} 个权限`); selectedIds.value = []; batchDeleteVisible.value = false; batchLoading.value = false }, 600) }
</script>

<style scoped>
.perm-manage { height: 100%; display: flex; flex-direction: column; gap: 16px; }
.search-card { flex-shrink: 0; } .search-card .el-form { margin-bottom: 0; }
.table-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 8px; }
.selected-tip { font-size: 13px; color: #6b7280; } .selected-tip strong { color: #2563eb; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; flex-shrink: 0; }
.dialog-form { padding-right: 20px; }
.batch-delete-text { font-size: 15px; color: #374151; text-align: center; padding: 16px 0; }
.batch-delete-text strong { color: #ef4444; font-size: 18px; }
</style>