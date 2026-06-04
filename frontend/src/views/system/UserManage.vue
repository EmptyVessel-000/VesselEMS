<template>
  <div class="user-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.departmentId" placeholder="请选择部门" clearable style="width: 150px">
            <el-option label="全部" :value="null" />
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.dept_name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleId" placeholder="请选择角色" clearable style="width: 150px">
            <el-option label="全部" :value="null" />
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.role_name" :value="r.id" />
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
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
          <el-button type="danger" :icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
          <el-button :icon="Download" @click="handleExport">批量导出</el-button>
        </div>
        <div class="toolbar-right">
          <span class="selected-tip" v-if="selectedIds.length > 0">
            已选择 <strong>{{ selectedIds.length }}</strong> 项
          </span>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        :data="pagedData"
        v-loading="tableLoading"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="110" />
        <el-table-column prop="nickname" label="昵称" width="100" />
        <el-table-column prop="real_name" label="真实姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="65" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? '' : row.gender === 2 ? 'danger' : 'info'" size="small">
              {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="telephone" label="手机号" width="130" />
        <el-table-column prop="deptName" label="所属部门" width="110" align="center" />
        <el-table-column prop="roleName" label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isSuperAdmin ? 'danger' : ''" size="small">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.isSuperAdmin"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.isSuperAdmin">
              <el-tag type="danger" size="small">系统保护</el-tag>
            </template>
            <template v-else>
              <el-button type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除该用户吗？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button type="danger" size="small" :icon="Delete" link>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50, 100]"
          :total="filteredData.length" layout="total, sizes, prev, pager, next, jumper" background
          @size-change="handleSizeChange" @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="dialog-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="password" :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="real_name">
          <el-input v-model="formData.real_name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="formData.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="telephone">
          <el-input v-model="formData.telephone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="formData.department_id" placeholder="请选择部门" style="width: 100%">
            <el-option label="无" :value="null" />
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.dept_name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="formData.roleIds" placeholder="请选择角色" multiple style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.role_name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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

    <!-- 批量删除确认弹窗 -->
    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个用户吗？此操作不可恢复。</p>
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
import { Search, Refresh, Plus, Delete, Edit, Download } from '@element-plus/icons-vue'

// ===== 部门/角色选项 =====
const deptOptions = ref([
  { id: 1, dept_name: '技术部' }, { id: 2, dept_name: '产品部' }, { id: 3, dept_name: '运营部' },
  { id: 4, dept_name: '市场部' }, { id: 5, dept_name: '财务部' }, { id: 6, dept_name: '人事部' }
])
const roleOptions = ref([
  { id: 1, role_name: '超级管理员' }, { id: 2, role_name: '管理员' }, { id: 3, role_name: '普通用户' }
])

const getDeptName = (id) => { const d = deptOptions.value.find(x => x.id === id); return d ? d.dept_name : '' }
const getRoleName = (ids) => {
  if (!ids || !ids.length) return '普通用户'
  const r = roleOptions.value.find(x => x.id === ids[0])
  return r ? r.role_name : '普通用户'
}
const isSuperAdminRole = (ids) => ids && ids.includes(1)

// ===== 数据 =====
const allUsers = ref([
  { id: 1, username: 'superadmin', nickname: '超级管理员', real_name: '系统管理员', gender: 1, email: 'admin@vessel.com', telephone: '13800000000', department_id: 1, roleIds: [1], password: '', status: 1, remark: '超级管理员，不可删除', last_login_ip: '192.168.1.100', lastLoginTime: '2026-06-04 16:00:00', createTime: '2026-01-01 00:00:00', isSuperAdmin: true },
  { id: 2, username: 'zhangsan', nickname: '张三', real_name: '张三丰', gender: 1, email: 'zhangsan@vessel.com', telephone: '13800001001', department_id: 1, roleIds: [2], password: '', status: 1, remark: '', last_login_ip: '192.168.1.101', lastLoginTime: '2026-06-03 14:30:00', createTime: '2026-01-15 10:30:00', isSuperAdmin: false },
  { id: 3, username: 'lisi', nickname: '李四', real_name: '李四娘', gender: 2, email: 'lisi@vessel.com', telephone: '13800001002', department_id: 2, roleIds: [3], password: '', status: 1, remark: '', last_login_ip: '', lastLoginTime: '', createTime: '2026-02-01 08:20:00', isSuperAdmin: false },
  { id: 4, username: 'wangwu', nickname: '王五', real_name: '王五', gender: 1, email: 'wangwu@vessel.com', telephone: '13800001003', department_id: 3, roleIds: [3], password: '', status: 0, remark: '离职', last_login_ip: '', lastLoginTime: '', createTime: '2026-02-10 14:15:00', isSuperAdmin: false },
  { id: 5, username: 'zhaoliu', nickname: '赵六', real_name: '赵六', gender: 0, email: 'zhaoliu@vessel.com', telephone: '13800001004', department_id: 4, roleIds: [2], password: '', status: 1, remark: '', last_login_ip: '10.0.0.5', lastLoginTime: '2026-06-01 09:00:00', createTime: '2026-03-05 09:00:00', isSuperAdmin: false },
  { id: 6, username: 'sunqi', nickname: '孙七', real_name: '孙七', gender: 2, email: 'sunqi@vessel.com', telephone: '13800001005', department_id: 5, roleIds: [3], password: '', status: 1, remark: '', last_login_ip: '', lastLoginTime: '', createTime: '2026-03-20 11:45:00', isSuperAdmin: false }
])
let nextId = 7

// 前置映射
allUsers.value.forEach(u => {
  u.deptName = getDeptName(u.department_id)
  u.roleName = getRoleName(u.roleIds)
  u.isSuperAdmin = isSuperAdminRole(u.roleIds)
})

// ===== 搜索 =====
const searchForm = reactive({ username: '', departmentId: null, roleId: null, status: null })
const filteredData = computed(() => allUsers.value.filter(item => {
  if (searchForm.username && !item.username.includes(searchForm.username)) return false
  if (searchForm.departmentId && item.department_id !== searchForm.departmentId) return false
  if (searchForm.roleId && !(item.roleIds || []).includes(searchForm.roleId)) return false
  if (searchForm.status !== null && item.status !== searchForm.status) return false
  return true
}))
function handleSearch() { currentPage.value = 1 }
function handleReset() { searchForm.username = ''; searchForm.departmentId = null; searchForm.roleId = null; searchForm.status = null; currentPage.value = 1 }

const currentPage = ref(1); const pageSize = ref(10)
const pagedData = computed(() => { const s = (currentPage.value - 1) * pageSize.value; return filteredData.value.slice(s, s + pageSize.value) })
function handleSizeChange() { currentPage.value = 1 }
function handleCurrentChange() {}

const selectedIds = ref([])
function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

function handleStatusChange(row) { ElMessage.success(`用户「${row.username}」已${row.status === 1 ? '启用' : '禁用'}`) }

const tableLoading = ref(false)
const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref(null); const submitLoading = ref(false); const formRef = ref(null)

const formData = reactive({ username: '', password: '', nickname: '', real_name: '', gender: 0, email: '', telephone: '', department_id: null, roleIds: [], remark: '', status: 1 })
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

function handleAdd() { isEdit.value = false; editId.value = null; resetForm(); dialogVisible.value = true }
function handleEdit(row) {
  if (row.isSuperAdmin) { ElMessage.warning('超级管理员信息不可修改'); return }
  isEdit.value = true; editId.value = row.id
  formData.username = row.username; formData.password = ''; formData.nickname = row.nickname; formData.real_name = row.real_name
  formData.gender = row.gender; formData.email = row.email; formData.telephone = row.telephone
  formData.department_id = row.department_id; formData.roleIds = [...(row.roleIds || [])]
  formData.remark = row.remark; formData.status = row.status
  dialogVisible.value = true
}
function resetForm() { formData.username = ''; formData.password = ''; formData.nickname = ''; formData.real_name = ''; formData.gender = 0; formData.email = ''; formData.telephone = ''; formData.department_id = null; formData.roleIds = []; formData.remark = ''; formData.status = 1 }
function handleDialogClose() { resetForm(); formRef.value?.resetFields() }

function handleSubmit() {
  if (!formRef.value) return
  formRef.value.validate().then(() => {
    if (!isEdit.value && !formData.password) { ElMessage.warning('请输入密码'); return }
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value) {
        const t = allUsers.value.find(u => u.id === editId.value)
        if (t) {
          t.username = formData.username; t.nickname = formData.nickname; t.real_name = formData.real_name
          t.gender = formData.gender; t.email = formData.email; t.telephone = formData.telephone
          t.department_id = formData.department_id; t.roleIds = [...formData.roleIds]; t.remark = formData.remark; t.status = formData.status
          t.deptName = getDeptName(t.department_id); t.roleName = getRoleName(t.roleIds); t.isSuperAdmin = isSuperAdminRole(t.roleIds)
          if (formData.password) t.password = formData.password
        }
        ElMessage.success('用户信息修改成功')
      } else {
        allUsers.value.unshift({
          id: nextId++, username: formData.username, password: formData.password, nickname: formData.nickname, real_name: formData.real_name,
          gender: formData.gender, email: formData.email, telephone: formData.telephone, department_id: formData.department_id,
          roleIds: [...formData.roleIds], remark: formData.remark, status: formData.status,
          last_login_ip: '', lastLoginTime: '', createTime: new Date().toLocaleString('zh-CN', { hour12: false }),
          deptName: getDeptName(formData.department_id), roleName: getRoleName(formData.roleIds), isSuperAdmin: isSuperAdminRole(formData.roleIds)
        })
        ElMessage.success('用户创建成功')
      }
      dialogVisible.value = false; resetForm(); submitLoading.value = false
    }, 600)
  }).catch(() => {})
}

function handleDelete(row) {
  if (row.isSuperAdmin) { ElMessage.warning('超级管理员不可删除'); return }
  const idx = allUsers.value.findIndex(u => u.id === row.id)
  if (idx > -1) { allUsers.value.splice(idx, 1); ElMessage.success(`用户「${row.username}」已删除`) }
}

const batchDeleteVisible = ref(false); const batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length === 0) { ElMessage.warning('请先选择要删除的用户'); return }; batchDeleteVisible.value = true }
function confirmBatchDelete() {
  batchLoading.value = true; setTimeout(() => {
    const toDelete = allUsers.value.filter(u => selectedIds.value.includes(u.id) && !u.isSuperAdmin)
    const skipped = selectedIds.value.length - toDelete.length
    allUsers.value = allUsers.value.filter(u => !selectedIds.value.includes(u.id) || u.isSuperAdmin)
    ElMessage.success(`已成功删除 ${toDelete.length} 个用户` + (skipped > 0 ? `，${skipped} 个超管已跳过` : ''))
    selectedIds.value = []; batchDeleteVisible.value = false; batchLoading.value = false
  }, 600)
}

function handleExport() { ElMessage.success('导出成功（演示模式）') }
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