<template>
  <div class="menu-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="菜单名称">
          <el-input v-model="searchForm.menu_name" placeholder="请输入菜单名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.menu_type" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="目录" :value="0" />
            <el-option label="菜单" :value="1" />
            <el-option label="按钮" :value="2" />
          </el-select>
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

    <!-- 操作按钮 -->
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增菜单</el-button>
          <el-button type="danger" :icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <span class="selected-tip" v-if="selectedIds.length > 0">已选择 <strong>{{ selectedIds.length }}</strong> 项</span>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        :data="pagedData"
        v-loading="tableLoading"
        stripe
        border
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="menu_name" label="菜单名称" min-width="180" />
        <el-table-column prop="menu_icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.menu_icon" :size="18"><component :is="row.menu_icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="menu_path" label="路由路径" width="180" show-overflow-tooltip />
        <el-table-column prop="menu_component" label="组件路径" width="180" show-overflow-tooltip />
        <el-table-column prop="menu_type" label="类型" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.menu_type === 0 ? '' : row.menu_type === 1 ? 'success' : 'info'" size="small">{{ ['目录','菜单','按钮'][row.menu_type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visible" label="可见" width="65" align="center">
          <template #default="{ row }"><el-tag :type="row.visible ? 'success' : 'info'" size="small">{{ row.visible ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="is_frame" label="外链" width="65" align="center">
          <template #default="{ row }"><el-tag :type="row.is_frame ? 'warning' : 'info'" size="small">{{ row.is_frame ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="sort_order" label="排序" width="60" align="center" />
        <el-table-column prop="status" label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.menu_type === 0" type="success" size="small" :icon="Plus" link @click="handleAdd(row)">添加子项</el-button>
            <el-button type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row)">
              <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]" :total="flatList.length" layout="total, sizes, prev, pager, next, jumper" background @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="dialog-form">
        <el-form-item v-if="!isEdit" label="上级菜单">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menu_name">
          <el-input v-model="formData.menu_name" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="菜单类型" prop="menu_type">
          <el-radio-group v-model="formData.menu_type">
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.menu_type !== 2" label="图标">
          <el-input v-model="formData.menu_icon" placeholder="如 Setting, User" />
        </el-form-item>
        <el-form-item v-if="formData.menu_type !== 0" label="路由路径" prop="menu_path">
          <el-input v-model="formData.menu_path" placeholder="如 /main/system/users" />
        </el-form-item>
        <el-form-item v-if="formData.menu_type !== 0" label="组件路径">
          <el-input v-model="formData.menu_component" placeholder="如 views/system/UserManage" />
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="formData.visible" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="是否外链">
          <el-switch v-model="formData.is_frame" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="formData.permission" placeholder="如 user:create" />
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

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个菜单吗？</p>
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

// ===== 数据 =====
const allMenus = ref([
  { id: 1, parent_id: 0, menu_name: '仪表盘', menu_icon: 'DataBoard', menu_path: '/main/dashboard', menu_component: 'views/Dashboard', menu_type: 1, visible: 1, is_frame: 0, permission: '', sort_order: 1, status: 1, createTime: '2026-01-15 10:00:00', children: [] },
  { id: 2, parent_id: 0, menu_name: '系统管理', menu_icon: 'Setting', menu_path: '', menu_component: '', menu_type: 0, visible: 1, is_frame: 0, permission: '', sort_order: 2, status: 1, createTime: '2026-01-15 10:00:00', children: [
    { id: 21, parent_id: 2, menu_name: '用户管理', menu_icon: '', menu_path: '/main/system/users', menu_component: 'views/system/UserManage', menu_type: 1, visible: 1, is_frame: 0, permission: 'user:view', sort_order: 1, status: 1, createTime: '2026-01-15 10:05:00', children: [] },
    { id: 22, parent_id: 2, menu_name: '角色管理', menu_icon: '', menu_path: '/main/system/roles', menu_component: 'views/system/RoleManage', menu_type: 1, visible: 1, is_frame: 0, permission: 'role:view', sort_order: 2, status: 1, createTime: '2026-01-15 10:05:00', children: [] },
    { id: 23, parent_id: 2, menu_name: '菜单管理', menu_icon: '', menu_path: '/main/system/menus', menu_component: 'views/system/MenuManage', menu_type: 1, visible: 1, is_frame: 0, permission: 'menu:view', sort_order: 3, status: 1, createTime: '2026-01-15 10:05:00', children: [] },
    { id: 24, parent_id: 2, menu_name: '权限管理', menu_icon: '', menu_path: '/main/system/permission', menu_component: 'views/system/PermissionManage', menu_type: 1, visible: 1, is_frame: 0, permission: 'permission:view', sort_order: 4, status: 1, createTime: '2026-02-01 08:00:00', children: [] },
    { id: 25, parent_id: 2, menu_name: '部门管理', menu_icon: '', menu_path: '/main/system/dept', menu_component: 'views/system/DeptManage', menu_type: 1, visible: 1, is_frame: 0, permission: 'dept:view', sort_order: 5, status: 1, createTime: '2026-02-01 08:00:00', children: [] }
  ]},
  { id: 3, parent_id: 0, menu_name: 'RAG管理', menu_icon: 'Cpu', menu_path: '', menu_component: '', menu_type: 0, visible: 1, is_frame: 0, permission: '', sort_order: 3, status: 1, createTime: '2026-01-15 10:00:00', children: [
    { id: 31, parent_id: 3, menu_name: '文档管理', menu_icon: '', menu_path: '/main/rag/documents', menu_component: 'views/rag/DocumentManage', menu_type: 1, visible: 1, is_frame: 0, permission: 'doc:view', sort_order: 1, status: 1, createTime: '2026-01-15 10:06:00', children: [] }
  ]},
  { id: 5, parent_id: 0, menu_name: '用户中心', menu_icon: 'User', menu_path: '', menu_component: '', menu_type: 0, visible: 1, is_frame: 0, permission: '', sort_order: 5, status: 1, createTime: '2026-01-15 10:00:00', children: [
    { id: 51, parent_id: 5, menu_name: '个人信息', menu_icon: '', menu_path: '/main/user/profile', menu_component: 'views/user/UserProfile', menu_type: 1, visible: 1, is_frame: 0, permission: '', sort_order: 1, status: 1, createTime: '2026-01-15 10:08:00', children: [] }
  ]}
])
let nextId = 100

const flatList = computed(() => { const r = []; function f(l) { for (const i of l) { r.push(i); if (i.children) f(i.children) } }; f(allMenus.value); return r })

// ===== 搜索 =====
const searchForm = reactive({ menu_name: '', menu_type: null, status: null })
const filteredData = computed(() => {
  if (!searchForm.menu_name && searchForm.menu_type === null && searchForm.status === null) return allMenus.value
  const kw = searchForm.menu_name
  function filter(list) {
    return list.reduce((acc, item) => {
      const match = (!kw || item.menu_name.includes(kw)) && (searchForm.menu_type === null || item.menu_type === searchForm.menu_type) && (searchForm.status === null || item.status === searchForm.status)
      const fc = item.children ? filter(item.children) : []
      if (match || fc.length > 0) acc.push({ ...item, children: fc.length > 0 ? fc : item.children })
      return acc
    }, [])
  }
  return filter(allMenus.value)
})
function handleSearch() { currentPage.value = 1 }
function handleReset() { searchForm.menu_name = ''; searchForm.menu_type = null; searchForm.status = null; currentPage.value = 1 }

const currentPage = ref(1); const pageSize = ref(50)
const pagedData = computed(() => filteredData.value)
function handleSizeChange() {}; function handleCurrentChange() {}

const selectedIds = ref([])
function handleSelectionChange(rows) { const ids = []; function c(l) { for (const i of l) { ids.push(i.id); if (i.children) c(i.children) } }; c(rows); selectedIds.value = ids }
function handleStatusChange(row) { ElMessage.success(`菜单「${row.menu_name}」已${row.status === 1 ? '启用' : '禁用'}`) }
const tableLoading = ref(false)

const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref(null); const submitLoading = ref(false); const formRef = ref(null)
const parentId = ref(null); const parentName = ref('根级菜单')
const formData = reactive({ menu_name: '', menu_type: 1, menu_icon: '', menu_path: '', menu_component: '', visible: 1, is_frame: 0, permission: '', sort_order: 0, status: 1 })
const formRules = { menu_name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }] }
const dialogTitle = computed(() => isEdit.value ? '编辑菜单' : '新增菜单')

function findById(list, id) { for (const i of list) { if (i.id === id) return i; if (i.children) { const f = findById(i.children, id); if (f) return f } } return null }

function handleAdd(parentRow) { isEdit.value = false; editId.value = null; resetForm(); if (parentRow) { parentId.value = parentRow.id; parentName.value = parentRow.menu_name } else { parentId.value = null; parentName.value = '根级菜单' }; dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; Object.assign(formData, row); parentName.value = '—'; dialogVisible.value = true }
function resetForm() { formData.menu_name = ''; formData.menu_type = 1; formData.menu_icon = ''; formData.menu_path = ''; formData.menu_component = ''; formData.visible = 1; formData.is_frame = 0; formData.permission = ''; formData.sort_order = 0; formData.status = 1 }
function handleDialogClose() { resetForm(); formRef.value?.resetFields() }

function handleSubmit() {
  if (!formRef.value) return
  formRef.value.validate().then(() => {
    submitLoading.value = true; setTimeout(() => {
      if (isEdit.value) {
        const t = findById(allMenus.value, editId.value)
        if (t) { t.menu_name = formData.menu_name; t.menu_icon = formData.menu_icon; t.menu_path = formData.menu_path; t.menu_component = formData.menu_component; t.visible = formData.visible; t.is_frame = formData.is_frame; t.permission = formData.permission; t.sort_order = formData.sort_order; t.status = formData.status }
        ElMessage.success('菜单修改成功')
      } else {
        const nn = { id: nextId++, parent_id: parentId.value || 0, menu_name: formData.menu_name, menu_type: formData.menu_type, menu_icon: formData.menu_icon, menu_path: formData.menu_path, menu_component: formData.menu_component, visible: formData.visible, is_frame: formData.is_frame, permission: formData.permission, sort_order: formData.sort_order, status: formData.status, createTime: new Date().toLocaleString('zh-CN', { hour12: false }), children: formData.menu_type === 0 ? [] : undefined }
        if (parentId.value) { const p = findById(allMenus.value, parentId.value); if (p && p.children) p.children.push(nn) } else { allMenus.value.push(nn) }
        ElMessage.success('菜单创建成功')
      }
      dialogVisible.value = false; resetForm(); submitLoading.value = false
    }, 600)
  }).catch(() => {})
}

function removeById(list, id) { for (let i = 0; i < list.length; i++) { if (list[i].id === id) { list.splice(i, 1); return true } if (list[i].children && removeById(list[i].children, id)) return true } return false }
function handleDelete(row) { removeById(allMenus.value, row.id); ElMessage.success(`菜单「${row.menu_name}」已删除`) }

const batchDeleteVisible = ref(false); const batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length === 0) { ElMessage.warning('请先选择'); return }; batchDeleteVisible.value = true }
function confirmBatchDelete() { batchLoading.value = true; setTimeout(() => { for (const id of selectedIds.value) removeById(allMenus.value, id); ElMessage.success(`已删除 ${selectedIds.value.length} 个菜单`); selectedIds.value = []; batchDeleteVisible.value = false; batchLoading.value = false }, 600) }
</script>

<style scoped>
.menu-manage { height: 100%; display: flex; flex-direction: column; gap: 16px; }
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