<template>
  <div class="role-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色标识"><el-input v-model="searchForm.roleName" placeholder="请输入角色标识" clearable /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <div class="toolbar"><div class="toolbar-left"><el-button v-if="hasMenu(22) && hasPermission('role:create')" type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button><el-button v-if="hasMenu(22) && hasPermission('role:delete')" type="danger" :icon="Delete" :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</el-button></div></div>
      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width:100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" :selectable="(row) => !BUILT_IN_ROLES.includes(row.roleName)" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="roleName" label="角色标识" min-width="150" />
        <el-table-column prop="description" label="角色描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
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
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" class="dialog-form">
        <el-form-item label="角色标识" prop="roleName"><el-input v-model="formData.roleName" :disabled="isEdit" /></el-form-item>
        <el-form-item label="角色描述" prop="description"><el-input v-model="formData.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="formData.sortOrder" :min="0" :max="999" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="formData.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" :title="'分配菜单 - ' + currentRoleName" width="520px" :close-on-click-modal="false">
      <el-tree ref="menuTreeRef" :data="menuTreeData" show-checkbox node-key="id" default-expand-all :default-checked-keys="currentMenuKeys" />
      <template #footer><el-button @click="menuDialogVisible=false">取消</el-button><el-button type="primary" @click="handleMenuSave">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="permDialogVisible" :title="'分配权限 - ' + currentRoleName" width="560px" :close-on-click-modal="false">
      <el-tree ref="permTreeRef" :data="permTreeData" show-checkbox node-key="id" default-expand-all :default-checked-keys="currentPermKeys" />
      <template #footer><el-button @click="permDialogVisible=false">取消</el-button><el-button type="primary" @click="handlePermSave">保存</el-button></template>
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
import { Search, Refresh, Plus, Delete, Edit, Menu, Key } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission, hasMenu, BUILT_IN_ROLES } from '../../stores/permissions.js'

const allRoles = ref([]), tableLoading = ref(false)

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
const currentMenuKeys=ref([]), currentPermKeys=ref([]), menuTreeRef=ref(null), permTreeRef=ref(null)

const menuTreeData = ref([])
const permTreeData = ref([])

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
  try {
    const permList = await request.get('/api/permissions')
    permTreeData.value = (permList || []).map(p => ({ id: p.id, label: p.permissionCode }))
    const assigned = await request.get(`/api/roles/${row.id}/permissions`)
    currentPermKeys.value = (assigned || []).map(rp => rp.permissionId)
  } catch { permTreeData.value = []; currentPermKeys.value = [] }
  permDialogVisible.value = true
}

async function handlePermSave() {
  try {
    const checkedIds = permTreeRef.value.getCheckedKeys()
    await request.post(`/api/roles/${currentRoleId.value}/permissions`, checkedIds)
    ElMessage.success(`权限分配成功，共 ${checkedIds.length} 项`)
  } catch { ElMessage.error('权限分配失败') }
  permDialogVisible.value = false
}

onMounted(() => { fetchRoles() })
</script>

<style scoped>
.role-manage { height:100%; display:flex; flex-direction:column; gap:16px; }
.search-card { flex-shrink:0; } .search-card .el-form { margin-bottom:0; }
.table-card { flex:1; display:flex; flex-direction:column; overflow:hidden; }
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
.toolbar-left { display:flex; gap:8px; }
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; flex-shrink:0; }
.dialog-form { padding-right:20px; }
.batch-delete-text { font-size:15px; color:#374151; text-align:center; padding:16px 0; }
.batch-delete-text strong { color:#ef4444; font-size:18px; }
</style>