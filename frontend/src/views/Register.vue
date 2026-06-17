<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-brand">
        <div class="brand-content">
          <div class="brand-icon">
            <el-icon :size="48"><Ship /></el-icon>
          </div>
          <h1 class="brand-title">VesselEMS</h1>
          <p class="brand-subtitle">企业级管理系统</p>
        </div>
        <div class="brand-decoration">
          <div class="deco-circle deco-circle-1"></div>
          <div class="deco-circle deco-circle-2"></div>
          <div class="deco-circle deco-circle-3"></div>
        </div>
      </div>
      <div class="register-form-panel">
        <div class="form-header">
          <h2 class="form-title">创建账号</h2>
          <p class="form-subtitle">填写以下信息完成注册</p>
        </div>
        <el-form ref="formRef" :model="formData" :rules="formRules" class="register-form" @keyup.enter="handleRegister">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item prop="username">
                <el-input v-model="formData.username" placeholder="用户名" :prefix-icon="User" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="nickname">
                <el-input v-model="formData.nickname" placeholder="昵称" :prefix-icon="User" size="large" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item prop="realName">
                <el-input v-model="formData.realName" placeholder="真实姓名" :prefix-icon="User" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="gender">
                <el-select v-model="formData.gender" placeholder="性别" size="large" style="width:100%">
                  <el-option label="未知" :value="0" />
                  <el-option label="男" :value="1" />
                  <el-option label="女" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item prop="email">
            <el-input v-model="formData.email" placeholder="邮箱" :prefix-icon="Message" size="large" />
          </el-form-item>
          <el-form-item prop="telephone">
            <el-input v-model="formData.telephone" placeholder="手机号（选填）" :prefix-icon="Message" size="large" />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item prop="password">
                <el-input v-model="formData.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="confirmPassword">
                <el-input v-model="formData.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" size="large" show-password />
              </el-form-item>
            </el-col>
          </el-row>
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
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f4;
  padding: 20px;
}

.register-container {
  display: flex;
  width: 800px;
  max-width: 100%;
  min-height: 600px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

/* 品牌区 - 经典蓝 */
.register-brand {
  flex: 0 0 340px;
  background: #2563eb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  padding: 48px 40px;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.brand-icon {
  margin-bottom: 20px;
  color: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 8px;
  color: #ffffff;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 4px;
}

/* 装饰圆 */
.brand-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}

.deco-circle-1 {
  width: 240px;
  height: 240px;
  top: -80px;
  right: -60px;
}

.deco-circle-2 {
  width: 160px;
  height: 160px;
  bottom: -40px;
  left: -40px;
}

.deco-circle-3 {
  width: 80px;
  height: 80px;
  bottom: 60px;
  right: 40px;
  background: rgba(255, 255, 255, 0.08);
}

/* 表单区 */
.register-form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
}

.form-header {
  text-align: center;
  margin-bottom: 28px;
  width: 100%;
}

.form-title {
  font-size: 22px;
  font-weight: 700;
  color: #1c1917;
  margin-bottom: 6px;
}

.form-subtitle {
  font-size: 14px;
  color: #a8a29e;
}

.register-form {
  width: 100%;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 1px;
}

.login-link {
  font-size: 13px;
  color: #a8a29e;
  margin-top: 12px;
  text-align: center;
}

.login-link a {
  color: #2563eb;
  text-decoration: none;
  margin-left: 4px;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}

@media (max-width: 640px) {
  .register-container {
    flex-direction: column;
    min-height: auto;
  }
  .register-brand {
    flex: 0 0 auto;
    padding: 40px 24px;
    min-height: 200px;
  }
  .brand-icon {
    margin-bottom: 12px;
  }
  .register-form-panel {
    padding: 32px 24px;
  }
}
</style>