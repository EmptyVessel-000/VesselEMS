<template>
  <div class="user-profile">
    <!-- 顶部欢迎区 -->
    <div class="profile-hero">
      <div class="hero-content">
        <el-avatar :size="72" class="hero-avatar">
          {{ avatarChar }}
        </el-avatar>
        <div class="hero-text">
          <h2 class="hero-title">{{ displayName }}</h2>
          <p class="hero-subtitle">{{ roleText }}</p>
        </div>
      </div>
      <div class="hero-icon">
        <el-icon :size="56"><UserFilled /></el-icon>
      </div>
    </div>

    <div class="profile-body" v-loading="loading">
      <!-- 左列：基本信息卡片 -->
      <div class="profile-sidebar">
        <div class="info-card">
          <h3 class="info-card-title">账号信息</h3>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">用户 ID</span>
              <span class="info-value">{{ userData.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userData.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态</span>
              <span class="info-value">
                <el-tag :type="userData.status === 1 ? 'success' : 'danger'" size="small">
                  {{ userData.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间</span>
              <span class="info-value text-secondary">{{ formatTime(userData.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后登录</span>
              <span class="info-value text-secondary">{{ formatTime(userData.lastLoginTime) || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后登录 IP</span>
              <span class="info-value text-secondary">{{ userData.lastLoginIp || '—' }}</span>
            </div>
          </div>
        </div>

        <div class="info-card">
          <h3 class="info-card-title">角色权限</h3>
          <div class="role-list">
            <el-tag v-for="r in userData.roles || []" :key="r" class="role-tag">{{ r }}</el-tag>
            <span v-if="!userData.roles?.length" class="text-secondary" style="font-size:13px">暂无角色</span>
          </div>
        </div>
      </div>

      <!-- 右列：可编辑表单 -->
      <div class="profile-main">
        <div class="edit-card">
          <div class="edit-card-header">
            <h3 class="info-card-title">编辑资料</h3>
            <span class="text-muted">修改后点击保存生效</span>
          </div>
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" class="edit-form">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="formData.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="请选择性别" style="width:100%">
                <el-option label="未知" :value="0" />
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="telephone">
              <el-input v-model="formData.telephone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="备注信息" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave" size="large">
                保存修改
              </el-button>
              <el-button @click="handleReset" size="large">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div class="edit-card">
          <div class="edit-card-header">
            <h3 class="info-card-title">修改密码</h3>
            <span class="text-muted">不修改则留空</span>
          </div>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px" class="edit-form">
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdSaving" @click="handleChangePwd" size="large">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { checkAuth, userStore } from '../../stores/user.js'
import request from '../../api/request.js'

const user = userStore
const loading = ref(false)
const saving = ref(false)
const pwdSaving = ref(false)
const formRef = ref(null)
const pwdFormRef = ref(null)

const userData = computed(() => user.user || {})

const displayName = computed(() => {
  return userData.value.nickname || userData.value.username || ''
})

const avatarChar = computed(() => {
  return (displayName.value || '?')[0]
})

const roleText = computed(() => {
  const roles = userData.value.roles
  if (roles && roles.length > 0) return roles.join(' · ')
  return '暂无角色'
})

const formData = reactive({
  nickname: '',
  realName: '',
  gender: 0,
  email: '',
  telephone: '',
  remark: ''
})

const formRules = {
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  telephone: [{ pattern: /^[\d\-+]{0,32}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const pwdForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  newPassword: [
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: (rule, value, callback) => {
      if (!value) callback(new Error('请再次输入密码'))
      else if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
      else callback()
    }, trigger: 'blur' }
  ]
}

function loadUserData() {
  const u = userData.value
  formData.nickname = u.nickname || ''
  formData.realName = u.realName || ''
  formData.gender = u.gender != null ? u.gender : 0
  formData.email = u.email || ''
  formData.telephone = u.telephone || ''
  formData.remark = u.remark || ''
}

async function handleSave() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  saving.value = true
  try {
    await request.put(`/api/users/${userData.value.id}/info`, {
      nickname: formData.nickname,
      realName: formData.realName,
      gender: formData.gender,
      email: formData.email,
      telephone: formData.telephone,
      remark: formData.remark
    })
    ElMessage.success('资料修改成功')
    // 刷新用户信息
    await checkAuth()
  } catch (e) {
    ElMessage.error(e?.message || '修改失败')
  } finally {
    saving.value = false
  }
}

function handleReset() {
  loadUserData()
  formRef.value?.resetFields()
}

async function handleChangePwd() {
  if (!pwdFormRef.value) return
  try { await pwdFormRef.value.validate() } catch { return }
  if (!pwdForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  pwdSaving.value = true
  try {
    await request.put(`/api/users/${userData.value.id}/info`, {
      password: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value?.resetFields()
  } catch (e) {
    ElMessage.error(e?.message || '修改失败')
  } finally {
    pwdSaving.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  try {
    return new Date(t).toLocaleString('zh-CN', { hour12: false })
  } catch {
    return t
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await checkAuth()
    loadUserData()
  } catch {} finally {
    loading.value = false
  }
})
</script>

<style scoped>
.user-profile {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow-y: auto;
}

/* ===== 顶部欢迎区 ===== */
.profile-hero {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
  border-radius: 16px;
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #ffffff;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.15);
  flex-shrink: 0;
}

.hero-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.hero-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 28px;
  font-weight: 700;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 4px;
  line-height: 1.4;
}

.hero-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.5;
}

.hero-icon {
  color: rgba(255, 255, 255, 0.12);
  flex-shrink: 0;
}

/* ===== 主体区域 ===== */
.profile-body {
  flex: 1;
  display: flex;
  gap: 24px;
  min-height: 0;
}

/* ===== 左侧信息卡片 ===== */
.profile-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
}

.info-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1c1917;
  margin-bottom: 16px;
  line-height: 1.5;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 12px;
  font-weight: 500;
  color: #a8a29e;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  color: #44403c;
  line-height: 1.5;
}

.role-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-tag {
  font-size: 12px;
}

/* ===== 右侧编辑区 ===== */
.profile-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.edit-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
}

.edit-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.edit-card-header .info-card-title {
  margin-bottom: 0;
}

.edit-form {
  max-width: 480px;
}

@media (max-width: 768px) {
  .profile-body {
    flex-direction: column;
  }
  .profile-sidebar {
    width: 100%;
  }
}
</style>