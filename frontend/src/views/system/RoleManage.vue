<template>
  <div class="role-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色标识">
          <el-input v-model="searchForm.role_name" placeholder="请输入角色标识" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 110px">
            <el-option label="全部" :value="null" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
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
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
          <el-button type="danger" :icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <span class="selected-tip" v-if="selectedIds.length > 0">已选择 <strong>{{ selectedIds.length }}</strong> 项</span>
        </div>
      </div>

      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="role_name" label="角色标识" min-width="150" />
        <el-table-column prop="description" label="角色描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sort_order" label="排序" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" :disabled="row.role_name === 'super_admin'" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.role_name === 'super_admin'">
              <el-tag type="danger" size="small">系统保护</el-tag>
            </template>
            <template v-else>
              <el-button type="success" size="small" :icon="Menu" link @click="handleAssignMenu(row)">分配菜单</el-button>
              <el-button type="warning" size="small" :icon="Key" link @click="handleAssignPermission(row)">分配权限</el-button>
              <el-button type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除该角色吗？" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button type="danger" size="small" :icon="Delete" link>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]" :total="filteredData.length" layout="total, sizes, prev, pager, next, jumper" background @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" class="dialog-form">
        <el-form-item label="角色标识" prop="role_name">
          <el-input v-model="formData.role_name" placeholder="如 admin, user" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sort_order">
          <el-input-number v-model="formData.sort_order" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog v-model="menuDialogVisible" :title="'分配菜单 - ' + currentRoleName" width="520px" :close-on-click-modal="false">
      <el-tree ref="menuTreeRef" :data="menuTreeData" show-checkbox node-key="id" default-expand-all :default-checked-keys="currentMenuKeys" />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMenuSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permDialogVisible" :title="'分配权限 - ' + currentRoleName" width="560px" :close-on-click-modal="false">
      <el-tree ref="permTreeRef" :data="permTreeData" show-checkbox node-key="id" default-expand-all :default-checked-keys="currentPermKeys" />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个角色吗？</p>
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
import { Search, Refresh, Plus, Delete, Edit, Menu, Key } from '@element-plus/icons-vue'

const allRoles = ref([
  { id: 1, role_name: 'super_admin', description: '超级管理员，拥有系统全部权限', sort_order: 0, status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 2, role_name: 'admin', description: '管理员，可管理用户和系统配置', sort_order: 1, status: 1, createTime: '2026-01-15 10:00:00' },
  { id: 3, role_name: 'user', description: '普通用户，仅可查看和编辑个人数据', sort_order: 2, status: 1, createTime: '2026-01-15 10:05:00' }
])
let nextId = 4

const searchForm = reactive({ role_name: '', status: null })
const filteredData = computed(() => allRoles.value.filter(item => {
  if (searchForm.role_name && !item.role_name.includes(searchForm.role_name)) return false
  if (searchForm.status !== null && item.status !== searchForm.status) return false
  return true
}))
function handleSearch() { currentPage.value = 1 }
function handleReset() { searchForm.role_name = ''; searchForm.status = null; currentPage.value = 1 }

const currentPage = ref(1); const pageSize = ref(10)
const pagedData = computed(() => { const s = (currentPage.value - 1) * pageSize.value; return filteredData.value.slice(s, s + pageSize.value) })
function handleSizeChange() { currentPage.value = 1 }
function handleCurrentChange() {}

const selectedIds = ref([])
function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }
function handleStatusChange(row) { ElMessage.success(`角色「${row.role_name}」已${row.status === 1 ? '启用' : '禁用'}`) }
const tableLoading = ref(false)

const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref(null); const submitLoading = ref(false); const formRef = ref(null)
const formData = reactive({ role_name: '', description: '', sort_order: 0, status: 1 })
const formRules = {
  role_name: [{ required: true, message: '请输入角色标识', trigger: 'blur' }, { pattern: /^[a-z_]+$/, message: '必须为小写字母和下划线', trigger: 'blur' }]
}
const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

function handleAdd() { isEdit.value = false; editId.value = null; resetForm(); dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; formData.role_name = row.role_name; formData.description = row.description; formData.sort_order = row.sort_order; formData.status = row.status; dialogVisible.value = true }
function resetForm() { formData.role_name = ''; formData.description = ''; formData.sort_order = 0; formData.status = 1 }
function handleDialogClose() { resetForm(); formRef.value?.resetFields() }

function handleSubmit() {
  if (!formRef.value) return
  formRef.value.validate().then(() => {
    submitLoading.value = true; setTimeout(() => {
      if (isEdit.value) {
        const t = allRoles.value.find(r => r.id === editId.value)
        if (t) { t.description = formData.description; t.sort_order = formData.sort_order; t.status = formData.status }
        ElMessage.success('角色修改成功')
      } else {
        allRoles.value.push({ id: nextId++, role_name: formData.role_name, description: formData.description, sort_order: formData.sort_order, status: formData.status, createTime: new Date().toLocaleString('zh-CN', { hour12: false }) })
        ElMessage.success('角色创建成功')
      }
      dialogVisible.value = false; resetForm(); submitLoading.value = false
    }, 600)
  }).catch(() => {})
}

function handleDelete(row) {
  const idx = allRoles.value.findIndex(r => r.id === row.id)
  if (idx > -1) { allRoles.value.splice(idx, 1); ElMessage.success(`角色「${row.role_name}」已删除`) }
}

const batchDeleteVisible = ref(false); const batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length === 0) { ElMessage.warning('请先选择要删除的角色'); return }; batchDeleteVisible.value = true }
function confirmBatchDelete() { batchLoading.value = true; setTimeout(() => { allRoles.value = allRoles.value.filter(r => !selectedIds.value.includes(r.id)); ElMessage.success(`已删除 ${selectedIds.value.length} 个角色`); selectedIds.value = []; batchDeleteVisible.value = false; batchLoading.value = false }, 600) }

// ===== 分配菜单 =====
const menuDialogVisible = ref(false); const currentRoleName = ref(''); const currentRoleId = ref(null)
const currentMenuKeys = ref([]); const menuTreeRef = ref(null)

const menuTreeData = ref([
  { id: 1, label: '仪表盘' },
  { id: 2, label: '系统管理', children: [
    { id: 21, label: '用户管理' }, { id: 22, label: '角色管理' }, { id: 23, label: '菜单管理' }, { id: 24, label: '权限管理' }, { id: 25, label: '部门管理' }
  ]},
  { id: 3, label: 'RAG管理', children: [{ id: 31, label: '文档管理' }] },
  { id: 4, label: '学生中心', children: [{ id: 41, label: '学生管理' }] },
  { id: 5, label: '用户中心', children: [{ id: 51, label: '个人信息' }] }
])

function handleAssignMenu(row) {
  currentRoleName.value = row.role_name; currentRoleId.value = row.id
  if (row.role_name === 'super_admin' || row.role_name === 'admin') {
    currentMenuKeys.value = [1, 2, 21, 22, 23, 24, 25, 3, 31, 4, 41, 5, 51]
  } else {
    currentMenuKeys.value = [1, 4, 41, 5, 51]
  }
  menuDialogVisible.value = true
}

function handleMenuSave() {
  const keys = menuTreeRef.value.getCheckedKeys()
  ElMessage.success(`菜单分配成功，共 ${keys.length} 项权限`)
  menuDialogVisible.value = false
}

// ===== 分配权限 =====
const permDialogVisible = ref(false)
const currentPermKeys = ref([])
const permTreeRef = ref(null)

const permTreeData = ref([
  { id: 'p1', label: '用户管理', children: [
    { id: 'p1-1', label: 'user:view - 查看用户' },
    { id: 'p1-2', label: 'user:create - 创建用户' },
    { id: 'p1-3', label: 'user:update - 编辑用户' },
    { id: 'p1-4', label: 'user:delete - 删除用户' }
  ]},
  { id: 'p2', label: '角色管理', children: [
    { id: 'p2-1', label: 'role:manage - 管理角色' }
  ]},
  { id: 'p3', label: '菜单管理', children: [
    { id: 'p3-1', label: 'menu:manage - 管理菜单' }
  ]},
  { id: 'p4', label: '部门管理', children: [
    { id: 'p4-1', label: 'dept:view - 查看部门' },
    { id: 'p4-2', label: 'dept:manage - 管理部门' }
  ]},
  { id: 'p5', label: 'RAG管理', children: [
    { id: 'p5-1', label: 'doc:manage - 文档管理' }
  ]},
  { id: 'p6', label: '学生中心', children: [
    { id: 'p6-1', label: 'student:view - 查看学生' }
  ]},
  { id: 'p7', label: '数据导出', children: [
    { id: 'p7-1', label: 'data:export - 导出数据' }
  ]}
])

function handleAssignPermission(row) {
  currentRoleName.value = row.role_name
  currentRoleId.value = row.id
  if (row.role_name === 'super_admin') {
    currentPermKeys.value = ['p1-1', 'p1-2', 'p1-3', 'p1-4', 'p2-1', 'p3-1', 'p4-1', 'p4-2', 'p5-1', 'p6-1', 'p7-1']
  } else if (row.role_name === 'admin') {
    currentPermKeys.value = ['p1-1', 'p1-2', 'p1-3', 'p2-1', 'p3-1', 'p4-1', 'p5-1']
  } else {
    currentPermKeys.value = ['p1-1', 'p6-1']
  }
  permDialogVisible.value = true
}

function handlePermSave() {
  const keys = permTreeRef.value.getCheckedKeys()
  ElMessage.success(`权限分配成功，共 ${keys.length} 项权限`)
  permDialogVisible.value = false
}
</script>

<style scoped>
.role-manage { height: 100%; display: flex; flex-direction: column; gap: 16px; }
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