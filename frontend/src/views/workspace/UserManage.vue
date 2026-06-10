<template>
  <div class="user-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名"><el-input v-model="searchForm.username" placeholder="请输入用户名" clearable /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button v-if="hasMenu(21) && hasPermission('user:create')" type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
          <el-button type="success" :icon="Upload" @click="importVisible = true">导入用户</el-button>
          <el-button v-if="hasMenu(21) && hasPermission('user:delete')" type="danger" :icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <span class="selected-tip" v-if="selectedIds.length > 0">已选择 <strong>{{ selectedIds.length }}</strong> 项</span>
        </div>
      </div>
      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" :selectable="(row) => !row.isSuperAdmin" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="110" />
        <el-table-column prop="nickname" label="昵称" width="100" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column label="角色" width="140" align="center">
          <template #default="{ row }">
            <div style="display:flex;flex-wrap:wrap;gap: 4px;justify-content:center;">
              <el-tag v-for="(name, idx) in row.roleNames" :key="idx" :type="name === 'super_admin' ? 'danger' : ''" size="small">{{ name }}</el-tag>
              <span v-if="!row.roleNames || row.roleNames.length === 0" style="color:#9ca3af">—</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" :disabled="row.isSuperAdmin" @change="handleStatusToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.isSuperAdmin">
              <el-tag type="danger" size="small">系统保护</el-tag>
            </template>
            <template v-else>
              <el-button v-if="hasMenu(21) && hasPermission('user:update')" type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm v-if="hasMenu(21) && hasPermission('user:delete')" title="确定删除该用户吗？" @confirm="handleDelete(row)">
                <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
              </el-popconfirm>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="filteredData.length" layout="total,sizes,prev,pager,next,jumper" background @size-change="handleSizeChange" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="dialog-form">
        <el-form-item label="用户名" prop="username"><el-input v-model="formData.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="formData.password" type="password" :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'" show-password /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="formData.email" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="formData.nickname" /></el-form-item>
        <el-form-item label="真实姓名"><el-input v-model="formData.realName" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="formData.gender">
            <el-option label="未知" :value="0" />
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="formData.telephone" /></el-form-item>
        <el-form-item label="部门">
          <el-select v-model="formData.departmentId" placeholder="请选择部门" clearable style="width:100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.dept_name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="formData.roleIds" placeholder="请选择角色" multiple style="width:100%">
            <el-option v-for="r in roleOptions.filter(r => r.role_name !== SUPER_ADMIN_ROLE_NAME)" :key="r.id" :label="r.role_name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态"><el-switch v-model="formData.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个用户吗？</p>
      <template #footer><el-button @click="batchDeleteVisible=false">取消</el-button><el-button type="danger" :loading="batchLoading" @click="confirmBatchDelete">确定删除</el-button></template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="导入用户" width="500px" :close-on-click-modal="false" @close="resetImport">
      <div style="margin-bottom:16px">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".csv,.xlsx,.xls"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          drag
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip" style="margin-top:8px">
              支持 .csv / .xlsx / .xls 格式 |
              <el-button type="primary" link size="small" @click="downloadTemplate">下载模板</el-button>
            </div>
          </template>
        </el-upload>
      </div>
      <div v-if="importResult" style="margin-top:12px">
        <el-alert :title="`导入完成：成功 ${importResult.success} 条，失败 ${importResult.failed} 条`"
          :type="importResult.failed > 0 ? 'warning' : 'success'" :closable="false" />
        <ul v-if="importResult.errors && importResult.errors.length" style="margin-top:8px;font-size:12px;color:#ef4444;max-height:120px;overflow-y:auto">
          <li v-for="(e, i) in importResult.errors" :key="i">{{ e }}</li>
        </ul>
      </div>
      <template #footer>
        <el-button @click="importVisible=false">关闭</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, Upload, UploadFilled } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission, hasMenu } from '../../stores/permissions.js'

const roleOptions = ref([])
const deptOptions = ref([])
const allUsers = ref([])
const tableLoading = ref(false)
const SUPER_ADMIN_ROLE_NAME = 'super_admin'

async function fetchRoles() {
  try {
    const d = await request.get('/api/roles')
    roleOptions.value = (d || []).filter(r => r.status !== 0).map(r => ({ id: r.id, role_name: r.roleName || r.name || '' }))
  } catch {}
}

async function fetchDepts() {
  try {
    const d = await request.get('/api/departments')
    deptOptions.value = (d || []).map(d => ({ id: d.id, dept_name: d.deptName || d.dept_name || '' }))
  } catch {}
}

function getRoleNames(roleIds) {
  if (!roleIds || !roleIds.length) return []
  return roleIds.map(id => {
    const found = roleOptions.value.find(r => r.id === id)
    return found ? found.role_name : ''
  }).filter(Boolean)
}

function hasSuperAdminRole(roleIds) {
  if (!roleIds || !roleIds.length) return false
  return roleIds.some(id => {
    const found = roleOptions.value.find(r => r.id === id)
    return found && found.role_name === SUPER_ADMIN_ROLE_NAME
  })
}

async function fetchUsers() {
  tableLoading.value = true
  try {
    const data = await request.get('/api/users')
    allUsers.value = (data || []).map(u => {
      // Prefer backend-provided roleIds/roleNames/isSuperAdmin from UserResponseDto
      const roleIds = u.roleIds || []
      const roleNames = u.roleNames && u.roleNames.length
        ? u.roleNames
        : getRoleNames(roleIds)
      const isSuperAdmin = u.isSuperAdmin !== undefined
        ? u.isSuperAdmin
        : hasSuperAdminRole(roleIds)
      // Format createTime
      let createTimeStr = ''
      if (u.createTime) {
        if (Array.isArray(u.createTime)) {
          const [y, m, d, h = 0, min = 0, s = 0] = u.createTime
          createTimeStr = `${y}/${String(m).padStart(2,'0')}/${String(d).padStart(2,'0')} ${String(h).padStart(2,'0')}:${String(min).padStart(2,'0')}:${String(s).padStart(2,'0')}`
        } else {
          createTimeStr = new Date(u.createTime).toLocaleString('zh-CN', { hour12: false })
        }
      }
      return {
        id: u.id, username: u.username || '', nickname: u.nickname || '', email: u.email || '',
        roleIds, roleNames, isSuperAdmin,
        status: u.status !== undefined ? u.status : 1, telephone: u.telephone || '',
        createTime: createTimeStr,
        _raw: u
      }
    })
  } catch { allUsers.value = [] }
  finally { tableLoading.value = false }
}

const searchForm = reactive({ username: '' })
const filteredData = computed(() => allUsers.value.filter(i => !searchForm.username || i.username.includes(searchForm.username)))
function handleSearch() { currentPage.value = 1 }
function handleReset() { searchForm.username = ''; currentPage.value = 1 }

const currentPage = ref(1), pageSize = ref(10)
const pagedData = computed(() => { const s = (currentPage.value - 1) * pageSize.value; return filteredData.value.slice(s, s + pageSize.value) })
function handleSizeChange() { currentPage.value = 1 }

const selectedIds = ref([])
function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

async function handleStatusToggle(row) {
  if (row.isSuperAdmin) return
  try {
    await request.put(`/api/users/${row.id}/info`, { username: row.username, email: row.email, enabled: row.status === 1 })
    ElMessage.success(`用户「${row.username}」已${row.status === 1 ? '启用' : '禁用'}`)
  } catch {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null), submitLoading = ref(false), formRef = ref(null)
const formData = reactive({ username: '', password: '', email: '', nickname: '', realName: '', gender: 0, telephone: '', departmentId: null, remark: '', roleIds: [], status: 1 })
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

function handleAdd() { isEdit.value = false; editId.value = null; resetForm(); dialogVisible.value = true }
function handleEdit(row) {
  if (row.isSuperAdmin) { ElMessage.warning('超级管理员信息不可修改'); return }
  isEdit.value = true; editId.value = row.id
  formData.username = row.username; formData.password = ''; formData.email = row.email
  formData.nickname = row.nickname || ''; formData.realName = row.realName || ''
  formData.gender = row.gender != null ? row.gender : 0
  formData.telephone = row.telephone || ''
  formData.departmentId = row.departmentId || null
  formData.remark = row.remark || ''
  formData.roleIds = [...(row.roleIds || [])]; formData.status = row.status
  dialogVisible.value = true
}
function resetForm() { formData.username = ''; formData.password = ''; formData.email = ''; formData.nickname = ''; formData.realName = ''; formData.gender = 0; formData.telephone = ''; formData.departmentId = null; formData.remark = ''; formData.roleIds = []; formData.status = 1 }
function handleDialogClose() { resetForm(); formRef.value?.resetFields() }

async function handleSubmit() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  if (!isEdit.value && !formData.password) { ElMessage.warning('请输入密码'); return }
  submitLoading.value = true
  try {
    const payload = { username: formData.username, email: formData.email, nickname: formData.nickname, realName: formData.realName, gender: formData.gender, telephone: formData.telephone, departmentId: formData.departmentId, remark: formData.remark, roles: formData.roleIds, enabled: formData.status === 1 }
    if (formData.password) payload.password = formData.password
    if (isEdit.value) { await request.put(`/api/users/${editId.value}/info`, payload); ElMessage.success('用户信息修改成功') }
    else { await request.post('/api/users', payload); ElMessage.success('用户创建成功') }
    dialogVisible.value = false; resetForm(); await fetchUsers()
  } catch {} finally { submitLoading.value = false }
}

async function handleDelete(row) {
  if (row.isSuperAdmin) { ElMessage.warning('超级管理员不可删除'); return }
  try { await request.delete(`/api/users/${row.id}`); ElMessage.success(`用户「${row.username}」已删除`); await fetchUsers() } catch {}
}

const batchDeleteVisible = ref(false), batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length === 0) { ElMessage.warning('请先选择'); return }; batchDeleteVisible.value = true }
async function confirmBatchDelete() {
  batchLoading.value = true
  try {
    for (const id of selectedIds.value) {
      const user = allUsers.value.find(u => u.id === id)
      if (user && user.isSuperAdmin) continue
      await request.delete(`/api/users/${id}`)
    }
    ElMessage.success('批量删除完成'); selectedIds.value = []; batchDeleteVisible.value = false; await fetchUsers()
  } catch { ElMessage.error('批量删除失败') }
  finally { batchLoading.value = false }
}

const importVisible = ref(false), importLoading = ref(false), importResult = ref(null), uploadRef = ref(null)
const importFile = ref(null)

function handleFileChange(file) { importFile.value = file.raw }
function handleFileRemove() { importFile.value = null }

function resetImport() {
  importFile.value = null
  importResult.value = null
  uploadRef.value?.clearFiles()
}

async function handleImport() {
  if (!importFile.value) { ElMessage.warning('请选择文件'); return }
  importLoading.value = true
  importResult.value = null
  try {
    const fd = new FormData()
    fd.append('file', importFile.value)
    const token = localStorage.getItem('token')
    const headers = token ? { Authorization: `Bearer ${token}` } : {}
    // Use fetch natively so Content-Type is automatically multipart/form-data
    const resp = await fetch('/api/users/import', { method: 'POST', headers, body: fd })
    const json = await resp.json()
    const d = json.data !== undefined ? json.data : json
    importResult.value = d
    if (d.success > 0) { await fetchUsers(); ElMessage.success(`成功导入 ${d.success} 条`) }
    else ElMessage.warning('没有成功导入任何数据')
  } catch { ElMessage.error('导入失败') }
  finally { importLoading.value = false }
}

function downloadTemplate() {
  const csv = '\uFEFF用户名,密码,邮箱,真实姓名,手机号\nzhangsan,123456,zs@example.com,张三,13800000001'
  const blob = new Blob([csv], { type: 'text/csv' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob); a.download = 'user-import-template.csv'; a.click()
}

onMounted(async () => { await fetchRoles(); await fetchDepts(); await fetchUsers() })
</script>

<style scoped>
.user-manage { height: 100%; display: flex; flex-direction: column; gap: 16px; }
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