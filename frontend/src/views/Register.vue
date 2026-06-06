<template>
  <div class="register-wrapper">
    <div class="register-card">
      <div class="register-brand">
        <div class="brand-icon"><el-icon :size="64"><Ship /></el-icon></div>
        <h1 class="brand-title">VesselEMS</h1>
        <p class="brand-subtitle">企业级管理系统</p>
      </div>
      <div class="register-form-area">
        <h2 class="form-title">创建账号</h2>
        <el-form ref="formRef" :model="formData" :rules="formRules" class="register-form" @keyup.enter="handleRegister">
          <el-form-item prop="username">
            <el-input v-model="formData.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="formData.nickname" placeholder="请输入昵称" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="realName">
            <el-input v-model="formData.realName" placeholder="请输入真实姓名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="gender">
            <el-select v-model="formData.gender" placeholder="请选择性别" size="large" style="width:100%">
              <el-option label="未知" :value="0" />
              <el-option label="男" :value="1" />
              <el-option label="女" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" :prefix-icon="Message" size="large" />
          </el-form-item>
          <el-form-item prop="telephone">
            <el-input v-model="formData.telephone" placeholder="请输入手机号（选填）" :prefix-icon="Message" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="formData.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="formData.confirmPassword" type="password" placeholder="请再次输入密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">注 册</el-button>
          </el-form-item>
        </el-form>
        <p class="login-link">已有账号？<router-link to="/login">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Ship, User } from '@element-plus/icons-vue'
import request from '../api/request.js'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const formData = reactive({
  username: '',
  nickname: '',
  realName: '',
  gender: null,
  email: '',
  telephone: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (!value) callback(new Error('请再次输入密码'))
  else if (value !== formData.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '用户名长度2-20位', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  telephone: [{ pattern: /^[\d\-+]{0,32}$/, message: '手机号格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码不少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

async function handleRegister() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    await request.post('/api/auth/register', {
      username: formData.username,
      nickname: formData.nickname,
      realName: formData.realName,
      gender: formData.gender,
      email: formData.email,
      telephone: formData.telephone || '',
      password: formData.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch { } finally { loading.value = false }
}
</script>

<style scoped>
.register-wrapper { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f1f5f9; }
.register-card { display: flex; width: 780px; max-width: 95vw; min-height: 680px; background: #ffffff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); overflow: hidden; }
.register-brand { flex: 0 0 320px; background: linear-gradient(135deg, #1e293b 0%, #334155 100%); display: flex; flex-direction: column; align-items: center; justify-content: center; color: #ffffff; padding: 40px; }
.brand-icon { margin-bottom: 20px; color: #3b82f6; }
.brand-title { font-size: 28px; font-weight: 700; letter-spacing: 2px; margin-bottom: 8px; }
.brand-subtitle { font-size: 14px; color: #94a3b8; letter-spacing: 4px; }
.register-form-area { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px 48px; }
.form-title { font-size: 22px; font-weight: 600; color: #1f2937; margin-bottom: 24px; align-self: flex-start; }
.register-form { width: 100%; }
.register-btn { width: 100%; }
.login-link { font-size: 13px; color: #6b7280; margin-top: 8px; }
.login-link a { color: #2563eb; text-decoration: none; margin-left: 4px; }
.login-link a:hover { text-decoration: underline; }
@media (max-width: 640px) {
  .register-card { flex-direction: column; width: 360px; }
  .register-brand { flex: 0 0 auto; padding: 32px 24px; }
  .brand-icon { margin-bottom: 12px; }
  .register-form-area { padding: 32px 24px; }
}
</style>