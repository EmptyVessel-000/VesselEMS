<template>
  <div class="datasource-manage">
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd" v-if="hasPermission('ds:create')">新增数据源</el-button>
      </div>
      <el-table :data="pagedData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="数据库名" width="140" />
        <el-table-column label="数据库类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.dbType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="主机" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.host }}{{ row.port ? ':' + row.port : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="databaseName" label="数据库" width="130" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="Edit" link @click="handleEdit(row)" v-if="hasPermission('ds:edit')">编辑</el-button>
            <el-button type="success" size="small" :icon="Connection" link @click="handleTest(row)">测试</el-button>
            <el-button type="warning" size="small" :icon="DataBoard" link @click="handleSchema(row)">结构</el-button>
            <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)" v-if="hasPermission('ds:delete')">
              <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10,20,50]" :total="list.length" layout="total,sizes,prev,pager,next" background />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据源' : '新增数据源'" width="520px" @close="resetForm">
      <el-form ref="fRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="给数据源起个名字" /></el-form-item>
        <el-form-item label="数据库类型" prop="dbType">
          <el-select v-model="form.dbType" placeholder="请选择" style="width:100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="Oracle" value="oracle" />
            <el-option label="SQL Server" value="sqlserver" />
            <el-option label="MariaDB" value="mariadb" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机名/IP" prop="host"><el-input v-model="form.host" placeholder="localhost 或 IP 地址" /></el-form-item>
        <el-form-item label="端口"><el-input-number v-model="form.port" :min="1" :max="65535" style="width:100%" /></el-form-item>
        <el-form-item label="数据库名"><el-input v-model="form.databaseName" placeholder="数据库名称" /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="留空则不修改密码" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="schemaVisible" title="表结构" width="700px">
      <div v-for="t in schemaTables" :key="t.table" style="margin-bottom:16px">
        <h4 style="margin:0 0 8px;color:#2563eb">{{ t.table }}</h4>
        <el-table :data="t.columns" size="small" border>
          <el-table-column prop="name" label="字段名" />
          <el-table-column prop="type" label="类型" />
          <el-table-column prop="size" label="长度" />
        </el-table>
      </div>
      <el-empty v-if="schemaTables.length===0" description="暂无数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Connection, DataBoard } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission } from '../../stores/permissions.js'

const defaultPortMap = { mysql: 3306, postgresql: 5432, oracle: 1521, sqlserver: 1433, mariadb: 3306 }

const list = ref([]), loading = ref(false)
const page = ref(1), size = ref(10)
const pagedData = computed(() => list.value.slice((page.value - 1) * size.value, page.value * size.value))

async function fetch() {
  loading.value = true
  try {
    const d = await request.get('/api/ds')
    list.value = (d || []).map(fmt)
  } catch { list.value = [] }
  finally { loading.value = false }
}

function fmt(r) {
  let t = ''
  if (r.createTime) {
    if (Array.isArray(r.createTime)) {
      const [y, m, d, h = 0, mm = 0, s = 0] = r.createTime
      t = `${y}/${String(m).padStart(2, '0')}/${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mm).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    } else {
      t = new Date(r.createTime).toLocaleString('zh-CN', { hour12: false })
    }
  }
  return { ...r, createTime: t }
}

async function handleStatusToggle(row) {
  try {
    await request.put(`/api/ds/${row.id}`, { status: row.status })
    ElMessage.success('状态已更新')
  } catch { row.status = row.status === 1 ? 0 : 1 }
}

const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null), submitting = ref(false), fRef = ref(null)
const form = reactive({ name: '', dbType: 'mysql', host: '', port: 3306, databaseName: '', username: '', password: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入名称' }],
  dbType: [{ required: true, message: '请选择数据库类型' }],
  host: [{ required: true, message: '请输入主机名/IP' }],
}

function handleAdd() { isEdit.value = false; editId.value = null; resetRaw(); dialogVisible.value = true }
function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name
  form.dbType = row.dbType || 'mysql'
  form.host = row.host || ''
  form.port = row.port || defaultPortMap[form.dbType] || 3306
  form.databaseName = row.databaseName || ''
  form.username = row.username || ''
  form.password = ''
  form.status = row.status
  dialogVisible.value = true
}
function resetRaw() { form.name = ''; form.dbType = 'mysql'; form.host = ''; form.port = 3306; form.databaseName = ''; form.username = ''; form.password = ''; form.status = 1 }
function resetForm() { resetRaw(); fRef.value?.resetFields() }

// Set default port when dbType changes
function onDbTypeChange(val) {
  if (!isEdit.value) {
    form.port = defaultPortMap[val] || 3306
  }
}

async function handleSubmit() {
  if (!fRef.value) return
  try { await fRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload = { ...form }
    // Don't send password if empty in edit mode
    if (isEdit.value && !payload.password) {
      delete payload.password
    }
    if (isEdit.value) { await request.put(`/api/ds/${editId.value}`, payload); ElMessage.success('已修改') }
    else { await request.post('/api/ds', payload); ElMessage.success('已新增') }
    dialogVisible.value = false; resetRaw(); await fetch()
  } catch {} finally { submitting.value = false }
}

async function handleDelete(row) {
  try { await request.delete(`/api/ds/${row.id}`); ElMessage.success('已删除'); await fetch() } catch {}
}

async function handleTest(row) {
  try {
    const ok = await request.post(`/api/ds/${row.id}/test`)
    ElMessage(ok ? { message: '连接成功', type: 'success' } : { message: '连接失败', type: 'error' })
  } catch { ElMessage.error('测试失败') }
}

const schemaVisible = ref(false), schemaTables = ref([])
async function handleSchema(row) {
  schemaVisible.value = true; schemaTables.value = []
  try {
    const d = await request.get(`/api/ds/${row.id}/schema`)
    schemaTables.value = d || []
  } catch { ElMessage.error('获取失败') }
}

onMounted(fetch)
</script>

<style scoped>
.datasource-manage { height: 100%; display: flex; flex-direction: column; }
.table-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; flex-shrink: 0; }
</style>