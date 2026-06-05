<template>
  <div class="perm-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="权限标识"><el-input v-model="searchForm.permissionCode" placeholder="请输入权限标识" clearable /></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <el-table :data="pagedData" v-loading="tableLoading" stripe border style="width:100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="permissionCode" label="权限标识" min-width="180"><template #default="{row}"><el-tag size="small">{{ row.permissionCode }}</el-tag></template></el-table-column>
        <el-table-column prop="description" label="权限描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10,20,50]" :total="filteredData.length" layout="total,sizes,prev,pager,next,jumper" background @size-change="handleSizeChange" /></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
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