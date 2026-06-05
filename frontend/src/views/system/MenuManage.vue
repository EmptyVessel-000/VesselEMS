<template>
  <div class="menu-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="菜单名称"><el-input v-model="searchForm.menuName" placeholder="请输入菜单名称" clearable /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <div class="toolbar"><div class="toolbar-left"><el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增菜单</el-button><el-button type="danger" :icon="Delete" :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</el-button></div></div>
      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width:100%" row-key="id" :tree-props="{children:'children'}" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{row}"><el-icon v-if="row.icon" :size="18"><component :is="row.icon" /></el-icon></template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="180" show-overflow-tooltip />
        <el-table-column prop="menuType" label="类型" width="70" align="center">
          <template #default="{row}"><el-tag :type="row.menuType===0?'':row.menuType===1?'success':'info'" size="small">{{['目录','菜单','按钮'][row.menuType]}}</el-tag></template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.menuType===0" type="success" size="small" :icon="Plus" link @click="handleAdd(row)">添加子项</el-button>
            <el-button type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row)"><template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="flatList.length" layout="total,sizes,prev,pager,next,jumper" background @size-change="handleSizeChange" /></div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="dialog-form">
        <el-form-item v-if="!isEdit" label="上级菜单"><el-input :value="parentName" disabled /></el-form-item>
        <el-form-item label="菜单名称" prop="menuName"><el-input v-model="formData.menuName" /></el-form-item>
        <el-form-item v-if="!isEdit" label="菜单类型" prop="menuType">
          <el-radio-group v-model="formData.menuType"><el-radio :value="0">目录</el-radio><el-radio :value="1">菜单</el-radio><el-radio :value="2">按钮</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.menuType!==2" label="图标"><el-input v-model="formData.icon" placeholder="如 Setting,User" /></el-form-item>
        <el-form-item v-if="formData.menuType!==0" label="路由路径"><el-input v-model="formData.path" /></el-form-item>
        <el-form-item v-if="formData.menuType!==0" label="组件路径"><el-input v-model="formData.component" /></el-form-item>
        <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="formData.sortOrder" :min="0" :max="999" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="batchDeleteVisible" title="批量删除确认" width="420px" :close-on-click-modal="false">
      <p class="batch-delete-text">确定要删除选中的 <strong>{{ selectedIds.length }}</strong> 个菜单吗？</p>
      <template #footer><el-button @click="batchDeleteVisible=false">取消</el-button><el-button type="danger" :loading="batchLoading" @click="confirmBatchDelete">确定删除</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const allMenus = ref([]), tableLoading = ref(false)

function buildTree(list) {
  const map={}, roots=[]
  list.forEach(item=>{item.children=[]; map[item.id]=item})
  list.forEach(item=>{if(item.parentId&&map[item.parentId])map[item.parentId].children.push(item);else roots.push(item)})
  return roots
}

async function fetchMenus() {
  tableLoading.value = true
  try {
    const d = await request.get('/api/menus')
    const list = (d||[]).map(m=>({...m, menuName:m.menuName||m.menu_name||'', path:m.path||m.menu_path||'', component:m.component||m.menu_component||'', icon:m.icon||m.menu_icon||'', menuType:m.menuType!==undefined?m.menuType:(m.menu_type||1), sortOrder:m.sortOrder||m.sort_order||0, createTime:m.createTime||(m.createdAt?new Date(m.createdAt).toLocaleString('zh-CN',{hour12:false}):'')}))
    allMenus.value = buildTree(list)
  } catch { allMenus.value = [] }
  finally { tableLoading.value = false }
}

const flatList = computed(()=>{const r=[];function f(l){for(const i of l){r.push(i);if(i.children)f(i.children)}};f(allMenus.value);return r})

const searchForm = reactive({ menuName: '' })
const filteredData = computed(()=>{
  if(!searchForm.menuName) return allMenus.value
  function filter(list){return list.reduce((acc,item)=>{const match=item.menuName.includes(searchForm.menuName);const fc=item.children?filter(item.children):[];if(match||fc.length>0)acc.push({...item,children:fc.length>0?fc:item.children});return acc},[])}
  return filter(allMenus.value)
})
function handleSearch(){currentPage.value=1}
function handleReset(){searchForm.menuName='';currentPage.value=1}

const currentPage=ref(1),pageSize=ref(50)
const pagedData=computed(()=>filteredData.value)
function handleSizeChange(){}

const selectedIds=ref([])
function handleSelectionChange(rows){const ids=[];function c(l){for(const i of l){ids.push(i.id);if(i.children)c(i.children)}};c(rows);selectedIds.value=ids}

const dialogVisible=ref(false),isEdit=ref(false),editId=ref(null),submitLoading=ref(false),formRef=ref(null)
const parentId=ref(null),parentName=ref('根级菜单')
const formData=reactive({menuName:'',menuType:1,icon:'',path:'',component:'',sortOrder:0})
const formRules={menuName:[{required:true,message:'请输入菜单名称',trigger:'blur'}]}
const dialogTitle=computed(()=>isEdit.value?'编辑菜单':'新增菜单')

function findById(list,id){for(const i of list){if(i.id===id)return i;if(i.children){const f=findById(i.children,id);if(f)return f}}return null}

function handleAdd(parentRow){isEdit.value=false;editId.value=null;resetForm();parentId.value=parentRow?parentRow.id:null;parentName.value=parentRow?parentRow.menuName:'根级菜单';dialogVisible.value=true}
function handleEdit(row){isEdit.value=true;editId.value=row.id;formData.menuName=row.menuName;formData.menuType=row.menuType;formData.icon=row.icon||'';formData.path=row.path||'';formData.component=row.component||'';formData.sortOrder=row.sortOrder||0;dialogVisible.value=true}
function resetForm(){formData.menuName='';formData.menuType=1;formData.icon='';formData.path='';formData.component='';formData.sortOrder=0}
function handleDialogClose(){resetForm();formRef.value?.resetFields()}

async function handleSubmit(){
  if(!formRef.value)return
  try{await formRef.value.validate()}catch{return}
  submitLoading.value=true
  try{
    const payload={menuName:formData.menuName,menuType:formData.menuType,icon:formData.icon,path:formData.path,component:formData.component,sortOrder:formData.sortOrder,parentId:parentId.value||0}
    if(isEdit.value){await request.put(`/api/menus/${editId.value}`,payload);ElMessage.success('菜单修改成功')}
    else{await request.post('/api/menus',payload);ElMessage.success('菜单创建成功')}
    dialogVisible.value=false;resetForm();await fetchMenus()
  }catch{}finally{submitLoading.value=false}
}

async function handleDelete(row){try{await request.delete(`/api/menus/${row.id}`);ElMessage.success(`菜单「${row.menuName}」已删除`);await fetchMenus()}catch{}}

const batchDeleteVisible=ref(false),batchLoading=ref(false)
function handleBatchDelete(){if(selectedIds.value.length===0){ElMessage.warning('请先选择');return};batchDeleteVisible.value=true}
async function confirmBatchDelete(){batchLoading.value=true;try{for(const id of selectedIds.value)await request.delete(`/api/menus/${id}`);ElMessage.success(`已删除 ${selectedIds.value.length} 个菜单`);selectedIds.value=[];batchDeleteVisible.value=false;await fetchMenus()}catch{ElMessage.error('批量删除失败')}finally{batchLoading.value=false}}

onMounted(()=>{fetchMenus()})
</script>

<style scoped>
.menu-manage{height:100%;display:flex;flex-direction:column;gap:16px}
.search-card{flex-shrink:0}.search-card .el-form{margin-bottom:0}
.table-card{flex:1;display:flex;flex-direction:column;overflow:hidden}
.toolbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}
.toolbar-left{display:flex;gap:8px}
.pagination-wrap{display:flex;justify-content:flex-end;padding-top:16px;flex-shrink:0}
.dialog-form{padding-right:20px}
.batch-delete-text{font-size:15px;color:#374151;text-align:center;padding:16px 0}
.batch-delete-text strong{color:#ef4444;font-size:18px}
</style>