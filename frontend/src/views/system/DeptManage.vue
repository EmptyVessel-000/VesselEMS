<template>
  <div class="dept-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="部门名称">
          <el-input v-model="searchForm.dept_name" placeholder="请输入部门名称" clearable />
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
          <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增部门</el-button>
          <el-button type="danger" :icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <span class="selected-tip" v-if="selectedIds.length > 0">已选择 <strong>{{ selectedIds.length }}</strong> 项</span>
        </div>
      </div>

      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width: 100%" row-key="id" :tree-props="{ children: 'children' }" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dept_name" label="部门名称" min-width="180" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="sort_order" label="排序" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" :icon="Plus" link @click="handleAdd(row)">添加子部门</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="dialog-form">
        <el-form-item v-if="!isEdit" label="上级部门">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="部门名称" prop="dept_name">
          <el-input v-model="formData.dept_name" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="formData.leader" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" />
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
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个部门吗？</p>
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

const allDepts = ref([
  { id: 1, parent_id: 0, dept_name: '总公司', leader: '张建国', phone: '13800001001', email: 'head@vessel.com', sort_order: 1, status: 1, createTime: '2026-01-15 10:00:00', children: [
    { id: 11, parent_id: 1, dept_name: '技术部', leader: '李伟强', phone: '13800001002', email: 'tech@vessel.com', sort_order: 1, status: 1, createTime: '2026-01-15 10:05:00', children: [] },
    { id: 12, parent_id: 1, dept_name: '产品部', leader: '王芳', phone: '13800001003', email: 'product@vessel.com', sort_order: 2, status: 1, createTime: '2026-01-15 10:05:00', children: [] },
    { id: 13, parent_id: 1, dept_name: '运营部', leader: '赵敏', phone: '13800001004', email: 'ops@vessel.com', sort_order: 3, status: 1, createTime: '2026-02-01 08:00:00', children: [] },
    { id: 14, parent_id: 1, dept_name: '市场部', leader: '刘洋', phone: '13800001005', email: 'market@vessel.com', sort_order: 4, status: 1, createTime: '2026-02-01 08:00:00', children: [] }
  ]},
  { id: 2, parent_id: 0, dept_name: '分公司A', leader: '陈娜', phone: '13800002001', email: 'branchA@vessel.com', sort_order: 2, status: 1, createTime: '2026-03-10 09:00:00', children: [
    { id: 21, parent_id: 2, dept_name: '财务部', leader: '杨杰', phone: '13800002002', email: 'finance@vessel.com', sort_order: 1, status: 1, createTime: '2026-03-10 09:05:00', children: [] },
    { id: 22, parent_id: 2, dept_name: '人事部', leader: '黄静', phone: '13800002003', email: 'hr@vessel.com', sort_order: 2, status: 1, createTime: '2026-03-10 09:05:00', children: [] }
  ]}
])
let nextId = 100

const flatList = computed(() => { const r = []; function f(l) { for (const i of l) { r.push(i); if (i.children) f(i.children) } }; f(allDepts.value); return r })

const searchForm = reactive({ dept_name: '', status: null })
const filteredData = computed(() => {
  if (!searchForm.dept_name && searchForm.status === null) return allDepts.value
  function filter(list) {
    return list.reduce((acc, item) => {
      const match = (!searchForm.dept_name || item.dept_name.includes(searchForm.dept_name)) && (searchForm.status === null || item.status === searchForm.status)
      const fc = item.children ? filter(item.children) : []
      if (match || fc.length > 0) acc.push({ ...item, children: fc.length > 0 ? fc : item.children })
      return acc
    }, [])
  }
  return filter(allDepts.value)
})
function handleSearch() { currentPage.value = 1 }
function handleReset() { searchForm.dept_name = ''; searchForm.status = null; currentPage.value = 1 }

const currentPage = ref(1); const pageSize = ref(50)
const pagedData = computed(() => filteredData.value)
function handleSizeChange() {}; function handleCurrentChange() {}

const selectedIds = ref([])
function handleSelectionChange(rows) { const ids = []; function c(l) { for (const i of l) { ids.push(i.id); if (i.children) c(i.children) } }; c(rows); selectedIds.value = ids }
function handleStatusChange(row) { ElMessage.success(`部门「${row.dept_name}」已${row.status === 1 ? '启用' : '禁用'}`) }
const tableLoading = ref(false)

const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref(null); const submitLoading = ref(false); const formRef = ref(null)
const parentId = ref(null); const parentName = ref('顶级部门')
const formData = reactive({ dept_name: '', leader: '', phone: '', email: '', sort_order: 0, status: 1 })
const formRules = { dept_name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }
const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')

function findById(list, id) { for (const i of list) { if (i.id === id) return i; if (i.children) { const f = findById(i.children, id); if (f) return f } } return null }

function handleAdd(parentRow) { isEdit.value = false; editId.value = null; resetForm(); if (parentRow) { parentId.value = parentRow.id; parentName.value = parentRow.dept_name } else { parentId.value = null; parentName.value = '顶级部门' }; dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; Object.assign(formData, row); parentName.value = '—'; dialogVisible.value = true }
function resetForm() { formData.dept_name = ''; formData.leader = ''; formData.phone = ''; formData.email = ''; formData.sort_order = 0; formData.status = 1 }
function handleDialogClose() { resetForm(); formRef.value?.resetFields() }

function handleSubmit() {
  if (!formRef.value) return
  formRef.value.validate().then(() => {
    submitLoading.value = true; setTimeout(() => {
      if (isEdit.value) {
        const t = findById(allDepts.value, editId.value)
        if (t) { t.dept_name = formData.dept_name; t.leader = formData.leader; t.phone = formData.phone; t.email = formData.email; t.sort_order = formData.sort_order; t.status = formData.status }
        ElMessage.success('部门信息修改成功')
      } else {
        const nn = { id: nextId++, parent_id: parentId.value || 0, dept_name: formData.dept_name, leader: formData.leader, phone: formData.phone, email: formData.email, sort_order: formData.sort_order, status: formData.status, createTime: new Date().toLocaleString('zh-CN', { hour12: false }), children: [] }
        if (parentId.value) { const p = findById(allDepts.value, parentId.value); if (p) p.children.push(nn) } else { allDepts.value.push(nn) }
        ElMessage.success('部门创建成功')
      }
      dialogVisible.value = false; resetForm(); submitLoading.value = false
    }, 600)
  }).catch(() => {})
}

function removeById(list, id) { for (let i = 0; i < list.length; i++) { if (list[i].id === id) { list.splice(i, 1); return true } if (list[i].children && removeById(list[i].children, id)) return true } return false }
function handleDelete(row) { removeById(allDepts.value, row.id); ElMessage.success(`部门「${row.dept_name}」已删除`) }

const batchDeleteVisible = ref(false); const batchLoading = ref(false)
function handleBatchDelete() { if (selectedIds.value.length === 0) { ElMessage.warning('请先选择'); return }; batchDeleteVisible.value = true }
function confirmBatchDelete() { batchLoading.value = true; setTimeout(() => { for (const id of selectedIds.value) removeById(allDepts.value, id); ElMessage.success(`已删除 ${selectedIds.value.length} 个部门`); selectedIds.value = []; batchDeleteVisible.value = false; batchLoading.value = false }, 600) }
</script>

<style scoped>
.dept-manage { height: 100%; display: flex; flex-direction: column; gap: 16px; }
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