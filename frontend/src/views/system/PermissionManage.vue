<template>
  <div class="perm-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="权限标识"><el-input v-model="searchForm.permissionCode" placeholder="请输入权限标识" clearable /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <div class="toolbar"><div class="toolbar-left"><el-button type="primary" :icon="Plus" @click="handleAdd">新增权限</el-button><el-button type="danger" :icon="Delete" :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</el-button></div></div>
      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width:100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="permissionCode" label="权限标识" min-width="180"><template #default="{row}"><el-tag size="small">{{ row.permissionCode }}</el-tag></template></el-table-column>
        <el-table-column prop="description" label="权限描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{row}">
            <el-button type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row)"><template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="filteredData.length" layout="total,sizes,prev,pager,next,jumper" background @size-change="handleSizeChange" /></div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" class="dialog-form">
        <el-form-item label="权限标识" prop="permissionCode"><el-input v-model="formData.permissionCode" :disabled="isEdit" /></el-form-item>
        <el-form-item label="权限描述" prop="description"><el-input v-model="formData.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="formData.sortOrder" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个权限吗？</p>
      <template #footer><el-button @click="batchDeleteVisible=false">取消</el-button><el-button type="danger" :loading="batchLoading" @click="confirmBatchDelete">确定删除</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const allPerms = ref([]), tableLoading = ref(false)

async function fetchPerms() {
  tableLoading.value = true
  try { const d = await request.get('/api/permissions'); allPerms.value = (d||[]).map(p=>({...p, permissionCode:p.permissionCode||p.permission_code||'', createTime:p.createTime||(p.createdAt?new Date(p.createdAt).toLocaleString('zh-CN',{hour12:false}):'')})) }
  catch { allPerms.value = [] }
  finally { tableLoading.value = false }
}

const searchForm = reactive({ permissionCode: '' })
const filteredData = computed(()=>allPerms.value.filter(i=>!searchForm.permissionCode||(i.permissionCode||'').includes(searchForm.permissionCode)))
function handleSearch(){currentPage.value=1}
function handleReset(){searchForm.permissionCode='';currentPage.value=1}

const currentPage=ref(1),pageSize=ref(10)
const pagedData=computed(()=>{const s=(currentPage.value-1)*pageSize.value;return filteredData.value.slice(s,s+pageSize.value)})
function handleSizeChange(){currentPage.value=1}
const selectedIds=ref([])
function handleSelectionChange(rows){selectedIds.value=rows.map(r=>r.id)}

const dialogVisible=ref(false),isEdit=ref(false),editId=ref(null),submitLoading=ref(false),formRef=ref(null)
const formData=reactive({permissionCode:'',description:'',sortOrder:0})
const formRules={permissionCode:[{required:true,message:'请输入权限标识',trigger:'blur'}]}
const dialogTitle=computed(()=>isEdit.value?'编辑权限':'新增权限')

function handleAdd(){isEdit.value=false;editId.value=null;resetForm();dialogVisible.value=true}
function handleEdit(row){isEdit.value=true;editId.value=row.id;formData.permissionCode=row.permissionCode;formData.description=row.description;formData.sortOrder=row.sortOrder||0;dialogVisible.value=true}
function resetForm(){formData.permissionCode='';formData.description='';formData.sortOrder=0}
function handleDialogClose(){resetForm();formRef.value?.resetFields()}

async function handleSubmit(){
  if(!formRef.value)return
  try{await formRef.value.validate()}catch{return}
  submitLoading.value=true
  try{
    const payload={permissionCode:formData.permissionCode,description:formData.description,sortOrder:formData.sortOrder}
    if(isEdit.value){await request.put(`/api/permissions/${editId.value}`,payload);ElMessage.success('权限修改成功')}
    else{await request.post('/api/permissions',payload);ElMessage.success('权限创建成功')}
    dialogVisible.value=false;resetForm();await fetchPerms()
  }catch{}finally{submitLoading.value=false}
}

async function handleDelete(row){try{await request.delete(`/api/permissions/${row.id}`);ElMessage.success(`权限「${row.permissionCode}」已删除`);await fetchPerms()}catch{}}

const batchDeleteVisible=ref(false),batchLoading=ref(false)
function handleBatchDelete(){if(selectedIds.value.length===0){ElMessage.warning('请先选择');return};batchDeleteVisible.value=true}
async function confirmBatchDelete(){batchLoading.value=true;try{for(const id of selectedIds.value)await request.delete(`/api/permissions/${id}`);ElMessage.success(`已删除 ${selectedIds.value.length} 个权限`);selectedIds.value=[];batchDeleteVisible.value=false;await fetchPerms()}catch{ElMessage.error('批量删除失败')}finally{batchLoading.value=false}}

onMounted(()=>{fetchPerms()})
</script>

<style scoped>
.perm-manage{height:100%;display:flex;flex-direction:column;gap:16px}
.search-card{flex-shrink:0}.search-card .el-form{margin-bottom:0}
.table-card{flex:1;display:flex;flex-direction:column;overflow:hidden}
.toolbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}
.toolbar-left{display:flex;gap:8px}
.pagination-wrap{display:flex;justify-content:flex-end;padding-top:16px;flex-shrink:0}
.dialog-form{padding-right:20px}
.batch-delete-text{font-size:15px;color:#374151;text-align:center;padding:16px 0}
.batch-delete-text strong{color:#ef4444;font-size:18px}
</style>