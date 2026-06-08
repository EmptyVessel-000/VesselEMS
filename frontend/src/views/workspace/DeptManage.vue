<template>
  <div class="dept-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="部门名称"><el-input v-model="searchForm.deptName" placeholder="请输入部门名称" clearable /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <div class="toolbar"><div class="toolbar-left"><el-button v-if="hasPermission('dept:manage')" type="primary" :icon="Plus" @click="handleAdd(null)">新增部门</el-button><el-button v-if="hasPermission('dept:manage')" type="danger" :icon="Delete" :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</el-button></div></div>
      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width:100%" row-key="id" :tree-props="{children:'children'}" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{row}">
            <el-button v-if="hasPermission('dept:manage')" type="success" size="small" :icon="Plus" link @click="handleAdd(row)">添加子部门</el-button>
            <el-button v-if="hasPermission('dept:manage')" type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm v-if="hasPermission('dept:manage')" title="确定删除?" @confirm="handleDelete(row)"><template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="flatList.length" layout="total,sizes,prev,pager,next,jumper" background @size-change="handleSizeChange" /></div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="dialog-form">
        <el-form-item v-if="!isEdit" label="上级部门"><el-input :value="parentName" disabled /></el-form-item>
        <el-form-item label="部门名称" prop="deptName"><el-input v-model="formData.deptName" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="formData.leader" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="formData.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="formData.email" /></el-form-item>
        <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="formData.sortOrder" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个部门吗？</p>
      <template #footer><el-button @click="batchDeleteVisible=false">取消</el-button><el-button type="danger" :loading="batchLoading" @click="confirmBatchDelete">确定删除</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission } from '../../stores/permissions.js'

const allDepts = ref([]), tableLoading = ref(false)

function buildTree(list) {
  const map={}, roots=[]
  list.forEach(item=>{item.children=[]; map[item.id]=item})
  list.forEach(item=>{
    if(item.parentId != null && map[item.parentId]) {
      map[item.parentId].children.push(item)
    } else {
      roots.push(item)
    }
  })
  return roots
}

async function fetchDepts() {
  tableLoading.value = true
  try {
    const d = await request.get('/api/departments')
    const list = (d||[]).map(m=>({...m, deptName:m.deptName||m.dept_name||'', leader:m.leader||'', phone:m.phone||'', email:m.email||'', sortOrder:m.sortOrder||m.sort_order||0, createTime:m.createTime||(m.createdAt?new Date(m.createdAt).toLocaleString('zh-CN',{hour12:false}):'')}))
    allDepts.value = buildTree(list)
  } catch { allDepts.value = [] }
  finally { tableLoading.value = false }
}

const flatList = computed(()=>{const r=[];function f(l){for(const i of l){r.push(i);if(i.children)f(i.children)}};f(allDepts.value);return r})

const searchForm = reactive({ deptName: '' })
const filteredData = computed(()=>{
  if(!searchForm.deptName) return allDepts.value
  function filter(list){return list.reduce((acc,item)=>{const match=item.deptName.includes(searchForm.deptName);const fc=item.children?filter(item.children):[];if(match||fc.length>0)acc.push({...item,children:fc.length>0?fc:item.children});return acc},[])}
  return filter(allDepts.value)
})
function handleSearch(){currentPage.value=1}
function handleReset(){searchForm.deptName='';currentPage.value=1}

const currentPage=ref(1),pageSize=ref(50)
const pagedData=computed(()=>filteredData.value)
function handleSizeChange(){}

const selectedIds=ref([])
function handleSelectionChange(rows){const ids=[];function c(l){for(const i of l){ids.push(i.id);if(i.children)c(i.children)}};c(rows);selectedIds.value=ids}

const dialogVisible=ref(false),isEdit=ref(false),editId=ref(null),submitLoading=ref(false),formRef=ref(null)
const parentId=ref(null),parentName=ref('顶级部门')
const formData=reactive({deptName:'',leader:'',phone:'',email:'',sortOrder:0})
const formRules={deptName:[{required:true,message:'请输入部门名称',trigger:'blur'}]}
const dialogTitle=computed(()=>isEdit.value?'编辑部门':'新增部门')

function findById(list,id){for(const i of list){if(i.id===id)return i;if(i.children){const f=findById(i.children,id);if(f)return f}}return null}

function handleAdd(parentRow){isEdit.value=false;editId.value=null;resetForm();parentId.value=parentRow?parentRow.id:null;parentName.value=parentRow?parentRow.deptName:'顶级部门';dialogVisible.value=true}
function handleEdit(row){isEdit.value=true;editId.value=row.id;formData.deptName=row.deptName;formData.leader=row.leader||'';formData.phone=row.phone||'';formData.email=row.email||'';formData.sortOrder=row.sortOrder||0;dialogVisible.value=true}
function resetForm(){formData.deptName='';formData.leader='';formData.phone='';formData.email='';formData.sortOrder=0}
function handleDialogClose(){resetForm();formRef.value?.resetFields()}

async function handleSubmit(){
  if(!formRef.value)return
  try{await formRef.value.validate()}catch{return}
  submitLoading.value=true
  try{
    const payload={deptName:formData.deptName,leader:formData.leader,phone:formData.phone,email:formData.email,sortOrder:formData.sortOrder,parentId:parentId.value||null}
    if(isEdit.value){await request.put(`/api/departments/${editId.value}`,payload);ElMessage.success('部门修改成功')}
    else{await request.post('/api/departments',payload);ElMessage.success('部门创建成功')}
    dialogVisible.value=false;resetForm();await fetchDepts()
  }catch{}finally{submitLoading.value=false}
}

async function handleDelete(row){try{await request.delete(`/api/departments/${row.id}`);ElMessage.success(`部门「${row.deptName}」已删除`);await fetchDepts()}catch{}}

const batchDeleteVisible=ref(false),batchLoading=ref(false)
function handleBatchDelete(){if(selectedIds.value.length===0){ElMessage.warning('请先选择');return};batchDeleteVisible.value=true}
async function confirmBatchDelete(){batchLoading.value=true;try{for(const id of selectedIds.value)await request.delete(`/api/departments/${id}`);ElMessage.success(`已删除 ${selectedIds.value.length} 个部门`);selectedIds.value=[];batchDeleteVisible.value=false;await fetchDepts()}catch{ElMessage.error('批量删除失败')}finally{batchLoading.value=false}}

onMounted(()=>{fetchDepts()})
</script>

<style scoped>
.dept-manage{height:100%;display:flex;flex-direction:column;gap:16px}
.search-card{flex-shrink:0}.search-card .el-form{margin-bottom:0}
.table-card{flex:1;display:flex;flex-direction:column;overflow:hidden}
.toolbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}
.toolbar-left{display:flex;gap:8px}
.pagination-wrap{display:flex;justify-content:flex-end;padding-top:16px;flex-shrink:0}
.dialog-form{padding-right:20px}
.batch-delete-text{font-size:15px;color:#374151;text-align:center;padding:16px 0}
.batch-delete-text strong{color:#ef4444;font-size:18px}
</style>