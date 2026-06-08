<template>
  <div class="menu-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="菜单名称"><el-input v-model="searchForm.menuName" placeholder="请输入菜单名称" clearable /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width:100%" row-key="id" :tree-props="{children:'children'}">
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
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="flatList.length" layout="total,sizes,prev,pager,next,jumper" background @size-change="handleSizeChange" /></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const allMenus = ref([]), tableLoading = ref(false)

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