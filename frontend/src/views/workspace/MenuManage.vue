<template>
  <div class="menu-manage">
    <!-- 搜索 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="菜单名称">
          <el-input v-model="searchForm.menuName" placeholder="请输入菜单名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" :icon="Plus" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="filteredTree"
        v-loading="tableLoading"
        stripe border
        style="width:100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column prop="menuPath" label="路由路径" width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.menuPath || '-' }}</template>
        </el-table-column>
        <el-table-column prop="menuComponent" label="组件" width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.menuComponent || '-' }}</template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 1 ? 'success' : 'info'" size="small">
              {{ row.menuType === 1 ? '页面' : '分组' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link :icon="Top" @click="handleMove(row.id, 'up')">上移</el-button>
            <el-button type="warning" link :icon="Bottom" @click="handleMove(row.id, 'down')">下移</el-button>
            <el-popconfirm title="确定删除该菜单吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        class="dialog-form"
      >
        <el-form-item label="父级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="无（顶级菜单）"
            clearable
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="menuPath">
          <el-input v-model="form.menuPath" placeholder="如 dashboard（分组留空）" />
        </el-form-item>
        <el-form-item label="组件路径" prop="menuComponent">
          <el-input v-model="form.menuComponent" placeholder="如 workspace/Dashboard（分组留空）" />
        </el-form-item>
        <el-form-item label="图标" prop="menuIcon">
          <el-input v-model="form.menuIcon" placeholder="如 DataBoard（可选）" />
        </el-form-item>
        <el-form-item label="类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">页面</el-radio>
            <el-radio :value="2">分组</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Edit, Top, Bottom, Delete } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const treeData = ref([])
const tableLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  parentId: null,
  menuName: '',
  menuPath: '',
  menuComponent: '',
  menuIcon: '',
  menuType: 1,
  visible: 1,
  sortOrder: 0,
  status: 1
})

const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

// 搜索
const searchForm = reactive({ menuName: '' })

function filterTree(list) {
  if (!searchForm.menuName) return list
  return list.reduce((acc, item) => {
    const match = item.menuName.includes(searchForm.menuName)
    const filteredChildren = item.children ? filterTree(item.children) : []
    if (match || filteredChildren.length > 0) {
      acc.push({ ...item, children: filteredChildren.length > 0 ? filteredChildren : item.children })
    }
    return acc
  }, [])
}

const filteredTree = computed(() => filterTree(treeData.value))

function handleSearch() {}
function handleReset() {
  searchForm.menuName = ''
}

// 父级下拉
const parentOptions = computed(() => treeData.value)

async function fetchTree() {
  tableLoading.value = true
  try {
    const d = await request.get('/api/menus/tree')
    treeData.value = d || []
  } catch {
    treeData.value = []
  } finally {
    tableLoading.value = false
  }
}

function resetForm() {
  form.parentId = null
  form.menuName = ''
  form.menuPath = ''
  form.menuComponent = ''
  form.menuIcon = ''
  form.menuType = 1
  form.visible = 1
  form.sortOrder = 0
  form.status = 1
  isEdit.value = false
  editingId.value = null
  formRef.value?.resetFields()
}

function handleAdd() {
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  isEdit.value = true
  editingId.value = row.id
  form.parentId = row.parentId
  form.menuName = row.menuName
  form.menuPath = row.menuPath
  form.menuComponent = row.menuComponent
  form.menuIcon = row.menuIcon
  form.menuType = row.menuType
  form.visible = row.visible
  form.sortOrder = row.sortOrder
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      parentId: form.parentId,
      menuName: form.menuName,
      menuPath: form.menuPath || null,
      menuComponent: form.menuComponent || null,
      menuIcon: form.menuIcon || null,
      menuType: form.menuType,
      visible: form.visible,
      sortOrder: form.sortOrder,
      status: form.status
    }

    if (isEdit.value) {
      await request.put(`/api/menus/${editingId.value}`, payload)
      ElMessage.success('修改成功')
    } else {
      await request.post('/api/menus', payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchTree()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await request.delete(`/api/menus/${id}`)
    ElMessage.success('删除成功')
    await fetchTree()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function handleMove(id, direction) {
  try {
    await request.post(`/api/menus/${id}/move?direction=${direction}`)
    ElMessage.success('排序成功')
    await fetchTree()
  } catch (e) {
    ElMessage.error(e?.message || '排序失败')
  }
}

onMounted(() => {
  fetchTree()
})
</script>

<style scoped>
.menu-manage {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  flex-shrink: 0;
}
.search-card .el-form {
  margin-bottom: 0;
}

.table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dialog-form {
  padding-right: 20px;
}
</style>