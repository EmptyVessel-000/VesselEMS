<template>
  <div class="rag-knowledge">
    <!-- 左侧文库列表 -->
    <div class="lib-sidebar">
      <div class="lib-sidebar-header">
        <el-button type="primary" :icon="Plus" size="small" @click="handleAddLib" v-if="hasPermission('rag:lib:create')">新建文库</el-button>
      </div>
      <div class="lib-list" v-loading="libLoading">
        <div
          v-for="lib in libraries"
          :key="lib.id"
          class="lib-item"
          :class="{ active: selectedLibId === lib.id }"
          @click="selectLib(lib)"
        >
          <div class="lib-item-main">
            <el-icon :size="18"><Folder /></el-icon>
            <span class="lib-name">{{ lib.name }}</span>
          </div>
          <div class="lib-item-meta">{{ lib.documentCount ?? 0 }} 个文件</div>
          <div class="lib-item-actions" @click.stop>
            <el-button :icon="Edit" link size="small" @click="handleEditLib(lib)"></el-button>
            <el-popconfirm title="将同时删除该文库下所有文件和标注数据，确定？" @confirm="handleDelLib(lib)">
              <template #reference><el-button :icon="Delete" link size="small"></el-button></template>
            </el-popconfirm>
          </div>
        </div>
        <el-empty v-if="libraries.length === 0 && !libLoading" description="暂无文库" :image-size="48" />
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="content-panel">
      <!-- 文库信息栏 -->
      <div class="content-header" v-if="selectedLib">
        <div class="content-header-left">
          <el-icon :size="22"><Folder /></el-icon>
          <span class="content-lib-name">{{ selectedLib.name }}</span>
          <span class="content-lib-desc" v-if="selectedLib.description">{{ selectedLib.description }}</span>
        </div>
        <div class="content-header-right">
          <el-button :icon="Upload" size="small" @click="uploadVisible = true">上传</el-button>
          <el-button :icon="Refresh" size="small" @click="fetchDocuments" :loading="docLoading">刷新状态</el-button>
        </div>
      </div>

      <!-- 文件表格 -->
      <div class="content-table" v-if="selectedLib">
        <el-table :data="documents" v-loading="docLoading" stripe border style="width:100%">
          <el-table-column label="类型" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="fileTypeTag(row.fileType)" size="small">{{ row.fileType.toUpperCase() }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
          <el-table-column label="大小" width="100" align="right">
            <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column label="标注数" width="80" align="center">
            <template #default="{ row }">{{ row.annotationCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="toggleAnnotations(row)">详情</el-button>
              <el-popconfirm title="将同时删除标注和向量数据，确定？" @confirm="handleDelDoc(row)">
                <template #reference><el-button type="danger" size="small" link>删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>

          <!-- 标注详情展开 -->
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="annotation-panel" v-if="row.annotations?.length">
                <div
                  v-for="ann in row.annotations"
                  :key="ann.id"
                  class="annotation-item"
                >
                  <span class="annotation-index" v-if="ann.annotationIndex != null">#{{ ann.annotationIndex }}</span>
                  <span class="annotation-index annotation-image" v-else>🖼 图片标注</span>
                  <span class="annotation-text">{{ ann.content }}</span>
                </div>
              </div>
              <el-empty v-else description="暂无标注" :image-size="40" />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="content-empty" v-else>
        <el-icon :size="48"><FolderOpened /></el-icon>
        <p>选择一个文库查看文件</p>
      </div>
    </div>

    <!-- 新建/编辑文库弹窗 -->
    <el-dialog v-model="libDialogVisible" :title="isEditLib ? '编辑文库' : '新建文库'" width="480px">
      <el-form ref="libFormRef" :model="libForm" :rules="libRules" label-width="70px">
        <el-form-item label="名称" prop="name"><el-input v-model="libForm.name" /></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="libForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="libForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="libDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="libSubmitting" @click="handleLibSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传文件" width="460px" @close="uploadFile = null">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        :on-change="onFileChange"
        :on-remove="() => uploadFile = null"
        accept=".txt,.pdf,.docx,.doc,.md,.jpg,.jpeg,.png,.gif,.bmp,.webp"
      >
        <el-icon :size="36"><UploadFilled /></el-icon>
        <div class="upload-text">拖拽或点击上传文件</div>
        <template #tip>
          <div class="upload-tip">支持 PDF / DOCX / TXT / MD / JPG / PNG，单个最大 50MB</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Upload, Refresh, Folder, FolderOpened, UploadFilled } from '@element-plus/icons-vue'
import request from '../../api/request.js'
import { hasPermission } from '../../stores/permissions.js'

const libraries = ref([])
const libLoading = ref(false)
const selectedLibId = ref(null)
const selectedLib = ref(null)

const documents = ref([])
const docLoading = ref(false)

const libDialogVisible = ref(false)
const isEditLib = ref(false)
const editLibId = ref(null)
const libSubmitting = ref(false)
const libFormRef = ref(null)
const libForm = reactive({ name: '', description: '', status: 1 })
const libRules = { name: [{ required: true, message: '请输入名称' }] }

const uploadVisible = ref(false)
const uploadRef = ref(null)
const uploadFile = ref(null)
const uploading = ref(false)

async function fetchLibraries() {
  libLoading.value = true
  try {
    const d = await request.get('/api/library')
    libraries.value = (d || []).map(lib => ({
      ...lib,
      documentCount: lib.documentCount ?? 0
    }))
    // Load doc counts
    for (const lib of libraries.value) {
      try {
        const docs = await request.get(`/api/library/${lib.id}/document`)
        lib.documentCount = docs ? docs.length : 0
      } catch { lib.documentCount = 0 }
    }
  } catch { libraries.value = [] }
  finally { libLoading.value = false }
}

async function fetchDocuments() {
  if (!selectedLibId.value) return
  docLoading.value = true
  try {
    const d = await request.get(`/api/library/${selectedLibId.value}/document`)
    documents.value = d || []
  } catch { documents.value = [] }
  finally { docLoading.value = false }
}

function selectLib(lib) {
  selectedLibId.value = lib.id
  selectedLib.value = lib
  fetchDocuments()
}

function handleAddLib() {
  isEditLib.value = false
  editLibId.value = null
  libForm.name = ''
  libForm.description = ''
  libForm.status = 1
  libDialogVisible.value = true
}

function handleEditLib(lib) {
  isEditLib.value = true
  editLibId.value = lib.id
  libForm.name = lib.name
  libForm.description = lib.description || ''
  libForm.status = lib.status ?? 1
  libDialogVisible.value = true
}

async function handleLibSubmit() {
  try { await libFormRef.value?.validate() } catch { return }
  libSubmitting.value = true
  try {
    if (isEditLib.value) {
      await request.put(`/api/library/${editLibId.value}`, { ...libForm })
      ElMessage.success('已修改')
    } else {
      await request.post('/api/library', { name: libForm.name, description: libForm.description })
      ElMessage.success('已创建')
    }
    libDialogVisible.value = false
    await fetchLibraries()
  } catch {} finally { libSubmitting.value = false }
}

async function handleDelLib(lib) {
  try {
    await request.delete(`/api/library/${lib.id}`)
    ElMessage.success('已删除')
    if (selectedLibId.value === lib.id) {
      selectedLibId.value = null
      selectedLib.value = null
      documents.value = []
    }
    await fetchLibraries()
  } catch {}
}

function onFileChange(file) {
  uploadFile.value = file.raw
}

async function handleUpload() {
  if (!uploadFile.value || !selectedLibId.value) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    const d = await request.post(`/api/library/${selectedLibId.value}/document`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success(d.status === 0 ? '上传成功，正在排队处理' : '已上传')
    uploadVisible.value = false
    uploadFile.value = null
    uploadRef.value?.clearFiles()
    await fetchDocuments()
    await fetchLibraries()
  } catch {} finally { uploading.value = false }
}

async function handleDelDoc(doc) {
  try {
    await request.delete(`/api/library/${selectedLibId.value}/document/${doc.id}`)
    ElMessage.success('已删除')
    await fetchDocuments()
  } catch {}
}

function toggleAnnotations(row) {
  // expand row is handled by el-table expand column
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

function fileTypeTag(type) {
  const textTypes = ['txt', 'pdf', 'docx', 'doc', 'md', 'markdown']
  const imageTypes = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
  if (textTypes.includes(type?.toLowerCase())) return ''
  if (imageTypes.includes(type?.toLowerCase())) return 'success'
  return 'info'
}

function statusType(status) {
  if (status === 2) return 'success'
  if (status === 1) return 'warning'
  if (status === -1) return 'danger'
  return 'info'
}

function statusText(status) {
  if (status === 0) return '排队中'
  if (status === 1) return '处理中'
  if (status === 2) return '已完成'
  if (status === -1) return '失败'
  return '未知'
}

onMounted(fetchLibraries)
</script>

<style scoped>
.rag-knowledge { height: 100%; display: flex; overflow: hidden; }
.lib-sidebar { width: 260px; background: #fff; border-right: 1px solid #e5e7eb; display: flex; flex-direction: column; flex-shrink: 0; }
.lib-sidebar-header { padding: 12px; border-bottom: 1px solid #e5e7eb; }
.lib-list { flex: 1; overflow-y: auto; }
.lib-item { padding: 12px 14px; cursor: pointer; border-bottom: 1px solid #f1f5f9; position: relative; }
.lib-item:hover { background: #f8fafc; }
.lib-item.active { background: #eff6ff; border-left: 3px solid #2563eb; }
.lib-item-main { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #1e293b; }
.lib-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.lib-item-meta { font-size: 12px; color: #9ca3af; margin-top: 4px; margin-left: 26px; }
.lib-item-actions { position: absolute; right: 8px; top: 10px; display: none; }
.lib-item:hover .lib-item-actions { display: flex; gap: 2px; }

.content-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: #f8fafc; }
.content-header { padding: 12px 16px; background: #fff; border-bottom: 1px solid #e5e7eb; display: flex; align-items: center; justify-content: space-between; }
.content-header-left { display: flex; align-items: center; gap: 8px; color: #1e293b; }
.content-lib-name { font-size: 16px; font-weight: 600; }
.content-lib-desc { font-size: 13px; color: #6b7280; }
.content-header-right { display: flex; gap: 8px; }
.content-table { flex: 1; overflow-y: auto; padding: 16px; }
.content-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #9ca3af; gap: 12px; }

.annotation-panel { padding: 8px 0; }
.annotation-item { padding: 6px 12px; border-bottom: 1px solid #f1f5f9; display: flex; gap: 10px; align-items: flex-start; }
.annotation-index { font-size: 12px; color: #6b7280; min-width: 50px; font-weight: 600; }
.annotation-index.annotation-image { color: #0891b2; }
.annotation-text { font-size: 13px; color: #374151; line-height: 1.6; white-space: pre-wrap; word-break: break-all; }

.upload-text { margin-top: 8px; font-size: 13px; color: #6b7280; }
.upload-tip { font-size: 12px; color: #9ca3af; margin-top: 4px; }
</style>