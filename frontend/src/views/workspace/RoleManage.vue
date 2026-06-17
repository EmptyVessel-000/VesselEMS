<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">角色管理</h2>
      <p class="page-subtitle">管理系统中的角色与权限分配</p>
    </div>

    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色标识">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色标识" clearable />
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
          <el-button v-if="hasPermission('role:create')" type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
          <el-button v-if="hasPermission('role:delete')" type="danger" :icon="Delete" :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</el-button>
        </div>
      </div>
      <el-table :data="pagedData" v-loading="tableLoading" stripe style="width:100%" @selection-change="handleSelectionChange" @sort-change="handleSortChange" :default-sort="{ prop: 'createTime', order: 'descending' }">
        <el-table-column type="selection" width="50" align="center" :selectable="(row) => !BUILT_IN_ROLES.includes(row.roleName)" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="roleName" label="角色标识" min-width="150" sortable />
        <el-table-column prop="description" label="角色描述" min-width="250" show-overflow-tooltip sortable />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" sortable />
        <el-table-column prop="createTime" label="创建时间" width="170" sortable />
        <el-table-column prop="status" label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-if="BUILT_IN_ROLES.includes(row.roleName)" :model-value="row.status" :active-value="1" :inactive-value="0" disabled />
            <el-switch v-else v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!BUILT_IN_ROLES.includes(row.roleName) && hasPermission('role:assign')" type="success" size="small" :icon="Menu" link @click="handleAssignMenu(row)">分配菜单</el-button>
            <el-button v-if="!BUILT_IN_ROLES.includes(row.roleName) && hasPermission('role:assign')" type="warning" size="small" :icon="Key" link @click="handleAssignPerm(row)">分配权限</el-button>
            <el-button v-if="!BUILT_IN_ROLES.includes(row.roleName) && hasPermission('role:edit')" type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm v-if="!BUILT_IN_ROLES.includes(row.roleName) && hasPermission('role:delete')" title="确定删除?" @confirm="handleDelete(row)"><template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="filteredData.length" layout="total,sizes,prev,pager,next,jumper" background @size-change="handleSizeChange" /></div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" class="dialog-form">
        <el-form-item label="角色标识" prop="roleName"><el-input v-model="formData.roleName" :disabled="isEdit" /></el-form-item>
        <el-form-item label="角色描述" prop="description"><el-input v-model="formData.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="formData.sortOrder" :min="0" :max="999" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="formData.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <!-- 分配菜单 -->
    <el-dialog v-model="menuDialogVisible" :title="'分配菜单 - ' + currentRoleName" width="520px" :close-on-click-modal="false">
      <el-tree ref="menuTreeRef" :data="menuTreeData" show-checkbox node-key="id" default-expand-all :default-checked-keys="currentMenuKeys" />
      <template #footer><el-button @click="menuDialogVisible=false">取消</el-button><el-button type="primary" @click="handleMenuSave">保存</el-button></template>
    </el-dialog>

    <!-- 分配权限（按菜单分组） -->
    <el-dialog v-model="permDialogVisible" :title="'分配权限 - ' + currentRoleName" width="600px" :close-on-click-modal="false">
      <div v-loading="permTreeLoading" class="perm-tree-container">
        <div v-for="group in permTreeData" :key="group.menuId" class="perm-group">
          <div class="perm-group-header">
            <el-icon :size="16"><Folder /></el-icon>
            <span class="perm-group-title">{{ group.menuName }}</span>
          </div>
          <div class="perm-group-body">
            <div v-for="perm in group.permissions" :key="perm.id" class="perm-item">
              <el-checkbox v-model="perm.checked" @change="onPermChange(perm)">
                <span class="perm-code">{{ perm.permissionCode }}</span>
                <span class="perm-desc" v-if="perm.description">{{ perm.description }}</span>
              </el-checkbox>
            </div>
          </div>
        </div>
        <el-empty v-if="permTreeData.length === 0" description="暂无可用权限" />
      </div>
      <template #footer>
        <el-button @click="permDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handlePermSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个角色吗？</p>
      <template #footer><el-button @click="batchDeleteVisible=false">取消</el-button><el-button type="danger" :loading="batchLoading" @click="confirmBatchDelete">确定删除</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, Menu, Key, Folder } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission, BUILT_IN_ROLES } from '../../stores/permissions.js'

const allRoles = ref([]), tableLoading = ref(false)
function handleSortChange(sort) {
  // 交给 el-table 的 sortable 属性处理
}

async function fetchRoles() {
  tableLoading.value = true
  try { const d = await request.get('/api/roles'); allRoles.value = (d || []).map(r => ({ ...r, roleName: r.roleName || r.role_name || '', createTime: r.createTime || (r.createdAt ? new Date(r.createdAt).toLocaleString('zh-CN',{hour12:false}) : '') })) }
  catch { allRoles.value = [] }
  finally { tableLoading.value = false }
}

const searchForm = reactive({ roleName: '' })
const filteredData = computed(() => allRoles.value.filter(i => !searchForm.roleName || (i.roleName||'').includes(searchForm.roleName)))
function handleSearch() { currentPage.value = 1 }
function handleReset() { searchForm.roleName = ''; currentPage.value = 1 }

const currentPage = ref(1), pageSize = ref(10)
const pagedData = computed(() => { const s = (currentPage.value-1)*pageSize.value; return filteredData.value.slice(s,s+pageSize.value) })
function handleSizeChange() { currentPage.value = 1 }
const selectedIds = ref([])
function handleSelectionChange(rows) { selectedIds.value = rows.map(r=>r.id) }

const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null), submitLoading = ref(false), formRef = ref(null)
const formData = reactive({ roleName: '', description: '', sortOrder: 0, status: 1 })
const formRules = { roleName: [{ required: true, message: '请输入角色标识', trigger: 'blur' }] }
const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

function handleAdd() { isEdit.value=false; editId.value=null; resetForm(); dialogVisible.value=true }
function handleEdit(row) { isEdit.value=true; editId.value=row.id; formData.roleName=row.roleName; formData.description=row.description; formData.sortOrder=row.sortOrder||0; formData.status=row.status||1; dialogVisible.value=true }
function resetForm() { formData.roleName=''; formData.description=''; formData.sortOrder=0; formData.status=1 }
function handleDialogClose() { resetForm(); formRef.value?.resetFields() }

async function handleSubmit() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    const payload = { roleName: formData.roleName, description: formData.description, sortOrder: formData.sortOrder, status: formData.status }
    if (isEdit.value) { await request.put(`/api/roles/${editId.value}`, payload); ElMessage.success('角色修改成功') }
    else { await request.post('/api/roles', payload); ElMessage.success('角色创建成功') }
    dialogVisible.value = false; resetForm(); await fetchRoles()
  } catch {} finally { submitLoading.value = false }
}

async function handleDelete(row) { try { await request.delete(`/api/roles/${row.id}`); ElMessage.success(`角色「${row.roleName}」已删除`); await fetchRoles() } catch {} }

async function handleStatusChange(row) {
  try {
    await request.put(`/api/roles/${row.id}`, { roleName: row.roleName, description: row.description, sortOrder: row.sortOrder, status: row.status })
    ElMessage.success(`角色「${row.roleName}」已${row.status === 1 ? '启用' : '禁用'}`)
  } catch {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const batchDeleteVisible = ref(false), batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length===0) { ElMessage.warning('请先选择'); return }; batchDeleteVisible.value = true }
async function confirmBatchDelete() { batchLoading.value=true; try { for (const id of selectedIds.value) await request.delete(`/api/roles/${id}`); ElMessage.success(`已删除 ${selectedIds.value.length} 个角色`); selectedIds.value=[]; batchDeleteVisible.value=false; await fetchRoles() } catch { ElMessage.error('批量删除失败') } finally { batchLoading.value=false } }

const menuDialogVisible=ref(false), permDialogVisible=ref(false), currentRoleName=ref(''), currentRoleId=ref(null)
const currentMenuKeys=ref([]), currentPermKeys=ref([]), menuTreeRef=ref(null)

const menuTreeData = ref([])
const permTreeData = ref([])
const permTreeLoading = ref(false)

function buildTree(list) {
  const map = {}, roots = []
  list.forEach(item => { item.children = []; map[item.id] = item })
  list.forEach(item => {
    if (item.parentId != null && map[item.parentId]) {
      map[item.parentId].children.push(item)
    } else {
      roots.push(item)
    }
  })
  return roots
}

async function handleAssignMenu(row) {
  currentRoleName.value = row.roleName; currentRoleId.value = row.id
  try {
    const menuList = await request.get('/api/menus')
    menuTreeData.value = buildTree((menuList || []).filter(m => m.id !== 1).map(m => ({ id: m.id, label: m.menuName, parentId: m.parentId })))
    const assigned = await request.get(`/api/roles/${row.id}/menus`)
    currentMenuKeys.value = (assigned || []).map(rm => rm.menuId)
  } catch { menuTreeData.value = []; currentMenuKeys.value = [] }
  menuDialogVisible.value = true
}

async function handleMenuSave() {
  try {
    const checkedIds = menuTreeRef.value.getCheckedKeys()
    await request.post(`/api/roles/${currentRoleId.value}/menus`, checkedIds)
    ElMessage.success(`菜单分配成功，共 ${checkedIds.length} 项`)
  } catch { ElMessage.error('菜单分配失败') }
  menuDialogVisible.value = false
}

async function handleAssignPerm(row) {
  currentRoleName.value = row.roleName; currentRoleId.value = row.id
  permDialogVisible.value = true
  permTreeLoading.value = true
  permTreeData.value = []
  try {
    const data = await request.get(`/api/roles/${row.id}/permissions/tree`)
    permTreeData.value = data || []
  } catch {
    permTreeData.value = []
    ElMessage.error('获取权限列表失败')
  } finally {
    permTreeLoading.value = false
  }
}

function onPermChange(perm) {
  // 实时更新选中状态，保存时统一提交
}

async function handlePermSave() {
  // 收集所有选中的权限ID
  const checkedIds = []
  for (const group of permTreeData.value) {
    for (const perm of group.permissions) {
      if (perm.checked) {
        checkedIds.push(perm.id)
      }
    }
  }
  try {
    await request.post(`/api/roles/${currentRoleId.value}/permissions`, checkedIds)
    ElMessage.success(`权限分配成功，共 ${checkedIds.length} 项`)
  } catch { ElMessage.error('权限分配失败') }
  permDialogVisible.value = false
}

onMounted(() => { fetchRoles() })
</script>

<style scoped>
/* RoleManage 使用全局 .page-container / .page-header / .search-section / .table-section 样式 */

/* 权限树容器 */
.perm-tree-container {
  max-height: 480px;
  overflow-y: auto;
}

/* 权限分组 */
.perm-group {
  margin-bottom: 16px;
  border: 1px solid #f0efed;
  border-radius: 8px;
  overflow: hidden;
}

.perm-group-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #fafaf9;
  font-size: 14px;
  font-weight: 600;
  color: #1c1917;
  border-bottom: 1px solid #f0efed;
}

.perm-group-title {
  font-size: 14px;
  font-weight: 600;
  color: #1c1917;
}

.perm-group-body {
  padding: 8px 14px;
}

.perm-item {
  padding: 6px 0;
}

.perm-item .el-checkbox {
  display: flex;
  align-items: center;
  height: auto;
}

.perm-code {
  font-size: 13px;
  font-weight: 500;
  color: #44403c;
  margin-right: 8px;
}

.perm-desc {
  font-size: 12px;
  color: #a8a29e;
}
</style>